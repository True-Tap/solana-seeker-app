package com.truetap.solana.seeker.ui.screens

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.seedvault.SeedVaultViewModel
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.R

/**
 * Seed Vault integration screen for TrueTap
 * Demonstrates secure key access and transaction signing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedVaultScreen(
    activityResultLauncher: ActivityResultLauncher<android.content.Intent>,
    modifier: Modifier = Modifier,
    onNavigateToSuccess: (() -> Unit)? = null,
    onNavigateToFailure: ((String, String?) -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: SeedVaultViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    val clipboardManager = LocalClipboardManager.current
    
    // Collect state from ViewModel
    val publicKey by viewModel.publicKey.collectAsState()
    val isAuthorized by viewModel.isAuthorized.collectAsState()
    val error by viewModel.error.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val walletConnected by viewModel.walletConnected.collectAsState()
    val lastSignedTransaction by viewModel.lastSignedTransaction.collectAsState()
    val deviceValidation by viewModel.deviceValidation.collectAsState()
    val providerInfo by viewModel.providerInfo.collectAsState()
    
    // Don't auto-initialize on screen load - let user click "Connect Wallet" button
    // LaunchedEffect(Unit) {
    //     android.util.Log.d("SeedVaultScreen", "🚀 SeedVaultScreen LaunchedEffect triggered - initializing Seed Vault")
    //     android.util.Log.d("SeedVaultScreen", "ViewModel instance: $viewModel")
    //     android.util.Log.d("SeedVaultScreen", "Activity: $activity")
    //     android.util.Log.d("SeedVaultScreen", "ActivityResultLauncher: $activityResultLauncher")
    //     android.util.Log.d("SeedVaultScreen", "About to call viewModel.initializeSeedVault...")
    //     viewModel.initializeSeedVault(activity, activityResultLauncher)
    //     android.util.Log.d("SeedVaultScreen", "Called viewModel.initializeSeedVault")
    // }
    
    // Don't auto-navigate - let user see the demo and manually continue
    // LaunchedEffect(publicKey, walletConnected) {
    //     if (publicKey != null && walletConnected && onNavigateToSuccess != null) {
    //         // Small delay to let user see the success state
    //         kotlinx.coroutines.delay(1000)
    //         onNavigateToSuccess()
    //     }
    // }
    
    // Handle successful connection
    LaunchedEffect(publicKey, walletConnected, isAuthorized) {
        android.util.Log.d("SeedVaultScreen", "Connection state - publicKey: $publicKey, walletConnected: $walletConnected, isAuthorized: $isAuthorized")
        if (publicKey != null && isAuthorized && onNavigateToSuccess != null) {
            android.util.Log.d("SeedVaultScreen", "✅ Seed Vault connection successful, navigating to success")
            kotlinx.coroutines.delay(1500) // Show success state briefly
            onNavigateToSuccess()
        }
    }
    
    // Handle connection errors
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            android.util.Log.e("SeedVaultScreen", "❌ Seed Vault connection error: $errorMessage")
            if (onNavigateToFailure != null) {
                kotlinx.coroutines.delay(2000) // Show error briefly
                onNavigateToFailure(errorMessage, "Seed Vault connection failed")
            }
        }
    }
    
    // Animation for the hardware wallet icon
    val infiniteTransition = rememberInfiniteTransition(label = "hardware_wallet")
    val bounceProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    val bounceHeight = 12.dp
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrueTapBackground
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Tappy with bounce animation
            Box(
                modifier = Modifier
                    .height(120.dp + bounceHeight + 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Image(
                    painter = painterResource(id = R.drawable.truetap_logo),
                    contentDescription = "TrueTap Mascot",
                    modifier = Modifier
                        .size(90.dp)
                        .offset(y = -(bounceProgress * bounceHeight.value).dp)
                        .graphicsLayer {
                            scaleX = 1f - (1f - bounceProgress) * 0.01f
                            scaleY = 1f + (1f - bounceProgress) * 0.005f
                        },
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header Text
            Text(
                text = "Connect Your Wallet",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TrueTapPrimary,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Use your Seeker to securely connect to TrueTap",
                fontSize = 18.sp,
                color = TrueTapTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            // Connection Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = TrueTapContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Use Seeker device icon instead of generic icons
                    Image(
                        painter = painterResource(id = R.drawable.skr),
                        contentDescription = "Seeker Device",
                        modifier = Modifier.size(40.dp),
                        contentScale = ContentScale.Fit
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = if (walletConnected) "Seeker Connected" else if (isLoading) "Connecting..." else "Connect Your Seeker",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapTextPrimary
                        )
                        
                        Text(
                            text = if (walletConnected) "Your wallet is ready to use" 
                                   else if (isLoading) "Please wait while we connect"
                                   else "Follow the prompts on your device",
                            fontSize = 14.sp,
                            color = TrueTapTextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        
            // Wallet Setup Section
            if (!walletConnected) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = TrueTapContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ready to Connect",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Your Seeker will guide you through connecting your wallet to TrueTap",
                            fontSize = 14.sp,
                            color = TrueTapTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                if (!isLoading) {
                                    android.util.Log.d("SeedVaultScreen", "🔄 Connect Wallet button clicked - starting connection sequence")
                                    // Clear any previous errors
                                    viewModel.clearError()
                                    // Start the connection process
                                    viewModel.initializeSeedVault(activity, activityResultLauncher)
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TrueTapPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Connecting...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            } else {
                                Text(
                                    text = "Connect Wallet",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            } else {
                // Connected State - Show Success
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = TrueTapSuccess.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "✓",
                            fontSize = 32.sp,
                            color = TrueTapSuccess
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Wallet Connected!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "You're ready to make secure payments with TrueTap",
                            fontSize = 14.sp,
                            color = TrueTapTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        
            Spacer(modifier = Modifier.height(24.dp))
            
            // Navigation Actions
            if (onNavigateToSuccess != null || onNavigateBack != null) {
                Column {
                    // Continue button (only show if wallet is connected)
                    if (walletConnected && onNavigateToSuccess != null) {
                        Button(
                            onClick = { onNavigateToSuccess() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TrueTapPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "Start Using TrueTap",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Back button
                    if (onNavigateBack != null) {
                        TextButton(
                            onClick = { onNavigateBack() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Use a Different Wallet",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TrueTapTextSecondary
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        
            // Error Display
            error?.let { errorMessage ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = TrueTapError.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️",
                            fontSize = 24.sp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Unable to Connect",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapError,
                            textAlign = TextAlign.Center
                        )
                        
                        Text(
                            text = "Please check your Seeker device and try again",
                            fontSize = 14.sp,
                            color = TrueTapTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        TextButton(
                            onClick = { viewModel.clearError() }
                        ) {
                            Text(
                                text = "Try Again",
                                color = TrueTapPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}