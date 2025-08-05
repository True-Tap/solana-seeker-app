package com.truetap.solana.seeker.ui.screens.nft

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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

/**
 * NFT Success Screen - Jetpack Compose
 * Shows when Genesis NFT is found with celebration animations
 * Converted from React Native TSX implementation
 */

@Composable
fun NFTSuccessScreen(
    onContinue: () -> Unit
) {
    val context = LocalContext.current

    
    // Screen dimensions for responsive design
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    // Calculate responsive sizes
    val baseUnit = minOf(screenWidth.value, screenHeight.value) * 0.01f
    val nftFrameSize = minOf(screenWidth.value * 0.55f, screenHeight.value * 0.3f).dp
    
    val titleSize = maxOf(baseUnit * 3.2f, 28f).sp
    val subtitleSize = maxOf(baseUnit * 1.6f, 16f).sp
    val nftNameSize = maxOf(baseUnit * 1.8f, 18f).sp
    val walletAddressSize = maxOf(baseUnit * 1.4f, 14f).sp
    val benefitsTitleSize = maxOf(baseUnit * 1.6f, 16f).sp
    val benefitTextSize = maxOf(baseUnit * 1.4f, 14f).sp
    val buttonTextSize = maxOf(baseUnit * 1.8f, 18f).sp
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "NFTSuccessAnimations")
    
    // Main fade-in animation
    val fadeAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "FadeIn"
    )
    
    // NFT entrance animations
    val nftScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500, delayMillis = 200),
        label = "NFTScale"
    )
    
    // NFT breathing animation
    val nftBreathing by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "NFTBreathing"
    )
    
    // NFT floating animation
    val nftFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -screenHeight.value * 0.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "NFTFloat"
    )
    
    // Text fade-in animation
    val textFade by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400, delayMillis = 600),
        label = "TextFade"
    )
    
    
    // Sparkle animations
    val sparkle1Animation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Sparkle1"
    )
    
    val sparkle2Animation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Sparkle2"
    )
    
    val sparkle3Animation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Sparkle3"
    )
    
    // Button press animation
    var buttonScale by remember { mutableFloatStateOf(1f) }
    val buttonScaleAnimation by animateFloatAsState(
        targetValue = buttonScale,
        animationSpec = tween(100),
        label = "ButtonScale"
    )
    
    fun handleContinue() {
        // Vibration feedback
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
        
        // Button press animation
        buttonScale = 0.95f
        // Reset scale after animation
        buttonScale = 1f
        onContinue()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueTapBackground)
            .graphicsLayer { alpha = fadeAnimation }
    ) {
        // Scrollable content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = screenWidth * 0.06f)
                .padding(
                    top = screenHeight * 0.08f,
                    bottom = 100.dp // Space for fixed button
                )
        ) {
            
            Spacer(modifier = Modifier.height(screenHeight * 0.07f))
            
            // NFT Display Section
            Box(
                modifier = Modifier
                    .size(nftFrameSize)
                    .graphicsLayer {
                        scaleX = nftScale * nftBreathing
                        scaleY = nftScale * nftBreathing
                        translationY = nftFloat
                    },
                contentAlignment = Alignment.Center
            ) {
                // NFT Image
                Image(
                    painter = painterResource(id = R.drawable.exclatap),
                    contentDescription = "Genesis NFT",
                    modifier = Modifier.size(nftFrameSize)
                )
                
                // Sparkles - using Phosphor-like icons
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Sparkle",
                    tint = TrueTapPrimary,
                    modifier = Modifier
                        .size((nftFrameSize.value * 0.1f).dp)
                        .offset(x = nftFrameSize * 0.4f, y = -nftFrameSize * 0.4f)
                        .graphicsLayer {
                            alpha = sparkle1Animation
                            translationY = -10f * sparkle1Animation
                            rotationZ = 180f * sparkle1Animation
                        }
                )
                
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Sparkle",
                    tint = TrueTapPrimary,
                    modifier = Modifier
                        .size((nftFrameSize.value * 0.08f).dp)
                        .offset(x = -nftFrameSize * 0.4f, y = nftFrameSize * 0.4f)
                        .graphicsLayer {
                            alpha = sparkle2Animation
                            translationY = -10f * sparkle2Animation
                            rotationZ = 180f * sparkle2Animation
                        }
                )
                
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Sparkle",
                    tint = TrueTapPrimary,
                    modifier = Modifier
                        .size((nftFrameSize.value * 0.07f).dp)
                        .offset(x = -nftFrameSize * 0.5f, y = -nftFrameSize * 0.3f)
                        .graphicsLayer {
                            alpha = sparkle3Animation
                            translationY = -10f * sparkle3Animation
                            rotationZ = 180f * sparkle3Animation
                        }
                )
            }
            
            Spacer(modifier = Modifier.height(screenHeight * 0.05f))
            
            // Message Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer { alpha = textFade }
            ) {
                Text(
                    text = "Found it!",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.01f))
                
                Text(
                    text = "Your Genesis NFT has been verified",
                    fontSize = subtitleSize,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.02f))
                
                Text(
                    text = "Genesis NFT #1234",
                    fontSize = nftNameSize,
                    fontWeight = FontWeight.SemiBold,
                    color = TrueTapPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.005f))
                
                Text(
                    text = "DYw8...hxGo",
                    fontSize = walletAddressSize,
                    color = TrueTapTextInactive,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Benefits Container - Always Visible
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TrueTapPrimary.copy(alpha = 0.06f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Your Exclusive Benefits:",
                        fontSize = benefitsTitleSize,
                        fontWeight = FontWeight.SemiBold,
                        color = TrueTapTextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Benefits List
                    BenefitItem(
                        icon = Icons.Default.TrackChanges,
                        text = "Early Access to New Features",
                        fontSize = benefitTextSize
                    )
                    
                    BenefitItem(
                        icon = Icons.Default.Diamond,
                        text = "Premium Rewards Program",
                        fontSize = benefitTextSize
                    )
                    
                    BenefitItem(
                        icon = Icons.Default.Bolt,
                        text = "Priority Support",
                        fontSize = benefitTextSize
                    )
                    
                    BenefitItem(
                        icon = Icons.Default.CardGiftcard,
                        text = "Exclusive Airdrops",
                        fontSize = benefitTextSize
                    )
                }
            }
            
            // Add equal weight spacing to match the top spacing
            Spacer(modifier = Modifier.weight(1f))
            
            // Extra bottom spacing to account for the fixed button
            Spacer(modifier = Modifier.height(80.dp))
        }
        
        // Fixed Continue Button at bottom - Exact Connect Wallet style
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = screenHeight * 0.06f),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { handleContinue() },
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
                Text(
                    text = "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BenefitItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TrueTapPrimary,
            modifier = Modifier
                .size(28.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = text,
            fontSize = fontSize,
            color = TrueTapTextSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}