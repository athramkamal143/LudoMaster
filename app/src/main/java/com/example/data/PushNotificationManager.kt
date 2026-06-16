package com.example.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

// --- Push Notification Model ---
data class PushNotification(
    val id: String = "",
    val type: String = "INFO", // "MATCH_INVITE", "FRIEND_REQUEST", "REWARD", "TOURNAMENT", "INFO"
    val title: String = "",
    val body: String = "",
    val timestamp: Long = 0L,
    val actionData: String = "", // JSON or raw reference data (e.g. roomId, coins amount)
    val senderName: String = "",
    var isRead: Boolean = false,
    var isActionTaken: Boolean = false
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "type" to type,
            "title" to title,
            "body" to body,
            "timestamp" to timestamp,
            "actionData" to actionData,
            "senderName" to senderName,
            "isRead" to isRead,
            "isActionTaken" to isActionTaken
        )
    }
}

object PushNotificationManager {
    private const val TAG = "PushNotificationMgr"
    
    // Notification Channels
    const val CHANNEL_INVITES = "match_invites_channel"
    const val CHANNEL_SOCIAL = "social_notifications_channel"
    const val CHANNEL_REWARDS = "rewards_notifications_channel"
    const val CHANNEL_TOURNAMENTS = "tournaments_notifications_channel"

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var db: FirebaseFirestore? = null

    // Roster of received notifications
    private val _notifications = MutableStateFlow<List<PushNotification>>(emptyList())
    val notifications = _notifications.asStateFlow()

    // Current registration token
    private val _fcmToken = MutableStateFlow<String>("")
    val fcmToken = _fcmToken.asStateFlow()

    // Push diagnostic system logs
    private val _pushLogs = MutableStateFlow<List<String>>(listOf("Push telemetry operational."))
    val pushLogs = _pushLogs.asStateFlow()

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        logPush("Setting up Firebase Messaging Service pipelines.")
        createNotificationChannels(context)

        // Sync token real-time
        try {
            db = FirebaseFirestore.getInstance()
            
            // Check if Firebase token can be fetched
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    logPush("Token acquisition failed: ${task.exception?.message}")
                    generateSimulationToken() // Fallback to sandbox simulated token
                    return@addOnCompleteListener
                }

                val token = task.result ?: ""
                _fcmToken.value = token
                logPush("FCM Token secured: ...${token.takeLast(10)}")
                
                // Store/Upload to FireStore when logged in
                coroutineScope.launch {
                    LudoMasterRepository.playerState.collect { profile ->
                        if (profile.id.isNotEmpty() && !MultiplayerManager.isSimulationMode) {
                            registerTokenInFirestore(profile.id, token)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logPush("FCM Client offline. Enabling sandbox token module.")
            generateSimulationToken()
        }

        // Initialize mock notifications history to give the users standard historical context
        loadMockNotificationHistory()
    }

    private fun registerTokenInFirestore(userId: String, token: String) {
        if (userId.isBlank() || token.isBlank()) return
        db?.collection("users")?.document(userId)
            ?.update("fcm_token", token)
            ?.addOnSuccessListener {
                logPush("Remote Synced: Secure key verified in Firestore.")
            }
            ?.addOnFailureListener {
                // Try setting it if update fails
                val data = mapOf("fcm_token" to token)
                db?.collection("users")?.document(userId)?.set(data, com.google.firebase.firestore.SetOptions.merge())
                    ?.addOnSuccessListener {
                        logPush("Remote Synced: Merged secure key in Firestore.")
                    }
            }
    }

    private fun generateSimulationToken() {
        val simToken = "fcm_sim_token_${UUID.randomUUID().toString().take(12).lowercase()}_secured"
        _fcmToken.value = simToken
        logPush("Mock Token Assigned: ...${simToken.takeLast(10)}")
    }

    fun logPush(msg: String) {
        _pushLogs.update { (listOf("[FCM] $msg") + it).take(25) }
    }

    // --- Create notification channels ---
    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channels = listOf(
                NotificationChannel(CHANNEL_INVITES, "Match Invites", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Signals regarding incoming tournament and classic board game match invitations"
                    enableLights(true)
                    lightColor = android.graphics.Color.RED
                    enableVibration(true)
                },
                NotificationChannel(CHANNEL_SOCIAL, "Social Circle & Friend Requests", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Signals from friends list updates, acceptances and friend invites"
                    enableLights(true)
                    lightColor = android.graphics.Color.BLUE
                },
                NotificationChannel(CHANNEL_REWARDS, "Coins & Rewards Promo", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "Reminders on claimable daily coins wagers, scratch cards and reward referrals"
                    enableLights(true)
                    lightColor = android.graphics.Color.GREEN
                },
                NotificationChannel(CHANNEL_TOURNAMENTS, "Major Tournaments Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Realtime notifications for upcoming championship leagues and tournament lobbies"
                    enableLights(true)
                    lightColor = android.graphics.Color.YELLOW
                    enableVibration(true)
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
            logPush("Standard notification channels registered.")
        }
    }

    // --- Handle incoming notifications (e.g. from service or simulation) ---
    fun handleIncomingNotification(context: Context, push: PushNotification) {
        // Core update local reactive list
        _notifications.update { listOf(push) + it }
        logPush("Arrived packet: [${push.type}] ${push.title}")

        // Draw an actual Android OS pull-down layout!
        showSystemNotification(context, push)
    }

    // --- Generate Real System Pull-down Native OS Notification ---
    private fun showSystemNotification(context: Context, push: PushNotification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                logPush("Android denied notification render: Permission POST_NOTIFICATIONS missing on client.")
                return
            }
        }

