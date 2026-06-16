package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdMobScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    val isSimMode by AdMobManager.isSimulationMode.collectAsState()
    val isInterstitialReady by AdMobManager.isInterstitialLoaded.collectAsState()
    val isRewardedReady by AdMobManager.isRewardedLoaded.collectAsState()
    val adLogs by AdMobManager.adLogs.collectAsState()
    
    // User current states to show reward impact
    val playerState by LudoMasterRepository.playerState.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admob_screen_scaffold"),
        containerColor = LudoDarkBg,
        topBar = {
            Column {
                SimpleGameHeader(title = "AdMob Center", onBack = onBack)
                
                // Show current statistics
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LudoSurfaceNavy.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GOOGLE MOBILE ADS INTEGRATION",
                        color = Color.White.copy(0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Stars,
                            contentDescription = "Gems",
                            tint = LudoAccentGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🪙 %,d".format(playerState.coins),
                            color = LudoAccentGold,
                            fontSize = 11.sp,
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
            
            // Toggle controller
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("admob_toggle_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                border = BorderStroke(1.dp, LudoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Simulation Fallback Mode",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Enables visual simulation of full screen ads in sandbox or cloud devices.",
                            color = Color.White.copy(0.6f),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = isSimMode,
                        onCheckedChange = { AdMobManager.toggleSimulationMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = LudoGreen,
                            checkedTrackColor = LudoGreen.copy(0.3f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.testTag("sim_mode_switch")
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // AD TYPE 1: ANCHORED ADMOB BANNER
                item {
                    Text(
                        text = "1. HOVERING BANNER AD (320X50)",
                        fontSize = 11.sp,
                        color = Color.White.copy(0.5f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LudoSurfaceNavy.copy(0.4f), RoundedCornerShape(10.dp))
                            .border(1.dp, LudoBorder, RoundedCornerShape(10.dp))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AdMobBanner()
                    }
                }

                // AD TYPE 2 & 3: FULL SCREEN TRIGGERS
                item {
                    Text(
                        text = "2. INTERSTITIAL & REWARDED AD PLAYERS",
                        fontSize = 11.sp,
                        color = Color.White.copy(0.5f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LudoCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Interstitial Ad (Full Screen Splash)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Excellent for transition phases, e.g. after exiting a match or starting an arena league.",
                                color = Color.White.copy(0.6f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (isSimMode) LudoGreen else if (isInterstitialReady) LudoAccentGem else Color.Gray,
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSimMode) "Sim Ready" else if (isInterstitialReady) "AdMob Loaded" else "Searching...",
                                        color = if (isSimMode) LudoGreen else if (isInterstitialReady) LudoAccentGem else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        activity?.let { act ->
                                            AdMobManager.showInterstitial(act) {
                                                AdMobManager.logAd("Returned cleanly from Interstitial dispatch.", "SUCCESS")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LudoBlue),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("trigger_interstitial_btn")
                                ) {
                                    Text("SHOW INTERSTITIAL", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    LudoCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Rewarded Video Ad (Earns Coins)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Motivates wagers. Completing the video grants exactly 1,500 Coins directly to the user's pocket!",
                                color = Color.White.copy(0.6f),
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (isSimMode) LudoGreen else if (isRewardedReady) LudoAccentGold else Color.Gray,
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSimMode) "Sim Ready" else if (isRewardedReady) "AdMob Loaded" else "Searching...",
                                        color = if (isSimMode) LudoGreen else if (isRewardedReady) LudoAccentGold else Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        activity?.let { act ->
                                            AdMobManager.showRewarded(act) { coinsGranted ->
                                                EconomyManager.recordTransaction(
                                                    amount = coinsGranted,
                                                    source = "AD_REWARD",
                                                    description = "Claimed campaign watcher bonus reward"
                                                ) { success ->
                                                    if (success) {
                                                        AdMobManager.logAd("Credited $coinsGranted Coins via transaction manager.", "SUCCESS")
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("trigger_rewarded_btn")
                                ) {
                                    Text("PLAY VIDEO AD (+1.5K 🪙)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // TELEMETRY LIVE LOGGER
                item {
                    Text(
                        text = "ADMOB SDK AGENT DIAGNOSTICS LOG",
                        fontSize = 11.sp,
                        color = Color.White.copy(0.5f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, LudoBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        adLogs.forEach { log ->
                            val color = when (log.type) {
                                "SUCCESS" -> LudoGreen
                                "WARNING" -> LudoYellow
                                "ERROR" -> LudoRed
                                else -> Color.White.copy(0.8f)
                            }
                            
                            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

                            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
                                Text(
                                    text = "[$time] ",
                                    color = Color.White.copy(0.4f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = log.message,
                                    color = color,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Composable: Top-level Overlaid Full-screen Ads Player simulator ---
@Composable
fun SimulatedAdOverlay() {
    val visible = AdMobManager.isSimAdVisible.value
    val simType = AdMobManager.currentSimType.value
    val countdown = AdMobManager.simCountdown.value
    val isPlaying = AdMobManager.isSimVideoPlaying.value

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandIn(),
        exit = fadeOut() + shrinkOut()
    ) {
        val themeBgBrush = Brush.verticalGradient(
            colors = listOf(LudoDarkBg, LudoSurfaceNavy, Color.Black)
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(themeBgBrush)
                .clickable(enabled = false) {} // block background click gestures
                .testTag("simulated_ad_overlay_container"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header (Ad Type and dismiss buttons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(0.08f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SPONSOR AD CAMPAIGN",
                            color = Color.White.copy(0.6f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Countdown bubble
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color.Black.copy(0.6f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.PlayCircleFilled else Icons.Filled.Timelapse,
                            contentDescription = null,
                            tint = if (isPlaying) LudoRed else LudoAccentGem,
                            size = 12.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (countdown > 0) "${countdown}s" else "Reward loaded!",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Middle interactive content block
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                    border = BorderStroke(1.dp, LudoBorder.copy(0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isPlaying) {
                            // Video Simulator visuals
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.SportsEsports,
                                        contentDescription = null,
                                        tint = LudoAccentGold,
                                        modifier = Modifier.size(54.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "LUDO MASTER CHAMP CHAMPIONSHIP",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "The finest boards gameplay wagers await!",
                                        color = Color.White.copy(0.5f),
                                        fontSize = 10.sp
                                    )
                                }

                                // Interactive loading ticker overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(0.4f)),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    LinearProgressIndicator(
                                        progress = { (8 - countdown) / 8f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp),
                                        color = LudoAccentGold,
                                        trackColor = Color.DarkGray
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "WATCHING REWARD VIDEO",
                                color = LudoAccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Do not exit! Credits will load upon complete visual timer countdown.",
                                color = Color.White.copy(0.6f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        } else {
                            // Static Interstitial visual
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = LudoBlue,
                                modifier = Modifier.size(64.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "DIAMOND SPARK CHAMP LOBBY",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Master local wagers with online comradery! Build massive wealth by challenging world experts to board battles.",
                                color = Color.White.copy(0.7f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(
                                modifier = Modifier
                                    .background(LudoBlue.copy(0.12f), RoundedCornerShape(8.dp))
                                    .border(1.dp, LudoBlue.copy(0.4f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.TipsAndUpdates, "Tips", 16.dp, LudoAccentGold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Strategy tip: Place tokens on safe slots before engaging bold tracks!",
                                        fontSize = 10.sp,
                                        color = Color.LightGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer (Action CTA and Dismiss trigger switches)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { AdMobManager.completeSimAd() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPlaying) LudoAccentGold else LudoBlue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("sim_ad_cta_btn")
                    ) {
                        Text(
                            text = if (isPlaying) "CLAIM YOUR REWARD 🪙" else "APPLY FOR BATTLE",
                            fontWeight = FontWeight.Bold,
                            color = if (isPlaying) Color.Black else Color.White,
                            fontSize = 12.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (countdown > 0) "Dismiss button locked for ${countdown}s." else "Dismiss enabled.",
                        color = Color.White.copy(0.4f),
                        fontSize = 9.sp
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))

                    TextButton(
                        onClick = { AdMobManager.closeSimAd() },
                        enabled = (countdown <= 0 || !isPlaying),
                        modifier = Modifier.testTag("dismiss_sim_ad_btn")
                    ) {
                        Text(
                            text = "CLOSE AD CAMPAIGN",
                            color = if (countdown <= 0 || !isPlaying) LudoRed else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// --- Composable: Embeddable ADMOB BANNER COMPOSABLE (Unified Auto-Toggle) ---
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier
) {
    val isSimMode by AdMobManager.isSimulationMode.collectAsState()

    if (isSimMode) {
        // High fidelity interactive simulated banner with customizable click triggers!
        val phrases = listOf(
            "🎁 FREE CLAIM: Watch video to win 1,500 Coins securely!",
            "🎲 DICE ROYALTY: Unlock custom golden dice models in the shop!",
            "🛡️ STREAK SECURED: Claim daily free rewards now!",
            "🏆 WAGER LEAGUE: Start multiplayer board with real comrades!"
        )
        
        var ticks by remember { mutableStateOf(0) }
        
        LaunchedEffect(Unit) {
            while (true) {
                delay(6000)
                ticks = (ticks + 1) % phrases.size
            }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(LudoSurfaceNavy, Color(0xFF14243D))
                    ),
                    RoundedCornerShape(8.dp)
                )
                .border(1.dp, LudoAccentGold.copy(0.2f), RoundedCornerShape(8.dp))
                .clickable {
                    AdMobManager.logAd("Simulated Banner banner clicked! Loading target campaigns.", "SUCCESS")
                }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .background(LudoAccentGold, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "AD",
                            color = Color.Black,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = phrases[ticks],
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = LudoAccentGold,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    } else {
        // Real Google Mobile Ads Banner View integration!
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = AdMobManager.TEST_BANNER_ID
                    loadAd(AdRequest.Builder().build())
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp)
        )
    }
}
