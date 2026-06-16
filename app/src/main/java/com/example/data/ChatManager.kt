package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

// --- Data Models for Chats and Clans ---

data class PrivateMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val isEmoji: Boolean = false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "senderId" to senderId,
            "senderName" to senderName,
            "message" to message,
            "timestamp" to timestamp,
            "isEmoji" to isEmoji
        )
    }
}

data class Clan(
    val id: String = "",
    val name: String = "",
    val tag: String = "", // E.g. [PRO]
    val description: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val memberCount: Int = 1,
    val members: List<String> = emptyList(), // List of member userIds
    val timestamp: Long = 0L
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "tag" to tag,
            "description" to description,
            "ownerId" to ownerId,
            "ownerName" to ownerName,
            "memberCount" to memberCount,
            "members" to members,
            "timestamp" to timestamp
        )
    }
}

object ChatManager {
    private const val TAG = "ChatManager"
    private var db: FirebaseFirestore? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var isSimulationMode = true
        private set

    // Real-time private chat messages list for selected friend
    private val _currentPrivateMessages = MutableStateFlow<List<PrivateMessage>>(emptyList())
    val currentPrivateMessages = _currentPrivateMessages.asStateFlow()

    // Current active private contact user ID
    private val _activeChatFriendId = MutableStateFlow<String?>(null)
    val activeChatFriendId = _activeChatFriendId.asStateFlow()

    // Clans States
    private val _allClans = MutableStateFlow<List<Clan>>(emptyList())
    val allClans = _allClans.asStateFlow()

    private val _myClan = MutableStateFlow<Clan?>(null)
    val myClan = _myClan.asStateFlow()

    private val _clanMessages = MutableStateFlow<List<PrivateMessage>>(emptyList())
    val clanMessages = _clanMessages.asStateFlow()

    // Real-time Chat Live Diagnostics Logs
    private val _chatLogs = MutableStateFlow<List<String>>(listOf("System Chat Engine initializing..."))
    val chatLogs = _chatLogs.asStateFlow()

    // Listeners handles
    private var privateChatListener: ListenerRegistration? = null
    private var clanListener: ListenerRegistration? = null
    private var clansListListener: ListenerRegistration? = null
    private var clanMessagesListener: ListenerRegistration? = null

    fun initialize(context: Context) {
        logDiagnostic("Chat Terminal operational.")
        
        // Link with client simulation flags
        isSimulationMode = MultiplayerManager.isSimulationMode

        try {
            db = FirebaseFirestore.getInstance()
            if (!isSimulationMode) {
                logDiagnostic("Real-time cloud database pipeline secured.")
                loadAllClansFromCloud()
            } else {
                logDiagnostic("Offline state selected: Loading sandbox simulation variables.")
                loadMockClans()
            }
        } catch (e: Exception) {
            isSimulationMode = true
            logDiagnostic("Offline sandbox activated (Firebase fallback initialized).")
            loadMockClans()
        }

        // Keep local user profile mapping updated to check if user has a clan
        coroutineScope.launch {
            LudoMasterRepository.playerState.collect { profile ->
                if (profile.id.isNotEmpty()) {
                    syncUserClanState(profile.id)
                }
            }
        }
    }

    private fun logDiagnostic(msg: String) {
        _chatLogs.update { (listOf("[Chat] $msg") + it).take(30) }
    }

    // --- Private Chats Core Logic ---

    fun openPrivateChatWithFriend(friendId: String) {
        val myId = LudoMasterRepository.playerState.value.id
        if (myId.isBlank() || friendId.isBlank()) return

        _activeChatFriendId.value = friendId
        privateChatListener?.remove()

        val chatId = getChatId(myId, friendId)
        logDiagnostic("Initiating chat terminal channel: $chatId")

        if (isSimulationMode) {
            // Load custom local simulated historical items
            loadSimulationPrivateHistory(friendId)
            return
        }

        // Firebase Realtime Listener on private chat subcollection
        privateChatListener = db!!.collection("private_chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Private chat subscription failed: ${error.message}")
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.getString("id") ?: ""
                    val senderId = doc.getString("senderId") ?: ""
                    val senderName = doc.getString("senderName") ?: ""
                    val msg = doc.getString("message") ?: ""
                    val ts = doc.getLong("timestamp") ?: 0L
                    val isEmoji = doc.getBoolean("isEmoji") ?: false
                    PrivateMessage(id, senderId, senderName, msg, ts, isEmoji)
                } ?: emptyList()

