package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LudoMasterRepository
import com.example.data.EconomyManager
import com.example.ui.theme.*

// --- Custom VIP Theme Definitions ---
val VipSilver = Color(0xFFE2E8F0)
val VipPlatinum = Color(0xFFA1A1AA)
val VipGoldDark = Color(0xFFCA8A04)

// --- Composable: Reusable Styled Premium Avatar Frame ---
@Composable
fun VipAvatarContainer(
    userName: String,
    avatarColor: Color = Color(0xFFEF4444), // LudoRed
    isVip: Boolean = false,
    vipFrame: String = "None",
    size: Dp = 90.dp
) {
    val initial = userName.firstOrNull()?.toString()?.uppercase() ?: "R"
    
    Box(
        modifier = Modifier
            .size(size)
            .testTag("vip_avatar_container"),
        contentAlignment = Alignment.Center
    ) {
        // Outer Frame Ring
        if (isVip && vipFrame != "None") {
            when (vipFrame) {
                "Elite Platinum Sovereign" -> {
                    // Double Ring Platinum Gradient
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.sweepGradient(
                                    colors = listOf(Color.White, VipPlatinum, Color.White, Color.Gray, Color.White)
                                ),
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LudoDarkBg, CircleShape)
                        )
                    }
                }
                "Golden Crown Gladiator" -> {
                    // Golden Sun-burst Grid Ring
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.sweepGradient(
                                    colors = listOf(LudoAccentGold, Color(0xFFFFD700), Color(0xFFB59410), LudoAccentGold)
                                ),
                                shape = CircleShape
                            )
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(LudoDarkBg, CircleShape)
                        )
                    }
                }
            }
        }

        // Inner Core Circle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isVip && vipFrame != "None") 6.dp else 0.dp)
                .background(avatarColor, CircleShape)
                .border(2.dp, Color.White.copy(0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = (size.value * 0.45f).sp
            )
        }

        // Top Crown or Verification Emblem Overlay
        if (isVip && vipFrame != "None") {
            when (vipFrame) {
                "Golden Crown Gladiator" -> {
                    Text(
                        text = "👑",
                        fontSize = (size.value * 0.35f).sp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-size.value * 0.18f).dp)
                    )
                }
                "Elite Platinum Sovereign" -> {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = "Platinum VIP",
                        tint = VipSilver,
                        modifier = Modifier
                            .size((size.value * 0.28f).dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-size.value * 0.02f).dp, y = (-size.value * 0.02f).dp)
                            .background(LudoDarkBg, CircleShape)
                            .padding(1.dp)
                    )
                }
            }
        }
    }
}

