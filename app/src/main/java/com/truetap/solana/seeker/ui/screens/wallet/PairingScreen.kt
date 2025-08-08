package com.truetap.solana.seeker.ui.screens.wallet

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.EntryPointAccessors
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.data.AuthState
import com.truetap.solana.seeker.data.WalletType
import com.truetap.solana.seeker.data.ConnectionResult
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.ui.accessibility.LocalAccessibilitySettings
import com.truetap.solana.seeker.ui.theme.LocalDynamicColors
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import com.truetap.solana.seeker.utils.SolflareDeepLinkParser
import kotlinx.coroutines.delay

data class WalletConfig(
    val id: String,
    val name: String,
    val logoRes: Int,
    val gradient: List<Color>,
    val description: String,
    val deepLinkBase: String? = null // Base URL for wallet-specific deep links
)

sealed class PairingConnectionState {
    object Connecting : PairingConnectionState()
}

// Wallet connection helper object
object WalletConnectionHelper {
    
    // Timeout configurations for wallet connections
    private const val MWA_CONNECTION_TIMEOUT = 60_000L // 60 seconds for MWA connection
    private const val SEED_VAULT_TIMEOUT = 30_000L     // 30 seconds for Seed Vault
    
    suspend fun attemptWalletConnection(
        walletType: WalletType,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>?,
        context: Context? = null,
        activityResultSender: ActivityResultSender? = null,
        retryCount: Int = 0,
        walletConfig: WalletConfig? = null,
        mwaService: com.truetap.solana.seeker.services.MobileWalletAdapterService? = null
    ): ConnectionResult {
        // Attempting wallet connection
        android.util.Log.d("WalletConnectionHelper", "Attempting connection for ${walletType.displayName}, retry: $retryCount")
        
        return try {
            val result = when (walletType) {
                WalletType.SOLFLARE, WalletType.PHANTOM, WalletType.EXTERNAL -> {
                    // Check if this wallet has a deep link configured for wallet-specific targeting
                    if (walletConfig?.deepLinkBase != null && context != null) {
                        // Using deep link connection
                        android.util.Log.d("WalletConnectionHelper", "Using deep link for ${walletType.displayName}")
                        connectWithWalletDeepLink(walletType, walletConfig.deepLinkBase, context)
                    } else if (activityResultSender != null && mwaService != null) {
                        // Using Mobile Wallet Adapter
                        android.util.Log.d("WalletConnectionHelper", "Using Mobile Wallet Adapter for ${walletType.displayName}")
                        connectWithMobileWalletAdapter(walletType, activityResultSender, mwaService)
                    } else {
                        // Missing ActivityResultSender or MWA service
                        android.util.Log.e("WalletConnectionHelper", "Missing ActivityResultSender or MWA service for MWA")
                        ConnectionResult.Failure(
                            error = "Mobile Wallet Adapter is not properly configured. Please restart the app.",
                            walletType = walletType
                        )
                    }
                }
                WalletType.SOLANA_SEEKER -> {
                    // Using Seed Vault for connection
                    connectWithSeedVault(walletType)
                }
            }
            // Connection attempt completed
            result
        } catch (e: Exception) {
            // Connection exception occurred
            if (retryCount < 2) {
                // Retrying connection after delay
                delay(1000) // Wait 1 second before retry
                attemptWalletConnection(walletType, activityResultLauncher, context, activityResultSender, retryCount + 1, walletConfig, mwaService)
            } else {
                ConnectionResult.Failure(
                    error = "Connection failed after ${retryCount + 1} attempts: ${e.message}",
                    exception = e,
                    walletType = walletType
                )
            }
        }
    }
    
