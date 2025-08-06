package com.truetap.solana.seeker.ui.screens.wallet

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
fun WalletSuccessScreen(
    onNavigateToNext: () -> Unit = {},
    onViewDetails: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Animation states
    var animationStarted by remember { mutableStateOf(false) }
    
    // Main fade animation
    val fadeAnim by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(600, easing = EaseInOut),
        label = "fade"
    )
    
    // Logo scale animation
    val logoScale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.8f,
        animationSpec = tween(500, delayMillis = 200, easing = EaseOutBack),
        label = "logo_scale"
    )
    
    // Text fade animation
    val textFade by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(400, delayMillis = 600, easing = EaseInOut),
        label = "text_fade"
    )
    
    // Checkmark animation
    var checkmarkVisible by remember { mutableStateOf(false) }
    val checkmarkScale by animateFloatAsState(
        targetValue = if (checkmarkVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkmark_scale"
    )
    
    // Continuous breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing"
    )
    
    // Continuous floating animation
    val floatingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )
    
    // Button press animation
    var buttonPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (buttonPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )
    
    // Start animations on composition
    LaunchedEffect(Unit) {
        animationStarted = true
        delay(1000) // Wait for logo to appear
        checkmarkVisible = true
        // Removed auto-navigation - user must press button to continue
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = TrueTapBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .graphicsLayer(alpha = fadeAnim),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Success Logo Section with Checkmark Overlay
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .scale(logoScale)
                    .scale(breathingScale)
                    .graphicsLayer(translationY = floatingOffset),
                contentAlignment = Alignment.Center
            ) {
                // Tappy in White Circle
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(4.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.truetap_logo),
                        contentDescription = "TrueTap Logo",
                        modifier = Modifier.size(110.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Success Checkmark Overlay - Properly Centered
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .scale(checkmarkScale)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-20).dp, y = (-20).dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(TrueTapSuccess, TrueTapSuccess.copy(alpha = 0.8f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Success Text Section
            Column(
                modifier = Modifier.graphicsLayer(alpha = textFade),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your TrueTap is now connected to Solana Seeker",
                    fontSize = 18.sp,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Action Buttons Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(alpha = textFade),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Primary Button - Continue to TrueTap
                Button(
                    onClick = { 
                        buttonPressed = true
                        onNavigateToNext()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .scale(buttonScale),
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
                            text = "Continue to Genesis Token",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-0.3).sp
                        )
                    }
                }
                
                // Secondary Button - View Wallet Details
                TextButton(
                    onClick = { 
                        buttonPressed = true
                        onViewDetails()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .scale(buttonScale)
                ) {
                    Text(
                        text = "View Wallet Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TrueTapTextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}