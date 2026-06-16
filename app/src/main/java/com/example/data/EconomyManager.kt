package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

// --- Economy Data Models ---

data class CoinTransaction(
    val id: String = "",
    val userId: String = "",
    val amount: Int = 0, // positive for credit, negative for debit
    val source: String = "", // "DAILY_REWARD", "WIN_REWARD", "REFERRAL_REWARD", "COIN_STORE", "ENTRY_FEE"
    val description: String = "",
    val timestamp: Long = 0L
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "amount" to amount,
            "source" to source,
            "description" to description,
            "timestamp" to timestamp
        )
    }
}

data class UserWallet(
    val userId: String = "",
    val coins: Int = 10000,
    val gems: Int = 150,
    val referralCode: String = "",
    val referredBy: String = "",
    val lastDailyClaimTime: Long = 0L,
    val dailyDaysStreak: Int = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "coins" to coins,
            "gems" to gems,
            "referralCode" to referralCode,
            "referredBy" to referredBy,
            "lastDailyClaimTime" to lastDailyClaimTime,
            "dailyDaysStreak" to dailyDaysStreak
        )
    }
}

object EconomyManager {
    private const val TAG = "EconomyManager"
    private var db: FirebaseFirestore? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var isSimulationMode = true
        private set

    // Active local user's wallet
    private val _userWalletState = MutableStateFlow(UserWallet())
    val userWalletState = _userWalletState.asStateFlow()

    // Transaction history ledger
    private val _transactionHistory = MutableStateFlow<List<CoinTransaction>>(emptyList())
    val transactionHistory = _transactionHistory.asStateFlow()

    // Logs showing real-time transaction packets passing through the virtual economy
    private val _economyLogs = MutableStateFlow<List<String>>(listOf("Booting Economy Engine..."))
    val economyLogs = _economyLogs.asStateFlow()

    // Synchronization listener
    private var walletListener: ListenerRegistration? = null
    private var transactionsListener: ListenerRegistration? = null

    fun initialize(context: Context) {
        logMessage("Economy Core Online.")

        // Listen to LudoMasterRepository profile name resets to sync user wallet profile keys
        coroutineScope.launch {
            LudoMasterRepository.playerState.collect { profile ->
                if (profile.id.isNotBlank() && _userWalletState.value.userId != profile.id) {
                    setupUserWalletAndSync(profile.id, profile.name)
                }
            }
        }

        try {
            db = FirebaseFirestore.getInstance()
            // We coordinate simulation flags with MultiplayerManager
            isSimulationMode = MultiplayerManager.isSimulationMode
            if (isSimulationMode) {
                logMessage("Sandbox Simulation: Isolated virtual ledger operational.")
                loadMockTransactions()
            } else {
                logMessage("Firestore Economy Node connected. Full state synchronization enabled.")
            }
        } catch (e: Exception) {
            isSimulationMode = true
            logMessage("Sandbox Simulation active (Firestore init failed).")
            loadMockTransactions()
            Log.e(TAG, "Using simulation due to economy Firestore error: ${e.message}")
        }
    }

    private fun logMessage(msg: String) {
        Log.d(TAG, msg)
        _economyLogs.update { (listOf("[Economy] $msg") + it).take(40) }
    }