    private suspend fun connectWithMobileWalletAdapter(
        walletType: WalletType,
        activityResultSender: ActivityResultSender,
        mwaService: com.truetap.solana.seeker.services.MobileWalletAdapterService
    ): ConnectionResult {
        return try {
            withTimeout(MWA_CONNECTION_TIMEOUT) {
                // Starting MWA connection
                android.util.Log.d("WalletConnectionHelper", "Starting MWA connection for ${walletType.displayName}")
                
                val result = mwaService.connectToWallet(walletType, activityResultSender)
                
                // MWA connection result received
                android.util.Log.d("WalletConnectionHelper", "MWA result: $result")
                
                result
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.w("WalletConnectionHelper", "MWA connection timed out after ${MWA_CONNECTION_TIMEOUT}ms")
            ConnectionResult.Failure(
                error = "Wallet connection timed out after ${MWA_CONNECTION_TIMEOUT / 1000} seconds. Please try again.",
                walletType = walletType
            )
        } catch (e: Exception) {
            android.util.Log.e("WalletConnectionHelper", "Unexpected error in MWA connection", e)
            ConnectionResult.Failure(
                error = "Unexpected error during wallet connection: ${e.localizedMessage ?: e.message}",
                exception = e,
                walletType = walletType
            )
        }
    }
    
    // Helper function to encode bytes to base58 (Solana public key format)
    private fun encodeBase58(bytes: ByteArray): String {
        val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        
        if (bytes.isEmpty()) return ""
        
        val input = bytes.copyOf()
        var zeros = 0
        while (zeros < input.size && input[zeros] == 0.toByte()) {
            zeros++
        }
        
        val encoded = mutableListOf<Char>()
        var i = zeros
        while (i < input.size) {
            var carry = input[i].toInt() and 0xFF
            var j = 0
            while (carry != 0 || j < encoded.size) {
                if (j >= encoded.size) {
                    encoded.add('1')
                }
                carry += (alphabet.indexOf(encoded[j]) * 256)
                encoded[j] = alphabet[carry % 58]
                carry /= 58
                j++
            }
            i++
        }
        
        // Add leading zeros
        repeat(zeros) {
            encoded.add(0, '1')
        }
        
        return encoded.reversed().joinToString("")
    }

    
    /**
     * Parse a wallet deep link response with support for encrypted Solflare responses
     */
    fun parseWalletDeepLinkResponse(
        responseUri: Uri,
        walletType: WalletType
    ): ConnectionResult {
        return try {
            // Check for error first
            val errorCode = responseUri.getQueryParameter("errorCode")
            if (errorCode != null) {
                val errorMessage = responseUri.getQueryParameter("errorMessage") ?: "Unknown error"
                return ConnectionResult.Failure(
                    error = "Wallet error ($errorCode): $errorMessage",
                    walletType = walletType
                )
            }
            
            if (walletType == WalletType.SOLFLARE) {
                // Handle encrypted Solflare response
                return parseSolflareEncryptedResponse(responseUri, walletType)
            } else {
                // Handle other wallet responses (simplified)
                val publicKey = responseUri.getQueryParameter("public_key")
                    ?: responseUri.getQueryParameter("publicKey")
                    ?: return ConnectionResult.Failure(
                        error = "Missing public key in wallet response",
                        walletType = walletType
                    )
                
                val session = responseUri.getQueryParameter("session")
                    ?: responseUri.getQueryParameter("authToken")
                
                ConnectionResult.Success(
                    publicKey = publicKey,
                    accountLabel = "${walletType.displayName} Wallet",
                    walletType = walletType,
                    authToken = session
                )
            }
            
        } catch (e: Exception) {
            ConnectionResult.Failure(
                error = "Failed to parse wallet response: ${e.localizedMessage ?: e.message}",
                exception = e,
                walletType = walletType
            )
        }
    }
    
    /**
     * Parse encrypted Solflare response
     * Note: This is now deprecated in favor of MWA, but kept for legacy deep link support
     */
    private fun parseSolflareEncryptedResponse(
        responseUri: Uri,
        walletType: WalletType
    ): ConnectionResult {
        return try {
            // Check for unencrypted parameters as fallback
            val publicKey = responseUri.getQueryParameter("public_key")
                ?: responseUri.getQueryParameter("publicKey")
                ?: return ConnectionResult.Failure(
                    error = "Legacy Solflare deep link not supported. Please use Mobile Wallet Adapter.",
                    walletType = walletType
                )
            
            val session = responseUri.getQueryParameter("session")
            
            ConnectionResult.Success(
                publicKey = publicKey,
                accountLabel = "${walletType.displayName} Wallet",
                walletType = walletType,
                authToken = session
            )
            
        } catch (e: Exception) {
            android.util.Log.e("WalletConnectionHelper", "Error parsing Solflare response", e)
            ConnectionResult.Failure(
                error = "Failed to parse Solflare response: ${e.localizedMessage ?: e.message}. Please use Mobile Wallet Adapter.",
                exception = e,
                walletType = walletType
            )
        }
    }

    /**
     * Connect to a specific wallet using its deep link URL
     * This enables true wallet-specific targeting (Phantom opens only Phantom, Solflare opens only Solflare)
     */
    suspend fun connectWithWalletDeepLink(
        walletType: WalletType,
        deepLinkBase: String,
        context: Context
    ): ConnectionResult {
        return try {
            // Attempting deep link connection
            android.util.Log.d("WalletConnectionHelper", "Deep link connection to ${walletType.displayName} at $deepLinkBase")
            
            val redirectLink = "truetap://onConnect" // Our app's custom scheme
            val appUrl = "https://truetap.app" // Our dApp's website
            val cluster = if (com.truetap.solana.seeker.BuildConfig.DEBUG) "devnet" else "mainnet-beta"
            
            // Note: Deep links are now deprecated in favor of MWA
            // This is kept for legacy compatibility but may not work reliably
            val deepLinkUri = Uri.parse(deepLinkBase + "connect").buildUpon()
                .appendQueryParameter("cluster", cluster)
                .appendQueryParameter("app_url", appUrl)
                .appendQueryParameter("redirect_link", redirectLink)
                .build()
            
            // Opening deep link URI
            android.util.Log.d("WalletConnectionHelper", "Opening deep link: $deepLinkUri")
            
            // Launch the deep link
            val intent = Intent(Intent.ACTION_VIEW, deepLinkUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Check if the wallet app can handle this intent
            val packageManager = context.packageManager
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent)
                
                // Return pending result - the actual connection will be handled by the deep link response
                ConnectionResult.Pending(
                    message = "Opening ${walletType.displayName}...",
                    walletType = walletType
                )
            } else {
                // Wallet app is not installed
                ConnectionResult.Failure(
                    error = "${walletType.displayName} app is not installed. Please install it from the Play Store.",
                    walletType = walletType
                )
            }
        } catch (e: Exception) {
            // Deep link connection error
            android.util.Log.e("WalletConnectionHelper", "Deep link connection error", e)
            ConnectionResult.Failure(
                error = "Failed to open ${walletType.displayName}: ${e.localizedMessage ?: e.message}",
                exception = e,
                walletType = walletType
            )
        }
    }
    
