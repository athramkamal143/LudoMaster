package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.LudoMasterRepository
import com.example.data.PushNotificationManager
import com.example.data.PushNotification
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onNavigateToScreen: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val notifications by PushNotificationManager.notifications.collectAsState()
    val fcmToken by PushNotificationManager.fcmToken.collectAsState()
    val pushLogs by PushNotificationManager.pushLogs.collectAsState()
    
    val player by LudoMasterRepository.playerState.collectAsState()
    val balance = player.coins

    // Determine notification permission status on compilation
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        PushNotificationManager.logPush("POST_NOTIFICATIONS status callback received: isGranted=$isGranted")
        Toast.makeText(
            context,
            if (isGranted) "Push Permissions Enabled Successfully!" else "Notification Permissions Denied.",
            Toast.LENGTH_SHORT
        ).show()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notifications_screen_scaffold"),
        containerColor = LudoDarkBg,
        topBar = {
            Column {
                SimpleGameHeader(title = "Push Center", onBack = onBack)
                
                // Active coins gauge to observe claims live
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LudoSurfaceNavy.copy(0.4f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FCM ENGINE LIVE DASHBOARD",
                        color = Color.White.copy(0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stars,
                            contentDescription = "Wallet Balance",
                            tint = LudoAccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🪙 %,d".format(balance),
                            color = LudoAccentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            
            // Notification Permission Banner for Android 13+
            if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("permission_alert_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = LudoRed.copy(0.12f)),
                    border = BorderStroke(1.dp, LudoRed.copy(0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsActive,
                            contentDescription = null,
                            tint = LudoRed,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "System Notifications Muted",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Grant POST_NOTIFICATIONS permission to render incoming FCM signals in the Android system shade.",
                                color = Color.White.copy(0.7f),
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            colors = ButtonDefaults.buttonColors(containerColor = LudoRed),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("enable_notifications_permission_btn")
                        ) {
                            Text("ALLOW", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Scrollable Settings & Simulations + Inbox list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // TOKEN REGISTRY BLOCK
                item {
                    LudoCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.0.dp)
                                            .background(
                                                if (fcmToken.isNotEmpty()) LudoGreen else LudoYellow,
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "FCM DEVICE TOKEN REGISTERED",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                
                                IconButton(
                                    onClick = {
                                        if (fcmToken.isNotEmpty()) {
                                            clipboardManager.setText(AnnotatedString(fcmToken))
                                            Toast.makeText(context, "FCM Token Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("copy_token_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy Token",
                                        tint = LudoAccentGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (fcmToken.isNotEmpty()) fcmToken else "Generating tokens from background play-services connection...",
                                color = Color.White.copy(0.6f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(0.4f), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                text = "Registered server-side under: users/${LudoMasterRepository.playerState.value.id.ifBlank() { "local_session" }}/fcm_token",
                                color = LudoGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // SIMULATOR STAGES PANEL
                item {
                    Text(
                        text = "FCM DISPATCH & TEST SUITE",
                        fontSize = 11.sp,
                        color = Color.White.copy(0.5f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    LudoCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Inject simulated backend push payload notifications directly into the Android system loop using our live controller:",
                                color = Color.White.copy(0.8f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Quick trigger grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { PushNotificationManager.triggerSimulatedFCMNotification(context, "MATCH_INVITE") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("trigger_match_invite_btn"),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LudoRed),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.SportsEsports, contentDescription = null, size = 12.dp, tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Match Invite", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                Button(
                                    onClick = { PushNotificationManager.triggerSimulatedFCMNotification(context, "FRIEND_REQUEST") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("trigger_friend_rq_btn"),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LudoBlue),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.GroupAdd, contentDescription = null, size = 12.dp, tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Friend req", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { PushNotificationManager.triggerSimulatedFCMNotification(context, "REWARD") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("trigger_rewards_btn"),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LudoGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CardGiftcard, contentDescription = null, size = 12.dp, tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reward Promo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                Button(
                                    onClick = { PushNotificationManager.triggerSimulatedFCMNotification(context, "TOURNAMENT") },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("trigger_tournament_btn"),
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = LudoYellow),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.EmojiEvents, contentDescription = null, size = 12.dp, tint = Color.Black)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tournament", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }

                // NOTIFICATION INBOX LEDGER LIST
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE INBOX LEDGER (${notifications.size})",
                            fontSize = 11.sp,
                            color = Color.White.copy(0.5f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (notifications.isNotEmpty()) {
                                Text(
                                    text = "Mark Read",
                                    color = LudoAccentGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { PushNotificationManager.markAllAsRead() }
                                        .testTag("mark_all_read_lbl")
                                )
                                Text(
                                    text = "Clear All",
                                    color = LudoRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable { PushNotificationManager.clearAllNotifications() }
                                        .testTag("clear_all_labels")
                                )
                            }
                        }
                    }
                }

                if (notifications.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy.copy(0.3f)),
                            border = BorderStroke(1.dp, LudoBorder.copy(0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp, horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Inbox,
                                    contentDescription = null,
                                    tint = Color.White.copy(0.2f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ledger Inbox Empty",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No real or simulated telemetry alerts recorded. Tap any event block above to fire a push!",
                                    color = Color.White.copy(0.5f),
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { item ->
                        NotificationItemCard(
                            item = item,
                            onAction = {
                                PushNotificationManager.takePushAction(context, item.id) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        if (item.type == "MATCH_INVITE") {
                                            onNavigateToScreen("PLAY_ONLINE")
                                        } else if (item.type == "TOURNAMENT") {
                                            onNavigateToScreen("TOURNAMENT")
                                        }
                                    }
                                }
                            },
                            onDismiss = {
                                PushNotificationManager.dismissNotification(item.id)
                            }
                        )
                    }
                }

                // TELEMETRY LOGS WINDOW
                item {
                    Text(
                        text = "FCM AGENT TELEMETRY LIVE LOGGER",
                        fontSize = 11.sp,
                        color = Color.White.copy(0.5f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, LudoBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        pushLogs.forEach { log ->
                            Text(
                                text = log,
                                color = if (log.contains("failed") || log.contains("denied")) LudoRed else if (log.contains("Secured") || log.contains("Arrived")) LudoGreen else Color.White.copy(0.8f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItemCard(
    item: PushNotification,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    val themeColor = when (item.type) {
        "MATCH_INVITE" -> LudoRed
        "FRIEND_REQUEST" -> LudoBlue
        "REWARD" -> LudoGreen
        "TOURNAMENT" -> LudoYellow
        else -> Color.White.copy(0.5f)
    }

    val iconVector = when (item.type) {
        "MATCH_INVITE" -> Icons.Filled.SportsEsports
        "FRIEND_REQUEST" -> Icons.Filled.GroupAdd
        "REWARD" -> Icons.Filled.CardGiftcard
        "TOURNAMENT" -> Icons.Filled.EmojiEvents
        else -> Icons.Filled.Notifications
    }

    val tagText = when (item.type) {
        "MATCH_INVITE" -> "MATCH"
        "FRIEND_REQUEST" -> "SOCIAL"
        "REWARD" -> "GIFT"
        "TOURNAMENT" -> "CHAMP"
        else -> "ALERT"
    }

    val actionButtonLabel = when (item.type) {
        "MATCH_INVITE" -> "JOIN LOBBY"
        "FRIEND_REQUEST" -> "ACCEPT CONRAD"
        "REWARD" -> "CLAIM 🪙"
        "TOURNAMENT" -> "ENTER LOBBY"
        else -> "OK"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_item_${item.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isRead) LudoSurfaceNavy.copy(0.6f) else LudoSurfaceNavy
        ),
        border = BorderStroke(
            with(1.dp) { 1.dp },
            if (item.isRead) LudoBorder.copy(0.4f) else themeColor.copy(0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(themeColor.copy(0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tagText,
                            color = themeColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    if (!item.isRead) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(LudoRed, CircleShape)
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = SimpleDateFormat("HH:mm.ss", Locale.getDefault()).format(Date(item.timestamp)),
                        color = Color.White.copy(0.4f),
                        fontSize = 9.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(0.4f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onDismiss() }
                            .testTag("dismiss_${item.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Body info
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(themeColor.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = themeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.body,
                        color = Color.White.copy(0.7f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            // Action section
            AnimatedVisibility(
                visible = !item.isActionTaken,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = LudoBorder.copy(0.3f))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onAction,
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("btn_action_${item.id}")
                        ) {
                            Text(
                                text = actionButtonLabel,
                                color = if (themeColor == LudoYellow) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            if (item.isActionTaken) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LudoGreen.copy(0.1f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Success",
                        tint = LudoGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Wager action cleared. Database updated.",
                        color = LudoGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

// Icon wrapper for backward compatibility sizing
@Composable
fun Icon(imageVector: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String?, size: androidx.compose.ui.unit.Dp, tint: Color) {
    androidx.compose.material3.Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        tint = tint
    )
}