                _currentPrivateMessages.value = list
            }
    }

    fun closePrivateChat() {
        privateChatListener?.remove()
        privateChatListener = null
        _activeChatFriendId.value = null
        _currentPrivateMessages.value = emptyList()
    }

    fun sendPrivateMessage(text: String, isEmoji: Boolean = false) {
        val myProfile = LudoMasterRepository.playerState.value
        val friendId = _activeChatFriendId.value
        if (myProfile.id.isBlank() || friendId == null || text.isBlank()) return

        val msgId = "msg_${UUID.randomUUID()}"
        val newMessage = PrivateMessage(
            id = msgId,
            senderId = myProfile.id,
            senderName = myProfile.name,
            message = text,
            timestamp = System.currentTimeMillis(),
            isEmoji = isEmoji
        )

        if (isSimulationMode) {
            _currentPrivateMessages.update { it + newMessage }
            logDiagnostic("Sent message internally: '$text'")
            
            // Auto schedule a fun reply reaction from simulated bots!
            scheduleSimulatedBotReply(friendId)
            return
        }

        val chatId = getChatId(myProfile.id, friendId)
        db!!.collection("private_chats")
            .document(chatId)
            .collection("messages")
            .document(msgId)
            .set(newMessage.toMap())
            .addOnSuccessListener {
                logDiagnostic("Dispatched database index payload to $chatId")
            }
            .addOnFailureListener { e ->
                logDiagnostic("Network failed to deliver private packet: ${e.message}")
            }
    }

    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    // --- Clans & Clan Chat Core Logic ---

    fun createClan(name: String, tag: String, description: String, onResult: (Boolean, String) -> Unit) {
        val myProfile = LudoMasterRepository.playerState.value
        if (myProfile.id.isBlank()) {
            onResult(false, "Profile incomplete.")
            return
        }

        val requiredCost = 1000
        if (myProfile.coins < requiredCost) {
            onResult(false, "You need at least $requiredCost Battle Coins to acquire a clan master permit!")
            return
        }

        val clanId = "clan_${UUID.randomUUID().toString().take(8).uppercase()}"
        val newClan = Clan(
            id = clanId,
            name = name.trim(),
            tag = tag.trim().uppercase(),
            description = description.trim(),
            ownerId = myProfile.id,
            ownerName = myProfile.name,
            memberCount = 1,
            members = listOf(myProfile.id),
            timestamp = System.currentTimeMillis()
        )

        // Deduct setup coins through our economy system
        EconomyManager.recordTransaction(
            amount = -requiredCost,
            source = "CLAN_CREATION",
            description = "Acquired Ludo Clan licence for Tag: [$tag]"
        ) { economySuccess ->
            if (economySuccess) {
                if (isSimulationMode) {
                    _myClan.value = newClan
                    _allClans.update { listOf(newClan) + it }
                    logDiagnostic("Founded Simulated Clan [$tag] $name with license fee of $requiredCost gc")
                    onResult(true, "Clan founded successfully!")
                    joinClanChatRoom(clanId)
                    return@recordTransaction
                }

                // Push clan document to firestore
                db!!.collection("clans").document(clanId)
                    .set(newClan.toMap())
                    .addOnSuccessListener {
                        logDiagnostic("Cloud Registry: Saved new Clan file tag [$tag].")
                        _myClan.value = newClan
                        joinClanChatRoom(clanId)
                        onResult(true, "Clan successfully deployed to cloud network!")
                    }
                    .addOnFailureListener { e ->
                        logDiagnostic("Could not registry clan to firestore: ${e.localizedMessage}")
                        // Rollback fee
                        EconomyManager.recordTransaction(requiredCost, "REVERT", "Failed to register clan.")
                        onResult(false, "Failed to deploy: ${e.message}")
                    }
            } else {
                onResult(false, "Clan charter purchase failed transaction clearance.")
            }
        }
    }

    fun joinClan(clanId: String, onResult: (Boolean, String) -> Unit) {
        val myProfile = LudoMasterRepository.playerState.value
        if (myProfile.id.isBlank()) {
            onResult(false, "Profile ID invalid.")
            return
        }

        if (_myClan.value != null) {
            onResult(false, "You must leave your current clan before enlisting into another!")
            return
        }

        if (isSimulationMode) {
            val clan = _allClans.value.find { it.id == clanId }
            if (clan != null) {
                val updatedMembers = clan.members + myProfile.id
                val updatedClan = clan.copy(
                    members = updatedMembers,
                    memberCount = updatedMembers.size
                )
                _myClan.value = updatedClan
                _allClans.update { list -> list.map { if (it.id == clanId) updatedClan else it } }
                logDiagnostic("Enlisted silently into simulated clan tag: [${clan.tag}]")
                onResult(true, "Enlisted into simulated clan successfully!")
                joinClanChatRoom(clanId)
            } else {
                onResult(false, "Simulated clan workspace expired.")
            }
            return
        }

        // Firestore real join
        val clanDoc = db!!.collection("clans").document(clanId)
        db!!.runTransaction { transaction ->
            val snapshot = transaction.get(clanDoc)
            if (!snapshot.exists()) throw Exception("Selected clan was disbanded.")

            val currentMembers = snapshot.get("members") as? List<String> ?: emptyList()
            if (currentMembers.contains(myProfile.id)) return@runTransaction

            val updatedMembers = currentMembers + myProfile.id
            transaction.update(clanDoc, "members", updatedMembers)
            transaction.update(clanDoc, "memberCount", updatedMembers.size)
        }.addOnSuccessListener {
            logDiagnostic("Joint venture: Added player ID to Clan directory: $clanId")
            syncUserClanState(myProfile.id)
            onResult(true, "Welcome to the crew! Joined clan successfully!")
        }.addOnFailureListener { e ->
            onResult(false, "Failed to join: ${e.message}")
        }
    }

    fun leaveClan(onResult: (Boolean, String) -> Unit) {
        val myProfile = LudoMasterRepository.playerState.value
        val currentClan = _myClan.value
        if (myProfile.id.isBlank() || currentClan == null) {
            onResult(false, "You are currently unaffiliated.")
            return
        }

        if (isSimulationMode) {
            _myClan.value = null
            _clanMessages.value = emptyList()
            clanMessagesListener?.remove()

            val updatedMembers = currentClan.members.filter { it != myProfile.id }
            val updatedClan = currentClan.copy(
                members = updatedMembers,
                memberCount = updatedMembers.size
            )
            _allClans.update { list -> list.map { if (it.id == currentClan.id) updatedClan else it } }
            logDiagnostic("Departed: Left simulated clan successfully.")
            onResult(true, "You left the clan.")
            return
        }

        // Firestore cloud leave
        val clanDoc = db!!.collection("clans").document(currentClan.id)
        db!!.runTransaction { transaction ->
            val snapshot = transaction.get(clanDoc)
            if (!snapshot.exists()) return@runTransaction

            val currentMembers = snapshot.get("members") as? List<String> ?: emptyList()
            val updatedMembers = currentMembers.filter { it != myProfile.id }

            if (updatedMembers.isEmpty() && currentClan.ownerId == myProfile.id) {
                // Dissolves empty clan if sole owner departs
                transaction.delete(clanDoc)
            } else {
                transaction.update(clanDoc, "members", updatedMembers)
                transaction.update(clanDoc, "memberCount", updatedMembers.size)
            }
        }.addOnSuccessListener {
            clanMessagesListener?.remove()
            _myClan.value = null
            _clanMessages.value = emptyList()
            logDiagnostic("Separation complete: Left Cloud Clan tag Successfully.")
            onResult(true, "Successfully departed clan.")
        }.addOnFailureListener { e ->
            onResult(false, "System was unable to submit leave form: ${e.localizedMessage}")
        }
    }

    fun sendClanMessage(text: String, isEmoji: Boolean = false) {
        val myProfile = LudoMasterRepository.playerState.value
        val clan = _myClan.value
        if (myProfile.id.isBlank() || clan == null || text.isBlank()) return

        val msgId = "msg_${UUID.randomUUID()}"
        val newMessage = PrivateMessage(
            id = msgId,
            senderId = myProfile.id,
            senderName = myProfile.name,
            message = text,
            timestamp = System.currentTimeMillis(),
            isEmoji = isEmoji
        )

        if (isSimulationMode) {
            _clanMessages.update { it + newMessage }
            logDiagnostic("Sent clan message internally: '$text'")
            
            // Sim clan responses
            scheduleSimulationClanReplies()
            return
        }

        db!!.collection("clans")
            .document(clan.id)
            .collection("chats")
            .document(msgId)
            .set(newMessage.toMap())
            .addOnSuccessListener {
                logDiagnostic("Pushed message payload to Clan channel safely.")
            }
    }

    private fun joinClanChatRoom(clanId: String) {
        clanMessagesListener?.remove()
        if (isSimulationMode) {
            loadMockClanHistory(clanId)
            return
        }

        clanMessagesListener = db!!.collection("clans")
            .document(clanId)
            .collection("chats")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Clan message feed error: ${error.message}")
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.getString("id") ?: ""
                    val senderId = doc.getString("senderId") ?: ""
                    val senderName = doc.getString("senderName") ?: ""
                    val msg = doc.getString("message") ?: ""
                    val ts = doc.getLong("timestamp") ?: 0L
                    val isEmoji = doc.getBoolean("isEmoji") ?: false
                    PrivateMessage(id, senderId, senderName, msg, ts, isEmoji)
                } ?: emptyList()

                _clanMessages.value = list
            }
    }

    private fun syncUserClanState(userId: String) {
        if (userId.isBlank() || isSimulationMode) return

        // Fetch User's Clan Realtime Listener
        clanListener?.remove()
        clanListener = db!!.collection("clans")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Search User Clan snapshot failed: ${error.message}")
                    return@addSnapshotListener
                }

                val userClanDoc = snapshot?.documents?.firstOrNull()
                if (userClanDoc != null && userClanDoc.exists()) {
                    val id = userClanDoc.getString("id") ?: ""
                    val name = userClanDoc.getString("name") ?: ""
                    val tag = userClanDoc.getString("tag") ?: ""
                    val desc = userClanDoc.getString("description") ?: ""
                    val ownerId = userClanDoc.getString("ownerId") ?: ""
                    val ownerName = userClanDoc.getString("ownerName") ?: ""
                    val memberCount = userClanDoc.getLong("memberCount")?.toInt() ?: 1
                    val members = userClanDoc.get("members") as? List<String> ?: emptyList()

                    val clanModel = Clan(id, name, tag, desc, ownerId, ownerName, memberCount, members)
                    _myClan.value = clanModel
                    joinClanChatRoom(id)
                } else {
                    _myClan.value = null
                    _clanMessages.value = emptyList()
                    clanMessagesListener?.remove()
                }
            }
    }

    private fun loadAllClansFromCloud() {
        clansListListener?.remove()
        clansListListener = db!!.collection("clans")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(20)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Clans query issue: ${error.message}")
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    val id = doc.getString("id") ?: ""
                    val name = doc.getString("name") ?: ""
                    val tag = doc.getString("tag") ?: ""
                    val desc = doc.getString("description") ?: ""
                    val ownerId = doc.getString("ownerId") ?: ""
                    val ownerName = doc.getString("ownerName") ?: ""
                    val memberCount = doc.getLong("memberCount")?.toInt() ?: 1
                    val members = doc.get("members") as? List<String> ?: emptyList()
                    Clan(id, name, tag, desc, ownerId, ownerName, memberCount, members)
                } ?: emptyList()

                _allClans.value = list
            }
    }

    // --- High-Fidelity Mock & Client Simulations ---

    private fun loadMockClans() {
        val cl1 = Clan("clan_9010", "Apex Ludo Elite", "APEX", "Seeking hyper competitive champions to challenge tournament bet matches. Regular wagers.", "host_2910", "Amit (Host)", 4, listOf("host_2910", "guest_3311"), System.currentTimeMillis() - 86400000)
        val cl2 = Clan("clan_9011", "Royal Dice Kings", "ROYAL", "Relaxed friendly clan focused on daily coins exchange and custom tabletop strategies.", "guest_4044", "Priya", 3, listOf("guest_4044"), System.currentTimeMillis() - 40000000)
        val cl3 = Clan("clan_9012", "Cosmic Phoenixes", "COSMIC", "We rise from defeat! Daily spin fans, referral hunters & active chatroom builders.", "guest_3311", "Neha", 8, listOf("guest_3311", "host_9210"), System.currentTimeMillis() - 20000000)
        
        _allClans.value = listOf(cl1, cl2, cl3)
    }

    private fun loadSimulationPrivateHistory(friendId: String) {
        val fName = getFriendName(friendId)
        _currentPrivateMessages.value = listOf(
            PrivateMessage("m1", friendId, fName, "GG! That double dice roll was absolutely heroic yesterday 🎲", System.currentTimeMillis() - 7200000),
            PrivateMessage("m2", LudoMasterRepository.playerState.value.id, LudoMasterRepository.playerState.value.name, "Haha thank you! The luck index was high! Ready to play another?", System.currentTimeMillis() - 3600000),
            PrivateMessage("m3", friendId, fName, "Definitely! Set up a bet room and invite me!", System.currentTimeMillis() - 1800000)
        )
    }

    private fun loadMockClanHistory(clanId: String) {
        val cName = _allClans.value.find { it.id == clanId }?.name ?: "Clan Lounge"
        _clanMessages.value = listOf(
            PrivateMessage("c1", "host_2910", "Amit (Host)", "Welcome to the central lounge for $cName!", System.currentTimeMillis() - 100000000),
            PrivateMessage("c2", "guest_3311", "Neha", "Hey guys! Just claimed my 500 referral coins today, so hype!", System.currentTimeMillis() - 50000000),
            PrivateMessage("c3", "guest_4044", "Priya", "Awesome! Friendly reminder we have a 5K wager lobby starting in 10 minutes, who is in? 👑", System.currentTimeMillis() - 15000000)
        )
    }

    private fun scheduleSimulatedBotReply(friendId: String) {
        coroutineScope.launch {
            delay(1500)
            val name = getFriendName(friendId)
            val replies = listOf(
                "Wow, incredible! Let's conquer the boards! 😎",
                "Yes! Totally in agreement. 👍",
                "Roll double-sixes, incoming match streak loading! 🔥",
                "I am practicing my tokens maneuvers, meet me in the battle matchmaking arena!",
                "Amazing chat! Let's get more coins via rewards portal."
            )
            val replyMessage = PrivateMessage(
                id = "msg_reply_${UUID.randomUUID()}",
                senderId = friendId,
                senderName = name,
                message = replies.random(),
                timestamp = System.currentTimeMillis()
            )
            _currentPrivateMessages.update { it + replyMessage }
            logDiagnostic("Received reaction from $name.")
        }
    }

    private fun scheduleSimulationClanReplies() {
        coroutineScope.launch {
            delay(2000)
            val clanBotId = listOf("host_2910", "guest_3311", "guest_4044").random()
            val clanBotName = getFriendName(clanBotId)
            val replies = listOf(
                "Wow, nice tactic! APEX rules the grid!",
                "Let's win maximum rewards! 🏆",
                "Welcome to candidates checking in! Let's climb level indices.",
                "Yes, we are on the road to the cosmic highscores!",
                "Excellent message, sending dice blessings! 🎰"
            )
            val msg = PrivateMessage(
                id = "c_reply_${UUID.randomUUID()}",
                senderId = clanBotId,
                senderName = clanBotName,
                message = replies.random(),
                timestamp = System.currentTimeMillis()
            )
            _clanMessages.update { it + msg }
            logDiagnostic("Clan Chat Update: Reply from $clanBotName received.")
        }
    }

    private fun getFriendName(friendId: String): String {
        return when (friendId) {
            "host_2910" -> "Amit"
            "guest_3311" -> "Neha"
            "guest_4044" -> "Priya"
            "host_9210" -> "Rajesh"
            else -> FriendsManager.friendsList.value.find { it.id == friendId }?.name ?: "Rival Master"
        }
    }
}
