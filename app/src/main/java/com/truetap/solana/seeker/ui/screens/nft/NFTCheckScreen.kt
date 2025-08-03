package com.truetap.solana.seeker.ui.screens.nft

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min as minOf
import kotlin.math.max as maxOf
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import kotlinx.coroutines.delay

/**
 * NFT Check Screen - Jetpack Compose
 * Verifies user's NFT holdings with smooth animations
 * Converted from React Native TSX implementation
 */

@Composable
fun NFTCheckScreen(
    onNavigateToSuccess: () -> Unit,
    onNavigateToFailure: () -> Unit
) {
    // Screen dimensions for responsive design
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    // Calculate responsive sizes
    val baseUnit = minOf(screenWidth.value, screenHeight.value) * 0.01f
    val tappySize = minOf(screenWidth.value * 0.35f, screenHeight.value * 0.2f).dp
    val titleSize = maxOf(baseUnit * 2.4f, 20f).sp
    val walletLabelSize = maxOf(baseUnit * 1.3f, 12f).sp
    val walletAddressSize = maxOf(baseUnit * 1.4f, 13f).sp
    val dotSize = maxOf(baseUnit * 0.8f, 6f).dp
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "NFTCheckAnimations")
    
    // Main fade-in animation
    val fadeAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "FadeIn"
    )
    
    // Tappy entrance animations
    val tappyScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500, delayMillis = 200),
        label = "TappyScale"
    )
    
    // Tappy breathing animation
    val tappyBreathing by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TappyBreathing"
    )
    
    // Tappy floating animation
    val tappyFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -screenHeight.value * 0.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TappyFloat"
    )
    
    // Text fade-in animation
    val textFade by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400, delayMillis = 600),
        label = "TextFade"
    )
    
    // Dot animations
    val dot1Animation by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot1"
    )
    
    val dot2Animation by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot2"
    )
    
    val dot3Animation by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot3"
    )
    
    val dot1Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot1Scale"
    )
    
    val dot2Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot2Scale"
    )
    
    val dot3Scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Dot3Scale"
    )
    
    // Auto navigation effect
    LaunchedEffect(Unit) {
        delay(4000) // Wait 4 seconds
        // TODO: Replace with actual NFT check logic
        val hasNFTs = true // Simulate successful verification
        
        if (hasNFTs) {
            onNavigateToSuccess()
        } else {
            onNavigateToFailure()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueTapBackground)
            .graphicsLayer { alpha = fadeAnimation },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = screenWidth * 0.05f)
                .padding(
                    top = screenHeight * 0.08f,
                    bottom = screenHeight * 0.06f
                )
        ) {
            
            // Tappy Icon Section
            Box(
                modifier = Modifier
                    .size(tappySize)
                    .graphicsLayer {
                        scaleX = tappyScale * tappyBreathing
                        scaleY = tappyScale * tappyBreathing
                        translationY = tappyFloat
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tappycomp),
                    contentDescription = "Tappy Character",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(screenHeight * 0.06f))
            
            // Text Section
            Box(
                modifier = Modifier.graphicsLayer { alpha = textFade }
            ) {
                Text(
                    text = "Searching for your Genesis NFT",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(screenHeight * 0.09f))
            
            // Loading Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(dotSize * 0.5f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dot 1
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .scale(dot1Scale)
                        .background(
                            TrueTapPrimary.copy(alpha = dot1Animation),
                            CircleShape
                        )
                )
                
                // Dot 2
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .scale(dot2Scale)
                        .background(
                            TrueTapPrimary.copy(alpha = dot2Animation),
                            CircleShape
                        )
                )
                
                // Dot 3
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .scale(dot3Scale)
                        .background(
                            TrueTapPrimary.copy(alpha = dot3Animation),
                            CircleShape
                        )
                )
            }
        }
        
        // Wallet Info at Bottom
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = screenHeight * 0.12f)
        ) {
            Text(
                text = "Wallet",
                fontSize = walletLabelSize,
                color = TrueTapTextInactive,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "DYw8...hxGo",
                fontSize = walletAddressSize,
                fontWeight = FontWeight.Medium,
                color = TrueTapPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}