        val channelId = when (push.type) {
            "MATCH_INVITE" -> CHANNEL_INVITES
            "FRIEND_REQUEST" -> CHANNEL_SOCIAL
            "REWARD" -> CHANNEL_REWARDS
            "TOURNAMENT" -> CHANNEL_TOURNAMENTS
            else -> CHANNEL_SOCIAL
        }

        // Tap navigates user into App MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("push_id", push.id)
            putExtra("push_type", push.type)
            putExtra("push_action", push.actionData)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            push.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // System dialogue default
            .setContentTitle(push.title)
            .setContentText(push.body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            
        // Style depending on category
        when (push.type) {
            "MATCH_INVITE" -> {
                builder.color = android.graphics.Color.parseColor("#E53935") // Beautiful Red
            }
            "FRIEND_REQUEST" -> {
                builder.color = android.graphics.Color.parseColor("#1E88E5") // Beautiful Blue
            }
            "REWARD" -> {
                builder.color = android.graphics.Color.parseColor("#4CAF50") // Elegant Green
            }
            "TOURNAMENT" -> {
                builder.color = android.graphics.Color.parseColor("#FFB300") // Warm Gold
            }
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(push.id.hashCode(), builder.build())
            logPush("FCM trigger dispatched native notification banner.")
        } catch (e: Exception) {
            Log.e(TAG, "Notification drawing issue: ${e.message}")
        }
    }

    // --- Action Actions Handles ---
    fun takePushAction(context: Context, pushId: String, onResult: (Boolean, String) -> Unit) {
        val list = _notifications.value
        val pushIndex = list.indexOfFirst { it.id == pushId }
        if (pushIndex == -1) {
            onResult(false, "Notification trace not found.")
            return
        }

        val push = list[pushIndex]
        if (push.isActionTaken) {
            onResult(false, "Combat action already recorded for this notification.")
            return
        }

        when (push.type) {
            "REWARD" -> {
                val coinsToGrant = push.actionData.toIntOrNull() ?: 1000
                EconomyManager.recordTransaction(
                    amount = coinsToGrant,
                    source = "PUSH_PROMO_REWARD",
                    description = "Claimed voucher code promo for: ${push.title}"
                ) { success ->
                    if (success) {
                        updateNotificationActionState(pushId)
                        onResult(true, "Congratulation! Converted ${coinsToGrant} Battle Coins to your profile.")
                    } else {
                        onResult(false, "Voucher is currently invalid.")
                    }
                }
            }
            "FRIEND_REQUEST" -> {
                val sName = push.senderName.ifBlank { "ApexGamer" }
                val exists = FriendsManager.friendsList.value.any { it.name.lowercase() == sName.lowercase() }
                if (exists) {
                    updateNotificationActionState(pushId)
                    onResult(true, "$sName is already in your companion circle!")
                    return
                }

                val req = FriendRequest(
                    id = "req_sim_${sName.lowercase()}",
                    senderId = "sim_${sName.lowercase()}",
                    senderName = sName,
                    receiverId = LudoMasterRepository.playerState.value.id,
                    receiverName = LudoMasterRepository.playerState.value.name,
                    status = "PENDING",
                    timestamp = System.currentTimeMillis()
                )
                FriendsManager.acceptFriendRequest(req) { success ->
                    if (success) {
                        updateNotificationActionState(pushId)
                        onResult(true, "Enlisted $sName as a companion comrade!")
                    } else {
                        onResult(false, "Could not accept social invite.")
                    }
                }
            }
            "MATCH_INVITE" -> {
                val rId = push.actionData
                if (rId.isBlank()) {
                    onResult(false, "Lobby index was dismantled.")
                    return
                }
                
                // Join space
                val profile = LudoMasterRepository.playerState.value
                MultiplayerManager.joinRoom(profile, rId) { success, msg ->
                    if (success) {
                        updateNotificationActionState(pushId)
                        onResult(true, "Redirecting inside Room Lobby #${rId}...")
                    } else {
                        onResult(false, "Match Invitation expired: $msg")
                    }
                }
            }
            "TOURNAMENT" -> {
                // Directly redirect to Tournament section
                updateNotificationActionState(pushId)
                onResult(true, "Cosmic Championship Arena details unlocked.")
            }
            else -> {
                updateNotificationActionState(pushId)
                onResult(true, "Alert item read successfully.")
            }
        }
    }

