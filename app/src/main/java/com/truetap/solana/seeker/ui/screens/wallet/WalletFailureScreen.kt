package com.truetap.solana.seeker.ui.screens.wallet

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletFailureScreen(
    walletId: String? = null,
    errorMessage: String? = null,
    onTryAgain: () -> Unit = {},
    onTryDifferentWallet: () -> Unit = {},
    onGetHelp: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Animation states
    var animationStarted by remember { mutableStateOf(false) }
    
    // Main fade animation
    val fadeAnim by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(500, easing = EaseInOut),
        label = "fade"
    )
    
    // Icon scale and shake animation
    val iconScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.8f,
        animationSpec = tween(600, delayMillis = 200, easing = EaseOutBack),
        label = "icon_scale"
    )
    
    // Shake animation
    var shakeOffset by remember { mutableStateOf(0f) }
    val shakeAnim by animateFloatAsState(
        targetValue = shakeOffset,
        animationSpec = tween(100, easing = EaseInOut),
        label = "shake"
    ) { shakeOffset = 0f }
    
    // Error bounce animation
    val infiniteTransition = rememberInfiniteTransition(label = "error_bounce")
    val errorBounce by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "error_bounce"
    )
    
    // Text fade animation
    val textFade by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(600, delayMillis = 400, easing = EaseInOut),
        label = "text_fade"
    )
    
    // Button animations
    var buttonsVisible by remember { mutableStateOf(false) }
    val buttonOpacity by animateFloatAsState(
        targetValue = if (buttonsVisible) 1f else 0f,
        animationSpec = tween(400, easing = EaseInOut),
        label = "button_opacity"
    )
    
    val buttonScale by animateFloatAsState(
        targetValue = if (buttonsVisible) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "button_scale"
    )
    
    // Button press animations
    var primaryButtonPressed by remember { mutableStateOf(false) }
    val primaryButtonScale by animateFloatAsState(
        targetValue = if (primaryButtonPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "primary_button_scale"
    )
    
    // Get wallet name from walletId
    fun getWalletName(id: String?): String {
        return when (id) {
            "phantom" -> "Phantom"
            "solflare" -> "Solflare"
            "external" -> "External Wallet"
            "solana" -> "Solana Seeker"
            else -> "Unknown Wallet"
        }
    }
    
    // Get wallet category-specific error message
    fun getCategorySpecificErrorMessage(id: String?, errorMessage: String?): String {
        return when {
            errorMessage != null -> errorMessage
            id == "external" -> "Unable to connect to your external wallet. Please ensure you have a compatible wallet like Phantom or Solflare installed and try again."
            id == "solana" -> "Unable to connect to your Solana Seeker hardware wallet. Please check your device connection and try again."
            else -> "Unable to connect to your wallet. Please check your connection and try again."
        }
    }
    
    // Start animations and shake effect on composition
    LaunchedEffect(Unit) {
        // Simulate haptic feedback with animation
        val shakeSequence = listOf(-10f, 10f, -10f, 10f, -10f, 10f, 0f)
        shakeSequence.forEach { offset ->
            shakeOffset = offset
            delay(50)
        }
        
        animationStarted = true
        delay(800)
        buttonsVisible = true
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrueTapBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp)
                .graphicsLayer(alpha = fadeAnim),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Error Logo with Bottom Right X Icon
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(iconScale)
                    .graphicsLayer(translationX = shakeAnim),
                contentAlignment = Alignment.Center
            ) {
                // Tappy in White Circle
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(4.dp, Color.Gray.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.truetap_logo),
                        contentDescription = "TrueTap Logo",
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Red Error Circle with X - Properly Centered
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .scale(errorBounce)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-15).dp, y = (-15).dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF4757),
                                        Color(0xFFFF3742)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Error",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Error Text
            Column(
                modifier = Modifier.graphicsLayer(alpha = textFade),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = getCategorySpecificErrorMessage(walletId, errorMessage),
                    fontSize = 18.sp,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Wallet: ${getWalletName(walletId)}",
                    fontSize = 14.sp,
                    color = TrueTapTextInactive,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(
                        alpha = buttonOpacity,
                        scaleX = buttonScale,
                        scaleY = buttonScale
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Primary Button - Try Again
                Button(
                    onClick = { 
                        primaryButtonPressed = true
                        onTryAgain()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(primaryButtonScale),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        TrueTapPrimary,
                                        TrueTapPrimary.copy(alpha = 0.9f),
                                        TrueTapPrimary
                                    )
                                ),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Try Again",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.3).sp
                        )
                    }
                }
                
                // Secondary Button - Try Different Wallet
                Button(
                    onClick = onTryDifferentWallet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Try Different Wallet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary,
                        letterSpacing = (-0.3).sp
                    )
                }
                
                // Tertiary Button - Get Help
                TextButton(
                    onClick = onGetHelp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Get Help",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrueTapTextSecondary
                    )
                }
            }
        }
    }
}