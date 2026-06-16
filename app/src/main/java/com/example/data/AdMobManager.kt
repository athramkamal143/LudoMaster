package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

// --- Ad Types ---
enum class AdType {
    BANNER,
    INTERSTITIAL,
    REWARDED
}

// --- Ad Log Entry ---
data class AdLog(
    val id: String = UUID.randomUUID().toString().take(6),
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val type: String = "INFO" // "INFO", "SUCCESS", "WARNING", "ERROR"
)

object AdMobManager {
    private const val TAG = "AdMobManager"

    // Test Ad Unit IDs (AdMob Standard Test IDs)
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // --- State of Simulation Module ---
    // This allows seamless preview and testing of full interstitial, rewarded video and banner ads even
    // in testing/sandboxed environments where google play services are absent (like our cloud emulator)
    private val _isSimulationMode = MutableStateFlow(true)
    val isSimulationMode = _isSimulationMode.asStateFlow()

    // --- Active Sim Ad States ---
    val isSimAdVisible = mutableStateOf(false)
    val currentSimType = mutableStateOf(AdType.INTERSTITIAL)
    val simCountdown = mutableStateOf(5)
    val isSimVideoPlaying = mutableStateOf(false)
    private var onSimFinishedCallback: (() -> Unit)? = null

    // --- Loaded States of Real AdMob Ads ---
    private var mInterstitialAd: InterstitialAd? = null
    private var mRewardedAd: RewardedAd? = null

    private val _isInterstitialLoaded = MutableStateFlow(false)
    val isInterstitialLoaded = _isInterstitialLoaded.asStateFlow()

    private val _isRewardedLoaded = MutableStateFlow(false)
    val isRewardedLoaded = _isRewardedLoaded.asStateFlow()

    // --- Diagnostic Logs --
    private val _adLogs = MutableStateFlow<List<AdLog>>(listOf(AdLog(message = "AdMob Manager Initialized.")))
    val adLogs = _adLogs.asStateFlow()

    private var isInitialized = false

    fun toggleSimulationMode(enabled: Boolean) {
        _isSimulationMode.value = enabled
        logAd("Developer toggled Simulation Mode: $enabled", "INFO")
    }

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        logAd("Initializing AdMob Play Services SDK...", "INFO")
        
