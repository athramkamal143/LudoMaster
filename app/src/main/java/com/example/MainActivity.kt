package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.LudoMasterRepository
import com.example.ui.screens.*
import com.example.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Pre-create WebView/Chromium cache subdirectories to prevent chromium simple_file_enumerator opendir errors
        try {
            val wasmDir = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
            if (!wasmDir.exists()) {
                wasmDir.mkdirs()
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to pre-create WebView-WASM directory: ${e.message}")
        }
        
        // Initialize SharedPreferences local storage and load profile stats
        LudoMasterRepository.init(applicationContext)
        com.example.data.MultiplayerManager.initialize(applicationContext)
        com.example.data.FriendsManager.initialize(applicationContext)
        com.example.data.EconomyManager.initialize(applicationContext)
        com.example.data.ChatManager.initialize(applicationContext)
        com.example.data.PushNotificationManager.initialize(applicationContext)
        com.example.data.AdMobManager.init(applicationContext)
        
        // Setup full Edge-to-Edge view boundary handling
        enableEdgeToEdge()
        
        setContent {
            val userThemePreference by LudoMasterRepository.darkTheme.collectAsState()
            
            MyApplicationTheme(darkTheme = userThemePreference) {
                val navController = rememberNavController()
                val sessionState by LudoMasterRepository.currentSessionState.collectAsState()
                val activeInvite by com.example.data.FriendsManager.activeInviteReceived.collectAsState()
                
                if (activeInvite != null) {
                    val invite = activeInvite!!
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { com.example.data.FriendsManager.declineInvitation(invite) {} },
                        title = { androidx.compose.material3.Text("Game Invitation", color = Color.White, fontWeight = FontWeight.Bold) },
                        text = { androidx.compose.material3.Text("${invite.senderName} has invited you to join a real-time battle in Room ${invite.roomId}!", color = Color.LightGray) },
                        confirmButton = {
                            androidx.compose.material3.Button(
                                onClick = {
                                    com.example.data.FriendsManager.acceptInvitation(invite) { success ->
                                        if (success) {
                                            val profile = LudoMasterRepository.playerState.value
                                            com.example.data.MultiplayerManager.joinRoom(profile, invite.roomId) { successJoin, msg ->
                                                if (successJoin) {
                                                    navController.navigate("ONLINE")
                                                } else {
                                                    android.widget.Toast.makeText(applicationContext, msg, android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
                                },
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LudoGreen)
                            ) {
                                androidx.compose.material3.Text("JOIN BATTLE", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(
                                onClick = { com.example.data.FriendsManager.declineInvitation(invite) {} }
                            ) {
                                androidx.compose.material3.Text("DECLINE", color = LudoRed)
                            }
                        },
                        containerColor = LudoSurfaceNavy
                    )
                }
                
                // Keep the root session routing in sync with logouts/logins
                navController.addOnDestinationChangedListener { _, destination, _ ->
                    val routeStr = destination.route
                    if (routeStr != null) {
                        // Monitor game session configurations
                    }
                }
                
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = "SPLASH",
                            modifier = Modifier.fillMaxSize()
                        ) {
                        composable("SPLASH") {
                            SplashScreen(
                                onNavigateFinished = {
                                    val session = LudoMasterRepository.currentSessionState.value
                                    if (session == "GUEST" || session == "GOOGLE" || session == "OTP") {
                                        navController.navigate("HOME") {
                                            popUpTo("SPLASH") { inclusive = true }
                                        }
                                    } else {
                                        navController.navigate("ONBOARDING") {
                                            popUpTo("SPLASH") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        
                        composable("ONBOARDING") {
                            OnboardingScreen(
                                onGetStarted = {
                                    navController.navigate("GUEST_LOGIN")
                                },
                                onPlayNowInstant = {
                                    navController.navigate("HOME") {
                                        popUpTo("ONBOARDING") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable("GUEST_LOGIN") {
                            GuestLoginScreen(
                                onGuestEntered = {
                                    navController.navigate("HOME") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onNavigateToGoogle = {
                                    navController.navigate("GOOGLE_LOGIN") {
                                        popUpTo("GUEST_LOGIN") { inclusive = true }
                                    }
                                },
                                onNavigateToOtp = {
                                    navController.navigate("OTP_LOGIN") {
                                        popUpTo("GUEST_LOGIN") { inclusive = true }
                                    }
                                },
                                onNavigateToEmail = {
                                    navController.navigate("EMAIL_LOGIN") {
                                        popUpTo("GUEST_LOGIN") { inclusive = true }
                                    }
                                },
                                onGoBack = {
                                    navController.navigate("ONBOARDING") {
                                        popUpTo("GUEST_LOGIN") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable("GOOGLE_LOGIN") {
                            GoogleLoginScreen(
                                onGoogleLoggedIn = {
                                    navController.navigate("HOME") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onNavigateToGuest = {
                                    navController.navigate("GUEST_LOGIN") {
                                        popUpTo("GOOGLE_LOGIN") { inclusive = true }
                                    }
                                },
                                onNavigateToOtp = {
                                    navController.navigate("OTP_LOGIN") {
                                        popUpTo("GOOGLE_LOGIN") { inclusive = true }
                                    }
                                },
                                onNavigateToEmail = {
                                    navController.navigate("EMAIL_LOGIN") {
                                        popUpTo("GOOGLE_LOGIN") { inclusive = true }
                                    }
                                },
                                onGoBack = {
                                    navController.navigate("ONBOARDING") {
                                        popUpTo("GOOGLE_LOGIN") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("OTP_LOGIN") {
                            OtpLoginScreen(
                                onOtpLoggedIn = {
                                    navController.navigate("HOME") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onNavigateToGuest = {
                                    navController.navigate("GUEST_LOGIN") {
                                        popUpTo("OTP_LOGIN") { inclusive = true }
                                    }
                                },
                                onNavigateToGoogle = {
                                    navController.navigate("GOOGLE_LOGIN") {
                                        popUpTo("OTP_LOGIN") { inclusive = true }
                                    }
                                },
                                onNavigateToEmail = {
                                    navController.navigate("EMAIL_LOGIN") {
                                        popUpTo("OTP_LOGIN") { inclusive = true }
                                    }
                                },
                                onGoBack = {
                                    navController.navigate("ONBOARDING") {
                                        popUpTo("OTP_LOGIN") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("EMAIL_LOGIN") {
                            EmailLoginScreen(
                                onEmailLoggedIn = {
                                    navController.navigate("HOME") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onNavigateToGuest = {
                                    navController.navigate("GUEST_LOGIN") {
                                        popUpTo("EMAIL_LOGIN") { inclusive = true }
                                    }
                                },
                                onNavigateToGoogle = {
                                    navController.navigate("GOOGLE_LOGIN") {
                                        popUpTo("EMAIL_LOGIN") { inclusive = true }
                                    }
                                },
                                onNavigateToOtp = {
                                    navController.navigate("OTP_LOGIN") {
                                        popUpTo("EMAIL_LOGIN") { inclusive = true }
                                    }
                                },
                                onGoBack = {
                                    navController.navigate("ONBOARDING") {
                                        popUpTo("EMAIL_LOGIN") { inclusive = true }
                                    }
                                }
                            )
                        }
                        
                        composable("HOME") {
                            HomeScreen(
                                onNavigateToScreen = { route ->
                                    navController.navigate(route)
                                }
                            )
                        }
                        
                        composable("PROFILE") {
                            ProfileScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("PLAY_OFFLINE") {
                            PlayOfflineScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("PLAY_ONLINE") {
                            PlayOnlineScreen(
                                onNavigateToChat = { navController.navigate("CHAT") },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("TOURNAMENT") {
                            TournamentScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("STORE") {
                            StoreScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("FRIENDS") {
                            FriendsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("CHAT") {
                            ChatScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("SETTINGS") {
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToNotifications = { navController.navigate("NOTIFICATIONS") },
                                onNavigateToAdMob = { navController.navigate("ADMOB") },
                                onNavigateToVip = { navController.navigate("VIP") }
                            )
                        }
                        
                        composable("VIP") {
                            com.example.ui.screens.VipScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("NOTIFICATIONS") {
                            NotificationsScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToScreen = { route -> navController.navigate(route) }
                            )
                        }
                        
                        composable("ADMOB") {
                            AdMobScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("REWARDS") {
                            RewardsScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        composable("ECONOMY") {
                            EconomyHubScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                    SimulatedAdOverlay()
                }
            }
        }
    }
}

    override fun onDestroy() {
        super.onDestroy()
        com.example.data.FriendsManager.makeOffline()
    }
}
