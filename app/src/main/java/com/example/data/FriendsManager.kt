package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

// --- Data Models for Friends, Requests, and Invites ---

data class FriendRequest(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val status: String = "PENDING", // "PENDING", "ACCEPTED", "DECLINED"
    val timestamp: Long = 0L
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "senderId" to senderId,
            "senderName" to senderName,
            "receiverId" to receiverId,
            "receiverName" to receiverName,
            "status" to status,
            "timestamp" to timestamp
        )
    }
}

data class LudoInvitation(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val roomId: String = "",
    val status: String = "PENDING", // "PENDING", "ACCEPTED", "DECLINED"
    val timestamp: Long = 0L
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "senderId" to senderId,
            "senderName" to senderName,
            "receiverId" to receiverId,
            "roomId" to roomId,
            "status" to status,
            "timestamp" to timestamp
        )
    }
}

object FriendsManager {
    private const val TAG = "FriendsManager"
    private var db: FirebaseFirestore? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    var isSimulationMode = true
        private set

    private var localPlayerProfile = PlayerProfile()

    // Observables
    private val _friendsList = MutableStateFlow<List<Friend>>(emptyList())
    val friendsList = _friendsList.asStateFlow()

    private val _pendingRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val pendingRequests = _pendingRequests.asStateFlow()

    private val _incomingInvitations = MutableStateFlow<List<LudoInvitation>>(emptyList())
    val incomingInvitations = _incomingInvitations.asStateFlow()

    private val _activeInviteReceived = MutableStateFlow<LudoInvitation?>(null)
    val activeInviteReceived = _activeInviteReceived.asStateFlow()

    private val _friendsLogs = MutableStateFlow<List<String>>(listOf("Initializing Social Hub..."))
    val friendsLogs = _friendsLogs.asStateFlow()

    // Listeners
    private var requestsListener: ListenerRegistration? = null
    private var invitesListener: ListenerRegistration? = null
    private var friendsListener: ListenerRegistration? = null
    private val presenceListeners = mutableMapOf<String, ListenerRegistration>()

    fun initialize(context: Context) {
        logMessage("Ludo Social Hub active.")
        
        // Keep synced with locally updated PlayerProfile (name, level, etc.)
        coroutineScope.launch {
            LudoMasterRepository.playerState.collect { profile ->
                localPlayerProfile = profile
                updatePresenceAndProfile()
            }
        }

        // Setup real pipeline or fallback mock simulation
        try {
            db = FirebaseFirestore.getInstance()
            // We read MultiplayerManager's offline fallback indicator so they align
            isSimulationMode = MultiplayerManager.isSimulationMode
            
            if (isSimulationMode) {
                logMessage("Sandbox Simulation: pre-loaded local multiplayer peers active.")
                setupMockData()
            } else {
                logMessage("Real Cloud Social synchronization online.")
                setupCloudSync()
            }
        } catch (e: Exception) {
            isSimulationMode = true
            logMessage("Sandbox Simulation active (Firestore init failed).")
            setupMockData()
            Log.e(TAG, "Using simulation due to Friends Firestore error: ${e.message}")
        }
    }

    private fun logMessage(msg: String) {
        Log.d(TAG, msg)
        _friendsLogs.update { (listOf("[Social] $msg") + it).take(30) }
    }

    // --- Presence System & Firestore Setup ---
    private fun updatePresenceAndProfile() {
        if (isSimulationMode || db == null) return
        
        val userMap = mapOf(
            "id" to localPlayerProfile.id,
            "name" to localPlayerProfile.name,
            "level" to localPlayerProfile.level,
            "coins" to localPlayerProfile.coins,
            "isOnline" to true,
            "lastSeen" to System.currentTimeMillis()
        )
        db?.collection("users")?.document(localPlayerProfile.id)?.set(userMap)
            ?.addOnFailureListener { e ->
                Log.e(TAG, "Failed to update profile presence in Firestore: ${e.message}")
            }
    }

    fun makeOffline() {
        if (isSimulationMode || db == null) return
        db?.collection("users")?.document(localPlayerProfile.id)?.update("isOnline", false)
    }