    private suspend fun connectWithSeedVault(walletType: WalletType): ConnectionResult {
        return try {
            withTimeout(SEED_VAULT_TIMEOUT) {
                android.util.Log.d("WalletConnectionHelper", "Starting Seed Vault connection for ${walletType.displayName}")
                // Redirect to SeedVaultScreen - this should be handled by navigation
                // This method should not be called when using the new modular system
                ConnectionResult.Failure(
                    error = "Please use the Seed Vault screen for hardware wallet connections",
                    walletType = walletType
                )
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.w("WalletConnectionHelper", "Seed Vault connection timed out after ${SEED_VAULT_TIMEOUT}ms")
            ConnectionResult.Failure(
                error = "Seed Vault connection timed out. Please try again.",
                walletType = walletType
            )
        } catch (e: Exception) {
            android.util.Log.e("WalletConnectionHelper", "Error in Seed Vault connection", e)
            ConnectionResult.Failure(
                error = "Seed Vault connection error: ${e.localizedMessage ?: e.message}",
                exception = e,
                walletType = walletType
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    walletId: String,
    onNavigateToSuccess: () -> Unit,
    onNavigateToFailure: (String, String?) -> Unit,
    onNavigateBack: () -> Unit,
    activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null,
    activityResultSender: ActivityResultSender? = null,
    pendingWalletConnection: Uri? = null,
    onWalletConnectionHandled: () -> Unit = {},
    viewModel: WalletViewModel = hiltViewModel(),
    mwaService: com.truetap.solana.seeker.services.MobileWalletAdapterService? = null
) {
    val context = LocalContext.current
    var connectionState by remember { mutableStateOf<PairingConnectionState>(PairingConnectionState.Connecting) }
    var debugMessage by remember { mutableStateOf("Initializing...") }
    
    // Get MWA service instance - create directly for now
    val actualMwaService = remember { 
        mwaService ?: com.truetap.solana.seeker.services.MobileWalletAdapterService(context)
    }
    
    // ActivityResultSender is now passed from MainActivity (created during onCreate)
    
    // Observe ViewModel state
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    
    // Wallet configurations - now prioritize MWA over deep links
    val walletConfigs = mapOf(
        "solflare" to WalletConfig(
            id = "solflare", 
            name = "Solflare",
            logoRes = R.drawable.solflare,
            gradient = listOf(Color(0xFFFFC107), Color(0xFFFF6B00)),
            description = "Feature-rich Solana wallet",
            deepLinkBase = null // Use MWA instead of deep links
        ),
        "phantom" to WalletConfig(
            id = "phantom",
            name = "Phantom",
            logoRes = R.drawable.phantom,
            gradient = listOf(Color(0xFF9945FF), Color(0xFF14F195)),
            description = "Popular Solana wallet",
            deepLinkBase = null // Use MWA instead of deep links
        ),
        "external" to WalletConfig(
            id = "external",
            name = "External Wallet",
            logoRes = R.drawable.ic_wallet,
            gradient = listOf(TrueTapPrimary, TrueTapPrimary.copy(alpha = 0.8f)),
            description = "Compatible external wallets",
            deepLinkBase = null // Uses generic MWA
        ),
        "solana" to WalletConfig(
            id = "solana",
            name = "Hardware Wallet",
            logoRes = R.drawable.skr,
            gradient = listOf(Color(0xFF00D4FF), Color(0xFF00FFA3)),
            description = "Seeker's built-in Seed Vault",
            deepLinkBase = null // Uses Seed Vault, not MWA
        )
    )
    
    val walletConfig = walletConfigs[walletId]
    // Detect installed wallets: Phantom & Solflare
    val pm = context.packageManager
    val phantomInstalled = remember {
        try { pm.getPackageInfo("app.phantom", 0); true } catch (_: Exception) { false }
    }
    val solflareInstalled = remember {
        try { pm.getPackageInfo("io.solflare.wallet", 0); true } catch (_: Exception) { false }
    }
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "pairing")
    
    // Static fade-in animation (no flashing)
    val fadeAnim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600, easing = EaseInOut),
        label = "fade"
    )
    
    // Synchronized heartbeat animation for both logos
    val heartbeatScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeat"
    )
    
