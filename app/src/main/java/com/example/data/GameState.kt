package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// --- Data Models ---
data class PlayerProfile(
    val id: String = "101",
    val name: String = "Rahul",
    val level: Int = 24,
    val xp: Int = 1850,
    val coins: Int = 15230,
    val gems: Int = 320,
    val selectedSkin: String = "Classic Blue",
    val winRate: String = "64.5%",
    val totalMatches: Int = 142,
    val wins: Int = 92,
    val authType: String = "GUEST", // "GUEST", "GOOGLE", "OTP", "EMAIL"
    val email: String = "",
    val phone: String = "",
    val isVip: Boolean = false,
    val vipFrame: String = "None", // "None", "Elite Platinum Sovereign", "Golden Crown Gladiator"
    val vipDice: String = "Classic" // "Classic", "Emperor's Golden Dice", "Cosmic Vortex Dice"
)

data class Friend(
    val id: String = "sim_friend",
    val name: String,
    val level: Int,
    val coins: Int = 3000,
    val isOnline: Boolean,
    val avatarColorIndex: Int
)

data class ChatMessage(
    val sender: String,
    val message: String,
    val timestamp: String,
    val isSystem: Boolean = false,
    val isEmoji: Boolean = false
)

data class LudoTournament(
    val id: String,
    val name: String,
    val entryFee: Int,
    val prizePool: Int,
    val participantsCount: Int,
    val maxParticipants: Int = 8,
    val isJoined: Boolean = false
)

data class StoreItem(
    val id: String,
    val title: String,
    val type: String, // "coins", "gems", "skins", "dice"
    val rewardAmount: Int,
    val costAmount: String, // "Free", "$0.99", "50 Gems", etc.
    val isPurchased: Boolean = false
)

// --- Centralized Reactive App Store / ViewModel State ---
object LudoMasterRepository {
    private var prefs: SharedPreferences? = null

    private val _playerState = MutableStateFlow(PlayerProfile())
    val playerState: StateFlow<PlayerProfile> = _playerState.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled = _soundEnabled.asStateFlow()

    private val _musicEnabled = MutableStateFlow(true)
    val musicEnabled = _musicEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled = _vibrationEnabled.asStateFlow()

    private val _darkTheme = MutableStateFlow(true)
    val darkTheme = _darkTheme.asStateFlow()

    private val _currentThemeType = MutableStateFlow("Classic Dark")
    val currentThemeType = _currentThemeType.asStateFlow()

    // Login status: "SPLASH", "ONBOARDING", "LOGOUT", "GUEST", "GOOGLE", "OTP"
    private val _currentSessionState = MutableStateFlow("SPLASH")
    val currentSessionState = _currentSessionState.asStateFlow()

    // Friends List
    private val _friendsList = MutableStateFlow(
        listOf(
            Friend("sim_priya", "Priya", 27, 2980, true, 0),
            Friend("sim_amit", "Amit", 23, 3120, true, 1),
            Friend("sim_neha", "Neha", 22, 3000, true, 2),
            Friend("sim_rajesh", "Rajesh", 19, 1450, false, 3),
            Friend("sim_vikram", "Vikram", 31, 8470, false, 4)
        )
    )
    val friendsList = _friendsList.asStateFlow()

    // Store items (Coins, Gems, Themes, Dice skins)
    private val _storeItems = MutableStateFlow(
        listOf(
            StoreItem("c1", "Splash Pack", "coins", 5000, "$0.99"),
            StoreItem("c2", "Master Chest", "coins", 25000, "$4.99"),
            StoreItem("g1", "Gem Pouch", "gems", 100, "$1.99"),
            StoreItem("g2", "Gem Vault", "gems", 650, "$9.99"),
            StoreItem("s1", "Futuristic Board Skin", "skins", 0, "150 Gems"),
            StoreItem("s2", "Royal Gold Board S", "skins", 0, "300 Gems"),
            StoreItem("d1", "Firetrail Dice FX", "dice", 0, "80 Gems")
        )
    )
    val storeItems = _storeItems.asStateFlow()