    // --- Cloud Synchronization ---
    private fun setupCloudSync() {
        val currentUid = localPlayerProfile.id
        if (currentUid.isBlank()) return

        // 1. Listen to incoming Friend Requests
        requestsListener?.remove()
        requestsListener = db?.collection("friend_requests")
            ?.whereEqualTo("receiverId", currentUid)
            ?.whereEqualTo("status", "PENDING")
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Requests snapshot error: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val senderId = doc.getString("senderId") ?: ""
                    val senderName = doc.getString("senderName") ?: ""
                    val receiverId = doc.getString("receiverId") ?: ""
                    val receiverName = doc.getString("receiverName") ?: ""
                    val status = doc.getString("status") ?: "PENDING"
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    FriendRequest(doc.id, senderId, senderName, receiverId, receiverName, status, timestamp)
                } ?: emptyList()
                _pendingRequests.value = list
                if (list.isNotEmpty()) {
                    logMessage("You have ${list.size} pending friend request(s)!")
                }
            }

        // 2. Listen to active invitations to a room
        invitesListener?.remove()
        invitesListener = db?.collection("invitations")
            ?.whereEqualTo("receiverId", currentUid)
            ?.whereEqualTo("status", "PENDING")
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Invites snapshot error: ${error.message}")
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val senderId = doc.getString("senderId") ?: ""
                    val senderName = doc.getString("senderName") ?: ""
                    val receiverId = doc.getString("receiverId") ?: ""
                    val roomId = doc.getString("roomId") ?: ""
                    val status = doc.getString("status") ?: "PENDING"
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    LudoInvitation(doc.id, senderId, senderName, receiverId, roomId, status, timestamp)
                } ?: emptyList()
                
                _incomingInvitations.value = list
                if (list.isNotEmpty()) {
                    val latest = list.maxByOrNull { it.timestamp }
                    _activeInviteReceived.value = latest
                    logMessage("New game invite received from ${latest?.senderName} for Room ${latest?.roomId}")
                } else {
                    _activeInviteReceived.value = null
                }
            }

        // 3. Listen to Friendships
        friendsListener?.remove()
        friendsListener = db?.collection("friends")
            ?.whereArrayContains("userIds", currentUid)
            ?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Friends listener error: ${error.message}")
                    return@addSnapshotListener
                }
                val rawFriendIds = mutableListOf<String>()
                snapshot?.documents?.forEach { doc ->
                    val ids = doc.get("userIds") as? List<*> ?: emptyList<Any>()
                    val peerId = ids.firstOrNull { it != currentUid }?.toString()
                    if (peerId != null) rawFriendIds.add(peerId)
                }
                
                // Keep presence syncing of friends
                syncFriendsPresenceList(rawFriendIds)
            }
    }

    private fun syncFriendsPresenceList(peerIds: List<String>) {
        if (isSimulationMode || db == null) return
        
        // Remove listeners for removed friends
        val removed = presenceListeners.keys.filter { !peerIds.contains(it) }
        removed.forEach { id ->
            presenceListeners[id]?.remove()
            presenceListeners.remove(id)
        }

        // Synchronize state list on snapshot changes
        if (peerIds.isEmpty()) {
            _friendsList.value = emptyList()
            return
        }

        val updatedMap = mutableMapOf<String, Friend>()
        // Seed initial entries if not exists
        _friendsList.value.forEach { f ->
            if (peerIds.contains(f.id)) {
                updatedMap[f.id] = f
            }
        }

        peerIds.forEach { peerId ->
            if (!presenceListeners.containsKey(peerId)) {
                val lis = db?.collection("users")?.document(peerId)
                    ?.addSnapshotListener { snapshot, err ->
                        if (err != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                        
                        val name = snapshot.getString("name") ?: "Peer Player"
                        val lvl = snapshot.getLong("level")?.toInt() ?: 1
                        val coins = snapshot.getLong("coins")?.toInt() ?: 1000
                        val isOnline = snapshot.getBoolean("isOnline") ?: false
                        val avCol = snapshot.getLong("avatarColorIndex")?.toInt() ?: 0

                        val updatedFriend = Friend(
                            id = peerId,
                            name = name,
                            level = lvl,
                            coins = coins,
                            isOnline = isOnline,
                            avatarColorIndex = avCol
                        )
                        updatedMap[peerId] = updatedFriend
                        _friendsList.value = updatedMap.values.toList().sortedByDescending { it.isOnline }
                    }
                if (lis != null) {
                    presenceListeners[peerId] = lis
                }
            }
        }
    }

    // --- Friend Action APIs ---

    fun sendFriendRequest(targetNickname: String, onFinished: (Boolean, String) -> Unit) {
        val trimmed = targetNickname.trim()
        if (trimmed.isEmpty()) {
            onFinished(false, "Please specify a nickname name.")
            return
        }
        if (trimmed.lowercase() == localPlayerProfile.name.lowercase()) {
            onFinished(false, "You cannot send a friend request to yourself!")
            return
        }

        if (isSimulationMode) {
            coroutineScope.launch {
                delay(1000)
                if (_friendsList.value.any { it.name.lowercase() == trimmed.lowercase() }) {
                    onFinished(false, "$trimmed is already your friend!")
                } else {
                    logMessage("Friend request sent to $trimmed (Simulated).")
                    // Automatically simulate that target user accepts the request in 3 seconds!
                    logMessage("$trimmed accepts request and joins your crew!")
                    val newSimulatedFriend = Friend(
                        id = "sim_id_${UUID.randomUUID()}",
                        name = trimmed,
                        level = (10..35).random(),
                        coins = (1000..5000).random(),
                        isOnline = true,
                        avatarColorIndex = (0..4).random()
                    )
                    _friendsList.update { it + newSimulatedFriend }
                    onFinished(true, "Friend request sent and auto-accepted!")
                }
            }
            return
        }

        // Firestore Flow
        db?.collection("users")
            ?.whereEqualTo("name", trimmed)
            ?.get()
            ?.addOnSuccessListener { query ->
                val targetDoc = query.documents.firstOrNull()
                if (targetDoc == null) {
                    onFinished(false, "Could not find a player with nickname definition '$trimmed'.")
                    return@addOnSuccessListener
                }
                
                val targetId = targetDoc.id
                val targetName = targetDoc.getString("name") ?: trimmed
                
                // Construct composite ID of the request
                val reqId = "${localPlayerProfile.id}_$targetId"
                
                // Validate if friendship already exists
                val fId = getFriendshipDocId(localPlayerProfile.id, targetId)
                db?.collection("friends")?.document(fId)?.get()
                    ?.addOnSuccessListener { friendDoc ->
                        if (friendDoc.exists()) {
                            onFinished(false, "$trimmed is already your active friend.")
                            return@addOnSuccessListener
                        }
                        
                        // Set pending request on firestore
                        val req = FriendRequest(
                            id = reqId,
                            senderId = localPlayerProfile.id,
                            senderName = localPlayerProfile.name,
                            receiverId = targetId,
                            receiverName = targetName,
                            status = "PENDING",
                            timestamp = System.currentTimeMillis()
                        )
                        db?.collection("friend_requests")?.document(reqId)?.set(req.toMap())
                            ?.addOnSuccessListener {
                                logMessage("Friend request successfully delivered to $targetName.")
                                onFinished(true, "Request successfully delivered!")
                            }
                            ?.addOnFailureListener { e ->
                                onFinished(false, "Failed to deliver request packet: ${e.message}")
                            }
                    }
            }
            ?.addOnFailureListener { e ->
                onFinished(false, "Search query pipeline error: ${e.message}")
            }
    }

    fun acceptFriendRequest(request: FriendRequest, onFinished: (Boolean) -> Unit) {
        if (isSimulationMode) {
            _pendingRequests.update { list -> list.filter { it.id != request.id } }
            val newFriend = Friend(
                id = request.senderId.ifBlank { "sim_${UUID.randomUUID()}" },
                name = request.senderName,
                level = 15,
                coins = 3000,
                isOnline = true,
                avatarColorIndex = (0..4).random()
            )
            _friendsList.update { list -> list + newFriend }
            logMessage("Accepted request from ${request.senderName}.")
            onFinished(true)
            return
        }

        // Firestore Flow
        db?.runBatch { batch ->
            // 1. Update status to ACCEPTED
            val reqRef = db!!.collection("friend_requests").document(request.id)
            batch.update(reqRef, "status", "ACCEPTED")

            // 2. Create Friends pairing document
            val fDocId = getFriendshipDocId(request.senderId, request.receiverId)
            val friendsRef = db!!.collection("friends").document(fDocId)
            batch.set(friendsRef, mapOf(
                "id" to fDocId,
                "userIds" to listOf(request.senderId, request.receiverId),
                "timestamp" to System.currentTimeMillis()
            ))
        }?.addOnSuccessListener {
            logMessage("Connection established with ${request.senderName}!")
            onFinished(true)
        }?.addOnFailureListener { e ->
            Log.e(TAG, "Batch friendship accept error: ${e.message}")
            onFinished(false)
        }
    }

    fun declineFriendRequest(request: FriendRequest, onFinished: (Boolean) -> Unit) {
        if (isSimulationMode) {
            _pendingRequests.update { list -> list.filter { it.id != request.id } }
            onFinished(true)
            return
        }

        db?.collection("friend_requests")?.document(request.id)?.delete()
            ?.addOnSuccessListener {
                logMessage("Declined friend request from ${request.senderName}.")
                onFinished(true)
            }
            ?.addOnFailureListener {
                onFinished(false)
            }
    }

    fun removeFriend(friendId: String, onFinished: (Boolean) -> Unit) {
        if (isSimulationMode) {
            _friendsList.update { list -> list.filter { it.id != friendId } }
            logMessage("Friend removed.")
            onFinished(true)
            return
        }

        val myId = localPlayerProfile.id
        val fDocId = getFriendshipDocId(myId, friendId)
        
        db?.runBatch { batch ->
            batch.delete(db!!.collection("friends").document(fDocId))
            // Also scrub composite request
            batch.delete(db!!.collection("friend_requests").document("${myId}_$friendId"))
            batch.delete(db!!.collection("friend_requests").document("${friendId}_$myId"))
        }?.addOnSuccessListener {
            logMessage("Unfriended user successfully.")
            onFinished(true)
        }?.addOnFailureListener { e ->
            Log.e(TAG, "Failed removal process: ${e.message}")
            onFinished(false)
        }
    }

    // --- Invite to Lobby APIs ---

    fun inviteFriendToLobby(friend: Friend, roomId: String, onFinished: (Boolean, String) -> Unit) {
        if (isSimulationMode) {
            logMessage("Sent match invitation to ${friend.name} for Room $roomId.")
            coroutineScope.launch {
                delay(3000)
                logMessage("${friend.name} accepted invite and joined room.")
                // Inject friend into MultiplayerManager's active room list
                val newLobbyPlayer = LudoOnlinePlayer(
                    id = friend.id,
                    name = friend.name,
                    colorIndex = (1..3).random(),
                    isHost = false,
                    isReady = true,
                    isConnected = true,
                    lastSeen = System.currentTimeMillis()
                )
                // Injects player into the room state so the lobby fills up!
                MultiplayerManager.currentRoom.value?.let { room ->
                    val updatedPlayers = room.players.filter { it.id != friend.id } + newLobbyPlayer
                    MultiplayerManager.updateRoomStateForSimulation(room.copy(
                        players = updatedPlayers,
                        actionLog = "${friend.name} accepted invitation."
                    ))
                }
            }
            onFinished(true, "Invite transmitted!")
            return
        }

        // Firestore flow
        val invitationId = "${localPlayerProfile.id}_${friend.id}_$roomId"
        val invite = LudoInvitation(
            id = invitationId,
            senderId = localPlayerProfile.id,
            senderName = localPlayerProfile.name,
            receiverId = friend.id,
            roomId = roomId,
            status = "PENDING",
            timestamp = System.currentTimeMillis()
        )
        db?.collection("invitations")?.document(invitationId)?.set(invite.toMap())
            ?.addOnSuccessListener {
                logMessage("Invitation transmitted successfully to ${friend.name}!")
                onFinished(true, "Invite successfully transmitted!")
            }
            ?.addOnFailureListener { e ->
                onFinished(false, "Transmission pipeline error: ${e.message}")
            }
    }

    fun acceptInvitation(invite: LudoInvitation, onFinished: (Boolean) -> Unit) {
        if (isSimulationMode) {
            _activeInviteReceived.value = null
            _incomingInvitations.update { list -> list.filter { it.id != invite.id } }
            onFinished(true)
            return
        }

        db?.collection("invitations")?.document(invite.id)?.update("status", "ACCEPTED")
            ?.addOnSuccessListener {
                _activeInviteReceived.value = null
                _incomingInvitations.update { list -> list.filter { it.id != invite.id } }
                onFinished(true)
            }
            ?.addOnFailureListener {
                onFinished(false)
            }
    }

    fun declineInvitation(invite: LudoInvitation, onFinished: (Boolean) -> Unit) {
        if (isSimulationMode) {
            _activeInviteReceived.value = null
            _incomingInvitations.update { list -> list.filter { it.id != invite.id } }
            onFinished(true)
            return
        }

        db?.collection("invitations")?.document(invite.id)?.delete()
            ?.addOnSuccessListener {
                _activeInviteReceived.value = null
                _incomingInvitations.update { list -> list.filter { it.id != invite.id } }
                onFinished(true)
            }
            ?.addOnFailureListener {
                onFinished(false)
            }
    }

    // Helpers
    private fun getFriendshipDocId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_$uid2" else "${uid2}_$uid1"
    }

    private fun setupMockData() {
        // Populates mock list
        _friendsList.value = listOf(
            Friend("sim_priya", "Priya", 27, 2980, true, 0),
            Friend("sim_amit", "Amit", 23, 3120, true, 1),
            Friend("sim_neha", "Neha", 22, 3000, true, 2),
            Friend("sim_rajesh", "Rajesh", 19, 1450, false, 3),
            Friend("sim_vikram", "Vikram", 31, 8470, false, 4)
        )
        // Simulate a periodic mock friend request arrival for immersive sandbox testing!
        coroutineScope.launch {
            delay(12000)
            val req = FriendRequest(
                id = "req_sim_karan",
                senderId = "sim_karan",
                senderName = "Karan Sharma",
                receiverId = localPlayerProfile.id,
                receiverName = localPlayerProfile.name,
                status = "PENDING",
                timestamp = System.currentTimeMillis()
            )
            _pendingRequests.update { list -> list + req }
            logMessage("Incoming friend request from Karan Sharma.")
        }
    }
}
