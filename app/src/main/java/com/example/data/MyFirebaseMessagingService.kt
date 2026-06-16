package com.example.data

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.UUID

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PushNotificationManager.logPush("FCM network refreshed token: ...${token.takeLast(10)}")
        
        // Save to Firestore if user profile exists
        val currentUserId = LudoMasterRepository.playerState.value.id
        if (currentUserId.isNotEmpty() && !MultiplayerManager.isSimulationMode) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            db.collection("users").document(currentUserId)
                .update("fcm_token", token)
                .addOnSuccessListener {
                    PushNotificationManager.logPush("Updated token in cloud collection.")
                }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        val notification = remoteMessage.notification
        val data = remoteMessage.data

        val title = notification?.title ?: data["title"] ?: "New Battle Alert!"
        val body = notification?.body ?: data["body"] ?: "Tap to read incoming game message details."
        val type = data["type"] ?: "INFO" // MATCH_INVITE, FRIEND_REQUEST, REWARD, TOURNAMENT, INFO
        val actionData = data["actionData"] ?: ""
        val senderName = data["senderName"] ?: ""

        val pushPacket = PushNotification(
            id = remoteMessage.messageId ?: "fcm_${UUID.randomUUID().toString().take(6).uppercase()}",
            type = type,
            title = title,
            body = body,
            timestamp = remoteMessage.sentTime.takeIf { it > 0 } ?: System.currentTimeMillis(),
            actionData = actionData,
            senderName = senderName
        )

        // Delegate to push notifications manager
        PushNotificationManager.handleIncomingNotification(applicationContext, pushPacket)
    }
}