    private fun updateNotificationActionState(pushId: String) {
        _notifications.update { list ->
            list.map {
                if (it.id == pushId) {
                    it.copy(isActionTaken = true, isRead = true)
                } else {
                    it
                }
            }
        }
    }

    fun dismissNotification(pushId: String) {
        _notifications.update { list -> list.filter { it.id != pushId } }
        logPush("Dismissed message ID: $pushId")
    }

    fun markAllAsRead() {
        _notifications.update { list -> list.map { it.copy(isRead = true) } }
        logPush("Marked entire logs ledger index as read.")
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
        logPush("Purged notification inbox history.")
    }

    // --- Interactive simulated FCM packets trigger ---
    fun triggerSimulatedFCMNotification(context: Context, type: String) {
        val uniqueId = "push_${UUID.randomUUID().toString().take(6).uppercase()}"
        val pushPacket = when (type) {
            "MATCH_INVITE" -> {
                val testRooms = listOf("9001", "3412", "7210")
                PushNotification(
                    id = uniqueId,
                    type = "MATCH_INVITE",
                    title = "🎲 Battle Invitation received",
                    body = "Rival Master host has invited you to a heavy wager Classic 4player Battle in Room #${testRooms.random()}!",
                    timestamp = System.currentTimeMillis(),
                    actionData = testRooms.random(),
                    senderName = "Rival Master"
                )
            }
            "FRIEND_REQUEST" -> {
                val names = listOf("ProLudoStar", "ApexGamer", "DiceGeneral", "CosmicQueen")
                val sender = names.random()
                PushNotification(
                    id = uniqueId,
                    type = "FRIEND_REQUEST",
                    title = "👥 New Companion Request",
                    body = "$sender requested to link profile index paths. Accept to exchange daily free dice!",
                    timestamp = System.currentTimeMillis(),
                    senderName = sender
                )
            }
            "REWARD" -> {
                val coins = listOf(1000, 1500, 2500, 5000).random()
                PushNotification(
                    id = uniqueId,
                    type = "REWARD",
                    title = "🎁 Double-Six Bonus Claimable",
                    body = "Weekend active streak detected! Claim your reward voucher of 🪙 $coins Battle Coins immediately before expiration!",
                    timestamp = System.currentTimeMillis(),
                    actionData = coins.toString()
                )
            }
            "TOURNAMENT" -> {
                PushNotification(
                    id = uniqueId,
                    type = "TOURNAMENT",
                    title = "🏆 Cosmic Wager Championship Starting",
                    body = "The Ultimate 50,000 Wager League Grand Final begins in exactly 5 minutes. Warm up your tokens!",
                    timestamp = System.currentTimeMillis()
                )
            }
            else -> {
                PushNotification(
                    id = uniqueId,
                    type = "INFO",
                    title = "⚠️ Server Mainframe Operational",
                    body = "The cloud real-time chat nodes and multiplayer registries are undergoing routine speedup indexing.",
                    timestamp = System.currentTimeMillis()
                )
            }
        }

        handleIncomingNotification(context, pushPacket)
    }

    private fun loadMockNotificationHistory() {
        _notifications.value = listOf(
            PushNotification(
                id = "mock_push_1",
                type = "REWARD",
                title = "🎁 Congratulations: Claim 1K Welcome",
                body = "System welcome index setup reward complete. Claim 1,000 Battle Coins instantly to begin staking!",
                timestamp = System.currentTimeMillis() - 14400000,
                actionData = "1000"
            ),
            PushNotification(
                id = "mock_push_2",
                type = "TOURNAMENT",
                title = "🏆 Elite Wager Lobby Finished",
                body = "Congratulations to Team Apex for claiming first place in our 20K tournament leaderboard!",
                timestamp = System.currentTimeMillis() - 28800000,
                isRead = true,
                isActionTaken = true
            )
        )
    }
}
