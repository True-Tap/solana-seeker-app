package com.truetap.solana.seeker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.core.animation.doOnEnd
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.truetap.solana.seeker.seedvault.SeedVaultManager
import com.truetap.solana.seeker.ui.navigation.NavGraph
import com.truetap.solana.seeker.ui.theme.SolanaseekerappTheme
import com.truetap.solana.seeker.ui.theme.TrueTapBackground
import com.truetap.solana.seeker.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    // Inject SeedVaultManager
    @Inject
    lateinit var seedVaultManager: SeedVaultManager
    
    // Activity Result launchers
    private lateinit var mwaActivityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var seedVaultActivityResultLauncher: ActivityResultLauncher<Intent>

    // ActivityResultSender for Mobile Wallet Adapter (created early to avoid lifecycle issues)
    private lateinit var activityResultSender: ActivityResultSender
    
    // State for handling deep link wallet connection
    private var pendingWalletConnection by mutableStateOf<Uri?>(null)
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install and configure the splash screen before calling super.onCreate()
        val splashScreen = splashScreen
        
        Log.d(TAG, "Configuring splash screen with custom exit animation")
        
        // Configure splash screen exit animation with debug logging
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            Log.d(TAG, "Splash screen exit animation starting - view size: ${splashScreenView.width}x${splashScreenView.height}")
            
            try {
                // Create slide up animation for smooth transition
                val slideUp = ObjectAnimator.ofFloat(
                    splashScreenView,
                    View.TRANSLATION_Y,
                    0f,
                    -splashScreenView.height.toFloat()
                )
                slideUp.interpolator = AnticipateInterpolator()
                slideUp.duration = 300L
                
                // Remove the splash screen view when animation completes
                slideUp.doOnEnd { 
                    Log.d(TAG, "Splash screen exit animation completed - removing splash view")
                    splashScreenView.remove() 
                }
                slideUp.start()
                
            } catch (e: Exception) {
                Log.e(TAG, "Error during splash screen exit animation", e)
                // Fallback: remove splash screen immediately if animation fails
                splashScreenView.remove()
            }
        }
        
        super.onCreate(savedInstanceState)
        
        // Initialize Activity Result Launchers
        mwaActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { /* handled by MWA clientlib via ActivityResultSender */ }

        seedVaultActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            seedVaultManager.handleActivityResult(0, result.resultCode, result.data)
        }
        
        // Initialize ActivityResultSender (must be done during onCreate to avoid lifecycle issues)
        activityResultSender = ActivityResultSender(this)
        
        // Handle intent from onCreate
        handleIntent(intent)
        
        enableEdgeToEdge()
        setContent {
            SolanaseekerappTheme {
                com.truetap.solana.seeker.ui.accessibility.AccessibilityProvider {
                    val settings = com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings.current
                    val dynamicColors = com.truetap.solana.seeker.ui.theme.getDynamicColors(
                        themeMode = settings.themeMode,
                        highContrastMode = settings.highContrastMode
                    )
                    
                    androidx.compose.runtime.CompositionLocalProvider(
                        com.truetap.solana.seeker.ui.theme.LocalDynamicColors provides dynamicColors
                    ) {
                        com.truetap.solana.seeker.ui.theme.DynamicBackground(
                            colors = dynamicColors,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            SolanaSeekerApp(
                                activityResultLauncherIntentSender = mwaActivityResultLauncher,
                                seedVaultActivityResultLauncher = seedVaultActivityResultLauncher,
                                activityResultSender = activityResultSender,
                                pendingWalletConnection = pendingWalletConnection,
                                onWalletConnectionHandled = { pendingWalletConnection = null }
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the activity's intent
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent) {
        Log.d(TAG, "Handling intent: ${intent.action}, data: ${intent.data}")
        
        // Check if this is a deep link from wallet connection
        if (intent.action == Intent.ACTION_VIEW && intent.data != null) {
            val uri = intent.data!!
            Log.d(TAG, "Processing deep link: $uri")
            
            // Check if this is the wallet connection response
            if (uri.scheme == "truetap") {
                when {
                    // Legacy Solflare connection (existing)
                    uri.host == "wallet-connected" -> {
                        Log.d(TAG, "Legacy wallet connection deep link detected")
                        pendingWalletConnection = uri
                    }
                    // New wallet-specific deep link connections
                    uri.path == "/onConnect" -> {
                        Log.d(TAG, "Wallet-specific deep link connection detected")
                        pendingWalletConnection = uri
                    }
                }
            }
        }
    }
    
    // onActivityResult no longer used; flows are handled via Activity Result API
}

@Composable
fun SolanaSeekerApp(
    activityResultLauncherIntentSender: ActivityResultLauncher<IntentSenderRequest>,
    seedVaultActivityResultLauncher: ActivityResultLauncher<Intent>,
    activityResultSender: ActivityResultSender,
    pendingWalletConnection: Uri?,
    onWalletConnectionHandled: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = hiltViewModel()
    
    val isInitialized by mainViewModel.isInitialized.collectAsState()
    val startDestination by mainViewModel.startDestination.collectAsState()

    if (!isInitialized) {
        // Show loading screen while determining start destination
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Initializing...",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    } else {
        NavGraph(
            navController = navController,
            activityResultLauncher = seedVaultActivityResultLauncher,
            activityResultLauncherIntentSender = this@MainActivity.mwaActivityResultLauncher,
            activityResultSender = activityResultSender,
            pendingWalletConnection = pendingWalletConnection,
            onWalletConnectionHandled = onWalletConnectionHandled,
            startDestination = startDestination,
            modifier = modifier
        )
    }
}