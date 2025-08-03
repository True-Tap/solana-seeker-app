package com.truetap.solana.seeker.ui.screens.wallet

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
// import androidx.hilt.navigation.compose.hiltViewModel
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import kotlinx.coroutines.delay

data class WalletConfig(
    val id: String,
    val name: String,
    val logoRes: Int,
    val gradient: List<Color>,
    val description: String
)

sealed class PairingConnectionState {
    object Connecting : PairingConnectionState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingScreen(
    walletId: String,
    onNavigateToSuccess: () -> Unit,
    onNavigateToFailure: (String, String?) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel? = null
) {
    val context = LocalContext.current
    var connectionState by remember { mutableStateOf<PairingConnectionState>(PairingConnectionState.Connecting) }
    
    // Wallet configurations
    val walletConfigs = mapOf(
        "phantom" to WalletConfig(
            id = "phantom",
            name = "Phantom",
            logoRes = R.drawable.phantom,
            gradient = listOf(Color(0xFFAB9FF2), Color(0xFF7B61FF)),
            description = "Most popular Solana wallet"
        ),
        "solflare" to WalletConfig(
            id = "solflare", 
            name = "Solflare",
            logoRes = R.drawable.solflare,
            gradient = listOf(Color(0xFFFFC107), Color(0xFFFF6B00)),
            description = "Feature-rich Solana wallet"
        ),
        "solana" to WalletConfig(
            id = "solana",
            name = "Solana Seeker",
            logoRes = R.drawable.skr,
            gradient = listOf(Color(0xFF00D4FF), Color(0xFF00FFA3)),
            description = "Official Solana wallet"
        )
    )
    
    val walletConfig = walletConfigs[walletId]
    
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
    
    // Connection process - simplified to just show connecting state
    LaunchedEffect(walletId) {
        // The actual connection logic will be handled by the navigation
        // This screen just shows the connecting animation
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
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrueTapBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = fadeAnim),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = "Syncing",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapTextPrimary,
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
                    color = TrueTapTextSecondary
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
            tint = TrueTapPrimary
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
            Image(
                painter = painterResource(id = walletConfig.logoRes),
                contentDescription = "${walletConfig.name} logo",
                modifier = Modifier.size(circleSize),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun ConnectingTextContent(
    walletConfig: WalletConfig,
    dotAnimations: List<State<Float>>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "We're better together!",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Syncing with ${walletConfig.name}",
            fontSize = 18.sp,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
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
                            TrueTapPrimary.copy(alpha = animatedOpacity.value),
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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrueTapBackground
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
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TrueTapTextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = subtitle,
                fontSize = 16.sp,
                color = TrueTapTextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TrueTapPrimary
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