package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EconomyManager
import com.example.data.CoinTransaction
import com.example.data.UserWallet
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EconomyHubScreen(
    onBack: () -> Unit
) {
    val wallet by EconomyManager.userWalletState.collectAsState()
    val transactions by EconomyManager.transactionHistory.collectAsState()
    val logs by EconomyManager.economyLogs.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 = WALLET & STORE, 1 = REFERRALS, 2 = LEDGER AUDIT
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var inputReferralCode by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val dateSdf = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LudoDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Economy Toolbar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(LudoSurfaceNavy, CircleShape)
                        .border(1.dp, LudoBorder, CircleShape)
                        .testTag("economy_back_btn")
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
                        text = "ECONOMY PORTAL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = LudoAccentGold,
                        letterSpacing = 1.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (EconomyManager.isSimulationMode) LudoYellow else LudoGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (EconomyManager.isSimulationMode) "Local Sandbox Node" else "Cloud Firestore Ledger",
                            color = Color.White.copy(0.5f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Top Wallet Balance Plate
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, LudoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Balance Section
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Coin Balance Display
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Stars,
                                    contentDescription = "Coins Logo",
                                    tint = LudoAccentGold,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = String.format("%,d", wallet.coins),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    modifier = Modifier.testTag("economy_coin_balance")
                                )
                            }
                            Text(
                                text = "BATTLE COINS",
                                color = LudoAccentGold.copy(0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Vertical bar
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(LudoBorder)
                        )

                        // Gems Display
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = "Gems Logo",
                                    tint = LudoAccentGem,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = String.format("%,d", wallet.gems),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp,
                                    modifier = Modifier.testTag("economy_gem_balance")
                                )
                            }
                            Text(
                                text = "GEMS VAULT",
                                color = LudoAccentGem.copy(0.7f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Network Sync Icon
                    Icon(
                        imageVector = if (EconomyManager.isSimulationMode) Icons.Filled.CloudOff else Icons.Filled.CloudSync,
                        contentDescription = "Sync Indicator",
                        tint = if (EconomyManager.isSimulationMode) LudoYellow.copy(0.5f) else LudoGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Material 3 Custom Navigation Segmented Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabHeaders = listOf(
                    Triple(Icons.Filled.Store, "Coin Store", 0),
                    Triple(Icons.Filled.Share, "Invite Codes", 1),
                    Triple(Icons.Filled.ReceiptLong, "Audit Ledger", 2)
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
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
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
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Tab Content Frame
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> EconomyStoreTab(
                        wallet = wallet,
                        onMessage = { alertMessage = it }
                    )
                    1 -> ReferralTab(
                        wallet = wallet,
                        inputCode = inputReferralCode,
                        onCodeChange = { inputReferralCode = it },
                        onMessage = { alertMessage = it }
                    )
                    2 -> TransactionLedgerTab(
                        transactions = transactions,
                        logs = logs,
                        dateSdf = dateSdf
                    )
                }
            }
        }
    }

    // Modal alerts
    if (alertMessage != null) {
        AlertDialog(
            onDismissRequest = { alertMessage = null },
            confirmButton = {
                Button(
                    onClick = { alertMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold)
                ) {
                    Text("DONE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            title = { Text("Economy Message", fontWeight = FontWeight.Bold, color = Color.White) },
            text = { Text(alertMessage ?: "", color = Color.LightGray) },
            containerColor = LudoSurfaceNavy,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// --- TAB 1: COIN STORE & GEMS VAULT CONVERSIONS ---
@Composable
fun EconomyStoreTab(
    wallet: UserWallet,
    onMessage: (String) -> Unit
) {
    val coinPacks = listOf(
        Triple("Starter Purse", 1000, 20),
        Triple("Squire Satchel", 5000, 80),
        Triple("King Champion Pile", 10000, 150),
        Triple("Grand Emperor Vault", 50000, 600)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "EXCHANGE GEMS FOR GOLD BATTLE COINS",
            fontSize = 11.sp,
            color = Color.White.copy(0.5f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )

        coinPacks.forEach { (name, coinsAmount, gemsCost) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, LudoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(LudoAccentGold.copy(0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MonetizationOn,
                                contentDescription = null,
                                tint = LudoAccentGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Instantly credits +$coinsAmount GC",
                                fontSize = 11.sp,
                                color = Color.White.copy(0.5f)
                            )
                        }
                    }

                    // Buy Conversion Button
                    Button(
                        onClick = {
                            EconomyManager.buyCoinPackWithGems(coinsAmount, gemsCost, name) { success, msg ->
                                onMessage(msg)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LudoAccentGem,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("buy_pack_${coinsAmount}")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$gemsCost GEMS",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Simulated credit cards or direct buy option
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy.copy(0.6f)),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, LudoBorder.copy(0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SIMULATED REAL PURCHASE BONUS",
                    color = LudoAccentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Acquire massive packages with simulated dollars. Instantly increments both coin wallets and ledger balance entries.",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            EconomyManager.recordTransaction(25000, "COIN_STORE", "In-App Simulated Store Pouch Purchase: $0.99") { success ->
                                if (success) onMessage("Successfully bought 25,000 Coins with simulated checkout!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LudoGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+$25K (Sim $0.99)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Button(
                        onClick = {
                            EconomyManager.recordTransaction(150000, "COIN_STORE", "In-App Simulated Store Vault Purchase: $4.99") { success ->
                                if (success) onMessage("Successfully bought 150,000 Mega Coins with simulated checkout!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LudoGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+$150K (Sim $4.99)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

// --- TAB 2: REFERRAL CODE PROGRAM & REWARD SYSTEM ---
@Composable
fun ReferralTab(
    wallet: UserWallet,
    inputCode: String,
    onCodeChange: (String) -> Unit,
    onMessage: (String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Campaign,
            contentDescription = null,
            tint = LudoAccentGold,
            modifier = Modifier.size(54.dp)
        )

        Text(
            text = "LUDO MASTER AFFILIATES",
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = "Share the excitement with friends and expand the crew! Both you and your friend will instantly claim 500 Battle Coins when they enter your custom referral code.",
            fontSize = 12.sp,
            color = Color.LightGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        // User's own referral code card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, LudoBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YOUR EXCLUSIVE REFERRAL CODE",
                    color = Color.White.copy(0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = wallet.referralCode,
                        color = LudoAccentGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        letterSpacing = 1.sp,
                        modifier = Modifier.testTag("my_referral_code_text")
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Ludo Referral Code", wallet.referralCode)
                            clipboard.setPrimaryClip(clip)
                            onMessage("Referral Code '${wallet.referralCode}' copied to clipboard!")
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White.copy(0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Claim box (if code the first time)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, LudoBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "REDEEM FRIEND'S CODE",
                    color = Color.White.copy(0.5f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (wallet.referredBy.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = LudoGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Linked with Referrer: ${wallet.referredBy}",
                            color = LudoGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = onCodeChange,
                            placeholder = { Text("E.g. PRIYA99", color = Color.White.copy(0.3f), fontSize = 13.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LudoAccentGold,
                                unfocusedBorderColor = LudoBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("referral_input_field")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                EconomyManager.submitReferralCode(inputCode) { success, msg ->
                                    onMessage(msg)
                                    if (success) onCodeChange("")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LudoAccentGold),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("apply_referral_btn")
                        ) {
                            Text("CLAIM", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- TAB 3: AUDITED TRANSACTION LEDGER & SYSTEM PIPES ---
@Composable
fun TransactionLedgerTab(
    transactions: List<CoinTransaction>,
    logs: List<String>,
    dateSdf: SimpleDateFormat
) {
    var ledgerTab by remember { mutableStateOf(0) } // 0 = LEDGER LIST, 1 = LIVE PACKETS
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Toggle tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { ledgerTab = 0 },
                colors = ButtonDefaults.buttonColors(containerColor = if (ledgerTab == 0) LudoBorder else LudoSurfaceNavy),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Ledger History (${transactions.size})", fontSize = 11.sp, color = Color.White)
            }
            Button(
                onClick = { ledgerTab = 1 },
                colors = ButtonDefaults.buttonColors(containerColor = if (ledgerTab == 1) LudoBorder else LudoSurfaceNavy),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Diagnostics Feed", fontSize = 11.sp, color = Color.White)
            }
        }

        if (ledgerTab == 0) {
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.ReceiptLong,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No recorded ledger logs found.",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(transactions) { tx ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = LudoSurfaceNavy),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Colored chip indicating type
                                        val chipBg = when (tx.source) {
                                            "DAILY_REWARD" -> LudoBlue.copy(0.2f)
                                            "WIN_REWARD" -> LudoGreen.copy(0.2f)
                                            "ENTRY_FEE" -> LudoRed.copy(0.2f)
                                            "REFERRAL_REWARD" -> LudoAccentGem.copy(0.2f)
                                            else -> LudoAccentGold.copy(0.2f)
                                        }
                                        val chipColor = when (tx.source) {
                                            "DAILY_REWARD" -> LudoBlue
                                            "WIN_REWARD" -> LudoGreen
                                            "ENTRY_FEE" -> LudoRed
                                            "REFERRAL_REWARD" -> LudoAccentGem
                                            else -> LudoAccentGold
                                        }

                                        Text(
                                            text = tx.source,
                                            color = chipColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier
                                                .background(chipBg, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        )

                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ID: ${tx.id.takeLast(7)}",
                                            color = Color.White.copy(0.3f),
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = tx.description,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = dateSdf.format(Date(tx.timestamp)),
                                        fontSize = 9.sp,
                                        color = Color.White.copy(0.4f)
                                    )
                                }

                                Text(
                                    text = if (tx.amount >= 0) "+${tx.amount}" else "${tx.amount}",
                                    color = if (tx.amount >= 0) LudoGreen else LudoRed,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    modifier = Modifier.testTag("tx_amt_${tx.id}")
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Sys Logs diagnostics
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black, RoundedCornerShape(10.dp))
                    .border(1.dp, LudoBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(logs) { log ->
                        Text(
                            text = log,
                            color = if (log.contains("failed", ignoreCase = true) || log.contains("error", ignoreCase = true)) LudoRed else LudoAccentGem,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