    private fun setupUserWalletAndSync(userId: String, name: String) {
        if (userId.isBlank()) return

        // Cancel previous sync nodes
        walletListener?.remove()
        transactionsListener?.remove()

        if (isSimulationMode) {
            // Local state constructor
            val referralCode = generateUserReferralCode(name)
            val initialWallet = UserWallet(
                userId = userId,
                coins = LudoMasterRepository.playerState.value.coins,
                gems = LudoMasterRepository.playerState.value.gems,
                referralCode = referralCode,
                referredBy = "",
                lastDailyClaimTime = System.currentTimeMillis() - 86400000L, // eligible to claim
                dailyDaysStreak = 2
            )
            _userWalletState.value = initialWallet
            logMessage("Created sandbox wallet mapping for ID: $userId code: $referralCode")
            return
        }

        // Real Firestore synchronizations
        val walletDoc = db!!.collection("wallets").document(userId)
        walletListener = walletDoc.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Wallet snapshot compilation error: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val coins = snapshot.getLong("coins")?.toInt() ?: 10000
                val gems = snapshot.getLong("gems")?.toInt() ?: 150
                val referral = snapshot.getString("referralCode") ?: generateUserReferralCode(name)
                val referredBy = snapshot.getString("referredBy") ?: ""
                val lastDaily = snapshot.getLong("lastDailyClaimTime") ?: 0L
                val streak = snapshot.getLong("dailyDaysStreak")?.toInt() ?: 0

                val wallet = UserWallet(
                    userId = userId,
                    coins = coins,
                    gems = gems,
                    referralCode = referral,
                    referredBy = referredBy,
                    lastDailyClaimTime = lastDaily,
                    dailyDaysStreak = streak
                )
                _userWalletState.value = wallet

                // Ensure local profile remains fully synchronized
                coroutineScope.launch {
                    val cur = LudoMasterRepository.playerState.value
                    if (cur.coins != coins || cur.gems != gems) {
                        LudoMasterRepository.addCoins(coins - cur.coins)
                        LudoMasterRepository.addGems(gems - cur.gems)
                    }
                }
            } else {
                // Provision a brand new wallet mapping on Firestore
                val startCoins = LudoMasterRepository.playerState.value.coins
                val startGems = LudoMasterRepository.playerState.value.gems
                val referral = generateUserReferralCode(name)
                
                val newWallet = UserWallet(
                    userId = userId,
                    coins = startCoins,
                    gems = startGems,
                    referralCode = referral,
                    referredBy = "",
                    lastDailyClaimTime = 0L,
                    dailyDaysStreak = 0
                )
                walletDoc.set(newWallet.toMap())
                    .addOnSuccessListener {
                        logMessage("Provisioned Firestore wallet ledger for $name")
                    }
            }
        }

        // Keep real transaction entries synchronized
        transactionsListener = db!!.collection("transactions")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Transactions read error: ${error.message}")
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.getString("id") ?: ""
                    val uid = doc.getString("userId") ?: ""
                    val amt = doc.getLong("amount")?.toInt() ?: 0
                    val src = doc.getString("source") ?: ""
                    val desc = doc.getString("description") ?: ""
                    val ts = doc.getLong("timestamp") ?: 0L
                    CoinTransaction(id, uid, amt, src, desc, ts)
                }?.sortedByDescending { it.timestamp } ?: emptyList()

                _transactionHistory.value = list
            }
    }

    private fun generateUserReferralCode(name: String): String {
        val uppercaseName = name.replace(" ", "").uppercase()
        val suffix = (1000..9999).random().toString()
        return if (uppercaseName.length > 5) {
            "${uppercaseName.take(5)}$suffix"
        } else {
            "$uppercaseName$suffix"
        }
    }

    // --- Dynamic Economy Operations & Transaction Sinks ---

    fun recordTransaction(amount: Int, source: String, description: String, onFinished: (Boolean) -> Unit = {}) {
        val uid = _userWalletState.value.userId
        if (uid.isBlank()) {
            onFinished(false)
            return
        }

        var finalAmount = amount
        var finalDescription = description
        if (amount > 0 && LudoMasterRepository.playerState.value.isVip) {
            finalAmount = amount * 2
            finalDescription = "[VIP 2X BENEFIT] $description"
        }

        val tid = "tx_${UUID.randomUUID()}"
        val transaction = CoinTransaction(
            id = tid,
            userId = uid,
            amount = finalAmount,
            source = source,
            description = finalDescription,
            timestamp = System.currentTimeMillis()
        )

        // Sync state locally first to avoid UI lag
        if (finalAmount > 0) {
            LudoMasterRepository.addCoins(finalAmount)
        } else {
            LudoMasterRepository.spendCoins(-finalAmount)
        }

        // Update local wallet model immediately
        _userWalletState.update { current ->
            current.copy(coins = current.coins + finalAmount)
        }

        if (isSimulationMode) {
            _transactionHistory.update { listOf(transaction) + it }
            logMessage("Ledger mapping added: [${source}] $finalAmount coins - $finalDescription")
            onFinished(true)
            return
        }

        // Firestore double-entry record keeping
        db?.runBatch { batch ->
            // Insert audit transaction log
            val txRef = db!!.collection("transactions").document(tid)
            batch.set(txRef, transaction.toMap())

            // Update user wallet balance balance atomically
            val walletRef = db!!.collection("wallets").document(uid)
            batch.update(walletRef, "coins", FieldValue.increment(finalAmount.toLong()))
        }?.addOnSuccessListener {
            logMessage("Verified Ledger: Credit/Debit $finalAmount for $source successfully transacted.")
            onFinished(true)
        }?.addOnFailureListener { e ->
            Log.e(TAG, "Transaction verification failure: ${e.message}")
            onFinished(false)
        }
    }

    fun recordGemsDeduction(gemsAmount: Int, coinsAwarded: Int, source: String, description: String, onFinished: (Boolean) -> Unit) {
        val uid = _userWalletState.value.userId
        if (uid.isBlank()) {
            onFinished(false)
            return
        }

        val txCoinsId = "tx_${UUID.randomUUID()}"
        val transaction = CoinTransaction(
            id = txCoinsId,
            userId = uid,
            amount = coinsAwarded,
            source = source,
            description = description,
            timestamp = System.currentTimeMillis()
        )

        // Direct updates
        LudoMasterRepository.spendGems(gemsAmount)
        LudoMasterRepository.addCoins(coinsAwarded)
        _userWalletState.update { current ->
            current.copy(
                gems = current.gems - gemsAmount,
                coins = current.coins + coinsAwarded
            )
        }

        if (isSimulationMode) {
            _transactionHistory.update { listOf(transaction) + it }
            logMessage("Converted: $gemsAmount Gems into $coinsAwarded Coins ($source)")
            onFinished(true)
            return
        }

        // Cloud Batch updates
        db?.runBatch { batch ->
            val txRef = db!!.collection("transactions").document(txCoinsId)
            batch.set(txRef, transaction.toMap())

            val walletRef = db!!.collection("wallets").document(uid)
            batch.update(walletRef, "gems", FieldValue.increment(-gemsAmount.toLong()))
            batch.update(walletRef, "coins", FieldValue.increment(coinsAwarded.toLong()))
        }?.addOnSuccessListener {
            logMessage("Cloud Converted: $gemsAmount Gems converted to $coinsAwarded Coins successfully.")
            onFinished(true)
        }?.addOnFailureListener { e ->
            Log.e(TAG, "Cloud conversion failed: ${e.message}")
            onFinished(false)
        }
    }

    // --- Action Pipelines ---

    // 1. Claim Daily Rewards via dynamic daily check-ins
    fun claimDailyDailyCheckIn(dayIndex: Int, coinAmount: Int, onResult: (Boolean, String) -> Unit) {
        val wallet = _userWalletState.value
        val now = System.currentTimeMillis()
        
        // Checks if 24 hours have elapsed
        val timeDiff = now - wallet.lastDailyClaimTime
        val isEligible = timeDiff >= 86400000L || wallet.lastDailyClaimTime == 0L

        if (!isEligible && !isSimulationMode) {
            val hoursLeft = 24 - (timeDiff / 3600000L)
            onResult(false, "Daily prize already unlocked! Please await remaining $hoursLeft hour(s).")
            return
        }

        // Record daily transaction!
        recordTransaction(
            amount = coinAmount,
            source = "DAILY_REWARD",
            description = "Day $dayIndex Multiplier Check-In"
        ) { success ->
            if (success) {
                // Update claim timeline timestamps
                _userWalletState.update { current ->
                    current.copy(
                        lastDailyClaimTime = now,
                        dailyDaysStreak = if (dayIndex == current.dailyDaysStreak + 1) dayIndex else 1
                    )
                }

                if (!isSimulationMode) {
                    val walletRef = db!!.collection("wallets").document(wallet.userId)
                    db?.runBatch { batch ->
                        batch.update(walletRef, "lastDailyClaimTime", now)
                        batch.update(walletRef, "dailyDaysStreak", if (dayIndex == wallet.dailyDaysStreak + 1) dayIndex else 1)
                    }?.addOnCompleteListener {
                        logMessage("Dynamic Daily Day $dayIndex reward claimed: +$coinAmount Coins.")
                    }
                }

                // Also invoke legacy handler so strings / triggers match UI legacy callbacks
                LudoMasterRepository.claimDailyDay(dayIndex, coinAmount)
                onResult(true, "Successfully claimed $coinAmount Ludo Coins!")
            } else {
                onResult(false, "Internal ledger pipeline issue. Please try again.")
            }
        }
    }

    // 2. Win Game Battle Rewards Pool Claim
    fun awardGameBattleVictory(roomId: String, entryFee: Int, playersCount: Int, onResult: (Boolean, Int) -> Unit) {
        val rewardAmount = entryFee * playersCount
        val desc = "Gold Battle Victory in Room $roomId (Pool: 1st Rank)"

        recordTransaction(
            amount = rewardAmount,
            source = "WIN_REWARD",
            description = desc
        ) { success ->
            if (success) {
                logMessage("Victory! Awarded total jackpot battle pool of $rewardAmount coins.")
                onResult(true, rewardAmount)
            } else {
                onResult(false, 0)
            }
        }
    }

    // 3. Referral Systems: Submit a friend's referral code to instantly claim referral coins (500 Coins both)
    fun submitReferralCode(code: String, onResult: (Boolean, String) -> Unit) {
        val trimmedCode = code.trim().uppercase()
        val wallet = _userWalletState.value

        if (trimmedCode.isEmpty()) {
            onResult(false, "Please input a referral code.")
            return
        }
        if (trimmedCode == wallet.referralCode) {
            onResult(false, "You cannot enter your own referral code!")
            return
        }
        if (wallet.referredBy.isNotBlank()) {
            onResult(false, "You have already claimed a referral bonus!")
            return
        }

        if (isSimulationMode) {
            coroutineScope.launch {
                delay(1200)
                // Simulated check
                recordTransaction(
                    amount = 500,
                    source = "REFERRAL_REWARD",
                    description = "Claimed Referral Promo Code: $trimmedCode"
                ) { success ->
                    if (success) {
                        _userWalletState.update { it.copy(referredBy = trimmedCode) }
                        logMessage("Referral mapping matches code: $trimmedCode. Added 500 Promo Coins.")
                        onResult(true, "Successfully claimed 500 Referral Coins!")
                    } else {
                        onResult(false, "Failed to submit referral.")
                    }
                }
            }
            return
        }

        // Firebase flow
        db?.collection("wallets")
            ?.whereEqualTo("referralCode", trimmedCode)
            ?.get()
            ?.addOnSuccessListener { query ->
                val referrerDoc = query.documents.firstOrNull()
                if (referrerDoc == null) {
                    onResult(false, "Invalid referral code. No player found.")
                    return@addOnSuccessListener
                }

                val referrerId = referrerDoc.id
                val myWalletRef = db!!.collection("wallets").document(wallet.userId)
                val referrerWalletRef = db!!.collection("wallets").document(referrerId)

                db?.runBatch { batch ->
                    // 1. Credit 500 Coins to local user
                    batch.update(myWalletRef, "referredBy", trimmedCode)
                    batch.update(myWalletRef, "coins", FieldValue.increment(500L))

                    // 2. Credit 500 Coins to Referrer user
                    batch.update(referrerWalletRef, "coins", FieldValue.increment(500L))

                    // 3. Document connection in refer family logs
                    val refId = "ref_${referrerId}_${wallet.userId}"
                    val refDoc = db!!.collection("referrals").document(refId)
                    refDoc.set(mapOf(
                        "id" to refId,
                        "referrerId" to referrerId,
                        "refereeId" to wallet.userId,
                        "codeUsed" to trimmedCode,
                        "timestamp" to System.currentTimeMillis()
                    ))

                    // 4. Audit ledger logs
                    val txId1 = "tx_${UUID.randomUUID()}"
                    batch.set(db!!.collection("transactions").document(txId1), CoinTransaction(
                        id = txId1,
                        userId = wallet.userId,
                        amount = 500,
                        source = "REFERRAL_REWARD",
                        description = "Enlisted via referral code: $trimmedCode",
                        timestamp = System.currentTimeMillis()
                    ).toMap())

                    val txId2 = "tx_${UUID.randomUUID()}"
                    batch.set(db!!.collection("transactions").document(txId2), CoinTransaction(
                        id = txId2,
                        userId = referrerId,
                        amount = 500,
                        source = "REFERRAL_REWARD",
                        description = "Affiliate reward for inviting user: ${LudoMasterRepository.playerState.value.name}",
                        timestamp = System.currentTimeMillis()
                    ).toMap())
                }?.addOnSuccessListener {
                    logMessage("Referral bonus claimed: Both parties rewarded with +500 Coins.")
                    onResult(true, "Success! You and your referrer both earned 500 Game Coins!")
                }?.addOnFailureListener { e ->
                    onResult(false, "Referral cloud linkage failed: ${e.message}")
                }
            }
            ?.addOnFailureListener { e ->
                onResult(false, "Referrals validation network error: ${e.message}")
            }
    }

    // 4. Coin Store conversion purchases
    fun buyCoinPackWithGems(coinsAmount: Int, gemsCost: Int, packName: String, onResult: (Boolean, String) -> Unit) {
        val wallet = _userWalletState.value
        if (wallet.gems < gemsCost) {
            onResult(false, "Insufficient gems! Purchase some in store first.")
            return
        }

        recordGemsDeduction(
            gemsAmount = gemsCost,
            coinsAwarded = coinsAmount,
            source = "COIN_STORE",
            description = "Secured '$packName' package from Coin Vault"
        ) { success ->
            if (success) {
                onResult(true, "Successfully purchased $coinsAmount Coins for $gemsCost Gems!")
            } else {
                onResult(false, "Vault conversion error. Try again.")
            }
        }
    }

    // 5. Room entry fee processes
    fun chargeEntryFee(entryFee: Int, onResult: (Boolean, String) -> Unit) {
        val wallet = _userWalletState.value
        val allowedFees = listOf(100, 500, 1000, 5000)

        if (!allowedFees.contains(entryFee)) {
            onResult(false, "Invalid Room Entry Fee magnitude ($entryFee).")
            return
        }

        if (wallet.coins < entryFee) {
            onResult(false, "Insufficient Battle Coins! You need at least $entryFee Coins to enlist.")
            return
        }

        recordTransaction(
            amount = -entryFee,
            source = "ENTRY_FEE",
            description = "Subscribed entry fee wager of $entryFee Coins to battle pool"
        ) { success ->
            if (success) {
                logMessage("Secured $entryFee Coins entry fee from your wallet.")
                onResult(true, "Subscribed entry fee successfully.")
            } else {
                onResult(false, "Failed to submit entry fee transaction to database ledger.")
            }
        }
    }

    private fun loadMockTransactions() {
        val uid = LudoMasterRepository.playerState.value.id
        _transactionHistory.value = listOf(
            CoinTransaction("tx_m1", uid, 1000, "DAILY_REWARD", "Day 1 Reward Claim", System.currentTimeMillis() - 172800000L),
            CoinTransaction("tx_m2", uid, -500, "ENTRY_FEE", "Paid Room Entry Fee for Lobby B490", System.currentTimeMillis() - 86400000L),
            CoinTransaction("tx_m3", uid, 2000, "WIN_REWARD", "Victory Jackpot Winner inside Room B490", System.currentTimeMillis() - 85000000L),
            CoinTransaction("tx_m4", uid, 500, "REFERRAL_REWARD", "Referral Join Incentive code: PRIYA99", System.currentTimeMillis() - 3600000L)
        )
    }
}