    // Tournament charts
    private val _tournaments = MutableStateFlow(
        listOf(
            LudoTournament("t1", "Star Club Open", 500, 2000, 5, 8),
            LudoTournament("t2", "Mega Jackpot Arena", 2000, 8000, 3, 8),
            LudoTournament("t3", "VIP King Blitz", 5000, 20000, 7, 8, isJoined = true)
        )
    )
    val tournaments = _tournaments.asStateFlow()

    // Chat room live feeds
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage("Amit", "All the best!", "10:31 AM"),
            ChatMessage("Priya", "Let's win this!", "10:32 AM"),
            ChatMessage("Neha", "GG all!", "10:33 AM")
        )
    )
    val chatMessages = _chatMessages.asStateFlow()

    // Daily reward claimed status
    private val _dailyRewardDaysClaimed = MutableStateFlow(listOf(1, 2))
    val dailyRewardDaysClaimed = _dailyRewardDaysClaimed.asStateFlow()

    private val _luckyWheelSpinningResult = MutableStateFlow<String?>(null)
    val luckyWheelSpinningResult = _luckyWheelSpinningResult.asStateFlow()

    // --- SharedPreferences Lifecycle Initialization ---
    fun init(context: Context) {
        val p = context.getSharedPreferences("LudoMasterPrefs", Context.MODE_PRIVATE)
        prefs = p

        // Load settings preference states
        _soundEnabled.value = p.getBoolean("settings_sound", true)
        _musicEnabled.value = p.getBoolean("settings_music", true)
        _vibrationEnabled.value = p.getBoolean("settings_vibration", true)
        _darkTheme.value = p.getBoolean("settings_dark_theme", true)
        _currentThemeType.value = p.getString("settings_theme_type", "Classic Dark") ?: "Classic Dark"
        _currentSessionState.value = p.getString("session_state", "SPLASH") ?: "SPLASH"

        // Load profile stats and progress
        _playerState.value = PlayerProfile(
            id = p.getString("player_id", "101") ?: "101",
            name = p.getString("player_name", "Rahul") ?: "Rahul",
            level = p.getInt("player_level", 24),
            xp = p.getInt("player_xp", 1850),
            coins = p.getInt("player_coins", 15230),
            gems = p.getInt("player_gems", 320),
            selectedSkin = p.getString("player_selected_skin", "Classic Blue") ?: "Classic Blue",
            winRate = p.getString("player_win_rate", "64.5%") ?: "64.5%",
            totalMatches = p.getInt("player_total_matches", 142),
            wins = p.getInt("player_wins", 92),
            authType = p.getString("player_auth_type", "GUEST") ?: "GUEST",
            email = p.getString("player_email", "") ?: "",
            phone = p.getString("player_phone", "") ?: "",
            isVip = p.getBoolean("player_is_vip", false),
            vipFrame = p.getString("player_vip_frame", "None") ?: "None",
            vipDice = p.getString("player_vip_dice", "Classic") ?: "Classic"
        )
    }

    fun savePlayerState(profile: PlayerProfile) {
        prefs?.edit()?.apply {
            putString("player_id", profile.id)
            putString("player_name", profile.name)
            putInt("player_level", profile.level)
            putInt("player_xp", profile.xp)
            putInt("player_coins", profile.coins)
            putInt("player_gems", profile.gems)
            putString("player_selected_skin", profile.selectedSkin)
            putString("player_win_rate", profile.winRate)
            putInt("player_total_matches", profile.totalMatches)
            putInt("player_wins", profile.wins)
            putString("player_auth_type", profile.authType)
            putString("player_email", profile.email)
            putString("player_phone", profile.phone)
            putBoolean("player_is_vip", profile.isVip)
            putString("player_vip_frame", profile.vipFrame)
            putString("player_vip_dice", profile.vipDice)
            apply()
        }
    }

    fun setVipEnabled(enabled: Boolean) {
        _playerState.update { current ->
            val updated = current.copy(isVip = enabled)
            savePlayerState(updated)
            updated
        }
    }

    fun setVipFrame(frame: String) {
        _playerState.update { current ->
            val updated = current.copy(vipFrame = frame)
            savePlayerState(updated)
            updated
        }
    }

    fun setVipDice(dice: String) {
        _playerState.update { current ->
            val updated = current.copy(vipDice = dice)
            savePlayerState(updated)
            updated
        }
    }

    // --- State Modifier Operations ---
    fun setSessionState(sessionState: String) {
        _currentSessionState.value = sessionState
        prefs?.edit()?.putString("session_state", sessionState)?.apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs?.edit()?.putBoolean("settings_sound", enabled)?.apply()
    }

    fun setMusicEnabled(enabled: Boolean) {
        _musicEnabled.value = enabled
        prefs?.edit()?.putBoolean("settings_music", enabled)?.apply()
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
        prefs?.edit()?.putBoolean("settings_vibration", enabled)?.apply()
    }

    fun toggleTheme() {
        val nextTheme = !_darkTheme.value
        _darkTheme.value = nextTheme
        prefs?.edit()?.putBoolean("settings_dark_theme", nextTheme)?.apply()
    }

    fun addCoins(amount: Int) {
        _playerState.update { current ->
            val updated = current.copy(coins = current.coins + amount)
            savePlayerState(updated)
            updated
        }
    }

    fun spendCoins(amount: Int): Boolean {
        val currentCoins = _playerState.value.coins
        if (currentCoins >= amount) {
            _playerState.update { current ->
                val updated = current.copy(coins = currentCoins - amount)
                savePlayerState(updated)
                updated
            }
            return true
        }
        return false
    }

    fun addGems(amount: Int) {
        _playerState.update { current ->
            val updated = current.copy(gems = current.gems + amount)
            savePlayerState(updated)
            updated
        }
    }

    fun spendGems(amount: Int): Boolean {
        val currentGems = _playerState.value.gems
        if (currentGems >= amount) {
            _playerState.update { current ->
                val updated = current.copy(gems = currentGems - amount)
                savePlayerState(updated)
                updated
            }
            return true
        }
        return false
    }

    fun updateProfileName(newName: String) {
        _playerState.update { current ->
            val updated = current.copy(name = newName)
            savePlayerState(updated)
            updated
        }
    }

    // --- Auth Merge Payload and Flow ---
    data class AuthMergePayload(
        val cloudProfile: PlayerProfile,
        val guestProfile: PlayerProfile,
        val onComplete: () -> Unit
    )

    private val _pendingMergePayload = MutableStateFlow<AuthMergePayload?>(null)
    val pendingMergePayload = _pendingMergePayload.asStateFlow()

    fun isGuestSessionWithProgress(): Boolean {
        val current = _playerState.value
        return current.authType == "GUEST" && (current.totalMatches > 0 || current.coins != 5000 || current.gems != 15 || current.level > 1)
    }

    fun getCloudProfile(authType: String, key: String, defaultName: String): PlayerProfile {
        val cleanKey = key.replace(".", "_").replace("@", "_").replace(" ", "_").replace("+", "_")
        val userId = "${authType}_$cleanKey"
        val p = prefs ?: return PlayerProfile()
        
        val hasStats = p.contains("cloud_${userId}_level")
        if (!hasStats) {
            return PlayerProfile(
                id = userId,
                name = defaultName,
                level = 1,
                xp = 100,
                coins = 3000, // starting coins/login bonus
                gems = 50,
                selectedSkin = "Classic Blue",
                winRate = "0%",
                totalMatches = 0,
                wins = 0,
                authType = authType,
                email = if (authType == "EMAIL" || authType == "GOOGLE") key else "",
                phone = if (authType == "OTP") key else ""
            )
        }
        
        return PlayerProfile(
            id = userId,
            name = p.getString("cloud_${userId}_name", defaultName) ?: defaultName,
            level = p.getInt("cloud_${userId}_level", 1),
            xp = p.getInt("cloud_${userId}_xp", 100),
            coins = p.getInt("cloud_${userId}_coins", 3000),
            gems = p.getInt("cloud_${userId}_gems", 50),
            selectedSkin = p.getString("cloud_${userId}_selected_skin", "Classic Blue") ?: "Classic Blue",
            winRate = p.getString("cloud_${userId}_win_rate", "0%") ?: "0%",
            totalMatches = p.getInt("cloud_${userId}_total_matches", 0),
            wins = p.getInt("cloud_${userId}_wins", 0),
            authType = authType,
            email = if (authType == "EMAIL" || authType == "GOOGLE") key else "",
            phone = if (authType == "OTP") key else ""
        )
    }

    fun saveCloudProfile(cloudProfile: PlayerProfile) {
        val p = prefs ?: return
        val key = if (cloudProfile.authType == "GOOGLE" || cloudProfile.authType == "EMAIL") {
            cloudProfile.email
        } else {
            cloudProfile.phone
        }
        val cleanKey = key.replace(".", "_").replace("@", "_").replace(" ", "_").replace("+", "_")
        val userId = "${cloudProfile.authType}_$cleanKey"
        
        p.edit().apply {
            putString("cloud_${userId}_name", cloudProfile.name)
            putInt("cloud_${userId}_level", cloudProfile.level)
            putInt("cloud_${userId}_xp", cloudProfile.xp)
            putInt("cloud_${userId}_coins", cloudProfile.coins)
            putInt("cloud_${userId}_gems", cloudProfile.gems)
            putString("cloud_${userId}_selected_skin", cloudProfile.selectedSkin)
            putString("cloud_${userId}_win_rate", cloudProfile.winRate)
            putInt("cloud_${userId}_total_matches", cloudProfile.totalMatches)
            putInt("cloud_${userId}_wins", cloudProfile.wins)
            apply()
        }
    }

    fun authenticateCloudUser(
        authType: String,
        key: String,
        displayName: String,
        onSuccess: () -> Unit
    ) {
        val currentLocal = _playerState.value
        val retrievedCloud = getCloudProfile(authType, key, displayName)
        
        if (isGuestSessionWithProgress()) {
            _pendingMergePayload.value = AuthMergePayload(
                cloudProfile = retrievedCloud,
                guestProfile = currentLocal,
                onComplete = onSuccess
            )
        } else {
            _playerState.value = retrievedCloud
            savePlayerState(retrievedCloud)
            setSessionState(authType)
            onSuccess()
        }
    }

    fun resolvePendingMerge(mergeLocalData: Boolean) {
        val payload = _pendingMergePayload.value ?: return
        
        if (mergeLocalData) {
            val guest = payload.guestProfile
            val cloud = payload.cloudProfile
            
            val mergedMatches = cloud.totalMatches + guest.totalMatches
            val mergedWins = cloud.wins + guest.wins
            val winPct = if (mergedMatches > 0) {
                (mergedWins.toFloat() / mergedMatches.toFloat()) * 100f
            } else {
                0f
            }
            val winRateStr = String.format("%.1f%%", winPct)
            
            val mergedCoins = cloud.coins + guest.coins
            val mergedGems = cloud.gems + guest.gems
            
            var mergedXp = cloud.xp + guest.xp
            var mergedLevel = cloud.level + (guest.level - 1)
            if (mergedXp >= 1000) {
                val levelsGained = mergedXp / 1000
                mergedLevel += levelsGained
                mergedXp %= 1000
            }
            
            val mergedProfile = cloud.copy(
                coins = mergedCoins,
                gems = mergedGems,
                level = mergedLevel,
                xp = mergedXp,
                totalMatches = mergedMatches,
                wins = mergedWins,
                winRate = winRateStr
            )
            
            _playerState.value = mergedProfile
            saveCloudProfile(mergedProfile)
            savePlayerState(mergedProfile)
        } else {
            val cloud = payload.cloudProfile
            _playerState.value = cloud
            savePlayerState(cloud)
        }
        
        setSessionState(payload.cloudProfile.authType)
        _pendingMergePayload.value = null
        payload.onComplete()
    }

    fun cancelMergeFlow() {
        _pendingMergePayload.value = null
    }

    // --- 1-Click Guest Profile Builder ---
    fun createInstantGuest() {
        val randomSuffix = (1000..9999).random()
        val generatedName = "Guest_$randomSuffix"
        _playerState.update { current ->
            val newProfile = current.copy(
                id = randomSuffix.toString(),
                name = generatedName,
                level = 1,
                xp = 0,
                coins = 5000, // 5k Guest starting coins
                gems = 15,
                totalMatches = 0,
                wins = 0,
                winRate = "0%",
                authType = "GUEST",
                email = "",
                phone = ""
            )
            savePlayerState(newProfile)
            newProfile
        }
        setSessionState("GUEST")
    }

    // --- Statistics and Progress Match Completion Integrations ---
    fun completeMatch(won: Boolean, xpEarned: Int, coinsEarned: Int) {
        _playerState.update { current ->
            val newMatches = current.totalMatches + 1
            val newWins = if (won) current.wins + 1 else current.wins
            
            // Calculate win percentage string safely
            val pctFloat = (newWins.toFloat() / newMatches.toFloat()) * 100f
            val winRatePct = String.format("%.1f%%", pctFloat)

            var newXp = current.xp + xpEarned
            var newLevel = current.level
            var levelsBonus = 0

            // Handle standard level threshold check (1000 XP levels, increments beautifully)
            if (newXp >= 1000) {
                val levelsGained = newXp / 1000
                newLevel += levelsGained
                newXp %= 1000
                levelsBonus = levelsGained * 500 // Level Up bonus!
            }

            val totalClaim = coinsEarned + levelsBonus
            if (totalClaim != 0) {
                EconomyManager.recordTransaction(
                    amount = totalClaim,
                    source = if (won) "WIN_REWARD" else "LOSE_MATCH",
                    description = "Finished Ludo Match (XP gained: +$xpEarned. Level-Up bonus: +$levelsBonus GC)"
                )
            }

            val updated = current.copy(
                totalMatches = newMatches,
                wins = newWins,
                winRate = winRatePct,
                xp = newXp,
                level = newLevel
            )
            savePlayerState(updated)
            updated
        }
    }

    fun purchaseStoreItem(id: String): String {
        var purchaseLog = "Purchase started..."
        val currentStore = _storeItems.value
        val targetItem = currentStore.find { it.id == id } ?: return "Item not found!"

        if (targetItem.costAmount.contains("$")) {
            // Simulated Real Money purchase
            if (targetItem.type == "coins") {
                addCoins(targetItem.rewardAmount)
                purchaseLog = "Successfully bought ${targetItem.rewardAmount} Coins!"
            } else if (targetItem.type == "gems") {
                addGems(targetItem.rewardAmount)
                purchaseLog = "Successfully bought ${targetItem.rewardAmount} Gems!"
            }
        } else if (targetItem.costAmount.contains("Gems")) {
            val costStr = targetItem.costAmount.replace(" Gems", "").trim()
            val costVal = costStr.toIntOrNull() ?: 0
            if (spendGems(costVal)) {
                _storeItems.update { list ->
                    list.map { if (it.id == id) it.copy(isPurchased = true) else it }
                }
                purchaseLog = "Equipped ${targetItem.title}!"
            } else {
                purchaseLog = "Insufficient gems! Purchase more in Store."
            }
        }
        return purchaseLog
    }

    fun sendLiveMessage(sender: String, text: String, isEmoji: Boolean = false) {
        _chatMessages.update { list ->
            list + ChatMessage(sender, text, "Now", isEmoji = isEmoji)
        }
    }

    fun claimDailyDay(dayIndex: Int, coinAmount: Int) {
        if (!_dailyRewardDaysClaimed.value.contains(dayIndex)) {
            _dailyRewardDaysClaimed.update { it + dayIndex }
            addCoins(coinAmount)
        }
    }

    fun joinTournament(tourId: String): Boolean {
        val currentList = _tournaments.value
        val tournament = currentList.find { it.id == tourId } ?: return false
        if (tournament.isJoined) return true

        if (spendCoins(tournament.entryFee)) {
            _tournaments.update { list ->
                list.map {
                    if (it.id == tourId) it.copy(isJoined = true, participantsCount = it.participantsCount + 1)
                    else it
                }
            }
            return true
        }
        return false
    }

    fun addFriend(name: String, level: Int): Boolean {
        if (_friendsList.value.any { it.name.lowercase() == name.lowercase() }) return false
        _friendsList.update { list ->
            list + Friend("sim_id_${java.util.UUID.randomUUID()}", name, level, 1000, true, (0..4).random())
        }
        return true
    }
}