// --- Main Composable Screen: VipScreen ---
@Composable
fun VipScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val player by LudoMasterRepository.playerState.collectAsState()
    
    // Configurable VIP Options
    val frames = listOf("None", "Elite Platinum Sovereign", "Golden Crown Gladiator")
    val dices = listOf("Classic", "Emperor's Golden Dice", "Cosmic Vortex Dice")
    
    // Local Simulation state for the demo rewards
    var simInputCredits by remember { mutableStateOf("1000") }
    var simDoubleResult by remember { mutableIntStateOf(2000) }
    
    LaunchedEffect(player.isVip, simInputCredits) {
        val intVal = simInputCredits.toIntOrNull() ?: 0
        simDoubleResult = if (player.isVip) intVal * 2 else intVal
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Premium Header Block
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LudoSurfaceNavy)
                    .statusBarsPadding()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("vip_back_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "VIP ROYALE CLUB",
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 18.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Experience Native Premium gameplay privilege",
                        color = LudoAccentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.Stars,
                    contentDescription = null,
                    tint = LudoAccentGold,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Scrollable Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // VIP Status Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vip_status_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (player.isVip) Color(0xFF1E1E2E) else LudoSurfaceNavy
                    ),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (player.isVip) LudoAccentGold else LudoBorder
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (player.isVip) "MEMBERSHIP: INSTANT VIP" else "MEMBERSHIP: STANDARD GUEST",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (player.isVip) "All Premium features fully unlocked" else "Join VIP to unlock custom items and 2x gold",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(0.6f)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (player.isVip) LudoAccentGold else Color.White.copy(0.08f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (player.isVip) "ACTIVE" else "LOCKED",
                                    fontWeight = FontWeight.Black,
                                    color = if (player.isVip) Color.Black else Color.White.copy(0.5f),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Divider(color = Color.White.copy(0.1f), modifier = Modifier.padding(vertical = 12.dp))

                        // Large Preview Avatar with Custom Frame
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            VipAvatarContainer(
                                userName = player.name,
                                avatarColor = LudoRed,
                                isVip = player.isVip,
                                vipFrame = player.vipFrame,
                                size = 84.dp
                            )

                            Column {
                                Text(
                                    text = player.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "Active Frame: ${player.vipFrame}",
                                    fontSize = 12.sp,
                                    color = if (player.isVip && player.vipFrame != "None") LudoAccentGold else Color.White.copy(0.5f)
                                )
                                Text(
                                    text = "Active Dice: ${player.vipDice}",
                                    fontSize = 12.sp,
                                    color = if (player.isVip && player.vipDice != "Classic") LudoAccentGold else Color.White.copy(0.5f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Toggle or simulated upgrade call
                        Button(
                            onClick = {
                                val nextStatus = !player.isVip
                                LudoMasterRepository.setVipEnabled(nextStatus)
                                if (nextStatus) {
                                    Toast.makeText(context, "Welcome to VIP Royale Club! Frame & Dice unlocked!", Toast.LENGTH_LONG).show()
                                    // Give default selections
                                    LudoMasterRepository.setVipFrame("Golden Crown Gladiator")
                                    LudoMasterRepository.setVipDice("Emperor's Golden Dice")
                                } else {
                                    Toast.makeText(context, "VIP Membership deactivated.", Toast.LENGTH_SHORT).show()
                                    LudoMasterRepository.setVipFrame("None")
                                    LudoMasterRepository.setVipDice("Classic")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("vip_toggle_upgrade_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (player.isVip) LudoRed else LudoAccentGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(
                                    imageVector = if (player.isVip) Icons.Filled.Close else Icons.Filled.WorkspacePremium,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (player.isVip) "CANCEL MEMBERSHIP (TEST)" else "CLAIM VIP MEMBERSHIP FOR FREE",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                // benefits header
                Text(
                    text = "ROYALE CLUB CHRONICLE BENEFITS",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(0.5f),
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                // Layout List of Benefits
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BenefitRow(
                        title = "Ad-free Gameplay",
                        description = "Enjoys total skip of simulated and real AdMob Interstitials. Instant rewarded credits with no video waiting times.",
                        icon = Icons.Filled.Block,
                        iconColor = LudoRed,
                        isActive = player.isVip
                    )
                    BenefitRow(
                        title = "Exclusive Dice Styles",
                        description = "Roll custom Gold or Cosmic theme cubes inside Match Screens, expressing high nobility during passes.",
                        icon = Icons.Filled.Casino,
                        iconColor = LudoAccentGold,
                        isActive = player.isVip
                    )
                    BenefitRow(
                        title = "Premium Avatar Frames",
                        description = "Glow beautifully with custom Golden Gladiator Crowns or Platinum Sovereign circles in dashboards and profile screens.",
                        icon = Icons.Filled.SupervisedUserCircle,
                        iconColor = LudoAccentGem,
                        isActive = player.isVip
                    )
                    BenefitRow(
                        title = "Double Rewards (2X)",
                        description = "Every incoming daily check-in, wheel spin, victory payout, or ad transaction balance is strictly doubled instantly.",
                        icon = Icons.Filled.FlashOn,
                        iconColor = Color.Magenta,
                        isActive = player.isVip
                    )
                }

                if (player.isVip) {
                    // Customize Avatar Premium Frame
                    Text(
                        text = "EQUIP PREMIUM AVATAR FRAME",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(0.5f),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        frames.forEach { frame ->
                            val isSelected = player.vipFrame == frame
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSelected) Color(0xFF2E2E3E) else LudoSurfaceNavy, RoundedCornerShape(12.dp))
                                    .border(1.5.dp, if (isSelected) LudoAccentGold else LudoBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        LudoMasterRepository.setVipFrame(frame)
                                        Toast.makeText(context, "$frame Equipped!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                                    .testTag("frame_selector_$frame"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    VipAvatarContainer(
                                        userName = player.name,
                                        isVip = true,
                                        vipFrame = frame,
                                        size = 46.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (frame == "None") "Default" else frame.replace(" Gladiator", "").replace(" Sovereign", ""),
                                        color = if (isSelected) LudoAccentGold else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Customize Exclusive Dice Style
                    Text(
                        text = "EQUIP EXCLUSIVE DICE SKIN",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(0.5f),
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dices.forEach { dice ->
                            val isSelected = player.vipDice == dice
                            val diceColor = when (dice) {
                                "Emperor's Golden Dice" -> LudoAccentGold
                                "Cosmic Vortex Dice" -> Color(0xFFC084FC)
                                else -> LudoBlue
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isSelected) Color(0xFF2E2E3E) else LudoSurfaceNavy, RoundedCornerShape(12.dp))
                                    .border(1.5.dp, if (isSelected) LudoAccentGold else LudoBorder, RoundedCornerShape(12.dp))
                                    .clickable {
                                        LudoMasterRepository.setVipDice(dice)
                                        Toast.makeText(context, "$dice is now active!", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(12.dp)
                                    .testTag("dice_selector_$dice"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Custom visual representative cube
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                brush = when (dice) {
                                                    "Emperor's Golden Dice" -> Brush.verticalGradient(
                                                        colors = listOf(Color(0xFFFFF2A3), Color(0xFFFFD700), Color(0xFFCC9900))
                                                    )
                                                    "Cosmic Vortex Dice" -> Brush.radialGradient(
                                                        colors = listOf(Color(0xFFD946EF), Color(0xFF8B5CF6), Color(0xFF312E81))
                                                    )
                                                    else -> Brush.verticalGradient(
                                                        colors = listOf(LudoBlue, LudoBlue.copy(0.6f))
                                                    )
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .border(1.5.dp, Color.White.copy(0.3f), RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("⚄", color = if (dice == "Emperor's Golden Dice") Color.Black else Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column {
                                        Text(
                                            text = dice,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = when(dice) {
                                                "Emperor's Golden Dice" -> "Glowing Royal gold aesthetic with gold sparkles"
                                                "Cosmic Vortex Dice" -> "Nebula violet custom energy vortex"
                                                else -> "Standard classic blue block"
                                            },
                                            fontSize = 11.sp,
                                            color = Color.White.copy(0.5f)
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Equipped",
                                        tint = LudoAccentGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Dynamic Reward Doubler Simulator
                Text(
                    text = "LIVE 2X REWARD MULTIPLIER SIMULATOR",
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(0.5f),
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Simulate any coin reward transaction payout here:",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Base Coins:",
                                color = Color.White.copy(0.6f),
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1.5f)
                            )
                            
                            java.util.UUID.randomUUID() // arbitrary code execution
                            
                            OutlinedTextField(
                                value = simInputCredits,
                                onValueChange = { input ->
                                    if (input.all { it.isDigit() }) {
                                        simInputCredits = input
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(2f)
                                    .height(50.dp)
                                    .testTag("vip_sim_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = LudoAccentGold,
                                    unfocusedBorderColor = LudoBorder
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(0.2f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "VIP Account Multiplier:",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (player.isVip) "2.0X (Active)" else "1.0X (Upgrade Required)",
                                color = if (player.isVip) LudoAccentGold else LudoRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Final Payout Credited:",
                                color = Color.White.copy(0.6f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "🪙 $simDoubleResult Coins",
                                color = if (player.isVip) LudoAccentGold else Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                modifier = Modifier.testTag("vip_sim_result")
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun BenefitRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LudoSurfaceNavy, RoundedCornerShape(12.dp))
            .border(1.dp, LudoBorder.copy(0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(iconColor.copy(0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
                
                if (isActive) {
                    Text(
                        text = "Active",
                        color = LudoAccentGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
            Text(
                text = description,
                fontSize = 11.sp,
                color = Color.White.copy(0.5f),
                lineHeight = 15.sp
            )
        }
    }
}
