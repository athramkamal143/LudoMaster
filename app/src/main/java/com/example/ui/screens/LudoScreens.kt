package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- REUSABLE UTILITIES & THEME WRAPPER ---

@Composable
fun LudoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = LudoSurfaceNavy,
    borderColor: Color = LudoBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var cardModifier = modifier
        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(backgroundColor)
    
    if (onClick != null) {
        cardModifier = cardModifier.clickable { onClick() }
    }
    
    Column(
        modifier = cardModifier.padding(16.dp),
        content = content
    )
}

@Composable
fun StatPill(
    icon: ImageVector,
    iconColor: Color,
    value: String,
    onPlusClick: (() -> Unit)? = null,
    testTag: String = ""
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .border(1.dp, LudoBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag(testTag)
        )
        if (onPlusClick != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(LudoAccentGold, CircleShape)
                    .clickable { onPlusClick() }
                    .testTag("${testTag}_plus"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    tint = Color.Black,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

// Custom TopBar for inner screens
@Composable
fun SimpleGameHeader(
    title: String,
    onBack: () -> Unit,
    showActionIcon: ImageVector? = null,
    onActionClick: (() -> Unit)? = null
) {
    val player by LudoMasterRepository.playerState.collectAsState()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .background(LudoSurfaceNavy, CircleShape)
                .border(1.dp, LudoBorder, CircleShape)
                .testTag("back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = title.uppercase(),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = LudoAccentGold,
            modifier = Modifier.weight(1f)
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatPill(
                icon = Icons.Filled.Stars,
                iconColor = LudoAccentGold,
                value = player.coins.toString(),
                onPlusClick = { LudoMasterRepository.addCoins(500) },
                testTag = "header_coins"
            )
            
            if (showActionIcon != null && onActionClick != null) {
                IconButton(
                    onClick = onActionClick,
                    modifier = Modifier
                        .size(32.dp)
                        .background(LudoSurfaceNavy, CircleShape)
                        .border(1.dp, LudoBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = showActionIcon,
                        contentDescription = "Action",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// --- 1. SPLASH SCREEN ---
@Composable
fun SplashScreen(onNavigateFinished: () -> Unit) {
    var loadingPercent by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        val animationSteps = 30
        for (i in 1..animationSteps) {
            delay(80)
            loadingPercent = i / animationSteps.toFloat()
        }
        onNavigateFinished()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkNavyGradientStart, LudoDarkBg)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Crown Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(LudoAccentGold.copy(alpha = 0.4f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.WorkspacePremium,
                    contentDescription = "Crown Emblem",
                    tint = LudoAccentGold,
                    modifier = Modifier.size(72.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "LUDO MASTER",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = LudoAccentGold,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )
            
            Text(
                text = "PREMIUM BOARD GAME EXPERIENCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Retro loading bar
            Column(
                modifier = Modifier.width(220.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { loadingPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = LudoAccentGold,
                    trackColor = LudoBorder
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "LOADING RESOURSES... ${(loadingPercent * 100).toInt()}%",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- 2. ONBOARDING SCREEN ---
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit, onPlayNowInstant: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    val onboardingSlides = listOf(
        Triple("PLAY ONLINE MULTIPLAYER", "Ludo Master connects you with competitive players globally in intense standard 4-player matching rooms.", Icons.Filled.Public),
        Triple("PLAY OFFLINE & PASS 'N PLAY", "Host local multiplayer passes at home or play vs a customized high-intelligence computer bot.", Icons.Filled.Computer),
        Triple("SPIN & EARN GOLD COINS", "Spin the grand lucky wheel daily to earn diamonds, customization credits, and golden crowns.", Icons.Filled.Redeem)
    )
    val currentSlide = onboardingSlides[currentPage]
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg)
            .padding(16.dp)
    ) {
        // Quick Play (Instant Guest Bypass Header)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    LudoMasterRepository.createInstantGuest()
                    onPlayNowInstant()
                },
                modifier = Modifier.testTag("skip_onboarding_play_now")
            ) {
                Text(
                    text = "SKIP & PLAY NOW ⚡",
                    color = LudoAccentGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Text(
                text = "WELCOME TO CHAMPIONSHIP",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LudoAccentGold,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .background(LudoSurfaceNavy, RoundedCornerShape(24.dp))
                    .border(2.dp, LudoBorder, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = currentSlide.third,
                    contentDescription = null,
                    tint = LudoAccentGold,
                    modifier = Modifier.size(64.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = currentSlide.first,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = currentSlide.second,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(36.dp))
            
            // Page Indicator dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onboardingSlides.forEachIndexed { idx, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (idx == currentPage) 24.dp else 8.dp, 8.dp)
                            .background(
                                color = if (idx == currentPage) LudoAccentGold else LudoBorder,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentPage > 0) {
                    TextButton(onClick = { currentPage-- }) {
                        Text("PREV", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
                
                Button(
                    onClick = {
                        if (currentPage < onboardingSlides.size - 1) {
                            currentPage++
                        } else {
                            onGetStarted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold),
                    modifier = Modifier.testTag("onboarding_next")
                ) {
                    Text(
                        text = if (currentPage < onboardingSlides.size - 1) "NEXT" else "GET STARTED",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- 3. GUEST LOGIN SCREEN ---
@Composable
fun GuestLoginScreen(
    onGuestEntered: () -> Unit,
    onNavigateToGoogle: () -> Unit,
    onNavigateToOtp: () -> Unit,
    onNavigateToEmail: () -> Unit,
    onGoBack: () -> Unit
) {
    var nickname by remember { mutableStateOf("Guest_Rahul") }
    val avatarColors = listOf(LudoRed, LudoBlue, LudoGreen, LudoYellow, LudoAccentGold)
    var selectedColorIndex by remember { mutableIntStateOf(1) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GUEST ACCOUNT SETUP",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = LudoAccentGold
            )
            Text(
                text = "Secure local gameplay profile creation",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Selected Avatar representation
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(avatarColors[selectedColorIndex], CircleShape)
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier.size(56.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Choose Color
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                avatarColors.forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(color, CircleShape)
                            .border(
                                width = if (selectedColorIndex == index) 3.dp else 1.dp,
                                color = if (selectedColorIndex == index) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedColorIndex = index }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Enter Nickname
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Display Name", color = Color.White.copy(alpha = 0.5f)) },
                textStyle = LocalTextStyle.current.copy(color = Color.White, fontWeight = FontWeight.Bold),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LudoAccentGold,
                    unfocusedBorderColor = LudoBorder,
                    cursorColor = LudoAccentGold
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guest_nickname_input")
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = {
                    LudoMasterRepository.createInstantGuest()
                    onGuestEntered()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("instant_guest_play_now_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = LudoGreen)
            ) {
                Text("PLAY NOW (QUICK START) ⚡", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    LudoMasterRepository.updateProfileName(nickname)
                    LudoMasterRepository.setSessionState("GUEST")
                    onGuestEntered()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("enter_as_guest_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold)
            ) {
                Text("CUSTOM GUEST ENTER", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Choose other authentication options list
            AuthSelectorFooter(
                currentAuth = "GUEST",
                onNavigateToGoogle = onNavigateToGoogle,
                onNavigateToOtp = onNavigateToOtp,
                onNavigateToEmail = onNavigateToEmail
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            TextButton(onClick = onGoBack) {
                Text("CANCEL", color = Color.White.copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- 4. GOOGLE LOGIN SCREEN ---
@Composable
fun GoogleLoginScreen(
    onGoogleLoggedIn: () -> Unit,
    onNavigateToGuest: () -> Unit,
    onNavigateToOtp: () -> Unit,
    onNavigateToEmail: () -> Unit,
    onGoBack: () -> Unit
) {
    var isSimulatingLogin by remember { mutableStateOf(false) }
    var selectedEmail by remember { mutableStateOf("") }
    var selectedName by remember { mutableStateOf("") }
    
    val googleAccounts = listOf(
        "rahul.play@gmail.com" to "Rahul Pro",
        "athram.kamal@gmail.com" to "Kamal Athram",
        "ludoking2026@gmail.com" to "Ludo Champion"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SIGN IN WITH GOOGLE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = LudoAccentGold
            )
            Text(
                text = "Secure cloud profiles linked with Google Play",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isSimulatingLogin) {
                CircularProgressIndicator(color = LudoAccentGold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "CONNECTING PLAY SERVICES...",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    "CHOOSE GOOGLE PLAY SERVICE USER:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(0.4f),
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Show list of quick google accounts
                googleAccounts.forEach { (email, name) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                selectedEmail = email
                                selectedName = name
                                isSimulatingLogin = true
                            },
                        colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                        border = BorderStroke(1.dp, LudoBorder.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(LudoAccentGold.copy(0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = LudoAccentGold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                                Text(email, color = Color.White.copy(0.5f), fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = LudoAccentGold, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Custom Google Email login textfield to fulfill "Any email" / "Simulation"
                var customEmail by remember { mutableStateOf("") }
                var customName by remember { mutableStateOf("") }
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy.copy(0.5f)),
                    border = BorderStroke(1.dp, LudoBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("SIGN IN WITH NEW GOOGLE ACCOUNT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = LudoAccentGold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Display Name", fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LudoAccentGold, unfocusedBorderColor = LudoBorder)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = customEmail,
                            onValueChange = { customEmail = it },
                            label = { Text("Google Email", fontSize = 11.sp) },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                            modifier = Modifier.fillMaxWidth().testTag("google_custom_email_input"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LudoAccentGold, unfocusedBorderColor = LudoBorder)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (customEmail.contains("@") && customName.isNotBlank()) {
                                    selectedEmail = customEmail
                                    selectedName = customName
                                    isSimulatingLogin = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold),
                            modifier = Modifier.fillMaxWidth().height(40.dp).testTag("google_login_submit"),
                            enabled = customEmail.isNotBlank() && customName.isNotBlank()
                        ) {
                            Text("SIGN IN ⚡", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 12.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AuthSelectorFooter(
                    currentAuth = "GOOGLE",
                    onNavigateToGuest = onNavigateToGuest,
                    onNavigateToOtp = onNavigateToOtp,
                    onNavigateToEmail = onNavigateToEmail
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(onClick = onGoBack) {
                    Text("BACK TO METHOD SELECTOR", color = Color.White.copy(alpha = 0.5f))
                }
            }
        }
        
        SimulatedMergeDialog()
    }
    
    if (isSimulatingLogin) {
        LaunchedEffect(selectedEmail, selectedName) {
            delay(1500)
            isSimulatingLogin = false
            LudoMasterRepository.authenticateCloudUser("GOOGLE", selectedEmail, selectedName) {
                onGoogleLoggedIn()
            }
        }
    }
}

// --- OTP MOBILE LOGIN SCREEN ---
@Composable
fun OtpLoginScreen(
    onOtpLoggedIn: () -> Unit,
    onNavigateToGuest: () -> Unit,
    onNavigateToGoogle: () -> Unit,
    onNavigateToEmail: () -> Unit,
    onGoBack: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    
    var isSendingOtp by remember { mutableStateOf(false) }
    var isVerifyingCode by remember { mutableStateOf(false) }
    
    var otpSentPhase by remember { mutableStateOf(false) }
    var timerCountdown by remember { mutableIntStateOf(59) }
    var mockSmsCodeDispatched by remember { mutableStateOf("") }
    var validationErrorText by remember { mutableStateOf("") }
    
    // Timer Effect
    LaunchedEffect(otpSentPhase, timerCountdown) {
        if (otpSentPhase && timerCountdown > 0) {
            delay(1000)
            timerCountdown--
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MOBILE PHONE SIGN-IN",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = LudoAccentGold
            )
            Text(
                text = "Simulated SMS OTP Authentication via Firebase Auth",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Animation/Progress Indicator
            if (isSendingOtp || isVerifyingCode) {
                CircularProgressIndicator(color = LudoAccentGold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isSendingOtp) "FB AUTH: SENDING SECURE SMS..." else "FB AUTH: CODES ALIGNING...",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            } else if (!otpSentPhase) {
                // PHASE 1: ENTER PHONE NUMBER
                Text(
                    "ENTER REGISTERED PHONE NUMBER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(0.4f),
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(LudoSurfaceNavy, RoundedCornerShape(12.dp))
                            .border(1.dp, LudoBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 15.dp)
                    ) {
                        Text("+91", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { input ->
                            if (input.all { char -> char.isDigit() }) phoneNumber = input
                            validationErrorText = ""
                        },
                        placeholder = { Text("Phone Number", color = Color.White.copy(alpha = 0.4f)) },
                        textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LudoAccentGold,
                            unfocusedBorderColor = LudoBorder
                        )
                    )
                }
                
                if (validationErrorText.isNotBlank()) {
                    Text(
                        text = validationErrorText,
                        color = LudoRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp).align(Alignment.Start)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (phoneNumber.length < 10) {
                            validationErrorText = "Please enter a valid 10-digit mobile number."
                        } else {
                            isSendingOtp = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("otp_send_request"),
                    colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Phone, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("REQUEST SMS OTP CODE", color = Color.Black, fontWeight = FontWeight.Black)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "You will receive a simulated 6-digit verification code.",
                    color = Color.White.copy(0.4f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            } else {
                // PHASE 2: VERIFICATION OTP CODE ENTRY
                Text(
                    "VERIFICATION SMS DISPATCHED!",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LudoGreen,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Check inbox. Real simulated code: $mockSmsCodeDispatched",
                    fontSize = 12.sp,
                    color = Color.White.copy(0.7f),
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = verificationCode,
                    onValueChange = { input ->
                        if (input.length <= 6 && input.all { char -> char.isDigit() }) verificationCode = input
                        validationErrorText = ""
                    },
                    label = { Text("6-Digit Code", color = Color.White.copy(alpha = 0.5f)) },
                    textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp, letterSpacing = 4.sp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("otp_code_verify_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LudoAccentGold,
                        unfocusedBorderColor = LudoBorder
                    )
                )
                
                if (validationErrorText.isNotBlank()) {
                    Text(
                        text = validationErrorText,
                        color = LudoRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 6.dp).align(Alignment.Start)
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = {
                        if (verificationCode == mockSmsCodeDispatched) {
                            isVerifyingCode = true
                        } else {
                            validationErrorText = "Invalid verification code! Try code: $mockSmsCodeDispatched"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("otp_verify_submit"),
                    colors = ButtonDefaults.buttonColors(containerColor = LudoGreen),
                    enabled = verificationCode.length == 6
                ) {
                    Text("VERIFY OTP CODE", color = Color.White, fontWeight = FontWeight.Black)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (timerCountdown > 0) "Resend in ${timerCountdown}s" else "Code sent successfully",
                        color = Color.White.copy(0.5f),
                        fontSize = 12.sp
                    )
                    
                    TextButton(
                        onClick = {
                            timerCountdown = 59
                            mockSmsCodeDispatched = (100000..999999).random().toString()
                        },
                        enabled = timerCountdown == 0
                    ) {
                        Text("RESEND CODE 🔄", color = if (timerCountdown == 0) LudoAccentGold else Color.White.copy(0.2f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AuthSelectorFooter(
                currentAuth = "OTP",
                onNavigateToGuest = onNavigateToGuest,
                onNavigateToGoogle = onNavigateToGoogle,
                onNavigateToEmail = onNavigateToEmail
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(onClick = onGoBack) {
                Text("BACK TO ONBOARDING", color = Color.White.copy(alpha = 0.5f))
            }
        }
        
        SimulatedMergeDialog()
    }
    
    // Handlers for mock async delays
    if (isSendingOtp) {
        LaunchedEffect(Unit) {
            delay(1200)
            isSendingOtp = false
            otpSentPhase = true
            mockSmsCodeDispatched = (100000..999999).random().toString()
        }
    }
    
    if (isVerifyingCode) {
        LaunchedEffect(phoneNumber) {
            delay(1200)
            isVerifyingCode = false
            LudoMasterRepository.authenticateCloudUser("OTP", phoneNumber, "Phone (+91 $phoneNumber)") {
                onOtpLoggedIn()
            }
        }
    }
}

// --- EMAIL PASSWORD LOGIN SCREEN ---
@Composable
fun EmailLoginScreen(
    onEmailLoggedIn: () -> Unit,
    onNavigateToGuest: () -> Unit,
    onNavigateToGoogle: () -> Unit,
    onNavigateToOtp: () -> Unit,
    onGoBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nameToSignUp by remember { mutableStateOf("") }
    
    var isSignUpMode by remember { mutableStateOf(false) }
    var isProcessingFlow by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isSignUpMode) "CREATE NEW ACCOUNT" else "EMAIL ACCOUNT LOGIN",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = LudoAccentGold
            )
            Text(
                text = if (isSignUpMode) "Simulate signup with Firestore Sync" else "Login via central Firebase Cloud Database",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = Color.White.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isProcessingFlow) {
                CircularProgressIndicator(color = LudoAccentGold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isSignUpMode) "FB AUTH: PROVISIONING ACCOUNT..." else "FB AUTH: SIGNING IN...",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                    border = BorderStroke(1.dp, LudoBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        
                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = nameToSignUp,
                                onValueChange = { nameToSignUp = it; validationError = "" },
                                label = { Text("Full Name", color = Color.White.copy(0.5f)) },
                                singleLine = true,
                                textStyle = TextStyle(color = Color.White),
                                modifier = Modifier.fillMaxWidth().testTag("email_sign_up_name"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LudoAccentGold, unfocusedBorderColor = LudoBorder)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it; validationError = "" },
                            label = { Text("Email Address", color = Color.White.copy(0.5f)) },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth().testTag("email_login_field"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LudoAccentGold, unfocusedBorderColor = LudoBorder)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; validationError = "" },
                            label = { Text("Password", color = Color.White.copy(0.5f)) },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White),
                            modifier = Modifier.fillMaxWidth().testTag("email_password_field"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LudoAccentGold, unfocusedBorderColor = LudoBorder)
                        )
                        
                        if (validationError.isNotBlank()) {
                            Text(
                                text = validationError,
                                color = LudoRed,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = {
                                if (!email.contains("@") || email.length < 5) {
                                    validationError = "Please enter a valid email address."
                                } else if (password.length < 6) {
                                    validationError = "Password must be at least 6 characters."
                                } else if (isSignUpMode && nameToSignUp.isBlank()) {
                                    validationError = "Please state your display name."
                                } else {
                                    isProcessingFlow = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("email_submit_btn")
                        ) {
                            Text(
                                if (isSignUpMode) "REGISTER & MERGE DATA" else "SIGN IN SECURELY 🔒",
                                color = Color.Black,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSignUpMode) "Already have an account?" else "No account registered?",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = { isSignUpMode = !isSignUpMode; validationError = "" }) {
                        Text(
                            text = if (isSignUpMode) "Login" else "Sign Up",
                            color = LudoAccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AuthSelectorFooter(
                    currentAuth = "EMAIL",
                    onNavigateToGuest = onNavigateToGuest,
                    onNavigateToGoogle = onNavigateToGoogle,
                    onNavigateToOtp = onNavigateToOtp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(onClick = onGoBack) {
                    Text("BACK TO ONBOARDING", color = Color.White.copy(alpha = 0.5f))
                }
            }
        }
        
        SimulatedMergeDialog()
    }
    
    if (isProcessingFlow) {
        LaunchedEffect(email, nameToSignUp, isSignUpMode) {
            delay(1500)
            isProcessingFlow = false
            
            // Generate display name
            val verifiedDisplayName = if (isSignUpMode) nameToSignUp else email.substringBefore("@").replaceFirstChar { it.uppercase() }
            
            LudoMasterRepository.authenticateCloudUser("EMAIL", email, verifiedDisplayName) {
                onEmailLoggedIn()
            }
        }
    }
}

@Composable
fun AuthSelectorFooter(
    currentAuth: String,
    onNavigateToGuest: (() -> Unit)? = null,
    onNavigateToGoogle: (() -> Unit)? = null,
    onNavigateToOtp: (() -> Unit)? = null,
    onNavigateToEmail: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(LudoSurfaceNavy.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .border(1.dp, LudoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "SWITCH SECURE WEB AUTH METHOD",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White.copy(0.4f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentAuth != "GUEST" && onNavigateToGuest != null) {
                IconButton(
                    onClick = onNavigateToGuest,
                    modifier = Modifier.size(44.dp).background(LudoSurfaceNavy, CircleShape).border(1.dp, LudoBorder, CircleShape)
                ) {
                    Icon(Icons.Filled.Person, contentDescription = "Guest Link", tint = LudoAccentGold)
                }
            }
            if (currentAuth != "GOOGLE" && onNavigateToGoogle != null) {
                IconButton(
                    onClick = onNavigateToGoogle,
                    modifier = Modifier.size(44.dp).background(LudoSurfaceNavy, CircleShape).border(1.dp, LudoBorder, CircleShape)
                ) {
                    Icon(Icons.Filled.AccountCircle, contentDescription = "Google App", tint = Color.White)
                }
            }
            if (currentAuth != "OTP" && onNavigateToOtp != null) {
                IconButton(
                    onClick = onNavigateToOtp,
                    modifier = Modifier.size(44.dp).background(LudoSurfaceNavy, CircleShape).border(1.dp, LudoBorder, CircleShape)
                ) {
                    Icon(Icons.Filled.Phone, contentDescription = "OTP SMS Link", tint = LudoGreen)
                }
            }
            if (currentAuth != "EMAIL" && onNavigateToEmail != null) {
                IconButton(
                    onClick = onNavigateToEmail,
                    modifier = Modifier.size(44.dp).background(LudoSurfaceNavy, CircleShape).border(1.dp, LudoBorder, CircleShape)
                ) {
                    Icon(Icons.Filled.Email, contentDescription = "Email Link", tint = LudoBlue)
                }
            }
        }
    }
}

@Composable
fun SimulatedMergeDialog() {
    val pendingMerge by LudoMasterRepository.pendingMergePayload.collectAsState()
    if (pendingMerge != null) {
        AlertDialog(
            onDismissRequest = { LudoMasterRepository.cancelMergeFlow() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = LudoAccentGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MERGE GUEST PROGRESS?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        "We noticed you have unsaved progress on this device (Guest profile). Would you like to merge these statistics into your Cloud profile, or overwrite and keep only the Cloud records?",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Guest statistics
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(LudoSurfaceNavy, RoundedCornerShape(12.dp))
                                .border(1.dp, LudoRed.copy(0.3f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("LOCAL GUEST", fontSize = 11.sp, fontWeight = FontWeight.Black, color = LudoRed)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Coins: 🪙 ${pendingMerge!!.guestProfile.coins}", color = Color.White, fontSize = 12.sp)
                            Text("Gems: 💎 ${pendingMerge!!.guestProfile.gems}", color = Color.White, fontSize = 12.sp)
                            Text("Matches: ⚔️ ${pendingMerge!!.guestProfile.totalMatches}", color = Color.White, fontSize = 12.sp)
                            Text("Wins: 🏆 ${pendingMerge!!.guestProfile.wins}", color = Color.White, fontSize = 12.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(10.dp))
                        
                        // Cloud statistics
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(LudoSurfaceNavy, RoundedCornerShape(12.dp))
                                .border(1.dp, LudoBlue.copy(0.3f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            Text("RETRIEVED CLOUD", fontSize = 11.sp, fontWeight = FontWeight.Black, color = LudoBlue)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Coins: 🪙 ${pendingMerge!!.cloudProfile.coins}", color = Color.White, fontSize = 12.sp)
                            Text("Gems: 💎 ${pendingMerge!!.cloudProfile.gems}", color = Color.White, fontSize = 12.sp)
                            Text("Matches: ⚔️ ${pendingMerge!!.cloudProfile.totalMatches}", color = Color.White, fontSize = 12.sp)
                            Text("Wins: 🏆 ${pendingMerge!!.cloudProfile.wins}", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        "⚠️ Click MERGE to join your coins and matches. Click OVERWRITE to restore only cloud profile numbers.",
                        color = Color.White.copy(0.5f),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { LudoMasterRepository.resolvePendingMerge(mergeLocalData = true) },
                    colors = ButtonDefaults.buttonColors(containerColor = LudoGreen),
                    modifier = Modifier.testTag("auth_merge_confirm")
                ) {
                    Text("MERGE DATA", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { LudoMasterRepository.resolvePendingMerge(mergeLocalData = false) },
                    modifier = Modifier.testTag("auth_merge_discard")
                ) {
                    Text("OVERWRITE", color = LudoRed, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                }
            },
            containerColor = LudoDarkBg,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// --- 5. MAIN HOME SCREEN ---
@Composable
fun HomeScreen(
    onNavigateToScreen: (String) -> Unit
) {
    val player by LudoMasterRepository.playerState.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkNavyGradientStart, LudoDarkBg)
                )
            )
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // TOP HEADER USER PROFILE BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onNavigateToScreen("PROFILE") }
                ) {
                    VipAvatarContainer(
                        userName = player.name,
                        avatarColor = LudoRed,
                        isVip = player.isVip,
                        vipFrame = player.vipFrame,
                        size = 48.dp
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = player.name,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.WorkspacePremium,
                                contentDescription = "Level",
                                tint = LudoAccentGold,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Lvl ${player.level}",
                                color = LudoAccentGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Resources indicators (Coins & Gems)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatPill(
                        icon = Icons.Filled.Stars,
                        iconColor = LudoAccentGold,
                        value = player.coins.toString(),
                        onPlusClick = { onNavigateToScreen("ECONOMY") },
                        testTag = "home_coins"
                    )
                    
                    StatPill(
                        icon = Icons.Filled.Favorite,
                        iconColor = LudoAccentGem,
                        value = player.gems.toString(),
                        onPlusClick = { onNavigateToScreen("ECONOMY") },
                        testTag = "home_gems"
                    )

                    // Live Unread Push Count Badge on the modern Bell button!
                    val pushList by com.example.data.PushNotificationManager.notifications.collectAsState()
                    val unreadCount = pushList.count { !it.isRead }
                    
                    Box(contentAlignment = Alignment.TopEnd) {
                        IconButton(
                            onClick = { onNavigateToScreen("NOTIFICATIONS") },
                            modifier = Modifier
                                .size(34.dp)
                                .background(LudoSurfaceNavy, CircleShape)
                                .border(1.dp, LudoBorder.copy(alpha = 0.5f), CircleShape)
                                .testTag("home_bell_notifications")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = "Developer Push Dashboard",
                                tint = if (unreadCount > 0) LudoAccentGold else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 1.dp, end = 1.dp)
                                    .size(14.dp)
                                    .background(LudoRed, CircleShape)
                                    .border(1.5.dp, LudoDarkBg, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            // PROMO / AD / DAILY REWARD HIGHLIGHT BANNER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(
                        Brush.horizontalGradient(listOf(LudoRed.copy(0.2f), LudoBlue.copy(0.2f))),
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, LudoBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clickable { onNavigateToScreen("REWARDS") }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Redeem,
                    contentDescription = null,
                    tint = LudoAccentGold,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DAILY SPIN IS AVAILABLE",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Claim free gold crowns and legendary futuristic dices now!",
                        fontSize = 11.sp,
                        color = Color.White.copy(0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = LudoAccentGold,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // MAIN MODES GRID
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // VIP Membership status / promo banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToScreen("VIP") }
                        .testTag("home_vip_promo_banner"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (player.isVip) Color(0xFF1E1E2E) else LudoSurfaceNavy
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (player.isVip) LudoAccentGold else LudoBorder.copy(0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        if (player.isVip) LudoAccentGold.copy(0.12f) else Color.White.copy(0.08f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Stars,
                                    contentDescription = null,
                                    tint = if (player.isVip) LudoAccentGold else Color.White.copy(0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (player.isVip) "VIP ROYALE CLUB MEMBER" else "JOIN THE VIP ROYALE CLUB",
                                    fontWeight = FontWeight.Black,
                                    color = if (player.isVip) LudoAccentGold else Color.White,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = if (player.isVip) "Active privileges: 2x gold, ad-free, frames & dice styles" else "Unlock 2x Gold, Ad-free play, frames & dices free",
                                    color = Color.White.copy(0.6f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Navigate VIP",
                            tint = Color.White.copy(0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = "SELECT CHANNELS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Play Online Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .background(
                                Brush.verticalGradient(listOf(LudoBlue, LudoBlue.copy(alpha = 0.6f))),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onNavigateToScreen("PLAY_ONLINE") }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Filled.Public, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Column {
                                Text("PLAY ONLINE", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                                Text("Real lobbies matching", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                    
                    // Offline Card (Play vs Computer)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .background(
                                Brush.verticalGradient(listOf(LudoGreen, LudoGreen.copy(alpha = 0.6f))),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onNavigateToScreen("PLAY_OFFLINE") }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Filled.Computer, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Column {
                                Text("PLAY OFFLINE", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                                Text("Practice vs Smart AI", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tournaments Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .background(
                                Brush.verticalGradient(listOf(LudoRed, LudoRed.copy(alpha = 0.6f))),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onNavigateToScreen("TOURNAMENT") }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Column {
                                Text("TOURNAMENTS", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                                Text("Win maximum coins stakes", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                            }
                        }
                    }
                    
                    // Store Card
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .background(
                                Brush.verticalGradient(listOf(GoldGradientEnd, GoldGradientStart)),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onNavigateToScreen("STORE") }
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Filled.ShoppingCart, null, tint = Color.Black, modifier = Modifier.size(24.dp))
                            Column {
                                Text("COINS & GEMS STORE", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 14.sp)
                                Text("Acquire skins & values", fontSize = 10.sp, color = Color.Black.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
                
                // PASS 'N PLAY MODE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LudoSurfaceNavy, RoundedCornerShape(16.dp))
                        .border(1.dp, LudoBorder, RoundedCornerShape(16.dp))
                        .clickable { onNavigateToScreen("PLAY_OFFLINE") // Routes to same offline board
                        }
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(LudoYellow, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Filled.People, contentDescription = null, tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("PASS 'N PLAY (LOCAL GAME)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.dp.value.sp)
                            Text("Share same device screen with family, up to 4 players local", fontSize = 11.sp, color = Color.White.copy(0.6f))
                        }
                        Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null, tint = LudoAccentGold)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // SYSTEM BOT NAVIGATION BAR (Footer menu)
            NavigationBar(
                containerColor = LudoSurfaceNavy,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateToScreen("STORE") },
                    icon = { Icon(Icons.Filled.Store, contentDescription = "Inventory", tint = Color.White) },
                    label = { Text("Inventory", color = Color.White, fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateToScreen("FRIENDS") },
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Friends", tint = Color.White) },
                    label = { Text("Friends", color = Color.White, fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home", tint = LudoAccentGold) },
                    label = { Text("Home", color = LudoAccentGold, fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateToScreen("REWARDS") },
                    icon = { Icon(Icons.Filled.Redeem, contentDescription = "Missions", tint = Color.White) },
                    label = { Text("Rewards", color = Color.White, fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { onNavigateToScreen("SETTINGS") },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White) },
                    label = { Text("Settings", color = Color.White, fontSize = 10.sp) }
                )
            }
        }
    }
}

// --- 6. USER PROFILE SCREEN ---
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val player by LudoMasterRepository.playerState.collectAsState()
    var isEditingName by remember { mutableStateOf(false) }
    var currentInputName by remember { mutableStateOf(player.name) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleGameHeader(title = "Player Profile", onBack = onBack)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Big Profile Icon
                VipAvatarContainer(
                    userName = player.name,
                    avatarColor = LudoRed,
                    isVip = player.isVip,
                    vipFrame = player.vipFrame,
                    size = 110.dp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name section with editing support
                if (isEditingName) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedTextField(
                            value = currentInputName,
                            onValueChange = { currentInputName = it },
                            textStyle = LocalTextStyle.current.copy(color = Color.White, fontWeight = FontWeight.Bold),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LudoAccentGold,
                                unfocusedBorderColor = LudoBorder
                            ),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_name_textfield")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (currentInputName.isNotBlank()) {
                                    LudoMasterRepository.updateProfileName(currentInputName)
                                }
                                isEditingName = false
                            },
                            modifier = Modifier
                                .background(LudoGreen, RoundedCornerShape(8.dp))
                                .testTag("profile_name_save_btn")
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = "Save", tint = Color.Black)
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = player.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { isEditingName = true },
                            modifier = Modifier.size(24.dp).testTag("profile_name_edit_btn")
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Name", tint = LudoAccentGold, modifier = Modifier.size(18.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "MEMBER ID: #824792",
                    fontSize = 11.sp,
                    color = Color.White.copy(0.5f),
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // LEVEL & XP PROGRESS CARD
                LudoCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("LEVEL ${player.level}", fontWeight = FontWeight.Black, color = LudoAccentGold, fontSize = 16.sp)
                        Text("${player.xp} / 3000 XP", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { player.xp / 3000f },
                        color = LudoAccentGold,
                        trackColor = LudoBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // STAT VALUES ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(LudoSurfaceNavy, RoundedCornerShape(16.dp))
                            .border(1.dp, LudoBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL MATCHES", color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(player.totalMatches.toString(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(LudoSurfaceNavy, RoundedCornerShape(16.dp))
                            .border(1.dp, LudoBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("WIN RATIO", color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(player.winRate, color = LudoAccentGold, fontWeight = FontWeight.Black, fontSize = 20.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // HISTORIC TROPHIES LIST
                LudoCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ACHIEVED DECORATIONS",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(44.dp).background(LudoBlue.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Verified, "Verified", tint = LudoBlue, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Veteran", color = Color.White, fontSize = 11.sp)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(44.dp).background(LudoAccentGold.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.WorkspacePremium, "Premium", tint = LudoAccentGold, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Millionaire", color = Color.White, fontSize = 11.sp)
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(44.dp).background(LudoRed.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.LocalFireDepartment, "Fire", tint = LudoRed, modifier = Modifier.size(24.dp))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Unstoppable", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- 7. PLAY OFFLINE SCREEN (WITH COMPLETE LUDO ENGINE & AI DIFFICULTY SYSTEM) ---

enum class LudoPlayerType { HUMAN, AI, DISABLED }
enum class LudoDifficulty { EASY, MEDIUM, HARD, EXPERT }

data class LudoPlayerConfig(
    val index: Int,
    val name: String,
    val color: Color,
    val type: LudoPlayerType,
    val difficulty: LudoDifficulty,
    val startCell: Int, // Enters at indices 1, 14, 27, 40 etc.
    val homeTrackStart: Int
)

data class LudoToken(
    val playerId: Int,
    val tokenId: Int, // 0..3
    val position: Int, // -1 is yard, 0..50 on common outer track, 51..55 in home column, 56 is finished
    val color: Color
)

// Master 52-cell track coordinates clockwise on Ludo board:
val ludoTrackCells = listOf(
    Pair(6, 0), Pair(6, 1), Pair(6, 2), Pair(6, 3), Pair(6, 4), Pair(6, 5), // 0..5
    Pair(5, 6), Pair(4, 6), Pair(3, 6), Pair(2, 6), Pair(1, 6), Pair(0, 6), // 6..11
    Pair(0, 7), // 12 (Top center)
    Pair(0, 8), Pair(1, 8), Pair(2, 8), Pair(3, 8), Pair(4, 8), Pair(5, 8), // 13..18
    Pair(6, 9), Pair(6, 10), Pair(6, 11), Pair(6, 12), Pair(6, 13), Pair(6, 14), // 19..24
    Pair(7, 14), // 25 (Right center)
    Pair(8, 14), Pair(8, 13), Pair(8, 12), Pair(8, 11), Pair(8, 10), Pair(8, 9), // 26..31
    Pair(9, 8), Pair(10, 8), Pair(11, 8), Pair(12, 8), Pair(13, 8), Pair(14, 8), // 32..37
    Pair(14, 7), // 38 (Bottom center)
    Pair(14, 6), Pair(13, 6), Pair(12, 6), Pair(11, 6), Pair(10, 6), Pair(9, 6), // 39..44
    Pair(8, 5), Pair(8, 4), Pair(8, 3), Pair(8, 2), Pair(8, 1), Pair(8, 0), // 45..50
    Pair(7, 0) // 51 (Left center)
)

// List of home column coordinates for each player index
fun getHomeColumnCoords(playerIndex: Int): List<Pair<Int, Int>> {
    return when (playerIndex) {
        0 -> listOf(Pair(7, 1), Pair(7, 2), Pair(7, 3), Pair(7, 4), Pair(7, 5)) // BLUE
        1 -> listOf(Pair(1, 7), Pair(2, 7), Pair(3, 7), Pair(4, 7), Pair(5, 7)) // RED
        2 -> listOf(Pair(7, 13), Pair(7, 12), Pair(7, 11), Pair(7, 10), Pair(7, 9)) // GREEN
        3 -> listOf(Pair(13, 7), Pair(12, 7), Pair(11, 7), Pair(10, 7), Pair(9, 7)) // YELLOW
        4 -> listOf(Pair(1, 7), Pair(2, 7), Pair(3, 7), Pair(4, 7), Pair(5, 7)) // PURPLE (Shares RED track corridor)
        5 -> listOf(Pair(13, 7), Pair(12, 7), Pair(11, 7), Pair(10, 7), Pair(9, 7)) // ORANGE (Shares YELLOW track corridor)
        else -> emptyList()
    }
}

// Check safe zones (Stars + Start Points)
fun isCellSafe(coord: Pair<Int, Int>): Boolean {
    val safeCells = listOf(
        Pair(6, 2), Pair(2, 8), Pair(8, 12), Pair(12, 6), // Star indicators
        Pair(6, 1), Pair(1, 8), Pair(8, 13), Pair(13, 6) // Quad entrance blocks
    )
    return safeCells.contains(coord)
}

// Fetch grid cell position of any token
fun getTokenCoordsOnly(token: LudoToken, configs: List<LudoPlayerConfig>): Pair<Int, Int>? {
    if (token.position == -1) return null
    if (token.position in 0..50) {
        val config = configs.find { it.index == token.playerId } ?: return null
        val commonIdx = (config.startCell + token.position) % 52
        return ludoTrackCells[commonIdx]
    }
    if (token.position in 51..55) {
        val homeCol = getHomeColumnCoords(token.playerId)
        return homeCol.getOrNull(token.position - 51)
    }
    return Pair(7, 7) // Central victory
}

// Checker to verify if opponent token is behind our token within rolls 1..6
fun isFirstBehindSecond(opt: LudoToken, token: LudoToken, configs: List<LudoPlayerConfig>): Boolean {
    if (opt.position == -1 || token.position == -1) return false
    val optConfig = configs.find { it.index == opt.playerId } ?: return false
    val ourConfig = configs.find { it.index == token.playerId } ?: return false
    
    val optGlobalCell = (optConfig.startCell + opt.position) % 52
    val ourGlobalCell = (ourConfig.startCell + token.position) % 52
    
    val dist = (ourGlobalCell - optGlobalCell + 52) % 52
    return dist in 1..6
}

// Checker to see if an active enemy threatens a specific coordinate on the track
fun isOpponentThreateningCell(opt: LudoToken, targetCoord: Pair<Int, Int>, configs: List<LudoPlayerConfig>): Boolean {
    if (opt.position == -1) return false
    val optConfig = configs.find { it.index == opt.playerId } ?: return false
    
    for (roll in 1..6) {
        val optTargetPos = opt.position + roll
        if (optTargetPos <= 50) {
            val checkGlobalCell = (optConfig.startCell + optTargetPos) % 52
            val checkCoord = ludoTrackCells[checkGlobalCell]
            if (checkCoord == targetCoord) return true
        }
    }
    return false
}

// Verify if a token is allowed to move with a given dice roll
fun canMoveToken(token: LudoToken, rollValue: Int): Boolean {
    if (token.position == -1) {
        return rollValue == 6 // Requires 6 to escape base
    }
    return (token.position + rollValue) <= 56 // Cannot overshoot home (56)
}

// Evaluate scoring for moves to determine AI choice
fun evaluateMoveScore(
    token: LudoToken,
    diceValue: Int,
    tokens: List<LudoToken>,
    configs: List<LudoPlayerConfig>,
    isExpert: Boolean
): Int {
    val config = configs.find { it.index == token.playerId } ?: return 0
    val startPos = token.position
    val targetPos = if (startPos == -1) 0 else startPos + diceValue
    
    if (startPos == -1 && diceValue == 6) {
        return 450 // High incentive to release pieces
    }
    
    if (targetPos == 56) {
        return 800 // Extreme incentive to complete a token home
    }
    
    val targetCoord = if (targetPos in 0..50) {
        val commonIdx = (config.startCell + targetPos) % 52
        ludoTrackCells[commonIdx]
    } else if (targetPos in 51..55) {
        getHomeColumnCoords(token.playerId).getOrNull(targetPos - 51)
    } else {
        null
    }
    
    if (targetCoord == null) return -9999 // Overrun is invalid
    
    var score = 0
    score += targetPos * 4 // Prefer advancing further along pieces
    
    val isSafeTarget = isCellSafe(targetCoord)
    
    // Check for kills / captures (Top Priority)
    val hitsEnemy = tokens.find { opt ->
        opt.playerId != token.playerId &&
        opt.position != -1 &&
        getTokenCoordsOnly(opt, configs) == targetCoord
    }
    
    if (hitsEnemy != null && !isSafeTarget) {
        score += 1500 // Prioritize capture!
    }
    
    // Safety bonus
    if (isSafeTarget) {
        score += 200
    }
    
    // Intelligent evasion and positioning (Expert / Hard heuristics)
    if (isExpert) {
        val currentCoord = getTokenCoordsOnly(token, configs)
        if (currentCoord != null && !isCellSafe(currentCoord)) {
            val isThreatenedNow = tokens.any { opt ->
                opt.playerId != token.playerId &&
                opt.position != -1 &&
                isFirstBehindSecond(opt, token, configs)
            }
            if (isThreatenedNow) {
                val isThreatenedAtTarget = tokens.any { opt ->
                    opt.playerId != token.playerId &&
                    opt.position != -1 &&
                    isOpponentThreateningCell(opt, targetCoord, configs)
                }
                if (!isThreatenedAtTarget) {
                    score += 450 // Great incentive to escape threat
                }
            }
        }
        
        // Self endangerment lookahead
        val isThreatenedAtTarget = tokens.any { opt ->
            opt.playerId != token.playerId &&
            opt.position != -1 &&
            isOpponentThreateningCell(opt, targetCoord, configs)
        }
        if (isThreatenedAtTarget && !isSafeTarget) {
            score -= 300 // Avoid stepping in front of an active threat
        }
        
        // Grouping blocks
        val hasAllyAtTarget = tokens.any { opt ->
            opt.playerId == token.playerId &&
            opt.tokenId != token.tokenId &&
            opt.position != -1 &&
            getTokenCoordsOnly(opt, configs) == targetCoord
        }
        if (hasAllyAtTarget) {
            score += 100
        }
    } else {
        // Standard Hard Bot checks
        val isThreatenedAtTarget = tokens.any { opt ->
            opt.playerId != token.playerId &&
            opt.position != -1 &&
            isOpponentThreateningCell(opt, targetCoord, configs)
        }
        if (isThreatenedAtTarget && !isSafeTarget) {
            score -= 150
        }
    }
    
    return score
}

/**
 * Calculates a random outcome for a 6-sided dice (1 to 6).
 */
fun calculateRandomDiceOutcome(): Int {
    return (1..6).random()
}

/**
 * Maps a determined dice outcome/result to a solid dynamic background Color.
 * Outlines 6 vibrant, custom-themed shades of colors as requested by the user.
 */
fun getDiceShadeColor(value: Int): Color {
    return when (value) {
        1 -> Color(0xFFEF4444) // Shade 1: Beautiful Crimson Red
        2 -> Color(0xFFF97316) // Shade 2: Vibrant Tiger Orange
        3 -> Color(0xFFEAB308) // Shade 3: Sunny Amber Yellow
        4 -> Color(0xFF10B981) // Shade 4: Fresh Emerald Green
        5 -> Color(0xFF3B82F6) // Shade 5: Royal Cobalt Blue
        6 -> Color(0xFF8B5CF6) // Shade 6: Amethyst Purple/Indigo
        else -> Color(0xFF6B7280) // Gray fallback
    }
}

/**
 * Determines the optimal contrast color for pips/dots matching the shade color.
 */
fun getDiceShadeContentColor(value: Int): Color {
    return when (value) {
        3 -> Color(0xFF1E293B) // Slate dark for excellent contrast on yellow background
        else -> Color.White // Crisp white for matching other colorful shades
    }
}

/**
 * Helper extension to safely darken Compose Color for beautiful gradient styling.
 */
fun Color.darken(factor: Float = 0.15f): Color {
    return Color(
        red = (red * (1f - factor)).coerceIn(0f, 1f),
        green = (green * (1f - factor)).coerceIn(0f, 1f),
        blue = (blue * (1f - factor)).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/**
 * Localized animation sequence for rolling a virtual six-sided dice.
 * Operates on the coroutine scope, shuffling shown dice values rapidly,
 * and executes [onRollComplete] once the dice settles on the final outcome.
 */
fun triggerAnimatedDiceRoll(
    scope: kotlinx.coroutines.CoroutineScope,
    onValueUpdate: (Int) -> Unit,
    onRollingStateChange: (Boolean) -> Unit,
    onRollComplete: (Int) -> Unit
) {
    scope.launch {
        onRollingStateChange(true)
        var lastVal = calculateRandomDiceOutcome()
        onValueUpdate(lastVal)
        
        // Localized micro-shuffles to simulate the physical container shaking/tumbling
        val rollSteps = (6..8).random() // realistic tumbling duration
        for (i in 0 until rollSteps) {
            var nextVal = calculateRandomDiceOutcome()
            while (nextVal == lastVal) {
                nextVal = calculateRandomDiceOutcome()
            }
            lastVal = nextVal
            onValueUpdate(lastVal)
            delay(110) // speed of pip transition
        }
        
        onRollingStateChange(false)
        // Wait a small moment for placement animation to complete settling
        delay(350)
        onRollComplete(lastVal)
    }
}

@Composable
fun AnimatedDiceRollView(
    diceValue: Int,
    isRolling: Boolean,
    backgroundColorModifier: Modifier,
    activeConfigColor: Color,
    pipColor: Color,
    showClickableActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Elegant 2D physics & shake animation variables
    var targetRotation2D by remember { mutableStateOf(0f) }
    var targetOffsetX by remember { mutableStateOf(0f) }
    var targetOffsetY by remember { mutableStateOf(0f) }
    var targetScale by remember { mutableStateOf(1f) }

    LaunchedEffect(isRolling, diceValue) {
        if (isRolling) {
            targetScale = 1.15f
            while (isRolling) {
                // High-fidelity flat 2D tumbling, shaking and micro-rotations (under 25 deg)
                targetRotation2D = (-25..25).random().toFloat()
                targetOffsetX = (-12..12).random().toFloat()
                targetOffsetY = (-12..12).random().toFloat()
                delay(60)
            }
        } else {
            // Settle smoothly back to flat vertical posture
            targetScale = 1.0f
            targetRotation2D = 0f
            targetOffsetX = 0f
            targetOffsetY = 0f
        }
    }

    val rotZ by animateFloatAsState(
        targetValue = targetRotation2D,
        animationSpec = if (isRolling) {
            spring(dampingRatio = 0.5f, stiffness = 900f)
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        },
        label = "rotZ"
    )
    val offX by animateFloatAsState(
        targetValue = targetOffsetX,
        animationSpec = if (isRolling) {
            spring(dampingRatio = 0.4f, stiffness = 850f)
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        },
        label = "offX"
    )
    val offY by animateFloatAsState(
        targetValue = targetOffsetY,
        animationSpec = if (isRolling) {
            spring(dampingRatio = 0.4f, stiffness = 850f)
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        },
        label = "offY"
    )
    val scaleFactor by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    val shadeColor = getDiceShadeColor(diceValue)
    val contentColor = getDiceShadeContentColor(diceValue)

    Box(
        modifier = modifier
            .graphicsLayer {
                rotationZ = rotZ
                translationX = offX
                translationY = offY
                scaleX = scaleFactor
                scaleY = scaleFactor
            }
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        shadeColor,
                        shadeColor.darken(0.15f)
                    )
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 3.dp,
                color = if (showClickableActive) LudoAccentGold else Color.White.copy(0.35f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = showClickableActive) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        DicePipsView(
            value = diceValue,
            dotColor = contentColor,
            modifier = Modifier.fillMaxSize()
        )

        // Soft micro-glowing overlay during roll animation state
        if (isRolling) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(0.12f), RoundedCornerShape(14.dp))
            )
        }
    }
}

@Composable
fun DicePipsView(value: Int, dotColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.aspectRatio(1f).padding(9.dp),
        contentAlignment = Alignment.Center
    ) {
        val pips = when (value) {
            1 -> listOf(Pair(1, 1))
            2 -> listOf(Pair(0, 0), Pair(2, 2))
            3 -> listOf(Pair(0, 0), Pair(1, 1), Pair(2, 2))
            4 -> listOf(Pair(0, 0), Pair(0, 2), Pair(2, 0), Pair(2, 2))
            5 -> listOf(Pair(0, 0), Pair(0, 2), Pair(1, 1), Pair(2, 0), Pair(2, 2))
            6 -> listOf(Pair(0, 0), Pair(0, 2), Pair(1, 0), Pair(1, 2), Pair(2, 0), Pair(2, 2))
            else -> listOf(Pair(1, 1))
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (col in 0..2) {
                        val hasPip = pips.contains(Pair(row, col))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasPip) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize(0.6f)
                                        .background(dotColor, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LudoVisualTurnIndicator(
    activeConfig: LudoPlayerConfig,
    isDiceRolled: Boolean,
    isRolling: Boolean,
    matchBetAmount: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_turn")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.99f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val colorHighlight by animateColorAsState(
        targetValue = activeConfig.color,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "playerColor"
    )

    val actionText = when {
        isRolling -> "Shaking dice... 🗳️"
        isDiceRolled -> "Move your bud! 🚶"
        activeConfig.type == LudoPlayerType.AI -> "Bot calculating moves..."
        else -> "Awaiting your roll!"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .scale(pulseScale)
            .border(
                width = 2.dp,
                color = colorHighlight.copy(alpha = pulseAlpha),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("visual_turn_indicator"),
        colors = CardDefaults.cardColors(
            containerColor = LudoSurfaceNavy.copy(0.95f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(colorHighlight.copy(alpha = 0.15f), CircleShape)
                        .border(
                            width = 2.5.dp,
                            color = colorHighlight,
                            shape = CircleShape
                        )
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorHighlight, CircleShape)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = activeConfig.name.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (activeConfig.type == LudoPlayerType.AI) {
                            Icon(
                                imageVector = Icons.Filled.Computer,
                                contentDescription = "AI BOT",
                                tint = LudoAccentGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${activeConfig.difficulty.name} BOT",
                                color = LudoAccentGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Player",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "HUMAN",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                if (activeConfig.type == LudoPlayerType.HUMAN) {
                    Surface(
                        color = LudoAccentGold,
                        shape = RoundedCornerShape(30.dp),
                        modifier = Modifier
                            .border(1.5.dp, Color.White.copy(0.6f), RoundedCornerShape(30.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "YOUR TURN",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                } else {
                    Surface(
                        color = Color.White.copy(0.08f),
                        shape = RoundedCornerShape(30.dp),
                        modifier = Modifier.border(1.dp, Color.White.copy(0.15f), RoundedCornerShape(30.dp))
                    ) {
                        Text(
                            text = "BOT TURN",
                            color = Color.White.copy(0.8f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = actionText,
                    color = Color.White.copy(0.6f),
                    fontSize = 11.sp,
                    fontWeight = Modifier.testTag("turn_status_subtext").let { FontWeight.Medium }
                )
            }
        }
    }
}

@Composable
fun LudoGameplayProfileCard(
    config: LudoPlayerConfig,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_border")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val borderThickness = if (isActive) 2.5.dp else 1.dp
    val borderColor = if (isActive) config.color.copy(pulseAlpha) else config.color.copy(0.35f)

    Card(
        modifier = modifier
            .width(110.dp)
            .height(58.dp)
            .border(borderThickness, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0D1527)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(config.color.copy(0.15f), CircleShape)
                    .border(1.dp, config.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = config.name.substringBefore(" ").take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = config.name.substringBefore(" (").substringBefore(" "),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = "Marker",
                        tint = config.color,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "3,250",
                        color = Color(0xFFFFD700),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun LudoDiceBox(
    value: Int,
    isRolling: Boolean,
    isActive: Boolean,
    playerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_dice")
    val diceScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "diceScale"
    )

    val activeScale = if (isActive && !isRolling) diceScale else 1f

    Box(
        modifier = modifier
            .scale(activeScale)
            .size(54.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isActive) {
                        listOf(Color.White, Color(0xFFF3F4F6))
                    } else {
                        listOf(Color(0xFFE5E7EB), Color(0xFFD1D5DB))
                    }
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isActive) 2.5.dp else 1.dp,
                color = if (isActive) playerColor else Color.White.copy(0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = isActive && !isRolling) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        var targetRotation by remember { mutableStateOf(0f) }
        LaunchedEffect(isRolling) {
            if (isRolling && isActive) {
                while (isRolling) {
                    targetRotation = (-180..180).random().toFloat()
                    delay(80)
                }
            } else {
                targetRotation = 0f
            }
        }
        val rotationVal by animateFloatAsState(targetValue = targetRotation, label = "dice_rot")

        Box(
            modifier = Modifier
                .graphicsLayer { rotationZ = rotationVal }
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DicePipsView(
                value = value,
                dotColor = if (isActive) playerColor else Color.Gray,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isActive && !isRolling) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = playerColor,
                    modifier = Modifier
                        .size(11.dp)
                        .background(Color.White, CircleShape)
                )
            }
        }
    }
}

// Core decision-maker for AI players based on difficulty levels
fun calculateBestMove(
    playerIndex: Int,
    diceValue: Int,
    tokens: List<LudoToken>,
    difficulty: LudoDifficulty,
    playerConfigs: List<LudoPlayerConfig>
): Int? {
    val validTokenIds = mutableListOf<Int>()
    for (i in 0..3) {
        val t = tokens.find { it.playerId == playerIndex && it.tokenId == i } ?: continue
        if (canMoveToken(t, diceValue)) {
            validTokenIds.add(i)
        }
    }
    
    if (validTokenIds.isEmpty()) return null
    
    return when (difficulty) {
        LudoDifficulty.EASY -> {
            // Easy bot moves complete randomly
            validTokenIds.random()
        }
        LudoDifficulty.MEDIUM -> {
            // Medium bot releases immediately on 6, kills when obvious, otherwise random
            val releaseTokenId = validTokenIds.find { id ->
                val t = tokens.find { it.playerId == playerIndex && it.tokenId == id }!!
                t.position == -1 && diceValue == 6
            }
            if (releaseTokenId != null) return releaseTokenId
            
            val killTokenId = validTokenIds.find { id ->
                val t = tokens.find { it.playerId == playerIndex && it.tokenId == id }!!
                val targetPos = t.position + diceValue
                if (targetPos <= 50) {
                    val config = playerConfigs.find { it.index == playerIndex }!!
                    val targetCoord = ludoTrackCells[(config.startCell + targetPos) % 52]
                    tokens.any { opt ->
                        opt.playerId != playerIndex && opt.position != -1 &&
                        getTokenCoordsOnly(opt, playerConfigs) == targetCoord && !isCellSafe(targetCoord)
                    }
                } else false
            }
            if (killTokenId != null) return killTokenId
            
            validTokenIds.random()
        }
        LudoDifficulty.HARD -> {
            // Hard Bot scores all moves using normal heuristics
            validTokenIds.maxByOrNull { id ->
                val t = tokens.find { it.playerId == playerIndex && it.tokenId == id }!!
                evaluateMoveScore(t, diceValue, tokens, playerConfigs, isExpert = false)
            }
        }
        LudoDifficulty.EXPERT -> {
            // Expert Bot uses deep lookahead threat scoring to pick optimal moves
            validTokenIds.maxByOrNull { id ->
                val t = tokens.find { it.playerId == playerIndex && it.tokenId == id }!!
                evaluateMoveScore(t, diceValue, tokens, playerConfigs, isExpert = true)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PlayOfflineScreen(onBack: () -> Unit) {
    val LudoPurple = Color(0xFF9C27B0)
    val LudoOrange = Color(0xFFFF9800)
    
    val scope = rememberCoroutineScope()
    val player by LudoMasterRepository.playerState.collectAsState()
    
    // Setup match states
    var isMatchActive by remember { mutableStateOf(false) }
    var playerCount by remember { mutableIntStateOf(4) }
    var matchBetAmount by remember { mutableIntStateOf(500) }
    
    // Player Configuration models 0..5
    val defaultConfigs = remember {
        mutableStateListOf(
            LudoPlayerConfig(0, "Rahul (Human)", LudoBlue, LudoPlayerType.HUMAN, LudoDifficulty.EASY, 1, 50),
            LudoPlayerConfig(1, "Priya", LudoRed, LudoPlayerType.AI, LudoDifficulty.MEDIUM, 14, 50),
            LudoPlayerConfig(2, "Neha", LudoGreen, LudoPlayerType.AI, LudoDifficulty.HARD, 27, 50),
            LudoPlayerConfig(3, "Amit", LudoYellow, LudoPlayerType.AI, LudoDifficulty.EXPERT, 40, 50),
            LudoPlayerConfig(4, "Zeta Bot", LudoPurple, LudoPlayerType.DISABLED, LudoDifficulty.MEDIUM, 14, 50),
            LudoPlayerConfig(5, "Sigma Bot", LudoOrange, LudoPlayerType.DISABLED, LudoDifficulty.EXPERT, 40, 50)
        )
    }
    
    // Live gameplay engine states
    var currentPlayerIndex by remember { mutableIntStateOf(0) }
    var diceValue by remember { mutableIntStateOf(1) }
    var isRolling by remember { mutableStateOf(false) }
    var isDiceRolled by remember { mutableStateOf(false) }
    var consecutiveSixes by remember { mutableIntStateOf(0) }
    var tokens by remember { mutableStateOf(emptyList<LudoToken>()) }
    var gameLogs by remember { mutableStateOf(listOf("Select settings and click START MATCH!")) }
    var matchRankings by remember { mutableStateOf(emptyList<Int>()) } // finished players list
    var isManualMoveModeEnabled by remember { mutableStateOf(false) }
    var showLogsPanel by remember { mutableStateOf(false) }
    var showWinnerOverlay by remember { mutableStateOf(false) }
    
    // Automatically open or close the winner overlay when match results alter
    LaunchedEffect(matchRankings) {
        if (matchRankings.isNotEmpty()) {
            showWinnerOverlay = true
        } else {
            showWinnerOverlay = false
        }
    }
    
    // Reset player configurations based on chosen count
    LaunchedEffect(playerCount) {
        for (i in 0 until 6) {
            val config = defaultConfigs[i]
            if (i < playerCount) {
                if (config.type == LudoPlayerType.DISABLED) {
                    val fallbackType = if (i == 0) LudoPlayerType.HUMAN else LudoPlayerType.AI
                    defaultConfigs[i] = config.copy(type = fallbackType)
                }
            } else {
                defaultConfigs[i] = config.copy(type = LudoPlayerType.DISABLED)
            }
        }
    }
    
    // Setup log helper
    fun addLog(msg: String) {
        gameLogs = (listOf(msg) + gameLogs).take(60)
    }
    
    // Move to next player safely
    fun passToNextPlayer() {
        isDiceRolled = false
        isRolling = false
        
        var nextIdx = currentPlayerIndex
        var found = false
        for (i in 1..6) {
            val checkIdx = (currentPlayerIndex + i) % 6
            val checkConfig = defaultConfigs.getOrNull(checkIdx) ?: continue
            if (checkConfig.type != LudoPlayerType.DISABLED) {
                // Verify if this player already wrapped up
                val isFinished = tokens.filter { it.playerId == checkIdx }.all { it.position == 56 }
                if (!isFinished) {
                    nextIdx = checkIdx
                    found = true
                    break
                }
            }
        }
        
        if (found) {
            currentPlayerIndex = nextIdx
            val nextName = defaultConfigs[currentPlayerIndex].name
            addLog("$nextName's Turn. Click roll!")
        } else {
            addLog("🏁 Ludo Practice Match completed completely!")
        }
    }
    
    // Handles finished moves, captures, turns and bonus rolls
    fun handleTokenFinishedOrCapture(movedToken: LudoToken, rolledVal: Int, config: LudoPlayerConfig) {
        val finalCoord = getTokenCoordsOnly(movedToken, defaultConfigs)
        var gainedExtraRoll = false
        
        if (movedToken.position == 56) {
            addLog("🏆 ${config.name}'s Token ${movedToken.tokenId + 1} reached Home!")
            gainedExtraRoll = true
            
            // Check if player won of all 4 tokens!
            val playerTokens = tokens.filter { it.playerId == movedToken.playerId }
            if (playerTokens.all { it.position == 56 }) {
                if (!matchRankings.contains(movedToken.playerId)) {
                    matchRankings = matchRankings + movedToken.playerId
                    val placeWord = when (matchRankings.size) {
                        1 -> "1st"
                        2 -> "2nd"
                        3 -> "3rd"
                        4 -> "4th"
                        5 -> "5th"
                        else -> "6th"
                    }
                    addLog("🏁 ${config.name} finished the match at $placeWord rank!")
                    
                    if (movedToken.playerId == 0) {
                        // राहुल राहुल (Human) user win tracking
                        LudoMasterRepository.completeMatch(won = true, xpEarned = 250, coinsEarned = matchBetAmount * 3)
                    }
                }
            }
        } else if (finalCoord != null) {
            // Check capture
            val isOnCommon = movedToken.position in 0..50
            val isSafe = isCellSafe(finalCoord)
            
            if (isOnCommon && !isSafe) {
                val victimToken = tokens.find { opt ->
                    opt.playerId != movedToken.playerId &&
                    opt.position != -1 &&
                    getTokenCoordsOnly(opt, defaultConfigs) == finalCoord
                }
                
                if (victimToken != null) {
                    val victimName = defaultConfigs.getOrNull(victimToken.playerId)?.name ?: "Opponent"
                    addLog("⚔️ ${config.name} CAPTURES ${victimName}'s Token ${victimToken.tokenId + 1}! Sent back to Base!")
                    
                    tokens = tokens.map { tok ->
                        if (tok.playerId == victimToken.playerId && tok.tokenId == victimToken.tokenId) {
                            tok.copy(position = -1)
                        } else {
                            tok
                        }
                    }
                    gainedExtraRoll = true
                }
            }
        }
        
        // Turn transition rules
        if (gainedExtraRoll) {
            consecutiveSixes = 0
            isDiceRolled = false
            isRolling = false
            addLog("${config.name} awarded EXTRA BONUS roll! 🎉")
        } else if (rolledVal == 6) {
            consecutiveSixes += 1
            if (consecutiveSixes == 3) {
                addLog("🚫 skipped: 3 consecutive 6s! Turn passes.")
                consecutiveSixes = 0
                passToNextPlayer()
            } else {
                isDiceRolled = false
                isRolling = false
                addLog("${config.name} rolled a 6! One extra turn. 🎲")
            }
        } else {
            consecutiveSixes = 0
            passToNextPlayer()
        }
    }
    
    // Core Bot Turn Automation Runner - Handles multi-stage offline moves, human handoffs, and bonus extra turns smoothly
    LaunchedEffect(currentPlayerIndex, isMatchActive) {
        if (!isMatchActive) return@LaunchedEffect
        val launchedIndex = currentPlayerIndex
        
        while (isMatchActive) {
            if (currentPlayerIndex != launchedIndex) {
                break
            }
            val currentConfig = defaultConfigs.getOrNull(currentPlayerIndex) ?: break
            
            // Skip disabled slots immediately
            if (currentConfig.type == LudoPlayerType.DISABLED) {
                passToNextPlayer()
                delay(200)
                continue
            }
            
            // Skip already finished players immediately
            val allFinished = tokens.filter { it.playerId == currentPlayerIndex }.all { it.position == 56 }
            if (allFinished) {
                passToNextPlayer()
                delay(200)
                continue
            }
            
            if (currentConfig.type == LudoPlayerType.AI) {
                if (!isDiceRolled && !isRolling) {
                    delay(1000)
                    
                    // 1. Roll the virtual 2D colorful dice
                    isRolling = true
                    addLog("${currentConfig.name} is Shaking...")
                    
                    val rollSteps = (6..8).random()
                    for (i in 0 until rollSteps) {
                        diceValue = calculateRandomDiceOutcome()
                        delay(120)
                    }
                    
                    isRolling = false
                    isDiceRolled = true
                    addLog("${currentConfig.name} Rolled a $diceValue! 🎲")
                    delay(600)
                    
                    // 2. AI Decision Planning & Bud Selection
                    val bestTokenId = calculateBestMove(
                        playerIndex = currentPlayerIndex,
                        diceValue = diceValue,
                        tokens = tokens,
                        difficulty = currentConfig.difficulty,
                        playerConfigs = defaultConfigs.toList()
                    )
                    
                    if (bestTokenId != null) {
                        val t = tokens.find { it.playerId == currentPlayerIndex && it.tokenId == bestTokenId }!!
                        val isReleasing = t.position == -1 && diceValue == 6
                        val stepsToWalk = if (isReleasing) 1 else diceValue
                        val startPos = t.position
                        
                        addLog("${currentConfig.name} moving Token ${bestTokenId + 1}...")
                        
                        // Incrementally step-walk the token on cells
                        for (s in 1..stepsToWalk) {
                            tokens = tokens.map { tok ->
                                if (tok.playerId == currentPlayerIndex && tok.tokenId == bestTokenId) {
                                    val nextPos = if (startPos == -1) 0 else tok.position + 1
                                    tok.copy(position = nextPos)
                                } else {
                                    tok
                                }
                            }
                            delay(120)
                        }
                        
                        val updatedT = tokens.find { it.playerId == currentPlayerIndex && it.tokenId == bestTokenId }!!
                        handleTokenFinishedOrCapture(updatedT, diceValue, currentConfig)
                    } else {
                        addLog("${currentConfig.name} has no valid moves. Skipped!")
                        delay(800)
                        passToNextPlayer()
                    }
                } else {
                    // Turn sequence is active / rolling. Wait and check in next cycle.
                    delay(250)
                }
            } else {
                // Next turn is HUMAN. Exit task loop and wait for manual interaction.
                break
            }
        }
    }
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = LudoDarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LudoSurfaceNavy)
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (isMatchActive) {
                        isMatchActive = false
                    } else {
                        onBack()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isMatchActive) "Practice Match" else "Offline Ludo Engine",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (isMatchActive) "2 - 6 Play Mode, AI Levels" else "Configure local bots with Smart Desicions",
                        color = Color.White.copy(0.5f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    ) { paddingVals ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
        ) {
            if (!isMatchActive) {
                // SETUP LOBBY SCREEN
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        LudoCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = LudoSurfaceNavy,
                            borderColor = LudoBlue
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Group, null, tint = LudoAccentGold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("PLAYER SLOT MANAGER", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "Support 2 to 6 Players. Toggle any player slot to be Human or AI Bot with various difficulty settings.",
                                    color = Color.White.copy(0.6f),
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Player Count Picker
                                Text("NUMBER OF RUNNERS:", color = LudoAccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    (2..6).forEach { num ->
                                        val isSel = playerCount == num
                                        Button(
                                            onClick = { playerCount = num },
                                            modifier = Modifier.weight(1f).testTag("select_player_$num"),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSel) LudoBlue else LudoDarkBg.copy(0.5f)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text("${num}P", fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color.White.copy(0.6f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        Text(
                            text = "CUSTOMIZE ACTIVE PLAYERS & SLOTS DIFFICULTY",
                            color = Color.White.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.fillMaxWidth().padding(start = 4.dp)
                        )
                    }
                    
                    // Render player slot controllers
                    items(playerCount) { idx ->
                        val config = defaultConfigs[idx]
                        LudoCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = LudoSurfaceNavy,
                            borderColor = config.color.copy(0.4f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.2f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(config.color, CircleShape)
                                            .border(1.5.dp, Color.White, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(config.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Position Slot ${idx + 1}", color = Color.White.copy(0.4f), fontSize = 11.sp)
                                    }
                                }
                                
                                Column(horizontalAlignment = Alignment.End, modifier = Modifier.weight(2f)) {
                                    // Row 1: Mode selector
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf(LudoPlayerType.HUMAN, LudoPlayerType.AI).forEach { mode ->
                                            val isSel = config.type == mode
                                            Text(
                                                text = mode.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier
                                                    .background(
                                                        if (isSel) config.color else Color.Transparent,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .border(1.dp, config.color.copy(0.7f), RoundedCornerShape(4.dp))
                                                    .clickable {
                                                        defaultConfigs[idx] = config.copy(type = mode)
                                                    }
                                                    .padding(horizontal = 6.dp, vertical = 3.dp),
                                                color = if (isSel) {
                                                    if (config.color == LudoYellow) Color.Black else Color.White
                                                } else Color.White.copy(0.5f)
                                            )
                                        }
                                    }
                                    
                                    if (config.type == LudoPlayerType.AI) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        // Level Selector
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            LudoDifficulty.values().forEach { level ->
                                                val isLel = config.difficulty == level
                                                Text(
                                                    text = level.name.take(3),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier
                                                        .background(
                                                            if (isLel) LudoAccentGold else Color.Black.copy(0.3f),
                                                            RoundedCornerShape(3.dp)
                                                        )
                                                        .clickable {
                                                            defaultConfigs[idx] = config.copy(difficulty = level)
                                                        }
                                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                                    color = if (isLel) Color.Black else Color.White.copy(0.5f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        LudoCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = LudoSurfaceNavy,
                            borderColor = LudoBorder
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("LOBBY PRACTICE COIN BETTING", color = LudoAccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(100, 500, 2000, 10000).forEach { amt ->
                                        val isSel = matchBetAmount == amt
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(
                                                    if (isSel) LudoBlue.copy(0.3f) else Color.Transparent,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSel) LudoBlue else LudoBorder,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { matchBetAmount = amt }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🪙 $amt", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    item {
                        Button(
                            onClick = {
                                // Initialize 4 tokens for each active player
                                val initialTokens = mutableListOf<LudoToken>()
                                for (i in 0 until playerCount) {
                                    val config = defaultConfigs[i]
                                    for (t in 0..3) {
                                        initialTokens.add(LudoToken(i, t, -1, config.color))
                                    }
                                }
                                tokens = initialTokens
                                matchRankings = emptyList()
                                isManualMoveModeEnabled = false
                                currentPlayerIndex = 0
                                isDiceRolled = false
                                isRolling = false
                                gameLogs = listOf("Match Started! Rahul (Player 1) starts first. Roll!")
                                isMatchActive = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_lobby_match_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("START THE MATCH ⚔️", fontWeight = FontWeight.Black, color = Color.Black, fontSize = 15.sp)
                        }
                    }
                }
            } else {
                // ACTIVE GAMEPLAY WITH FULL LUDO BOARD & LOGS
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val activeConfig = defaultConfigs[currentPlayerIndex]

                    // TOP GAMEPLAY HEADER BAR (like image 2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Hamburger Menu Icon inside dark bordered block
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(LudoSurfaceNavy, RoundedCornerShape(10.dp))
                                .border(1.5.dp, LudoBlue, RoundedCornerShape(10.dp))
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Menu, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }

                        // Middle: Prize pool and gems capsules
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Prize Capsule: Prize 5,000
                            Row(
                                modifier = Modifier
                                    .background(LudoSurfaceNavy, RoundedCornerShape(18.dp))
                                    .border(1.dp, LudoAccentGold, RoundedCornerShape(18.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Prize", color = Color.White.copy(0.6f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Filled.Stars, null, tint = LudoAccentGold, modifier = Modifier.size(12.dp))
                                Text(
                                    text = "${matchBetAmount * playerCount}",
                                    color = LudoAccentGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            // Gems Capsule
                            Row(
                                modifier = Modifier
                                    .background(LudoSurfaceNavy, RoundedCornerShape(18.dp))
                                    .border(1.dp, LudoAccentGem, RoundedCornerShape(18.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.Favorite, null, tint = LudoAccentGem, modifier = Modifier.size(12.dp))
                                Text(
                                    text = "${player.gems}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Right: Toggle logs panel button
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(LudoSurfaceNavy, RoundedCornerShape(10.dp))
                                .border(1.5.dp, LudoAccentGold.copy(0.4f), RoundedCornerShape(10.dp))
                                .clickable { showLogsPanel = !showLogsPanel },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Chat, null, tint = if (showLogsPanel) LudoAccentGold else Color.White, modifier = Modifier.size(18.dp))
                        }
                    }

                    // Turn Indication Banner (Compact inline reminder)
                    LudoVisualTurnIndicator(
                        activeConfig = activeConfig,
                        isDiceRolled = isDiceRolled,
                        isRolling = isRolling,
                        matchBetAmount = matchBetAmount
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (isMatchActive && activeConfig.type == LudoPlayerType.HUMAN && !isDiceRolled) {
                        Button(
                            onClick = {
                                if (!isRolling) {
                                    triggerAnimatedDiceRoll(
                                        scope = scope,
                                        onValueUpdate = { newVal -> diceValue = newVal },
                                        onRollingStateChange = { rolling -> isRolling = rolling },
                                        onRollComplete = { finalOutcome ->
                                            isDiceRolled = true
                                            addLog("${defaultConfigs[currentPlayerIndex].name} Rolled a $finalOutcome! 🎲")
                                            
                                            val validMoves = tokens.filter { it.playerId == currentPlayerIndex && canMoveToken(it, finalOutcome) }
                                            if (validMoves.isEmpty()) {
                                                addLog("${defaultConfigs[currentPlayerIndex].name} has no valid moves. Skipped!")
                                                scope.launch {
                                                    delay(1200)
                                                    passToNextPlayer()
                                                }
                                            }
                                        }
                                    )
                                }
                            },
                            enabled = !isRolling,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LudoAccentGold,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .height(50.dp)
                                .testTag("btn_roll_dice_offline"),
                            border = BorderStroke(2.dp, Color.White.copy(0.6f))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Casino,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isRolling) "ROLLING..." else "ROLL DICE 🎲",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // TOP PLAYERS & DICE ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top-Left: Rahul (P0)
                        LudoGameplayProfileCard(
                            config = defaultConfigs[0],
                            isActive = (currentPlayerIndex == 0)
                        )

                        // Top-Center Interactive Dice Box
                        val showClickableActiveTop = isMatchActive &&
                                (currentPlayerIndex == 0 || currentPlayerIndex == 1) &&
                                defaultConfigs[currentPlayerIndex].type == LudoPlayerType.HUMAN &&
                                !isDiceRolled && !isRolling

                        val topDiceValue = if (currentPlayerIndex == 0 || currentPlayerIndex == 1) diceValue else 5

                        LudoDiceBox(
                            value = topDiceValue,
                            isRolling = isRolling && (currentPlayerIndex == 0 || currentPlayerIndex == 1),
                            isActive = showClickableActiveTop,
                            playerColor = defaultConfigs[currentPlayerIndex].color,
                            onClick = {
                                triggerAnimatedDiceRoll(
                                    scope = scope,
                                    onValueUpdate = { newVal -> diceValue = newVal },
                                    onRollingStateChange = { rolling -> isRolling = rolling },
                                    onRollComplete = { finalOutcome ->
                                        isDiceRolled = true
                                        addLog("${defaultConfigs[currentPlayerIndex].name} Rolled a $finalOutcome! 🎲")
                                        
                                        val validMoves = tokens.filter { it.playerId == currentPlayerIndex && canMoveToken(it, finalOutcome) }
                                        if (validMoves.isEmpty()) {
                                            addLog("${defaultConfigs[currentPlayerIndex].name} has no valid moves. Skipped!")
                                            scope.launch {
                                                delay(1000)
                                                passToNextPlayer()
                                            }
                                        }
                                    }
                                )
                            }
                        )

                        // Top-Right: Priya (P1)
                        LudoGameplayProfileCard(
                            config = defaultConfigs[1],
                            isActive = (currentPlayerIndex == 1)
                        )
                    }

                    // LUDO BOARD SCANNED VIEW
                    BoxWithConstraints(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                            .padding(10.dp)
                            .border(4.dp, LudoBorder.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                    ) {
                        val boardWidth = maxWidth
                        val cellSize = boardWidth / 15
                        
                        // Render standard cells grid row-by-row
                        Column {
                            for (row in 0 until 15) {
                                Row {
                                    for (col in 0 until 15) {
                                        val cellColor = when {
                                            // Blue Yard top-left
                                            row in 0..5 && col in 0..5 -> LudoBlue.copy(0.12f)
                                            // Red Yard top-right
                                            row in 0..5 && col in 9..14 -> LudoRed.copy(0.12f)
                                            // Green Yard bottom-right
                                            row in 9..14 && col in 9..14 -> LudoGreen.copy(0.12f)
                                            // Yellow Yard bottom-left
                                            row in 9..14 && col in 0..5 -> LudoYellow.copy(0.12f)
                                            
                                            // Triangles center (6..8, 6..8) are handles by Custom Canvas overlay
                                            row in 6..8 && col in 6..8 -> Color.Transparent
                                            
                                            // Colored home tracks matching the online / screenshot scheme
                                            row == 7 && col in 1..5 -> LudoBlue
                                            row == 7 && col in 9..13 -> LudoGreen
                                            col == 7 && row in 1..5 -> LudoRed
                                            col == 7 && row in 9..13 -> LudoYellow
                                            
                                            // Quadrants entering starting spaces (correctly colored)
                                            (row == 6 && col == 1) -> LudoBlue.copy(0.4f)
                                            (row == 8 && col == 13) -> LudoGreen.copy(0.4f)
                                            (row == 1 && col == 8) -> LudoRed.copy(0.4f)
                                            (row == 13 && col == 6) -> LudoYellow.copy(0.4f)
                                            
                                            else -> Color.White
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(cellSize)
                                                .border(0.5.dp, Color.Black.copy(0.12f))
                                                .background(cellColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Draw Star icon on safe cells (supporting both traditional & screenshot locations)
                                            if ((row == 6 && col == 2) || (row == 8 && col == 12) || (row == 2 && col == 8) || (row == 12 && col == 6) ||
                                                (row == 8 && col == 2) || (row == 6 && col == 12) || (row == 2 && col == 6) || (row == 12 && col == 8)) {
                                                
                                                val starColor = when {
                                                    (row == 2 && col == 6) -> LudoRed
                                                    (row == 12 && col == 8) -> LudoYellow
                                                    (row == 8 && col == 2) -> LudoBlue
                                                    (row == 6 && col == 12) -> LudoGreen
                                                    else -> Color.LightGray
                                                }
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = starColor.copy(alpha = 0.9f),
                                                    modifier = Modifier.size(cellSize * 0.72f)
                                                )
                                            }
                                            // Draw direction entering arrows exactly matching the screenshot
                                            if (row == 7 && col == 0) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = LudoBlue.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(cellSize * 0.72f)
                                                )
                                            } else if (row == 7 && col == 14) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowBack,
                                                    contentDescription = null,
                                                    tint = LudoGreen.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(cellSize * 0.72f)
                                                )
                                            } else if (row == 0 && col == 7) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDownward,
                                                    contentDescription = null,
                                                    tint = LudoRed.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(cellSize * 0.72f)
                                                )
                                            } else if (row == 14 && col == 7) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowUpward,
                                                    contentDescription = null,
                                                    tint = LudoYellow.copy(alpha = 0.95f),
                                                    modifier = Modifier.size(cellSize * 0.72f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Center Triangles overlay (exactly covering rows 6..8, cols 6..8)
                        Box(
                            modifier = Modifier
                                .offset(x = cellSize * 6, y = cellSize * 6)
                                .size(cellSize * 3)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val cx = w / 2f
                                val cy = h / 2f
                                
                                val leftPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(cx, cy)
                                    lineTo(0f, h)
                                    close()
                                }
                                drawPath(leftPath, LudoBlue)
                                
                                val topPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(cx, cy)
                                    lineTo(w, 0f)
                                    close()
                                }
                                drawPath(topPath, LudoRed)
                                
                                val rightPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(w, 0f)
                                    lineTo(cx, cy)
                                    lineTo(w, h)
                                    close()
                                }
                                drawPath(rightPath, LudoGreen)
                                
                                val bottomPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, h)
                                    lineTo(cx, cy)
                                    lineTo(w, h)
                                    close()
                                }
                                drawPath(bottomPath, LudoYellow)
                                
                                // Beautiful white boundary separation lines
                                drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(w, h), strokeWidth = 2.dp.toPx())
                                drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(0f, h), end = androidx.compose.ui.geometry.Offset(w, 0f), strokeWidth = 2.dp.toPx())
                                
                                // Draw outer rectangle boundaries
                                drawRect(Color.White, size = size, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                            }
                        }
                        
                        // Draw beautiful base circular yards on corners (re-designed rounded-square containers with glossy slots)
                        val yardSize = cellSize * 6
                        // Top-Left Blue Base Yard
                        Box(
                            modifier = Modifier
                                .size(yardSize)
                                .background(LudoBlue, RoundedCornerShape(16.dp))
                                .padding(cellSize * 0.7f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(cellSize * 0.35f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoBlue, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoBlue, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoBlue, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoBlue, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                }
                            }
                        }
                        
                        // Top-Right Red Base Yard
                        Box(
                            modifier = Modifier
                                .offset(x = yardSize + cellSize * 3)
                                .size(yardSize)
                                .background(LudoRed, RoundedCornerShape(16.dp))
                                .padding(cellSize * 0.7f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(cellSize * 0.35f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoRed, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoRed, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoRed, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoRed, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                }
                            }
                        }
                        
                        // Bottom-Left Yellow Base Yard
                        Box(
                            modifier = Modifier
                                .offset(y = yardSize + cellSize * 3)
                                .size(yardSize)
                                .background(LudoYellow, RoundedCornerShape(16.dp))
                                .padding(cellSize * 0.7f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(cellSize * 0.35f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoYellow, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoYellow, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoYellow, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoYellow, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                }
                            }
                        }
                        
                        // Bottom-Right Green Base Yard
                        Box(
                            modifier = Modifier
                                .offset(x = yardSize + cellSize * 3, y = yardSize + cellSize * 3)
                                .size(yardSize)
                                .background(LudoGreen, RoundedCornerShape(16.dp))
                                .padding(cellSize * 0.7f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(cellSize * 0.35f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoGreen, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoGreen, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoGreen, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoGreen, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                }
                            }
                        }
                        
                        // RENDER TOKENS DYNAMICALLY ON BOARD WITH CELL SHIFTS
                        tokens.forEach { token ->
                            val isSelectable = if (isManualMoveModeEnabled) {
                                isMatchActive &&
                                        token.playerId == currentPlayerIndex &&
                                        defaultConfigs[currentPlayerIndex].type == LudoPlayerType.HUMAN &&
                                        !isRolling
                            } else {
                                isMatchActive &&
                                        token.playerId == currentPlayerIndex &&
                                        defaultConfigs[currentPlayerIndex].type == LudoPlayerType.HUMAN &&
                                        isDiceRolled && !isRolling &&
                                        canMoveToken(token, diceValue)
                            }
                            
                            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                            val scaleProp by infiniteTransition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse_scale"
                            )
                            
                            // Determine relative floating coordinate
                            var floatCoords = getTokenCoords(token, defaultConfigs.toList())
                            
                            // Check overlap shift
                            val otherTokensTarget = tokens.filter {
                                it.position != -1 && it != token &&
                                getTokenCoordsOnly(it, defaultConfigs.toList()) == getTokenCoordsOnly(token, defaultConfigs.toList())
                            }
                            
                            if (otherTokensTarget.isNotEmpty() && token.position != -1) {
                                val offsetShift = when (token.playerId) {
                                    0 -> Pair(-0.16f, -0.16f)
                                    1 -> Pair(0.16f, -0.16f)
                                    2 -> Pair(0.16f, 0.16f)
                                    3 -> Pair(-0.16f, 0.16f)
                                    4 -> Pair(0f, -0.16f)
                                    else -> Pair(0f, 0.16f)
                                }
                                floatCoords = Pair(floatCoords.first + offsetShift.first, floatCoords.second + offsetShift.second)
                            }
                            
                            if (floatCoords.first >= 0f) {
                                Box(
                                    modifier = Modifier
                                        .offset(
                                            x = cellSize * floatCoords.second,
                                            y = cellSize * floatCoords.first
                                        )
                                        .size(cellSize)
                                        .padding(3.dp)
                                        .scale(if (isSelectable) scaleProp else 1.0f)
                                        .background(token.color, CircleShape)
                                        .border(
                                            if (isSelectable) 2.5.dp else 1.5.dp,
                                            if (isSelectable) LudoAccentGold else Color.White,
                                            CircleShape
                                        )
                                        .clickable(enabled = isSelectable) {
                                            // Handle click
                                            scope.launch {
                                                isRolling = true
                                                val startPos = token.position
                                                val stepsToWalk = if (isManualMoveModeEnabled) {
                                                    1
                                                } else {
                                                    if (startPos == -1 && diceValue == 6) 1 else diceValue
                                                }
                                                
                                                val moveName = defaultConfigs[token.playerId].name
                                                addLog("$moveName manually moved Token ${token.tokenId + 1}...")
                                                
                                                for (s in 1..stepsToWalk) {
                                                    tokens = tokens.map { tok ->
                                                        if (tok.playerId == token.playerId && tok.tokenId == token.tokenId) {
                                                            val nextPos = if (startPos == -1) 0 else tok.position + 1
                                                            tok.copy(position = nextPos)
                                                        } else {
                                                            tok
                                                        }
                                                    }
                                                    delay(120)
                                                }
                                                
                                                isRolling = false
                                                val updatedT = tokens.find { it.playerId == token.playerId && it.tokenId == token.tokenId }!!
                                                
                                                if (isManualMoveModeEnabled) {
                                                    val finalCoord = getTokenCoordsOnly(updatedT, defaultConfigs)
                                                    if (updatedT.position == 56) {
                                                        val pName = defaultConfigs.getOrNull(updatedT.playerId)?.name ?: "Player"
                                                        addLog("🏆 $pName's Token ${updatedT.tokenId + 1} reached Home manually!")
                                                        
                                                        // Check manual victory
                                                        val pTokens = tokens.filter { it.playerId == updatedT.playerId }
                                                        if (pTokens.isNotEmpty() && pTokens.all { it.position == 56 }) {
                                                            if (!matchRankings.contains(updatedT.playerId)) {
                                                                 matchRankings = matchRankings + updatedT.playerId
                                                                 val placeWord = when (matchRankings.size) {
                                                                     1 -> "1st"
                                                                     2 -> "2nd"
                                                                     3 -> "3rd"
                                                                     4 -> "4th"
                                                                     5 -> "5th"
                                                                     else -> "6th"
                                                                 }
                                                                 addLog("🏁 $pName finished the match at $placeWord rank manually!")
                                                                 if (updatedT.playerId == 0) {
                                                                     LudoMasterRepository.completeMatch(won = true, xpEarned = 250, coinsEarned = matchBetAmount * 3)
                                                                 }
                                                             }
                                                         }
                                                    } else if (finalCoord != null) {
                                                        val isOnCommon = updatedT.position in 0..50
                                                        val isSafe = isCellSafe(finalCoord)
                                                        if (isOnCommon && !isSafe) {
                                                            val victimToken = tokens.find { opt ->
                                                                opt.playerId != updatedT.playerId &&
                                                                opt.position != -1 &&
                                                                getTokenCoordsOnly(opt, defaultConfigs) == finalCoord
                                                            }
                                                            if (victimToken != null) {
                                                                val victimName = defaultConfigs.getOrNull(victimToken.playerId)?.name ?: "Opponent"
                                                                addLog("⚔️ CAPTURES ${victimName}'s Token ${victimToken.tokenId + 1}!")
                                                                tokens = tokens.map { tok ->
                                                                    if (tok.playerId == victimToken.playerId && tok.tokenId == victimToken.tokenId) {
                                                                        tok.copy(position = -1)
                                                                    } else {
                                                                        tok
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    handleTokenFinishedOrCapture(updatedT, diceValue, activeConfig)
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${token.tokenId + 1}",
                                        color = if (token.color == LudoYellow) Color.Black else Color.White,
                                        fontSize = (cellSize.value * 0.35f).sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // BOTTOM PLAYERS & DICE ROW
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Bottom-Left: Amit (P3)
                        LudoGameplayProfileCard(
                            config = defaultConfigs[3],
                            isActive = (currentPlayerIndex == 3)
                        )

                        // Bottom-Center Interactive Dice Box
                        val showClickableActiveBottom = isMatchActive &&
                                (currentPlayerIndex == 3 || currentPlayerIndex == 2) &&
                                defaultConfigs[currentPlayerIndex].type == LudoPlayerType.HUMAN &&
                                !isDiceRolled && !isRolling

                        val bottomDiceValue = if (currentPlayerIndex == 3 || currentPlayerIndex == 2) diceValue else 1

                        LudoDiceBox(
                            value = bottomDiceValue,
                            isRolling = isRolling && (currentPlayerIndex == 3 || currentPlayerIndex == 2),
                            isActive = showClickableActiveBottom,
                            playerColor = defaultConfigs[currentPlayerIndex].color,
                            onClick = {
                                triggerAnimatedDiceRoll(
                                    scope = scope,
                                    onValueUpdate = { newVal -> diceValue = newVal },
                                    onRollingStateChange = { rolling -> isRolling = rolling },
                                    onRollComplete = { finalOutcome ->
                                        isDiceRolled = true
                                        addLog("${defaultConfigs[currentPlayerIndex].name} Rolled a $finalOutcome! 🎲")
                                        
                                        // Check if has any valid move
                                        val validMoves = tokens.filter { it.playerId == currentPlayerIndex && canMoveToken(it, finalOutcome) }
                                        if (validMoves.isEmpty()) {
                                            addLog("${defaultConfigs[currentPlayerIndex].name} has no valid moves. Skipped!")
                                            scope.launch {
                                                delay(1000)
                                                passToNextPlayer()
                                            }
                                        }
                                    }
                                )
                            }
                        )

                        // Bottom-Right: Neha (P2)
                        LudoGameplayProfileCard(
                            config = defaultConfigs[2],
                            isActive = (currentPlayerIndex == 2)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // ACTION BUTTONS ROW (EMOJI & LAUNCH LOGS TOGGLE)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(LudoSurfaceNavy, RoundedCornerShape(19.dp))
                                .border(1.dp, LudoBorder.copy(0.4f), RoundedCornerShape(19.dp))
                                .clickable {
                                    val randomEmoji = listOf("👍", "🔥", "👑", "🎯", "😂", "👊").random()
                                    addLog("${player.name} sent emoji: $randomEmoji")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("👉 EMOJI 👍", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.sp)
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .background(LudoSurfaceNavy, RoundedCornerShape(19.dp))
                                .border(1.dp, if (showLogsPanel) LudoAccentGold else LudoBorder.copy(0.4f), RoundedCornerShape(19.dp))
                                .clickable { showLogsPanel = !showLogsPanel }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.ChatBubble, null, tint = if (showLogsPanel) LudoAccentGold else Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DEV LOGS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    // MANUAL COIN MOVEMENT CONTROL RIBBON
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TouchApp,
                                contentDescription = null,
                                tint = if (isManualMoveModeEnabled) LudoAccentGold else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MANUAL PIECE MOVEMENT",
                                color = if (isManualMoveModeEnabled) LudoAccentGold else Color.White.copy(0.5f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Switch(
                                checked = isManualMoveModeEnabled,
                                onCheckedChange = { isManualMoveModeEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = LudoAccentGold,
                                    checkedTrackColor = LudoBlue,
                                    uncheckedThumbColor = Color.Gray,
                                    uncheckedTrackColor = Color.Black.copy(0.4f)
                                ),
                                modifier = Modifier.scale(0.82f).testTag("manual_coins_toggle")
                            )
                            if (isManualMoveModeEnabled) {
                                Button(
                                    onClick = { passToNextPlayer() },
                                    colors = ButtonDefaults.buttonColors(containerColor = LudoRed),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("SKIP TURN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }

                    // RENDER ACTIVE PLAYER TOKENS DIRECT CONTROLLER
                    if (isMatchActive && activeConfig.type == LudoPlayerType.HUMAN) {
                        LudoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            backgroundColor = LudoSurfaceNavy,
                            borderColor = activeConfig.color.copy(0.6f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "YOUR PIECES STATUS (TAP TO MOVE):",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White.copy(0.4f),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val playerTokens = tokens.filter { it.playerId == currentPlayerIndex }
                                    playerTokens.forEach { token ->
                                        val isSelectable = if (isManualMoveModeEnabled) {
                                            !isRolling
                                        } else {
                                            isDiceRolled && !isRolling && canMoveToken(token, diceValue)
                                        }
                                        val statWord = if (token.position == -1) "Yard 🏰" else if (token.position == 56) "Home 🏆" else "${token.position}/56 🏃"
                                        
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    isRolling = true
                                                    val startPos = token.position
                                                    val stepsToWalk = if (isManualMoveModeEnabled) {
                                                        1
                                                    } else {
                                                        val isReleasing = token.position == -1 && diceValue == 6
                                                        if (isReleasing) 1 else diceValue
                                                    }
                                                    
                                                    val moveName = defaultConfigs[token.playerId].name
                                                    addLog("$moveName manually moved Token ${token.tokenId + 1}...")
                                                    for (s in 1..stepsToWalk) {
                                                        tokens = tokens.map { tok ->
                                                            if (tok.playerId == token.playerId && tok.tokenId == token.tokenId) {
                                                                val nextPos = if (startPos == -1) 0 else tok.position + 1
                                                                tok.copy(position = nextPos)
                                                            } else {
                                                                tok
                                                            }
                                                        }
                                                        delay(120)
                                                    }
                                                    isRolling = false
                                                    val updatedT = tokens.find { it.playerId == token.playerId && it.tokenId == token.tokenId }!!
                                                    
                                                    if (isManualMoveModeEnabled) {
                                                        val finalCoord = getTokenCoordsOnly(updatedT, defaultConfigs)
                                                        if (updatedT.position == 56) {
                                                            val pName = defaultConfigs.getOrNull(updatedT.playerId)?.name ?: "Player"
                                                            addLog("🏆 $pName's Token ${updatedT.tokenId + 1} reached Home manually!")
                                                            
                                                            // Check manual victory
                                                            val pTokens = tokens.filter { it.playerId == updatedT.playerId }
                                                            if (pTokens.isNotEmpty() && pTokens.all { it.position == 56 }) {
                                                                if (!matchRankings.contains(updatedT.playerId)) {
                                                                    matchRankings = matchRankings + updatedT.playerId
                                                                    val placeWord = when (matchRankings.size) {
                                                                        1 -> "1st"
                                                                        2 -> "2nd"
                                                                        3 -> "3rd"
                                                                        4 -> "4th"
                                                                        5 -> "5th"
                                                                        else -> "6th"
                                                                    }
                                                                    addLog("🏁 $pName finished the match at $placeWord rank manually!")
                                                                    if (updatedT.playerId == 0) {
                                                                        LudoMasterRepository.completeMatch(won = true, xpEarned = 250, coinsEarned = matchBetAmount * 3)
                                                                    }
                                                                }
                                                            }
                                                        } else if (finalCoord != null) {
                                                            val isOnCommon = updatedT.position in 0..50
                                                            val isSafe = isCellSafe(finalCoord)
                                                            if (isOnCommon && !isSafe) {
                                                                val victimToken = tokens.find { opt ->
                                                                    opt.playerId != updatedT.playerId &&
                                                                    opt.position != -1 &&
                                                                    getTokenCoordsOnly(opt, defaultConfigs) == finalCoord
                                                                }
                                                                if (victimToken != null) {
                                                                    val victimName = defaultConfigs.getOrNull(victimToken.playerId)?.name ?: "Opponent"
                                                                    addLog("⚔️ CAPTURES ${victimName}'s Token ${victimToken.tokenId + 1}!")
                                                                    tokens = tokens.map { tok ->
                                                                        if (tok.playerId == victimToken.playerId && tok.tokenId == victimToken.tokenId) {
                                                                            tok.copy(position = -1)
                                                                        } else {
                                                                            tok
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        handleTokenFinishedOrCapture(updatedT, diceValue, activeConfig)
                                                    }
                                                }
                                            },
                                            enabled = isSelectable,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelectable) activeConfig.color else LudoDarkBg.copy(0.4f),
                                                disabledContainerColor = LudoDarkBg.copy(0.2f)
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("T${token.tokenId + 1}", fontWeight = FontWeight.Black, fontSize = 12.sp, color = if (isSelectable && activeConfig.color == LudoYellow) Color.Black else Color.White)
                                                Text(statWord, fontSize = 9.sp, color = if (isSelectable && activeConfig.color == LudoYellow) Color.Black.copy(0.7f) else Color.White.copy(0.6f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // CONSOLE LOG LOGGER (Displays real game captures and outcomes)
                    if (showLogsPanel) {
                        LudoCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            backgroundColor = Color.Black.copy(0.3f),
                            borderColor = LudoBorder.copy(0.4f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.AutoMirrored.Filled.Chat, null, tint = LudoAccentGold, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("GAMEPLAY REALTIME LOGS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp, letterSpacing = 1.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(gameLogs) { log ->
                                        Text(
                                            text = log,
                                            color = if (log.contains("CAPTURES") || log.contains("⚔️")) LudoRed else if (log.contains("Rolling") || log.contains("Shaking")) Color.White.copy(0.5f) else Color.White.copy(0.85f),
                                            fontSize = 11.sp,
                                            style = TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showWinnerOverlay && matchRankings.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(0.85f))
                        .clickable(enabled = false) { }
                        .testTag("winner_overlay"),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .widthIn(max = 450.dp)
                            .fillMaxWidth(0.9f)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(DarkNavyGradientStart, DarkNavyGradientEnd)
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(LudoAccentGold, Color.Transparent)
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title/Trophy Ring
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(LudoAccentGold.copy(0.12f), CircleShape)
                                .border(2.dp, LudoAccentGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = "Victory Trophy",
                                tint = LudoAccentGold,
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        // Champion Header text
                        val firstWinnerId = matchRankings.firstOrNull() ?: 0
                        val winnerConfig = defaultConfigs.getOrNull(firstWinnerId)
                        val winnerName = winnerConfig?.name ?: "Player"
                        val winnerColor = winnerConfig?.color ?: LudoAccentGold

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "MATCH CONCLUDED! 🏆",
                                color = LudoAccentGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = winnerName,
                                color = winnerColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = "👑 CHOSEN CHAMPION (ALL 4 HOME) 👑",
                                color = Color.White.copy(0.7f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(0.12f))
                        )

                        // Podium / Sequence
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "STANDINGS PODIUM",
                                color = Color.White.copy(0.5f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 1.sp
                            )

                            matchRankings.forEachIndexed { index, pId ->
                                val config = defaultConfigs.getOrNull(pId)
                                if (config != null) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(LudoSurfaceNavy, RoundedCornerShape(12.dp))
                                            .border(1.dp, config.color.copy(0.35f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(config.color.copy(0.15f), CircleShape)
                                                    .border(1.2.dp, config.color, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${index + 1}",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Text(
                                                text = config.name,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }

                                        val awardText = when(index) {
                                            0 -> "CHAMPION 👑"
                                            1 -> "Runner-Up"
                                            2 -> "Third"
                                            else -> "Finished"
                                        }
                                        Text(
                                            text = awardText,
                                            color = if (index == 0) LudoAccentGold else Color.White.copy(0.6f),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Controls
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val initialTokens = mutableListOf<LudoToken>()
                                    for (i in 0 until playerCount) {
                                        val config = defaultConfigs[i]
                                        for (t in 0..3) {
                                            initialTokens.add(LudoToken(i, t, -1, config.color))
                                        }
                                    }
                                    tokens = initialTokens
                                    matchRankings = emptyList()
                                    isManualMoveModeEnabled = false
                                    currentPlayerIndex = 0
                                    isDiceRolled = false
                                    isRolling = false
                                    gameLogs = listOf("Match Restarted! Rahul (Player 1) starts first. Roll!")
                                    showWinnerOverlay = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("winner_rematch_btn")
                            ) {
                                Text(
                                    "RESTART PRACTICE MATCH ⚔️",
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    fontSize = 13.sp
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    showWinnerOverlay = false
                                    isMatchActive = false
                                },
                                border = BorderStroke(1.dp, Color.White.copy(0.35f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("winner_exit_btn")
                            ) {
                                Text(
                                    "LEAVE TO GAME LOBBY 🏠",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Coordinate support offset vector mapping helper
fun getTokenCoords(token: LudoToken, configs: List<LudoPlayerConfig>): Pair<Float, Float> {
    if (token.position == -1) {
        return when (token.playerId) {
            0 -> { // BLUE (top-left) - Row 1..4, Col 1..4
                when (token.tokenId) {
                    0 -> Pair(1.4f, 1.4f)
                    1 -> Pair(1.4f, 3.4f)
                    2 -> Pair(3.4f, 1.4f)
                    else -> Pair(3.4f, 3.4f)
                }
            }
            1 -> { // RED (top-right) - Row 1..4, Col 10..13
                when (token.tokenId) {
                    0 -> Pair(1.4f, 10.4f)
                    1 -> Pair(1.4f, 12.4f)
                    2 -> Pair(3.4f, 10.4f)
                    else -> Pair(3.4f, 12.4f)
                }
            }
            2 -> { // GREEN (bottom-right) - Row 10..13, Col 10..13
                when (token.tokenId) {
                    0 -> Pair(10.4f, 10.4f)
                    1 -> Pair(10.4f, 12.4f)
                    2 -> Pair(12.4f, 10.4f)
                    else -> Pair(12.4f, 12.4f)
                }
            }
            3 -> { // YELLOW (bottom-left) - Row 10..13, Col 1..4
                when (token.tokenId) {
                    0 -> Pair(10.4f, 1.4f)
                    1 -> Pair(10.4f, 3.4f)
                    2 -> Pair(12.4f, 1.4f)
                    else -> Pair(12.4f, 3.4f)
                }
            }
            else -> Pair(-5f, -5f) // Hide Purple P5 & Orange P6 off Grid in yards (they enter track dynamically)
        }
    }
    
    if (token.position in 0..50) {
        val config = configs.find { it.index == token.playerId } ?: return Pair(0f, 0f)
        val commonIdx = (config.startCell + token.position) % 52
        val cell = ludoTrackCells[commonIdx]
        return Pair(cell.first.toFloat(), cell.second.toFloat())
    }
    
    if (token.position in 51..55) {
        val homeCol = getHomeColumnCoords(token.playerId)
        val cell = homeCol[token.position - 51]
        return Pair(cell.first.toFloat(), cell.second.toFloat())
    }
    
    return Pair(7f, 7f) // Home triangle center
}

// --- 8. PLAY ONLINE SCREEN (LOBBY MATCHMAKING MULTIPLAYER) ---
@Composable
fun PlayOnlineScreen(
    onNavigateToChat: () -> Unit,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val playerProfile by LudoMasterRepository.playerState.collectAsState()
    val roomState by com.example.data.MultiplayerManager.currentRoom.collectAsState()
    val isConnecting by com.example.data.MultiplayerManager.isConnecting.collectAsState()
    val onlineLogs by com.example.data.MultiplayerManager.onlineLogs.collectAsState()
    val onlineChats by com.example.data.MultiplayerManager.activeChat.collectAsState()

    var activeTabCreate by remember { mutableStateOf(true) }
    var lobbyCodeInput by remember { mutableStateOf("") }
    var selectPlayersCount by remember { mutableIntStateOf(4) }
    var selectBetAmount by remember { mutableIntStateOf(500) }

    var localMatchmakingSpinner by remember { mutableStateOf(false) }

    // Inside-Lobby Chat typing state
    var lobbyChatText by remember { mutableStateOf("") }

    // Exit room easily on back pressed or manual click
    val leaveAndBack: () -> Unit = {
        com.example.data.MultiplayerManager.leaveRoom()
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleGameHeader(
                title = if (roomState != null) "Room: ${roomState!!.roomId}" else "Online Lobby",
                onBack = leaveAndBack
            )

            // --- 1. LOBBY ENTRY FORMS (IF NOT IN A ROOM) ---
            if (roomState == null) {
                if (localMatchmakingSpinner) {
                    // Matchmaking loading spinner
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = LudoAccentGold, strokeWidth = 4.dp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "ESTABLISHING RTC DUEL MATCH...",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Querying live tournament queues (Bet: ${selectBetAmount} Coins)",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        // Display match logs dynamically
                        LudoCard(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(onlineLogs) { log ->
                                    Text(log, color = Color.Gray, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(30.dp))
                        Button(
                            onClick = { localMatchmakingSpinner = false },
                            colors = ButtonDefaults.buttonColors(containerColor = LudoRed),
                            modifier = Modifier.height(45.dp)
                        ) {
                            Text("ABORT MATCHMAKING", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Selection tab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Tab Selector Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(0.3f), RoundedCornerShape(24.dp))
                                .padding(4.dp)
                        ) {
                            Button(
                                onClick = { activeTabCreate = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activeTabCreate) LudoSurfaceNavy else Color.Transparent
                                ),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Text("HOST ROOM", color = if (activeTabCreate) LudoAccentGold else Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { activeTabCreate = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (!activeTabCreate) LudoSurfaceNavy else Color.Transparent
                                ),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Text("JOIN BY CODE", color = if (!activeTabCreate) LudoAccentGold else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (activeTabCreate) {
                            // HOST PARAMETERS
                            LudoCard {
                                Text("CHOOSE THE PLAYER CAPACITY", color = LudoAccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(2, 3, 4, 6).forEach { count ->
                                        val active = selectPlayersCount == count
                                        Button(
                                            onClick = { selectPlayersCount = count },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (active) LudoBlue else LudoSurfaceNavy
                                            ),
                                            border = BorderStroke(1.dp, if (active) LudoAccentGold else LudoBorder),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                        ) {
                                            Text("${count}P", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))
                                Text("CHOOSE ENTRY BET SIZE", color = LudoAccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    listOf(100, 500, 1000, 5000).forEach { bet ->
                                        val active = selectBetAmount == bet
                                        Button(
                                            onClick = { selectBetAmount = bet },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (active) LudoGreen else LudoSurfaceNavy
                                            ),
                                            border = BorderStroke(1.dp, if (active) LudoAccentGold else LudoBorder),
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp)
                                        ) {
                                            Text("${bet} 🪙", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Create Button
                            Button(
                                onClick = {
                                    if (playerProfile.coins < selectBetAmount) {
                                        android.widget.Toast.makeText(context, "Insufficient Battle Coins! You need $selectBetAmount Coins to host.", android.widget.Toast.LENGTH_LONG).show()
                                    } else {
                                        com.example.data.EconomyManager.chargeEntryFee(selectBetAmount) { success, msg ->
                                            if (success) {
                                                com.example.data.MultiplayerManager.createRoom(
                                                    playerProfile = playerProfile,
                                                    targetPlayersCount = selectPlayersCount,
                                                    bet = selectBetAmount
                                                )
                                            } else {
                                                android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LudoGreen),
                                enabled = !isConnecting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_create_room_btn")
                            ) {
                                if (isConnecting) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 1.5.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("CONNECTING...")
                                } else {
                                    Text("CREATE LOBBY & ENLIST", color = Color.White, fontWeight = FontWeight.Black)
                                }
                            }
                            
                            // Matchmaking Button
                            Button(
                                onClick = {
                                    if (playerProfile.coins < selectBetAmount) {
                                        android.widget.Toast.makeText(context, "Insufficient Battle Coins! You need $selectBetAmount Coins to match.", android.widget.Toast.LENGTH_LONG).show()
                                    } else {
                                        localMatchmakingSpinner = true
                                        scope.launch {
                                            delay(2000)
                                            com.example.data.EconomyManager.chargeEntryFee(selectBetAmount) { success, msg ->
                                                if (success) {
                                                    com.example.data.MultiplayerManager.createRoom(
                                                        playerProfile = playerProfile,
                                                        targetPlayersCount = selectPlayersCount,
                                                        bet = selectBetAmount
                                                    )
                                                } else {
                                                    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                                }
                                                localMatchmakingSpinner = false
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LudoBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_quick_match_btn")
                            ) {
                                Text("QUICK MATCHMAKING (BET: ${selectBetAmount} 🪙)", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // JOIN PARAMETERS
                            LudoCard {
                                Text("ENTER 6-DIGIT MULTIPLAYER ROOM CODE", color = LudoAccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                OutlinedTextField(
                                    value = lobbyCodeInput,
                                    onValueChange = { if (it.length <= 6) lobbyCodeInput = it.uppercase() },
                                    placeholder = { Text("Example: 849204", color = Color.Gray) },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Black.copy(0.3f),
                                        unfocusedContainerColor = Color.Black.copy(0.3f),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedIndicatorColor = LudoAccentGold,
                                        unfocusedIndicatorColor = LudoBorder
                                    ),
                                    textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp),
                                    modifier = Modifier.fillMaxWidth().testTag("enter_lobby_code_input")
                                )
                            }

                            Button(
                                onClick = {
                                    com.example.data.MultiplayerManager.joinRoom(playerProfile, lobbyCodeInput) { success, msg ->
                                        if (!success) {
                                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LudoBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("submit_join_room_btn")
                            ) {
                                Text("ENTER BATTLEFIELD ROOM", color = Color.White, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }

            // --- 2. ACTIVE LOBBY STATE (BEFORE GAME INITIATING) ---
            else if (roomState!!.status == "LOBBY") {
                val room = roomState!!
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LudoCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("LOBBY ROOM CODE", color = Color.White.copy(0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(room.roomId, color = LudoAccentGold, fontWeight = FontWeight.Black, fontSize = 24.sp, modifier = Modifier.testTag("room_code_display"))
                            }
                            
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { 
                                        val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clipData = android.content.ClipData.newPlainText("Ludo Lobby Code", room.roomId)
                                        clipboardManager.setPrimaryClip(clipData)
                                        android.widget.Toast.makeText(context, "Room Code Copied to Clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LudoBlue),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("copy_room_button")
                                ) {
                                    Icon(Icons.Filled.ContentCopy, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("COPY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, "Join my Ludo Master real-time online battle room now! Code: ${room.roomId}")
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Share Room Code"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LudoGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Filled.Share, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SHARE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = LudoBorder, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("PRIZE POOL", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                Text("${room.betAmount * room.playerCount} 🪙", color = LudoAccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("LOBBY SIZE REQUIRED", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                Text("${room.players.size}/${room.playerCount} Connected", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }

                    // ROSTER GRID
                    Text(
                        text = "ESTABLISHED PEERS (${room.players.size}/${room.playerCount})",
                        fontSize = 11.sp,
                        color = Color.White.copy(0.5f),
                        fontWeight = FontWeight.Bold
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(110.dp)
                    ) {
                        items(room.players) { item ->
                            val col = when (item.colorIndex) {
                                0 -> LudoBlue
                                1 -> LudoRed
                                2 -> LudoGreen
                                3 -> LudoYellow
                                4 -> Color(0xFF9C27B0)
                                else -> Color(0xFFFF9800)
                            }
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                                border = BorderStroke(1.dp, if (item.id == playerProfile.id) col else LudoBorder)
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(col, CircleShape)
                                            .border(1.5.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(item.name.take(1).uppercase(), color = if (item.colorIndex == 3) Color.Black else Color.White, fontWeight = FontWeight.Black, fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, maxLines = 1)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .background(if (item.isConnected) LudoGreen else Color.Gray, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (item.isHost) "Host" else if (item.isReady) "Ready" else "Waiting",
                                                color = if (item.isHost) LudoAccentGold else if (item.isReady) LudoGreen else Color.LightGray,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // CHAT FEED INTERACTIVE BOX
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.Black.copy(0.2f), RoundedCornerShape(12.dp))
                            .border(1.dp, LudoBorder, RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text("LIVE LOBBY DISCUSSION", color = Color.White.copy(0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Divider(color = LudoBorder.copy(0.3f), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(onlineChats) { log ->
                                    Row {
                                        Text("${log.sender}: ", color = LudoAccentGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        Text(log.message, color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }

                            // Quick Emoji Taps
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("👍", "👑", "🎯", "🔥", "😂", "👊").forEach { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(LudoSurfaceNavy, RoundedCornerShape(4.dp))
                                            .border(0.5.dp, LudoBorder, RoundedCornerShape(4.dp))
                                            .clickable { com.example.data.MultiplayerManager.sendLobbyMessage(emoji, true) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 14.sp)
                                    }
                                }
                            }

                            // Input Box
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.foundation.text.BasicTextField(
                                    value = lobbyChatText,
                                    onValueChange = { lobbyChatText = it },
                                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp)
                                        .background(Color.Black.copy(0.4f), RoundedCornerShape(16.dp))
                                        .border(0.5.dp, LudoBorder, RoundedCornerShape(16.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                                Button(
                                    onClick = {
                                        if (lobbyChatText.isNotBlank()) {
                                            com.example.data.MultiplayerManager.sendLobbyMessage(lobbyChatText)
                                            lobbyChatText = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = LudoBlue),
                                    contentPadding = PaddingValues(horizontal = 12.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("SEND", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // ACTION LAUNCH MATCH BTN
                    val isHost = room.hostId == playerProfile.id
                    val everyoneReady = room.players.size >= 2 && room.players.all { it.isReady || it.isHost }

                    if (isHost) {
                        Button(
                            onClick = { com.example.data.MultiplayerManager.startOnlineMatch() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (everyoneReady) LudoGreen else Color.Gray
                            ),
                            enabled = everyoneReady,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_online_match_btn")
                        ) {
                            Text(
                                text = if (everyoneReady) "START REAL-TIME DUEL" else "WAITING FOR PEERS TO READY CHECK",
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else {
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = LudoBlue.copy(0.4f)),
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("READY & WAITING FOR HOST TO ROLLOUT...", color = Color.LightGray)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // --- 3. ACTIVE GAMEPLAY BOARD SCREEN ---
            else if (roomState!!.status == "PLAYING") {
                val room = roomState!!
                val activePlayer = room.players.find { it.colorIndex == room.currentPlayerIndex }
                val isMyTurn = activePlayer != null && activePlayer.id == playerProfile.id
                val myColorIdx = room.players.find { it.id == playerProfile.id }?.colorIndex ?: 0

                val configs = room.players.map { player ->
                    val matchingColor = when (player.colorIndex) {
                        0 -> LudoBlue
                        1 -> LudoRed
                        2 -> LudoGreen
                        3 -> LudoYellow
                        4 -> Color(0xFF9C27B0)
                        else -> Color(0xFFFF9800)
                    }
                    val entrance = when (player.colorIndex) {
                        0 -> 1
                        1 -> 14
                        2 -> 27
                        3 -> 40
                        4 -> 14
                        else -> 40
                    }
                    LudoPlayerConfig(
                        index = player.colorIndex,
                        name = player.name,
                        color = matchingColor,
                        type = if (player.id == playerProfile.id) LudoPlayerType.HUMAN else LudoPlayerType.AI,
                        difficulty = LudoDifficulty.HARD,
                        startCell = entrance,
                        homeTrackStart = 50
                    )
                }

                val convertedTokens = room.tokens.map { tok ->
                    val tokenColor = configs.find { it.index == tok.playerId }?.color ?: Color.White
                    LudoToken(
                        playerId = tok.playerId,
                        tokenId = tok.tokenId,
                        position = tok.position,
                        color = tokenColor
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Turn metadata panel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(LudoSurfaceNavy, RoundedCornerShape(8.dp))
                            .border(0.5.dp, LudoBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("TURN: ${activePlayer?.name ?: "Opponent"}", color = LudoAccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(room.actionLog, color = Color.White, fontSize = 10.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }

                        // Connection badge
                        if (activePlayer != null && !activePlayer.isConnected) {
                            Box(
                                modifier = Modifier
                                    .background(LudoRed, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("LAGGING / BOT PLAN", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(LudoGreen.copy(0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("SYNCED", color = LudoGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // THE LUDO BOARD CANVAS BOX
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .border(4.dp, LudoBorder.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .testTag("ludo_game_board")
                    ) {
                        val boardWidth = maxWidth
                        val cellSize = boardWidth / 15f

                        // Draw track cells in grid form
                        Column {
                            for (row in 0..14) {
                                Row {
                                    for (col in 0..14) {
                                        val cellColor = when {
                                            // Base corner areas (represented by semi light colors)
                                            row in 0..5 && col in 0..5 -> LudoBlue.copy(0.12f)
                                            row in 0..5 && col in 9..14 -> LudoRed.copy(0.12f)
                                            row in 9..14 && col in 9..14 -> LudoGreen.copy(0.12f)
                                            row in 9..14 && col in 0..5 -> LudoYellow.copy(0.12f)
                                            
                                            // Triangles center handled by overlay Canvas
                                            row in 6..8 && col in 6..8 -> Color.Transparent
                                            
                                            // Colored home tracks
                                            row == 7 && col in 1..5 -> LudoBlue
                                            row == 7 && col in 9..13 -> LudoGreen
                                            col == 7 && row in 1..5 -> LudoRed
                                            col == 7 && row in 9..13 -> LudoYellow
                                            
                                            // Entering cells
                                            (row == 6 && col == 1) -> LudoBlue.copy(0.4f)
                                            (row == 8 && col == 13) -> LudoGreen.copy(0.4f)
                                            (row == 1 && col == 8) -> LudoRed.copy(0.4f)
                                            (row == 13 && col == 6) -> LudoYellow.copy(0.4f)
                                            
                                            else -> Color.White
                                        }
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(cellSize)
                                                .border(0.5.dp, Color.Black.copy(0.12f))
                                                .background(cellColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Draw Star icon on safe cells (supporting both traditional & screenshot locations)
                                            if ((row == 6 && col == 2) || (row == 8 && col == 12) || (row == 2 && col == 8) || (row == 12 && col == 6) ||
                                                (row == 8 && col == 2) || (row == 6 && col == 12) || (row == 2 && col == 6) || (row == 12 && col == 8)) {
                                                
                                                val starColor = when {
                                                    (row == 2 && col == 6) -> LudoRed
                                                    (row == 12 && col == 8) -> LudoYellow
                                                    (row == 8 && col == 2) -> LudoBlue
                                                    (row == 6 && col == 12) -> LudoGreen
                                                    else -> Color.LightGray
                                                }
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = starColor.copy(alpha = 0.9f),
                                                    modifier = Modifier.size(cellSize * 0.72f)
                                                )
                                            }
                                            // Draw direction entering arrows exactly matching the screenshot
                                            if (row == 7 && col == 0) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowForward,
                                                    contentDescription = null,
                                                    tint = LudoBlue.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(cellSize * 0.72f)
                                                )
                                            } else if (row == 7 && col == 14) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowBack,
                                                    contentDescription = null,
                                                    tint = LudoGreen.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(cellSize * 0.72f)
                                                )
                                            } else if (row == 0 && col == 7) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDownward,
                                                    contentDescription = null,
                                                    tint = LudoRed.copy(alpha = 0.85f),
                                                    modifier = Modifier.size(cellSize * 0.72f)
                                                )
                                            } else if (row == 14 && col == 7) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowUpward,
                                                    contentDescription = null,
                                                    tint = LudoYellow.copy(alpha = 0.95f),
                                                    modifier = Modifier.size(cellSize * 0.72f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Center Triangles overlay (exactly covering rows 6..8, cols 6..8)
                        Box(
                            modifier = Modifier
                                .offset(x = cellSize * 6, y = cellSize * 6)
                                .size(cellSize * 3)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val cx = w / 2f
                                val cy = h / 2f
                                
                                val leftPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(cx, cy)
                                    lineTo(0f, h)
                                    close()
                                }
                                drawPath(leftPath, LudoBlue)
                                
                                val topPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(cx, cy)
                                    lineTo(w, 0f)
                                    close()
                                }
                                drawPath(topPath, LudoRed)
                                
                                val rightPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(w, 0f)
                                    lineTo(cx, cy)
                                    lineTo(w, h)
                                    close()
                                }
                                drawPath(rightPath, LudoGreen)
                                
                                val bottomPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, h)
                                    lineTo(cx, cy)
                                    lineTo(w, h)
                                    close()
                                }
                                drawPath(bottomPath, LudoYellow)
                                
                                // Beautiful white boundary separation lines
                                drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(0f, 0f), end = androidx.compose.ui.geometry.Offset(w, h), strokeWidth = 2.dp.toPx())
                                drawLine(Color.White, start = androidx.compose.ui.geometry.Offset(0f, h), end = androidx.compose.ui.geometry.Offset(w, 0f), strokeWidth = 2.dp.toPx())
                                
                                // Draw outer rectangle boundaries
                                drawRect(Color.White, size = size, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                            }
                        }

                        // Core Corner Base Yards (re-designed rounded-square containers with glossy slots)
                        val yardSize = cellSize * 6
                        // Blue Home
                        Box(
                            modifier = Modifier
                                .size(yardSize)
                                .background(LudoBlue, RoundedCornerShape(16.dp))
                                .padding(cellSize * 0.7f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(cellSize * 0.35f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoBlue, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoBlue, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoBlue, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoBlue, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                }
                            }
                        }
                        // Red Home
                        Box(
                            modifier = Modifier
                                .offset(x = yardSize + cellSize * 3)
                                .size(yardSize)
                                .background(LudoRed, RoundedCornerShape(16.dp))
                                .padding(cellSize * 0.7f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(cellSize * 0.35f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoRed, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoRed, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoRed, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoRed, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                }
                            }
                        }
                        // Yellow Home
                        Box(
                            modifier = Modifier
                                .offset(y = yardSize + cellSize * 3)
                                .size(yardSize)
                                .background(LudoYellow, RoundedCornerShape(16.dp))
                                .padding(cellSize * 0.7f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(cellSize * 0.35f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoYellow, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoYellow, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoYellow, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoYellow, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                }
                            }
                        }
                        // Green Home
                        Box(
                            modifier = Modifier
                                .offset(x = yardSize + cellSize * 3, y = yardSize + cellSize * 3)
                                .size(yardSize)
                                .background(LudoGreen, RoundedCornerShape(16.dp))
                                .padding(cellSize * 0.7f),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White, RoundedCornerShape(16.dp))
                                    .padding(cellSize * 0.35f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceEvenly,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoGreen, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoGreen, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoGreen, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                        Box(modifier = Modifier.size(cellSize * 1.15f).background(LudoGreen, CircleShape).border(1.5.dp, Color.White, CircleShape).shadow(1.dp, CircleShape))
                                    }
                                }
                            }
                        }

                        // Render pieces dynamically
                        convertedTokens.forEach { token ->
                            val isTokenSelectable = isMyTurn &&
                                    token.playerId == myColorIdx &&
                                    room.diceRolled &&
                                    canMoveToken(token, room.diceValue)

                            val pulseTransition = rememberInfiniteTransition("pulse_online")
                            val pulseScale by pulseTransition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "pulse"
                            )

                            var floatCoords = getTokenCoords(token, configs)

                            // Apply overlay shifts
                            val overlapping = convertedTokens.filter {
                                it.position != -1 && it != token &&
                                getTokenCoordsOnly(it, configs) == getTokenCoordsOnly(token, configs)
                            }
                            if (overlapping.isNotEmpty() && token.position != -1) {
                                val offsetShift = when (token.playerId) {
                                    0 -> Pair(-0.16f, -0.16f)
                                    1 -> Pair(0.16f, -0.16f)
                                    2 -> Pair(0.16f, 0.16f)
                                    3 -> Pair(-0.16f, 0.16f)
                                    4 -> Pair(0f, -0.16f)
                                    else -> Pair(0f, 0.16f)
                                }
                                floatCoords = Pair(floatCoords.first + offsetShift.first, floatCoords.second + offsetShift.second)
                            }

                            if (floatCoords.first >= 0f) {
                                Box(
                                    modifier = Modifier
                                        .offset(
                                            x = cellSize * floatCoords.second,
                                            y = cellSize * floatCoords.first
                                        )
                                        .size(cellSize)
                                        .padding(3.dp)
                                        .scale(if (isTokenSelectable) pulseScale else 1.0f)
                                        .background(token.color, CircleShape)
                                        .border(
                                            if (isTokenSelectable) 2.5.dp else 1.5.dp,
                                            if (isTokenSelectable) LudoAccentGold else Color.White,
                                            CircleShape
                                        )
                                        .clickable(enabled = isTokenSelectable) {
                                            com.example.data.MultiplayerManager.performMoveTokenCommand(token.tokenId)
                                        }
                                )
                            }
                        }
                    }

                    // --- TURN ACTIONS & DICE BLOCK ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("DICE AMOUNT", color = Color.White.copy(0.6f), fontSize = 10.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(LudoSurfaceNavy, RoundedCornerShape(6.dp))
                                        .border(1.5.dp, if (isMyTurn) LudoAccentGold else LudoBorder, RoundedCornerShape(6.dp))
                                        .clickable(enabled = isMyTurn && !room.diceRolled) {
                                            com.example.data.MultiplayerManager.performRollDiceCommand()
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = room.diceValue.toString(),
                                        color = if (isMyTurn) LudoAccentGold else Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    )
                                }
                                if (isMyTurn && !room.diceRolled) {
                                    Button(
                                        onClick = { com.example.data.MultiplayerManager.performRollDiceCommand() },
                                        colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .padding(start = 10.dp)
                                            .height(36.dp)
                                            .testTag("btn_roll_dice_online"),
                                        border = BorderStroke(1.dp, Color.White.copy(0.5f))
                                    ) {
                                        Icon(Icons.Filled.Casino, null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("ROLL DICE 🎲", fontWeight = FontWeight.Black, fontSize = 11.sp)
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isMyTurn) {
                                                if (room.diceRolled) "SELECT A PIECE TO WALK!" else "YOUR TURN! ROLL!"
                                            } else {
                                                "Waiting for opponent..."
                                            },
                                            color = if (isMyTurn) LudoAccentGold else Color.LightGray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Fast Macro Taunts
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Aha!", "Oh No!", "Fast! ⏰").forEach { taunt ->
                                Button(
                                    onClick = { com.example.data.MultiplayerManager.sendLobbyMessage(taunt) },
                                    colors = ButtonDefaults.buttonColors(containerColor = LudoSurfaceNavy),
                                    border = BorderStroke(0.5.dp, LudoBorder),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(taunt, color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // Live Battle Logs Column
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.Black.copy(0.3f), RoundedCornerShape(8.dp))
                            .border(1.dp, LudoBorder, RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(onlineChats) { log ->
                                Row {
                                    Text("${log.sender}: ", color = LudoAccentGold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                    Text(log.message, color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // --- 4. CHAMPIONSHIP FINISHED STATE ---
            else if (roomState!!.status == "FINISHED") {
                val room = roomState!!
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.EmojiEvents, null, tint = LudoAccentGold, modifier = Modifier.size(80.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "CHAMPIONSHIP CONCLUDED!",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val myColorIdx = room.players.find { it.id == playerProfile.id }?.colorIndex ?: 0
                    val wonIndex = room.rankings.indexOf(myColorIdx)
                    val rewardMultiplier = when (wonIndex) {
                        0 -> 2.0f   // Winner gets double the bet amount!
                        1 -> 1.0f   // 2nd place gets entry fee back
                        else -> 0.0f
                    }
                    val winningsAmount = (room.betAmount * rewardMultiplier).toInt()

                    Text(
                        text = if (wonIndex == 0) "CORE VICTORY! GRAND PRIZE CLAIMABLE." else if (wonIndex == 1) "Runner Up finish!" else "Keep trying. Practice makes perfect!",
                        color = if (wonIndex == 0) LudoGreen else Color.LightGray,
                        fontSize = 14.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    LudoCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("RANKINGS PODIUM", color = LudoAccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            room.rankings.forEachIndexed { i, idx ->
                                val pName = room.players.find { it.colorIndex == idx }?.name ?: "Opponent"
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("${i + 1}. $pName", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(if (i == 0) "Winner 👑" else if (i == 1) "Second" else "Third", color = if (i == 0) LudoAccentGold else Color.LightGray, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    var claimedState by remember { mutableStateOf(false) }

                    Button(
                        onClick = {
                            if (!claimedState) {
                                claimedState = true
                                LudoMasterRepository.completeMatch(
                                    won = wonIndex == 0,
                                    xpEarned = if (wonIndex == 0) 400 else 100,
                                    coinsEarned = if (wonIndex == 0) winningsAmount else -room.betAmount
                                )
                                android.widget.Toast.makeText(context, "Awarded $winningsAmount Coins and XP claimed!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                            com.example.data.MultiplayerManager.leaveRoom()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LudoGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(if (claimedState) "RETURN TO HUB" else "CLAIM MULTIPLAYER REWARDS", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// --- 9. TOURNAMENT SCREEN ---
@Composable
fun TournamentScreen(onBack: () -> Unit) {
    val tournaments by LudoMasterRepository.tournaments.collectAsState()
    var purchaseMessage by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleGameHeader(title = "Grand Arenas", onBack = onBack)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = "STAKE TOURNAMENT BRACKETS",
                    fontSize = 11.sp,
                    color = Color.White.copy(0.5f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(tournaments) { tour ->
                        LudoCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = if (tour.isJoined) LudoGreen else LudoBorder
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(tour.name, fontWeight = FontWeight.Black, color = Color.White, fontSize = 16.sp)
                                        if (tour.isJoined) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(modifier = Modifier.background(LudoGreen, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                                Text("REGISTERED", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text("First Prize: ${tour.prizePool} GC", color = LudoAccentGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Active seats: ${tour.participantsCount} / ${tour.maxParticipants}", color = Color.White.copy(0.6f), fontSize = 11.sp)
                                }
                                
                                Button(
                                    onClick = {
                                        val joined = LudoMasterRepository.joinTournament(tour.id)
                                        purchaseMessage = if (joined) {
                                            "Registered in ${tour.name}! 500 Coins paid."
                                        } else {
                                            "Insufficient coins! Add more on home header."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (tour.isJoined) LudoBorder else LudoAccentGold
                                    ),
                                    enabled = !tour.isJoined,
                                    modifier = Modifier.testTag("join_tournament_btn_${tour.id}")
                                ) {
                                    Text(
                                        text = if (tour.isJoined) "LOCKED" else "ENTRY ${tour.entryFee}",
                                        color = if (tour.isJoined) Color.White.copy(0.6f) else Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (purchaseMessage != null) {
        AlertDialog(
            onDismissRequest = { purchaseMessage = null },
            confirmButton = {
                TextButton(onClick = { purchaseMessage = null }) {
                    Text("OK", color = LudoAccentGold)
                }
            },
            title = { Text("Bracket Registration", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(purchaseMessage ?: "", color = Color.LightGray) },
            containerColor = LudoSurfaceNavy
        )
    }
}

// --- 10. COINS & GEMS STORE SCREEN ---
@Composable
fun StoreScreen(onBack: () -> Unit) {
    val storeItems by LudoMasterRepository.storeItems.collectAsState()
    var promptInfoMsg by remember { mutableStateOf<String?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleGameHeader(title = "Item Shop", onBack = onBack)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(
                    text = "ACQUIRE ECONOMIC COINS & SHIPS",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(storeItems) { item ->
                        LudoCard(
                            modifier = Modifier.fillMaxWidth().testTag("store_item_${item.id}"),
                            borderColor = if (item.isPurchased) LudoGreen else LudoBorder
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                                Icon(
                                    imageVector = if (item.type == "coins") Icons.Filled.Stars else if (item.type == "gems") Icons.Filled.Favorite else Icons.Filled.Palette,
                                    contentDescription = null,
                                    tint = if (item.type == "coins") LudoAccentGold else if (item.type == "gems") LudoAccentGem else LudoBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(item.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = if (item.rewardAmount > 0) "${item.rewardAmount} Units" else "Item Skin",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(0.6f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val outcome = LudoMasterRepository.purchaseStoreItem(item.id)
                                        promptInfoMsg = outcome
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (item.isPurchased) LudoBorder else LudoAccentGold),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    enabled = !item.isPurchased,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (item.isPurchased) "EQUIPPED" else "BUY (${item.costAmount})",
                                        fontSize = 10.sp,
                                        color = if (item.isPurchased) Color.White.copy(0.6f) else Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (promptInfoMsg != null) {
        AlertDialog(
            onDismissRequest = { promptInfoMsg = null },
            confirmButton = {
                TextButton(onClick = { promptInfoMsg = null }) {
                    Text("OK", color = LudoAccentGold)
                }
            },
            title = { Text("Store transaction", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(promptInfoMsg ?: "", color = Color.LightGray) },
            containerColor = LudoSurfaceNavy
        )
    }
}

// --- 11. FRIENDS LIST SCREEN ---
@Composable
fun FriendsScreen(onBack: () -> Unit) {
    val friends by com.example.data.FriendsManager.friendsList.collectAsState()
    val pendingRequests by com.example.data.FriendsManager.pendingRequests.collectAsState()
    val friendsLogs by com.example.data.FriendsManager.friendsLogs.collectAsState()
    val currentRoom by com.example.data.MultiplayerManager.currentRoom.collectAsState()
    
    var searchInputName by remember { mutableStateOf("") }
    var friendsAlertMsg by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = FRIENDS, 1 = REQUESTS, 2 = LOGS
    
    val avatarColors = listOf(LudoRed, LudoBlue, LudoGreen, LudoYellow, LudoAccentGold)
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleGameHeader(title = "Social Hub", onBack = onBack)
            
            // Sub-navigation Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabLabels = listOf("Friends (${friends.size})", "Requests (${pendingRequests.size})", "Status Logs")
                tabLabels.forEachIndexed { index, label ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSelected) LudoAccentGold else LudoSurfaceNavy,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) LudoAccentGold else LudoBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // --- FRIENDS TAB ---
                        // Add Friend input plate
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = searchInputName,
                                onValueChange = { searchInputName = it },
                                placeholder = { Text("Friend Nickname", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LudoAccentGold,
                                    unfocusedBorderColor = LudoBorder,
                                    cursorColor = LudoAccentGold,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("friend_search_input")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (searchInputName.isNotBlank()) {
                                        com.example.data.FriendsManager.sendFriendRequest(searchInputName) { success, msg ->
                                            friendsAlertMsg = msg
                                            if (success) {
                                                searchInputName = ""
                                            }
                                        }
                                    } else {
                                        friendsAlertMsg = "Please insert a valid nickname."
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold),
                                modifier = Modifier.testTag("add_friend_btn")
                            ) {
                                Text("SEND", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        
                        Text(
                            text = "ACTIVE CREW MEMBERS",
                            fontSize = 11.sp,
                            color = Color.White.copy(0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (friends.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No active friends yet.", color = Color.Gray, fontSize = 13.sp)
                                    Text("Invite or add friends by nickname above!", color = Color.Gray.copy(0.6f), fontSize = 11.sp)
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(friends) { friend ->
                                    LudoCard(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(avatarColors[friend.avatarColorIndex % avatarColors.size], CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = friend.name.firstOrNull()?.toString()?.uppercase() ?: "F",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(friend.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .size(6.dp)
                                                                .background(if (friend.isOnline) LudoGreen else Color.Gray, CircleShape)
                                                        )
                                                    }
                                                    Text("Lvl ${friend.level} • ${friend.coins} Coins", fontSize = 11.sp, color = Color.White.copy(0.5f))
                                                }
                                            }
                                            
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Invite Friend to Lobby if currently in hosting lobby & friend is online!
                                                if (currentRoom != null && friend.isOnline) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(LudoGreen, RoundedCornerShape(8.dp))
                                                            .clickable {
                                                                com.example.data.FriendsManager.inviteFriendToLobby(friend, currentRoom!!.roomId) { success, msg ->
                                                                    friendsAlertMsg = msg
                                                                }
                                                            }
                                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text("INVITE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                
                                                // Remove Friend Action
                                                IconButton(
                                                    onClick = {
                                                        com.example.data.FriendsManager.removeFriend(friend.id) { success ->
                                                            friendsAlertMsg = if (success) "Removed connection with ${friend.name}." else "Error removing friend."
                                                        }
                                                    },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = "Remove Friend",
                                                        tint = LudoRed.copy(0.7f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // --- REQUESTS TAB ---
                        Text(
                            text = "PENDING VISITOR INBOX",
                            fontSize = 11.sp,
                            color = Color.White.copy(0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (pendingRequests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Inbox is clear!", color = Color.Gray, fontSize = 13.sp)
                                    Text("Any pending requests will display right here.", color = Color.Gray.copy(0.6f), fontSize = 11.sp)
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(pendingRequests) { req ->
                                    LudoCard(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(LudoAccentGem, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = req.senderName.firstOrNull()?.toString()?.uppercase() ?: "R",
                                                        color = Color.Black,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(req.senderName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                                    Text("Wants to connect with you", fontSize = 11.sp, color = Color.White.copy(0.5f))
                                                }
                                            }
                                            
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                // Accept button
                                                IconButton(
                                                    onClick = {
                                                        com.example.data.FriendsManager.acceptFriendRequest(req) { success ->
                                                            if (success) {
                                                                friendsAlertMsg = "You are now friends with ${req.senderName}!"
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(LudoGreen, CircleShape)
                                                ) {
                                                    Icon(Icons.Filled.Check, contentDescription = "Accept", tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                                
                                                // Decline button
                                                IconButton(
                                                    onClick = {
                                                        com.example.data.FriendsManager.declineFriendRequest(req) { success ->
                                                            if (success) {
                                                                friendsAlertMsg = "Declined friend request from ${req.senderName}."
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(LudoRed, CircleShape)
                                                ) {
                                                    Icon(Icons.Filled.Close, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // --- LOGS / TRANSACTIONS TAB ---
                        Text(
                            text = "REAL-TIME PIPELINE MONITOR",
                            fontSize = 11.sp,
                            color = Color.White.copy(0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .background(Color.Black, RoundedCornerShape(12.dp))
                                .border(1.dp, LudoBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(friendsLogs) { log ->
                                    Text(
                                        text = log,
                                        color = if (log.contains("error", ignoreCase = true) || log.contains("failed", ignoreCase = true)) LudoRed else LudoAccentGem,
                                        fontSize = 11.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (friendsAlertMsg != null) {
        AlertDialog(
            onDismissRequest = { friendsAlertMsg = null },
            confirmButton = {
                TextButton(onClick = { friendsAlertMsg = null }) {
                    Text("OK", color = LudoAccentGold)
                }
            },
            title = { Text("Friends System", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(friendsAlertMsg ?: "", color = Color.LightGray) },
            containerColor = LudoSurfaceNavy
        )
    }
}

// --- 12. GAME LOBBY DISCUSSION FEED / CHAT DETAILS ---
@Composable
fun ChatScreen(onBack: () -> Unit) {
    // Collect states
    val currentPlayer by LudoMasterRepository.playerState.collectAsState()
    val friends by com.example.data.FriendsManager.friendsList.collectAsState()
    val activeRoom by com.example.data.MultiplayerManager.currentRoom.collectAsState()
    val roomChats by com.example.data.MultiplayerManager.activeChat.collectAsState()
    val myClan by com.example.data.ChatManager.myClan.collectAsState()
    val allClans by com.example.data.ChatManager.allClans.collectAsState()
    val clanMessages by com.example.data.ChatManager.clanMessages.collectAsState()
    val privateFriendId by com.example.data.ChatManager.activeChatFriendId.collectAsState()
    val privateMessages by com.example.data.ChatManager.currentPrivateMessages.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = PRIVATE, 1 = ROOM, 2 = CLANS
    var emojiPickerOpen by remember { mutableStateOf(false) }
    var chatText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val avatarColors = listOf(LudoRed, LudoBlue, LudoGreen, LudoYellow, LudoAccentGold)

    // Clan creation states
    var createClanOpen by remember { mutableStateOf(false) }
    var clanNameInput by remember { mutableStateOf("") }
    var clanTagInput by remember { mutableStateOf("") }
    var clanDescInput by remember { mutableStateOf("") }

    var clanAlertMessage by remember { mutableStateOf<String?>(null) }

    // List of interactive gaming emojis
    val emojiList = listOf("🙂", "😎", "😂", "🔥", "👑", "👍", "😮", "😡", "😭", "🥱", "❤️", "🎯", "🎲", "🎰", "🏆")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Unified Top Header Navigation Plate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        // If private chat is currently drill-down open, go back to contact roster list
                        if (privateFriendId != null) {
                            com.example.data.ChatManager.closePrivateChat()
                        } else {
                            onBack()
                        }
                    },
                    modifier = Modifier
                        .background(LudoSurfaceNavy, CircleShape)
                        .border(1.dp, LudoBorder, CircleShape)
                        .testTag("chat_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (privateFriendId != null) {
                            val activeFriend = friends.find { it.id == privateFriendId }
                            "CHAT WITH ${activeFriend?.name?.uppercase() ?: "FRIEND"}"
                        } else {
                            "COMMUNITY HUB"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LudoAccentGold,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (com.example.data.ChatManager.isSimulationMode) LudoYellow else LudoGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (com.example.data.ChatManager.isSimulationMode) "Local Sandbox Feed" else "Cloud Sync Channel Online",
                            color = Color.White.copy(0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Only show Tabs selection layout if not drill-down deep inside some active private chat
            if (privateFriendId == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabHeaders = listOf(
                        Triple(Icons.Filled.People, "Private Chat", 0),
                        Triple(Icons.Filled.Home, "Room Lobby", 1),
                        Triple(Icons.Filled.Stars, "Clans Elite", 2)
                    )

                    tabHeaders.forEach { (icon, title, index) ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = if (isSelected) LudoAccentGold else LudoSurfaceNavy,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) LudoAccentGold else LudoBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { 
                                    selectedTab = index 
                                    emojiPickerOpen = false
                                    chatText = ""
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Sub-Screen Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (privateFriendId != null) {
                    // 1. ACTIVE PRIVATE CHAT PANEL DRILL-DOWN VIEW!
                    Column(modifier = Modifier.fillMaxSize()) {
                        // User Profile Header Info
                        val activeFriend = friends.find { it.id == privateFriendId }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, LudoBorder),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(avatarColors[(activeFriend?.avatarColorIndex ?: 0) % avatarColors.size], CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = activeFriend?.name?.take(1)?.uppercase() ?: "F",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = activeFriend?.name ?: "Friend",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(if (activeFriend?.isOnline == true) LudoGreen else Color.Gray, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (activeFriend?.isOnline == true) "Active Online" else "Offline",
                                            color = Color.LightGray.copy(0.6f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Messages Ledger
                        LazyColumn(
                            reverseLayout = false,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(Color.Black.copy(0.2f), RoundedCornerShape(12.dp))
                                .border(1.dp, LudoBorder.copy(0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (privateMessages.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillParentMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Filled.FavoriteBorder, null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("No previous messages. Start the chat!", color = Color.Gray, fontSize = 11.sp)
                                        }
                                    }
                                }
                            } else {
                                items(privateMessages) { msg ->
                                    val isMe = msg.senderId == currentPlayer.id
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                    ) {
                                        Column(
                                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                                            modifier = Modifier.widthIn(max = 240.dp)
                                        ) {
                                            if (!isMe) {
                                                Text(msg.senderName, fontSize = 9.sp, color = LudoAccentGold.copy(0.8f), fontWeight = FontWeight.Bold)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = if (isMe) LudoBlue else LudoSurfaceNavy,
                                                        shape = RoundedCornerShape(
                                                            topStart = 12.dp,
                                                            topEnd = 12.dp,
                                                            bottomStart = if (isMe) 12.dp else 0.dp,
                                                            bottomEnd = if (isMe) 0.dp else 12.dp
                                                        )
                                                    )
                                                    .border(
                                                        1.dp,
                                                        if (isMe) LudoBlue else LudoBorder,
                                                        RoundedCornerShape(
                                                            topStart = 12.dp,
                                                            topEnd = 12.dp,
                                                            bottomStart = if (isMe) 12.dp else 0.dp,
                                                            bottomEnd = if (isMe) 0.dp else 12.dp
                                                        )
                                                    )
                                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                                            ) {
                                                if (msg.isEmoji) {
                                                    Text(msg.message, fontSize = 28.sp)
                                                } else {
                                                    Text(msg.message, color = Color.White, fontSize = 13.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Actions bar with input typing and emoji selector
                        ChatActionComposer(
                            text = chatText,
                            onValueChange = { chatText = it },
                            emojiList = emojiList,
                            emojiPickerOpen = emojiPickerOpen,
                            onToggleEmoji = { emojiPickerOpen = !emojiPickerOpen },
                            onSendText = {
                                if (chatText.isNotBlank()) {
                                    com.example.data.ChatManager.sendPrivateMessage(chatText)
                                    chatText = ""
                                    emojiPickerOpen = false
                                }
                            },
                            onSendEmoji = { emoji ->
                                com.example.data.ChatManager.sendPrivateMessage(emoji, isEmoji = true)
                            }
                        )
                    }
                } else {
                    // MAIN TABBED CHANNELS SELECTOR
                    when (selectedTab) {
                        0 -> {
                            // TAB 0: ROSTER OF CONVERSATIONS / FRIENDS LIST
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "SELECT AN ONLINE FRIEND TO SECURELY CHAT",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(0.5f),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                if (friends.isEmpty()) {
                                    Box(
                                        modifier = Modifier.weight(1f).fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Filled.Search, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("You have no friends in your social circle yet.", color = Color.Gray, fontSize = 12.sp)
                                            Text("Add friends inside 'Social Friends' screen!", color = Color.LightGray.copy(0.5f), fontSize = 11.sp)
                                        }
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(friends) { friend ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    com.example.data.ChatManager.openPrivateChatWithFriend(friend.id)
                                                },
                                                colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                                                border = BorderStroke(1.dp, LudoBorder)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .background(avatarColors[friend.avatarColorIndex % avatarColors.size], CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = friend.name.take(1).uppercase(),
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Black,
                                                                fontSize = 14.sp
                                                            )
                                                            // Online pulse dot
                                                            if (friend.isOnline) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(10.dp)
                                                                        .background(LudoGreen, CircleShape)
                                                                        .border(2.dp, LudoSurfaceNavy, CircleShape)
                                                                        .align(Alignment.BottomEnd)
                                                                )
                                                            }
                                                        }
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column {
                                                            Text(text = friend.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                            Text(text = "Lvl ${friend.level} • 🪙 ${friend.coins}", color = Color.White.copy(0.5f), fontSize = 11.sp)
                                                        }
                                                    }

                                                    Icon(
                                                        imageVector = Icons.Filled.Chat,
                                                        contentDescription = "Chat",
                                                        tint = LudoAccentGold,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            // TAB 1: CURRENT MATCH ROOM CHAT LOUNGE
                            if (activeRoom == null) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Block, null, tint = LudoRed, modifier = Modifier.size(54.dp))
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "NOT IN AN ACTIVE ROOM",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Lobby matchmaking chat feeds unlock automatically once you create or join an online multiplayer game battle.",
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            } else {
                                // Real Room Session Chat
                                val room = activeRoom!!
                                Column(modifier = Modifier.fillMaxSize()) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, LudoBorder),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "ROOM LOBBY #${room.roomId}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = LudoAccentGold,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = "Stakes: 🪙 ${room.betAmount} • ${room.players.size}/${room.playerCount} Slots Loaded",
                                                    color = Color.White.copy(0.6f),
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(LudoGreen.copy(0.2f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("ACTIVE MATCH", color = LudoGreen, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                            }
                                        }
                                    }

                                    // Room Chats list
                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(0.2f), RoundedCornerShape(12.dp))
                                            .border(1.dp, LudoBorder.copy(0.4f), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (roomChats.isEmpty()) {
                                            item {
                                                Box(
                                                    modifier = Modifier.fillParentMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("Trash talk begins now! Send lobby messages below.", color = Color.LightGray.copy(0.4f), fontSize = 11.sp)
                                                }
                                            }
                                        } else {
                                            items(roomChats) { chat ->
                                                val isMe = chat.sender == currentPlayer.name
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                                ) {
                                                    Column(
                                                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                                                        modifier = Modifier.widthIn(max = 240.dp)
                                                    ) {
                                                        if (!isMe) {
                                                            Text(chat.sender, fontSize = 9.sp, color = LudoAccentGold, fontWeight = FontWeight.Bold)
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .background(
                                                                    color = if (isMe) LudoBlue else LudoSurfaceNavy,
                                                                    shape = RoundedCornerShape(
                                                                        topStart = 12.dp,
                                                                        topEnd = 12.dp,
                                                                        bottomStart = if (isMe) 12.dp else 0.dp,
                                                                        bottomEnd = if (isMe) 0.dp else 12.dp
                                                                    )
                                                                )
                                                                .border(1.dp, if (isMe) LudoBlue else LudoBorder, RoundedCornerShape(12.dp))
                                                                .padding(8.dp)
                                                        ) {
                                                            if (chat.isEmoji) {
                                                                Text(chat.message, fontSize = 28.sp)
                                                            } else {
                                                                Text(chat.message, color = Color.White, fontSize = 13.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Send Action
                                    ChatActionComposer(
                                        text = chatText,
                                        onValueChange = { chatText = it },
                                        emojiList = emojiList,
                                        emojiPickerOpen = emojiPickerOpen,
                                        onToggleEmoji = { emojiPickerOpen = !emojiPickerOpen },
                                        onSendText = {
                                            if (chatText.isNotBlank()) {
                                                com.example.data.MultiplayerManager.sendLobbyMessage(chatText, isEmoji = false)
                                                chatText = ""
                                                emojiPickerOpen = false
                                            }
                                        },
                                        onSendEmoji = { emoji ->
                                            com.example.data.MultiplayerManager.sendLobbyMessage(emoji, isEmoji = true)
                                        }
                                    )
                                }
                            }
                        }
                        2 -> {
                            // TAB 2: CLANS LOBBY AND DISCUSSIONS
                            if (myClan == null) {
                                // User needs to found or join a clan!
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Found a Clan Toggle switcher
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (createClanOpen) "FOUND A NEW GUILD" else "ALL AVAILABLE CLANS",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(0.5f)
                                        )

                                        Button(
                                            onClick = { createClanOpen = !createClanOpen },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (createClanOpen) LudoRed else LudoAccentGold),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (createClanOpen) "SEE AVAILABLE" else "+ FOUND CLAN (-1K Coins)",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }

                                    if (createClanOpen) {
                                        // FOUND CLAN FORM
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                                            shape = RoundedCornerShape(16.dp),
                                            border = BorderStroke(1.dp, LudoBorder),
                                            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                Text("Clan Name", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                OutlinedTextField(
                                                    value = clanNameInput,
                                                    onValueChange = { clanNameInput = it },
                                                    placeholder = { Text("E.g. Table Knights", color = Color.White.copy(0.3f)) },
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = LudoAccentGold, unfocusedBorderColor = LudoBorder),
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                Text("Clan Tag (Max 5 upper letters)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                OutlinedTextField(
                                                    value = clanTagInput,
                                                    onValueChange = { if (it.length <= 5) clanTagInput = it },
                                                    placeholder = { Text("E.g. KNIGHT", color = Color.White.copy(0.3f)) },
                                                    singleLine = true,
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = LudoAccentGold, unfocusedBorderColor = LudoBorder),
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                Text("Motto / Description", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                OutlinedTextField(
                                                    value = clanDescInput,
                                                    onValueChange = { clanDescInput = it },
                                                    placeholder = { Text("Join the ultimate dice high rollers!", color = Color.White.copy(0.3f)) },
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = LudoAccentGold, unfocusedBorderColor = LudoBorder),
                                                    modifier = Modifier.fillMaxWidth().height(80.dp)
                                                )

                                                Button(
                                                    onClick = {
                                                        if (clanNameInput.isBlank() || clanTagInput.isBlank() || clanDescInput.isBlank()) {
                                                            clanAlertMessage = "Please complete all clan registration parameters."
                                                        } else {
                                                            com.example.data.ChatManager.createClan(
                                                                clanNameInput,
                                                                clanTagInput,
                                                                clanDescInput
                                                            ) { success, msg ->
                                                                clanAlertMessage = msg
                                                                if (success) {
                                                                    clanNameInput = ""
                                                                    clanTagInput = ""
                                                                    clanDescInput = ""
                                                                    createClanOpen = false
                                                                }
                                                            }
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = LudoGreen),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth().testTag("confirm_create_clan")
                                                ) {
                                                    Text("Deploy Clan (Costs 🪙 1,000 Coins)", color = Color.White, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    } else {
                                        // CLANS DISCOVERY ROSTER LIST
                                        if (allClans.isEmpty()) {
                                            Box(
                                                modifier = Modifier.weight(1f).fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("Discover feed is looking clear. Found the first clan!", color = Color.Gray, fontSize = 11.sp)
                                            }
                                        } else {
                                            LazyColumn(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                items(allClans) { clan ->
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                                                        border = BorderStroke(1.dp, LudoBorder)
                                                    ) {
                                                        Column(modifier = Modifier.padding(12.dp)) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Text(
                                                                        text = "[${clan.tag}]",
                                                                        color = LudoAccentGold,
                                                                        fontWeight = FontWeight.Black,
                                                                        fontSize = 14.sp
                                                                    )
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text(
                                                                        text = clan.name,
                                                                        color = Color.White,
                                                                        fontWeight = FontWeight.ExtraBold,
                                                                        fontSize = 14.sp
                                                                    )
                                                                }
                                                                Text("👥 ${clan.memberCount} members", color = Color.LightGray.copy(0.6f), fontSize = 11.sp)
                                                            }
                                                            Spacer(modifier = Modifier.height(4.dp))
                                                            Text(clan.description, color = Color.White.copy(0.7f), fontSize = 11.sp, maxLines = 2)

                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Button(
                                                                onClick = {
                                                                    com.example.data.ChatManager.joinClan(clan.id) { success, msg ->
                                                                        clanAlertMessage = msg
                                                                    }
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = LudoBlue),
                                                                shape = RoundedCornerShape(8.dp),
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                Text("ENLIST & JOIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // USER IS IN A CLAN -> DISPLAY CLAN INFO & REAL-TIME CLAN CHAT!
                                val clan = myClan!!
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Clan Status Header Panel
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, LudoBorder),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("[${clan.tag}]", color = LudoAccentGold, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(clan.name, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                                                }

                                                IconButton(
                                                    onClick = {
                                                        com.example.data.ChatManager.leaveClan { success, msg ->
                                                            clanAlertMessage = msg
                                                        }
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Filled.Close, null, tint = LudoRed)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(clan.description, color = Color.LightGray, fontSize = 11.sp)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Owner: ${clan.ownerName} • Total Clan Registry: ${clan.memberCount} Rivals Active", color = Color.White.copy(0.4f), fontSize = 9.sp)
                                        }
                                    }

                                    // Clan real-time messages
                                    LazyColumn(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(0.2f), RoundedCornerShape(12.dp))
                                            .border(1.dp, LudoBorder.copy(0.4f), RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (clanMessages.isEmpty()) {
                                            item {
                                                Box(
                                                    modifier = Modifier.fillParentMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("Silence rules. Found the chat by typing below!", color = Color.Gray, fontSize = 11.sp)
                                                }
                                            }
                                        } else {
                                            items(clanMessages) { msg ->
                                                val isMe = msg.senderId == currentPlayer.id
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                                ) {
                                                    Column(
                                                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                                                        modifier = Modifier.widthIn(max = 240.dp)
                                                    ) {
                                                        if (!isMe) {
                                                            Text(msg.senderName, fontSize = 9.sp, color = LudoAccentGold, fontWeight = FontWeight.Bold)
                                                        }
                                                        Box(
                                                            modifier = Modifier
                                                                .background(
                                                                    color = if (isMe) LudoBlue else LudoSurfaceNavy,
                                                                    shape = RoundedCornerShape(
                                                                        topStart = 12.dp,
                                                                        topEnd = 12.dp,
                                                                        bottomStart = if (isMe) 12.dp else 0.dp,
                                                                        bottomEnd = if (isMe) 0.dp else 12.dp
                                                                    )
                                                                )
                                                                .border(1.dp, if (isMe) LudoBlue else LudoBorder, RoundedCornerShape(12.dp))
                                                                .padding(8.dp)
                                                        ) {
                                                            if (msg.isEmoji) {
                                                                Text(msg.message, fontSize = 28.sp)
                                                            } else {
                                                                Text(msg.message, color = Color.White, fontSize = 13.sp)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Action send composer
                                    ChatActionComposer(
                                        text = chatText,
                                        onValueChange = { chatText = it },
                                        emojiList = emojiList,
                                        emojiPickerOpen = emojiPickerOpen,
                                        onToggleEmoji = { emojiPickerOpen = !emojiPickerOpen },
                                        onSendText = {
                                            if (chatText.isNotBlank()) {
                                                com.example.data.ChatManager.sendClanMessage(chatText, isEmoji = false)
                                                chatText = ""
                                                emojiPickerOpen = false
                                            }
                                        },
                                        onSendEmoji = { emoji ->
                                            com.example.data.ChatManager.sendClanMessage(emoji, isEmoji = true)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (clanAlertMessage != null) {
        AlertDialog(
            onDismissRequest = { clanAlertMessage = null },
            confirmButton = {
                Button(
                    onClick = { clanAlertMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold)
                ) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Clans Message", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text(clanAlertMessage ?: "", color = Color.LightGray) },
            containerColor = LudoSurfaceNavy,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun ChatActionComposer(
    text: String,
    onValueChange: (String) -> Unit,
    emojiList: List<String>,
    emojiPickerOpen: Boolean,
    onToggleEmoji: () -> Unit,
    onSendText: () -> Unit,
    onSendEmoji: (String) -> Unit
) {
    Column {
        // Toggleable Emoji list tray row
        AnimatedVisibility(
            visible = emojiPickerOpen,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LudoSurfaceNavy, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .border(1.dp, LudoBorder, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                emojiList.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Black.copy(0.2f), CircleShape)
                            .clickable { onSendEmoji(emoji) }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 20.sp)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleEmoji,
                modifier = Modifier
                    .background(if (emojiPickerOpen) LudoAccentGold else LudoSurfaceNavy, CircleShape)
                    .border(1.dp, LudoBorder, CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Face,
                    contentDescription = "Emojis",
                    tint = if (emojiPickerOpen) Color.Black else Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = text,
                onValueChange = onValueChange,
                placeholder = { Text("Write chat details...", color = Color.White.copy(0.5f), fontSize = 13.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LudoAccentGold,
                    unfocusedBorderColor = LudoBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("hub_chat_input_text")
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSendText,
                modifier = Modifier
                    .background(LudoAccentGold, CircleShape)
                    .size(44.dp)
                    .testTag("hub_send_chat_btn")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black)
            }
        }
    }
}

// --- 13. SETTINGS & SCHEMATIC CONFIG SCREEN ---
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAdMob: () -> Unit = {},
    onNavigateToVip: () -> Unit = {}
) {
    val sound by LudoMasterRepository.soundEnabled.collectAsState()
    val music by LudoMasterRepository.musicEnabled.collectAsState()
    val vibration by LudoMasterRepository.vibrationEnabled.collectAsState()
    val darkTheme by LudoMasterRepository.darkTheme.collectAsState()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleGameHeader(title = "App Config", onBack = onBack)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "AURA SOUND & GRAPHICS CONFIG",
                    fontSize = 11.sp,
                    color = Color.White.copy(0.5f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                
                // Sound Toggle Row
                LudoCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (sound) Icons.Filled.VolumeUp else Icons.Filled.VolumeMute,
                                contentDescription = null,
                                tint = LudoAccentGold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("SOUND EFFECTS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Dice clinks, token steps", color = Color.White.copy(0.5f), fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = sound,
                            onCheckedChange = { LudoMasterRepository.setSoundEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = LudoAccentGold),
                            modifier = Modifier.testTag("settings_sound_switch")
                        )
                    }
                }
                
                // Music Toggle Row
                LudoCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (music) Icons.Filled.MusicNote else Icons.Filled.MusicOff,
                                contentDescription = null,
                                tint = LudoAccentGold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("BACKGROUND MUSIC", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Ambient relaxing synth loops", color = Color.White.copy(0.5f), fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = music,
                            onCheckedChange = { LudoMasterRepository.setMusicEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = LudoAccentGold),
                            modifier = Modifier.testTag("settings_music_switch")
                        )
                    }
                }
                
                // Vibration Row
                LudoCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TouchApp,
                                contentDescription = null,
                                tint = LudoAccentGold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("HAPTIC VIBRATION", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Dice shaker tremors & captures", color = Color.White.copy(0.5f), fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = vibration,
                            onCheckedChange = { LudoMasterRepository.setVibrationEnabled(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = LudoAccentGold)
                        )
                    }
                }
                
                // Theme Toggle Row
                LudoCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = null,
                                tint = LudoAccentGold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("DARK THEME BACKPLATE", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Deep space eye-friendly skin", color = Color.White.copy(0.5f), fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { LudoMasterRepository.toggleTheme() },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = LudoAccentGold),
                            modifier = Modifier.testTag("theme_toggle_switch")
                        )
                    }
                }
                
                 // Notifications center button
                LudoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToNotifications() }
                        .testTag("settings_notifications_tile")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = null,
                                tint = LudoAccentGold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("NOTIFICATIONS HUB", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Configure FCM, view activity log and simulations", color = Color.White.copy(0.5f), fontSize = 11.sp)
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Navigate",
                            tint = Color.White.copy(0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                // VIP Membership center tile
                LudoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToVip() }
                        .testTag("settings_vip_tile")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Stars,
                                contentDescription = null,
                                tint = LudoAccentGold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("VIP ROYALE CLUB", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Configure avatar frames, dices & double reward claims!", color = Color.White.copy(0.5f), fontSize = 11.sp)
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Navigate",
                            tint = Color.White.copy(0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // AdMob monetization center
                LudoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAdMob() }
                        .testTag("settings_admob_tile")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.MonetizationOn,
                                contentDescription = null,
                                tint = LudoAccentGold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("ADMOB CAMPAIGNS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Play Rewarded Video for 1.5K Coins or test Banner ads", color = Color.White.copy(0.5f), fontSize = 11.sp)
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Navigate",
                            tint = Color.White.copy(0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Rules and logs details
                LudoCard(modifier = Modifier.fillMaxWidth()) {
                    Text("TERMS OF RESPONSIBLE PLAY", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Ludo Master is built as an interactive offline practice and multiplayer arcade game simulator. It does not handle actual real money wagering, prizes, or commercial betting of physical assets.",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
                
                Button(
                    onClick = {
                        LudoMasterRepository.setSessionState("LOGOUT")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LudoRed),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("LOG OUT SECURELY", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 14. REWARDS SCREEN (WITH ACTIVE SPIN WHEEL & CHECKINS) ---
@Composable
fun RewardsScreen(onBack: () -> Unit) {
    val daysClaimed by LudoMasterRepository.dailyRewardDaysClaimed.collectAsState()
    val wheelSpinMsg by LudoMasterRepository.luckyWheelSpinningResult.collectAsState()
    
    // Spinning animation variables
    var currentSpinDegree by remember { mutableFloatStateOf(0f) }
    var isSpinningNow by remember { mutableStateOf(false) }
    val animatedDegrees = animateFloatAsState(
        targetValue = currentSpinDegree,
        animationSpec = tween(
            durationMillis = 2500,
            easing = CubicBezierEasing(0.1f, 0.8f, 0.2f, 1f)
        ),
        label = "WheelSpin"
    )
    val scope = rememberCoroutineScope()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SimpleGameHeader(title = "Daily Prizes", onBack = onBack)
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                // --- CUSTOM CANVAS LUCKY SPIN WHEEL ---
                Text(
                    text = "LUCKY WHEEL OF FORTUNE",
                    color = LudoAccentGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Segmented color wheel using Canvas drawing
                    Canvas(
                        modifier = Modifier
                            .size(190.dp)
                            .rotate(animatedDegrees.value)
                    ) {
                        val segmentAngle = 360f / 6f
                        val colors = listOf(LudoRed, LudoBlue, LudoGreen, LudoYellow, LudoAccentGold, Color(0xFF9C27B0))
                        
                        for (i in 0 until 6) {
                            drawArc(
                                color = colors[i],
                                startAngle = i * segmentAngle,
                                sweepAngle = segmentAngle,
                                useCenter = true
                            )
                        }
                        
                        // Outer rim stroke
                        drawCircle(
                            color = Color.White,
                            radius = size.width / 2f,
                            style = Stroke(width = 6f)
                        )
                    }
                    
                    // Spinning pointer marker drawn on top
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.White, CircleShape)
                    )
                    
                    // Spinning pointer needle
                    Box(
                        modifier = Modifier
                            .offset(y = (-96).dp)
                            .size(14.dp, 24.dp)
                            .background(Color.White, RoundedCornerShape(2.dp))
                    )
                    
                    // Center spin activator button
                    Button(
                        onClick = {
                            if (!isSpinningNow) {
                                isSpinningNow = true
                                val extraSpins = (3..6).random() * 360f
                                val selectedAngleSegment = (0..5).random()
                                val targetAngle = extraSpins + (selectedAngleSegment * 60f)
                                currentSpinDegree = targetAngle
                                
                                scope.launch {
                                    delay(2600)
                                    isSpinningNow = false
                                    // Award prize based on selected angle segment
                                    val prizes = listOf("200 Coins", "5 Gems", "1000 Coins", "50 Coins", "Futuristic Frame", "300 Coins")
                                    val awarded = prizes[selectedAngleSegment]
                                    
                                    // Apply to store states
                                    if (awarded.contains("Coins")) {
                                        val amt = awarded.replace(" Coins", "").toInt()
                                        com.example.data.EconomyManager.recordTransaction(
                                            amount = amt,
                                            source = "DAILY_REWARD",
                                            description = "Lucky Spin Wheel: Awarded $amt gc"
                                        )
                                    } else if (awarded.contains("Gems")) {
                                        val amt = awarded.replace(" Gems", "").toInt()
                                        com.example.data.EconomyManager.recordGemsDeduction(
                                            gemsAmount = -amt,
                                            coinsAwarded = 0,
                                            source = "DAILY_REWARD",
                                            description = "Lucky Spin Wheel: Awarded Promo Gems"
                                        ) {}
                                    }
                                    
                                    LudoMasterRepository.claimDailyDay(99, 100) // Trigger dialog alert
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = CircleShape,
                        border = BorderStroke(2.dp, LudoAccentGold),
                        modifier = Modifier
                            .size(54.dp)
                            .testTag("rewards_circle_spin_btn")
                    ) {
                        Text(
                            text = "SPIN",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Text(
                    text = "DAILY CALENDAR LOG",
                    fontSize = 11.sp,
                    color = Color.White.copy(0.5f),
                    fontWeight = FontWeight.Bold
                )
                
                // Calendar matrix Day 1 to Day 6 rewards cards
                val rewardDays = listOf(
                    Pair(1, 100),
                    Pair(2, 200),
                    Pair(3, 500),
                    Pair(4, 1000),
                    Pair(5, 1500),
                    Pair(6, 3000)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(120.dp)
                ) {
                    items(rewardDays) { day ->
                        val isClaimed = daysClaimed.contains(day.first)
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isClaimed) LudoGreen.copy(0.15f) else LudoSurfaceNavy
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isClaimed) LudoGreen else LudoBorder
                            ),
                            modifier = Modifier
                                .clickable(enabled = !isClaimed) {
                                    com.example.data.EconomyManager.claimDailyDailyCheckIn(day.first, day.second) { success, msg ->
                                        // Synced
                                    }
                                }
                                .testTag("claim_day_${day.first}_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("DAY ${day.first}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Stars,
                                    contentDescription = null,
                                    tint = if (isClaimed) LudoGreen else LudoAccentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${day.second} GC", color = Color.White.copy(0.7f), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
