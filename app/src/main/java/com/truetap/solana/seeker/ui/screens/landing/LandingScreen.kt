package com.truetap.solana.seeker.ui.screens.landing

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import com.truetap.solana.seeker.viewmodels.WalletViewModel
import com.truetap.solana.seeker.data.AuthState
import kotlin.math.max as maxOf
import kotlin.math.min as minOf

@Composable
fun LandingScreen(
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier,
    walletViewModel: WalletViewModel = hiltViewModel()
) {
    var agreedToTerms by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    // Check if user is already connected
    val authState by walletViewModel.authState.collectAsStateWithLifecycle()
    
    // If user is already connected, skip directly to home
    LaunchedEffect(authState) {
        if (authState is AuthState.Connected) {
            onNavigateToHome()
        }
    }

    // Animation values
    var startAnimation by remember { mutableStateOf(false) }
    val fadeAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "fade_animation"
    )
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale_animation"
    )
    
    // Bouncing animation for Tappy
    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounceAnim = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    // Shadow animation
    val shadowAnim = infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadow"
    )

    // Sizing calculations - Tappy matches splash screen
    val logoSize = 160.dp  // Same as splash screen
    val titleFontSize = 48.sp  // Increased to 48sp
    val subtitleFontSize = 18.sp  // Increased from 14sp
    val buttonTextSize = 18.sp  // Increased from 16sp

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // Full screen background with click handling
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(TrueTapBackground, Color(0xFFF0ECE4))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Static Tappy to match SplashScreen positioning
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.truetap_logo),
                contentDescription = "TrueTap Logo",
                modifier = Modifier
                    .size(logoSize)
                    .alpha(fadeAnim.value)
                    .scale(scaleAnim.value),
                contentScale = ContentScale.Fit
            )

            // Welcome to TrueTap - no spacing, directly under Tappy container
            GradientText(
                text = "Welcome to TrueTap",
                fontSize = titleFontSize,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.alpha(fadeAnim.value)
            )
            
            // Space between Welcome and Payments text
            Spacer(modifier = Modifier.height(16.dp))
            
            // Payments Reimagined text
            Text(
                text = "Payments. Reimagined.",
                fontSize = subtitleFontSize,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(fadeAnim.value)
            )

            // Spacing before connect section
            Spacer(modifier = Modifier.height(80.dp))

            // Connect Wallet Button
            Button(
                onClick = { if (agreedToTerms) onNavigateToHome() },
                enabled = agreedToTerms,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (agreedToTerms) TrueTapPrimary else TrueTapTextSecondary.copy(alpha = 0.3f)
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
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Wallet",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Connect Wallet",
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
            
            // Terms and Conditions - Professional spacing following WhatsApp/Instagram pattern
            Spacer(modifier = Modifier.height(16.dp))
            
            // Terms acceptance with checkbox - following mobile UI best practices
            val uriHandler = LocalUriHandler.current
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .clickable { agreedToTerms = !agreedToTerms }, // Make entire row clickable
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = TrueTapPrimary,
                        uncheckedColor = TrueTapTextSecondary
                    )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Terms text with inline links - professional mobile pattern
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "I agree to the ",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = TrueTapTextSecondary,
                                lineHeight = 20.sp
                            )
                        )
                        Text(
                            text = "Terms of Service",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = TrueTapPrimary,
                                textDecoration = TextDecoration.Underline,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.clickable {
                                uriHandler.openUri("https://truetap.app/terms")
                            }
                        )
                        Text(
                            text = " and ",
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = TrueTapTextSecondary,
                                lineHeight = 20.sp
                            )
                        )
                    }
                    
                    Text(
                        text = "Privacy Policy",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = TrueTapPrimary,
                            textDecoration = TextDecoration.Underline,
                            lineHeight = 20.sp
                        ),
                        modifier = Modifier.clickable {
                            uriHandler.openUri("https://truetap.app/privacy")
                        }
                    )
                }
            }
            
            // Spacing before features
            Spacer(modifier = Modifier.height(40.dp))
            
            // Feature Icons Row - FULL screen width, icons above text, 1/3 distribution
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 0.dp), // NO horizontal padding for full width
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secure - exactly 1/3 width
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure",
                        tint = Color(0xFF4C4C4C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Secure",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4C4C4C),
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Dot spacer
                Text(
                    text = "•",
                    fontSize = 14.sp,
                    color = Color(0xFF4C4C4C),
                    textAlign = TextAlign.Center
                )
                
                // Instant - exactly 1/3 width
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Instant",
                        tint = Color(0xFF4C4C4C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Instant",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4C4C4C),
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
                
                // Dot spacer
                Text(
                    text = "•",
                    fontSize = 14.sp,
                    color = Color(0xFF4C4C4C),
                    textAlign = TextAlign.Center
                )
                
                // Decentralized - exactly 1/3 width
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Decentralized",
                        tint = Color(0xFF4C4C4C),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Decentralized",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4C4C4C),
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // 4dp spacing between features and "Powered by" - halved again
            Spacer(modifier = Modifier.height(4.dp))
            
            // "Powered by" text - just above swipe bar
            Text(
                text = "Powered by Solana • Built for Seeker",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4C4C4C),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Minimal spacing above swipe bar
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = TrueTapPrimary,
            modifier = Modifier
                .size(28.dp)  // Increased from 24dp
                .padding(bottom = 8.dp)  // Increased spacing to 8dp
        )
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}


/**
 * Bouncing Tappy Animation with Realistic Shadow
 * 
 * Creates a character that bounces vertically with a realistic elliptical shadow.
 * The shadow behaves like a soft spotlight shadow under a floating object.
 */
@Composable
private fun BouncingTappyWithShadow(
    modifier: Modifier = Modifier,
    tappySize: androidx.compose.ui.unit.Dp = 120.dp,
    bounceHeight: androidx.compose.ui.unit.Dp = 24.dp
) {
    // Infinite bouncing animation
    val infiniteTransition = rememberInfiniteTransition(label = "TappyBounce")
    
    // Bounce animation (0f = on ground, 1f = at peak)
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
    
    // Calculate current height (0 = ground, 1 = peak)
    val currentHeight = bounceProgress
    
    
    // Container for the bouncing system
    Box(
        modifier = modifier
            .height(tappySize + bounceHeight + 32.dp), // Total height needed
        contentAlignment = Alignment.BottomCenter
    ) {
        // Tappy character - clean bouncing animation
        Image(
            painter = painterResource(id = R.drawable.truetap_logo),
            contentDescription = "Tappy",
            modifier = Modifier
                .size(tappySize)
                .align(Alignment.BottomCenter)
                .offset(y = -(currentHeight * bounceHeight.value).dp) // Negative offset = up
                .graphicsLayer {
                    // Very subtle scale effect for impact anticipation
                    scaleX = 1f - (1f - currentHeight) * 0.01f // Barely wider when landing
                    scaleY = 1f + (1f - currentHeight) * 0.005f // Barely taller when landing
                },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun GradientText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit = 36.sp,
    fontWeight: FontWeight = FontWeight.ExtraBold,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFFFF6F00), // Orange 700
            Color(0xFFFF9800), // Orange 500
            Color(0xFFFFC107)  // Amber 500
        )
    )

    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center,
        modifier = modifier,
        style = TextStyle(
            brush = gradient
        )
    )
}