    // Logo floating animation
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )
    
    // Loading dots animations
    val dotAnimations = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = EaseInOut, delayMillis = index * 200),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_$index"
        )
    }
    
    // Get wallet type from ID
    val walletType = WalletType.fromId(walletId)
    
    // Handle pending wallet connection from deep link
    LaunchedEffect(pendingWalletConnection) {
        if (pendingWalletConnection != null) {
            debugMessage = "Processing deep link response..."
            // Processing pending wallet connection from deep link
            
            try {
                // Check if this is a new wallet-specific deep link or legacy Solflare
                val connectionResult = if (pendingWalletConnection.path == "/onConnect") {
                    // New wallet-specific deep link
                    WalletConnectionHelper.parseWalletDeepLinkResponse(pendingWalletConnection, walletType ?: WalletType.EXTERNAL)
                } else {
                    // Legacy Solflare deep link
                    SolflareDeepLinkParser.parseWalletConnectionResponse(pendingWalletConnection)
                }
                debugMessage = "Deep link parsed: $connectionResult"
                // Deep link response parsed
                
                when (connectionResult) {
                    is ConnectionResult.Success -> {
                        debugMessage = "Deep link success! Saving and navigating..."
                        // Deep link success - saving connection
                        
                        // Save connection result to repository via ViewModel
                        viewModel.saveWalletConnection(connectionResult)
                        
                        // Mark deep link as handled
                        onWalletConnectionHandled()
                        
                        // Navigate to success
                        onNavigateToSuccess()
                    }
                    is ConnectionResult.Failure -> {
                        debugMessage = "Deep link failure: ${connectionResult.error}"
                        // Deep link connection failed
                        
                        // Mark deep link as handled
                        onWalletConnectionHandled()
                        
                        // Navigate to failure
                        onNavigateToFailure(walletId, connectionResult.error)
                    }
                    is ConnectionResult.Pending -> {
                        debugMessage = "Deep link pending: ${connectionResult.message}"
                        // Deep link connection pending
                        
                        // This shouldn't happen when parsing a completed deep link response
                        // but we handle it gracefully
                        onWalletConnectionHandled()
                        onNavigateToFailure(walletId, "Unexpected pending state from deep link")
                    }
                }
                
            } catch (e: Exception) {
                debugMessage = "Deep link error: ${e.message}"
                // Deep link processing error
                
                // Mark deep link as handled
                onWalletConnectionHandled()
                
                // Navigate to failure
                onNavigateToFailure(walletId, "Failed to process wallet response: ${e.message}")
            }
            
            return@LaunchedEffect // Skip normal connection flow when processing deep link
        }
    }
    
    // Trigger wallet connection when screen loads - delegate to ViewModel
    LaunchedEffect(walletId) {
        debugMessage = "LaunchedEffect triggered with walletId: $walletId"
        // Launched effect triggered for wallet pairing
        println("PairingScreen: walletType: $walletType")
        
        if (walletType != null) {
            debugMessage = "Attempting connection for ${walletType.displayName}"
            println("PairingScreen: Attempting connection for ${walletType.displayName}")
            
            try {
                val result = viewModel.connectWithWallet(
                    walletType = walletType,
                    activity = (context as? android.app.Activity),
                    activityResultLauncher = activityResultLauncher,
                    activityResultSender = activityResultSender
                )
                
                debugMessage = "Connection result: $result"
                println("PairingScreen: Connection result: $result")
                
                when (result) {
                    is ConnectionResult.Success -> {
                        debugMessage = "Success! Saving and navigating..."
                        println("PairingScreen: Success! Saving connection and navigating...")
                        
                        // Navigate to success
                        debugMessage = "About to navigate to success"
                        println("PairingScreen: About to call onNavigateToSuccess()")
                        onNavigateToSuccess()
                        println("PairingScreen: Called onNavigateToSuccess()")
                    }
                    is ConnectionResult.Failure -> {
                        debugMessage = "Failure: ${result.error}"
                        println("PairingScreen: Failure! Error: ${result.error}")
                        // Navigate to failure with error message
                        debugMessage = "About to navigate to failure"
                        println("PairingScreen: About to call onNavigateToFailure()")
                        onNavigateToFailure(walletId, result.error)
                        println("PairingScreen: Called onNavigateToFailure()")
                    }
                    is ConnectionResult.Pending -> {
                        debugMessage = "Pending: ${result.message}"
                        println("PairingScreen: Pending connection - waiting for response: ${result.message}")
                        // Stay on the connecting screen and wait for deep link response
                        // The deep link LaunchedEffect will handle the actual connection
                    }
                }
            } catch (e: Exception) {
                debugMessage = "Exception: ${e.message}"
                println("PairingScreen: Exception in LaunchedEffect: ${e.message}")
                e.printStackTrace()
                onNavigateToFailure(walletId, "Unexpected error: ${e.message}")
            }
        } else {
            debugMessage = "Unknown wallet type for ID: $walletId"
            println("PairingScreen: Unknown wallet type for ID: $walletId")
            // Unknown wallet ID
            onNavigateToFailure(walletId, "Unknown wallet type: $walletId")
        }
    }
    
    if (walletConfig == null) {
        // Error screen for unknown wallet
        ErrorContent(
            title = "Wallet not found",
            subtitle = "Unknown wallet ID: $walletId",
            onRetry = onNavigateBack
        )
        return
    }
    
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    // Apply dynamic background color
    val backgroundModifier = if (dynamicColors.backgroundBrush != null) {
        Modifier.fillMaxSize().background(dynamicColors.backgroundBrush)
    } else {
        Modifier.fillMaxSize().background(dynamicColors.background)
    }
    
    Surface(
        modifier = backgroundModifier,
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = fadeAnim)
                .padding(WindowInsets.navigationBars.asPaddingValues()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = "Syncing",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = dynamicColors.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Two-circle connection animation
            TwoCircleConnectionAnimation(
                walletConfig = walletConfig,
                heartbeatScale = heartbeatScale,
                floatingOffset = floatingOffset
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Text content - only connecting state
            ConnectingTextContent(
                walletConfig = walletConfig,
                dotAnimations = dotAnimations
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Debug message for troubleshooting
            Text(
                text = "Debug: $debugMessage",
                fontSize = 12.sp,
                color = dynamicColors.textSecondary.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action button - only cancel option
            TextButton(
                onClick = onNavigateBack,
                modifier = Modifier.padding(bottom = 40.dp)
            ) {
                Text(
                    text = "Cancel Connection",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = dynamicColors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun TwoCircleConnectionAnimation(
    walletConfig: WalletConfig,
    heartbeatScale: Float,
    floatingOffset: Float
) {
    val circleSize = 100.dp
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(40.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TrueTap Circle with synchronized heartbeat
        Box(
            modifier = Modifier
                .size(circleSize)
                .scale(heartbeatScale)
                .graphicsLayer(translationY = floatingOffset)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.truetap_logo_large),
                contentDescription = "TrueTap Logo",
                modifier = Modifier.size(circleSize * 0.6f),
                contentScale = ContentScale.Fit
            )
        }
        
        // Connection Plus Icon
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(32.dp),
            tint = dynamicColors.primary
        )
        
        // Wallet Circle with synchronized heartbeat
        Box(
            modifier = Modifier
                .size(circleSize)
                .scale(heartbeatScale)
                .graphicsLayer(translationY = floatingOffset)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Check if this is the external wallet category to use appropriate rendering
            if (walletConfig.id == "external") {
                Box(
                    modifier = Modifier
                        .size(circleSize * 0.6f)
                        .clip(CircleShape)
                        .background(dynamicColors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = walletConfig.logoRes),
                        contentDescription = "${walletConfig.name} logo",
                        modifier = Modifier.size(circleSize * 0.4f),
                        tint = Color.White
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = walletConfig.logoRes),
                    contentDescription = "${walletConfig.name} logo",
                    modifier = Modifier.size(circleSize),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun ConnectingTextContent(
    walletConfig: WalletConfig,
    dotAnimations: List<State<Float>>
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "We're better together!",
            fontSize = if (accessibility.largeButtonMode) 32.sp else 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = dynamicColors.textPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Syncing with ${walletConfig.name}",
            fontSize = if (accessibility.largeButtonMode) 20.sp else 18.sp,
            color = dynamicColors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = if (accessibility.largeButtonMode) 28.sp else 24.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Loading dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            dotAnimations.forEach { animatedOpacity ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(animatedOpacity.value)
                        .background(
                            dynamicColors.primary.copy(alpha = animatedOpacity.value),
                            CircleShape
                        )
                )
            }
        }
    }
}



@Composable
private fun ErrorContent(
    title: String,
    subtitle: String,
    onRetry: () -> Unit
) {
    val accessibility = LocalAccessibilitySettings.current
    val dynamicColors = getDynamicColors(
        themeMode = accessibility.themeMode,
        highContrastMode = accessibility.highContrastMode
    )
    
    // Apply dynamic background color
    val backgroundModifier = if (dynamicColors.backgroundBrush != null) {
        Modifier.fillMaxSize().background(dynamicColors.backgroundBrush)
    } else {
        Modifier.fillMaxSize().background(dynamicColors.background)
    }
    
    Surface(
        modifier = backgroundModifier,
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = if (accessibility.largeButtonMode) 28.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = dynamicColors.textPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = subtitle,
                fontSize = if (accessibility.largeButtonMode) 18.sp else 16.sp,
                color = dynamicColors.textSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = dynamicColors.primary
                )
            ) {
                Text(
                    text = "Go Back",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}