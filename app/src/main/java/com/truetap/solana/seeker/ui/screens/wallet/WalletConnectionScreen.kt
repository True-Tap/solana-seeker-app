package com.truetap.solana.seeker.ui.screens.wallet

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import kotlinx.coroutines.delay

data class WalletOption(
    val id: String,
    val name: String,
    val description: String,
    val logoRes: Int,
    val gradient: List<Color>
)

sealed class ConnectionState {
    object Selection : ConnectionState()
    object Success : ConnectionState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletConnectionScreen(
    onNavigateToNext: () -> Unit,
    onNavigateToPairing: (String) -> Unit,
    activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null,
    viewModel: WalletViewModel? = null
) {
    val context = LocalContext.current
    var currentState by remember { mutableStateOf<ConnectionState>(ConnectionState.Selection) }
    var selectedWallet by remember { mutableStateOf<String?>(null) }
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "wallet_connection")
    // val isLoading by viewModel?.isLoading?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) }
    // val errorMessage by viewModel?.errorMessage?.collectAsStateWithLifecycle() ?: remember { mutableStateOf<String?>(null) }
    
    // Wallet options
    val walletOptions = listOf(
        WalletOption(
            id = "phantom",
            name = "Phantom",
            description = "Most popular Solana wallet",
            logoRes = R.drawable.phantom,
            gradient = listOf(TrueTapPrimary, TrueTapPrimary)
        ),
        WalletOption(
            id = "solflare",
            name = "Solflare", 
            description = "Feature-rich Solana wallet",
            logoRes = R.drawable.solflare,
            gradient = listOf(TrueTapPrimary, TrueTapPrimary)
        ),
        WalletOption(
            id = "solana",
            name = "Solana Seeker",
            description = "Official Solana wallet",
            logoRes = R.drawable.skr,
            gradient = listOf(TrueTapPrimary, TrueTapPrimary)
        )
    )
    
    // Handle wallet selection
    val handleWalletSelect: (String) -> Unit = { walletId ->
        selectedWallet = walletId
        // Navigate directly to pairing screen - no connecting state here
        onNavigateToPairing(walletId)
    }

    // Handle creating wallet with Phantom
    val handleCreateWalletWithPhantom: () -> Unit = {
        try {
            // Check if Phantom app is installed
            val packageManager = context.packageManager
            val phantomPackageName = "app.phantom"
            
            try {
                // Try to get Phantom app info
                packageManager.getPackageInfo(phantomPackageName, 0)
                
                // Phantom is installed - navigate to pairing screen
                onNavigateToPairing("phantom")
                
            } catch (e: Exception) {
                // Phantom is not installed - open Play Store
                val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("market://details?id=$phantomPackageName")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                
                // Fallback to web Play Store if app store not available
                if (playStoreIntent.resolveActivity(packageManager) != null) {
                    context.startActivity(playStoreIntent)
                } else {
                    val webIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=$phantomPackageName")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(webIntent)
                }
            }
        } catch (e: Exception) {
            // Fallback to pairing screen if there's any error
            onNavigateToPairing("phantom")
        }
    }

    // Handle temp bypass
    val handleTempBypass: () -> Unit = {
        onNavigateToNext()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrueTapBackground
    ) {
        when (currentState) {
            ConnectionState.Selection -> WalletSelectionContent(
                walletOptions = walletOptions,
                onWalletSelect = handleWalletSelect,
                onCreateWalletWithPhantom = handleCreateWalletWithPhantom,
                onTempBypass = handleTempBypass
            )
            ConnectionState.Success -> WalletSuccessContent()
        }
    }
}

@Composable
private fun WalletSelectionContent(
    walletOptions: List<WalletOption>,
    onWalletSelect: (String) -> Unit,
    onCreateWalletWithPhantom: () -> Unit,
    onTempBypass: () -> Unit
) {
    // Bounce animation for Tappy
    val infiniteTransition = rememberInfiniteTransition(label = "TappyBounce")
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
    val currentHeight = bounceProgress
    val bounceHeight = 16.dp // Reduced bounce to prevent cutoff
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Bouncing Tappy Mascot
        Box(
            modifier = Modifier
                .height(120.dp + bounceHeight + 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(id = R.drawable.truetap_logo),
                contentDescription = "TrueTap Logo",
                modifier = Modifier
                    .size(90.dp)
                    .offset(y = -(currentHeight * bounceHeight.value).dp)
                    .graphicsLayer {
                        scaleX = 1f - (1f - currentHeight) * 0.01f
                        scaleY = 1f + (1f - currentHeight) * 0.005f
                    },
                contentScale = ContentScale.Fit
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header Text
        Text(
            text = "Choose Your Wallet",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TrueTapPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Connect to start using TrueTap",
            fontSize = 18.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Wallet List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(walletOptions) { wallet ->
                WalletOptionCard(
                    wallet = wallet,
                    onClick = { onWalletSelect(wallet.id) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Footer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = "New to Solana?",
                fontSize = 16.sp,
                color = TrueTapTextInactive,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Create Wallet Button
            TextButton(
                onClick = onCreateWalletWithPhantom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(TrueTapPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))  // Increased from 8dp
                
                Text(
                    text = "Create a new wallet in Phantom",
                    fontSize = 18.sp,  // Increased from 16sp
                    fontWeight = FontWeight.Bold,  // Changed from SemiBold
                    color = TrueTapPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Temp Bypass Button - Connect Wallet style
            Button(
                onClick = onTempBypass,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrueTapPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 3.dp
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🚀 TEMP BYPASS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Continue",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletOptionCard(
    wallet: WalletOption,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Wallet Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TrueTapPrimary),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = wallet.logoRes),
                    contentDescription = "${wallet.name} logo",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Wallet Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = wallet.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                
                Text(
                    text = wallet.description,
                    fontSize = 14.sp,
                    color = TrueTapTextSecondary,
                    lineHeight = 20.sp
                )
            }
            
            // Arrow Icon
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = TrueTapTextInactive
            )
        }
    }
}



@Composable
private fun WalletSuccessContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Success Icon
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(TrueTapSuccess, TrueTapSuccess.copy(alpha = 0.8f))
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✅",
                fontSize = 48.sp,
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Wallet Connected!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Ready to make payments",
            fontSize = 18.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}