        try {
            // Initialize mobile ads sdk
            MobileAds.initialize(context) { status ->
                logAd("Play Services AdMob SDK Initialization Finished.", "SUCCESS")
                loadInterstitial(context)
                loadRewarded(context)
            }
        } catch (e: Exception) {
            logAd("Failed to boot real AdMob SDK: ${e.message}. Defaulting to visual simulation mode.", "WARNING")
        }
    }

    fun logAd(message: String, type: String = "INFO") {
        Log.d(TAG, "[$type] $message")
        _adLogs.update { (listOf(AdLog(message = message, type = type)) + it).take(40) }
    }

    // --- Load Interstitial (Real AdMob) ---
    fun loadInterstitial(context: Context) {
        if (isSimulationMode.value) return
        
        val adRequest = AdRequest.Builder().build()
        logAd("Loading real Interstitial Ad ($TEST_INTERSTITIAL_ID)...", "INFO")
        
        InterstitialAd.load(
            context,
            TEST_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    _isInterstitialLoaded.value = true
                    logAd("Real Interstitial loaded successfully.", "SUCCESS")

                    mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            logAd("Real Interstitial dismissed.", "INFO")
                            mInterstitialAd = null
                            _isInterstitialLoaded.value = false
                            // Preload next one
                            loadInterstitial(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            logAd("Real Interstitial failed to show: ${error.message}", "ERROR")
                            mInterstitialAd = null
                            _isInterstitialLoaded.value = false
                        }

                        override fun onAdShowedFullScreenContent() {
                            logAd("Real Interstitial overlay display verified.", "SUCCESS")
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    logAd("Real Interstitial load failed: ${error.message}", "WARNING")
                    mInterstitialAd = null
                    _isInterstitialLoaded.value = false
                }
            }
        )
    }

    // --- Load Rewarded Ad (Real AdMob) ---
    fun loadRewarded(context: Context) {
        if (isSimulationMode.value) return
        
        val adRequest = AdRequest.Builder().build()
        logAd("Loading real Rewarded Ad ($TEST_REWARDED_ID)...", "INFO")

        RewardedAd.load(
            context,
            TEST_REWARDED_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    mRewardedAd = rewardedAd
                    _isRewardedLoaded.value = true
                    logAd("Real Rewarded Ad loaded successfully.", "SUCCESS")

                    mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            logAd("Real Rewarded Ad dismissed by user.", "INFO")
                            mRewardedAd = null
                            _isRewardedLoaded.value = false
                            loadRewarded(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            logAd("Real Rewarded Ad failed to show: ${error.message}", "ERROR")
                            mRewardedAd = null
                            _isRewardedLoaded.value = false
                        }

                        override fun onAdShowedFullScreenContent() {
                            logAd("Real Rewarded Ad playing.", "SUCCESS")
                        }
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    logAd("Real Rewarded Ad load failed: ${error.message}", "WARNING")
                    mRewardedAd = null
                    _isRewardedLoaded.value = false
                }
            }
        )
    }

    // --- Show Interstitial Trigger ---
    fun showInterstitial(activity: Activity, onFinished: () -> Unit) {
        if (LudoMasterRepository.playerState.value.isVip) {
            logAd("[VIP Benefit] Ad-free status active. Bypassing Interstitial ad instantly.", "SUCCESS")
            onFinished()
            return
        }
        if (isSimulationMode.value) {
            logAd("Simulating Interstitial Ad...", "INFO")
            launchSimAd(AdType.INTERSTITIAL, onFinished)
        } else {
            val ad = mInterstitialAd
            if (ad != null) {
                logAd("Showing real loaded Interstitial Ad.", "SUCCESS")
                ad.show(activity)
                // Since dismissal callbacks handle standard progression, call completion
                onFinished()
            } else {
                logAd("No real Interstitial loaded. Fallback to Simulated Interstitial.", "WARNING")
                launchSimAd(AdType.INTERSTITIAL, onFinished)
                // Also trigger reload
                loadInterstitial(activity)
            }
        }
    }

    // --- Show Rewarded Ad Trigger ---
    fun showRewarded(activity: Activity, onRewarded: (amount: Int) -> Unit) {
        if (LudoMasterRepository.playerState.value.isVip) {
            logAd("[VIP Benefit] Ad-free status active. Instantly claiming Rewarded Video rewards without ads!", "SUCCESS")
            onRewarded(1500)
            return
        }
        if (isSimulationMode.value) {
            logAd("Simulating Rewarded Video Ad (Payout: 1,500 Coins)...", "INFO")
            launchSimAd(AdType.REWARDED) {
                // Grant reward coins
                onRewarded(1500)
            }
        } else {
            val ad = mRewardedAd
            if (ad != null) {
                logAd("Showing real loaded Rewarded Ad.", "SUCCESS")
                ad.show(activity) { rewardItem ->
                    val rewardAmount = rewardItem.amount.takeIf { it > 0 } ?: 1500
                    logAd("Real reward qualified: $rewardAmount of ${rewardItem.type}", "SUCCESS")
                    activity.runOnUiThread {
                        onRewarded(rewardAmount)
                    }
                }
            } else {
                logAd("No real Rewarded loaded. Fallback to Simulated Rewarded.", "WARNING")
                launchSimAd(AdType.REWARDED) {
                    onRewarded(1500)
                }
                loadRewarded(activity)
            }
        }
    }

    // --- Launch Simulation Graphics ---
    private fun launchSimAd(type: AdType, callback: () -> Unit) {
        currentSimType.value = type
        onSimFinishedCallback = callback
        simCountdown.value = if (type == AdType.REWARDED) 8 else 5 // Rewarded takes a little longer
        isSimVideoPlaying.value = (type == AdType.REWARDED)
        isSimAdVisible.value = true

        scope.launch {
            while (simCountdown.value > 0 && isSimAdVisible.value) {
                delay(1000)
                simCountdown.value -= 1
            }
            if (isSimAdVisible.value) {
                completeSimAd()
            }
        }
    }

    fun completeSimAd() {
        if (!isSimAdVisible.value) return
        isSimAdVisible.value = false
        logAd("Simulated campaign completed successfully. Granting callback hooks.", "SUCCESS")
        onSimFinishedCallback?.invoke()
        onSimFinishedCallback = null
    }

    fun closeSimAd() {
        if (!isSimAdVisible.value) return
        isSimAdVisible.value = false
        logAd("Simulated campaign closed early by user.", "WARNING")
        
        // Interstitials can be closed early without loss of progression, but rewarded might lose coins
        if (currentSimType.value == AdType.INTERSTITIAL) {
            onSimFinishedCallback?.invoke()
        }
        onSimFinishedCallback = null
    }
}
