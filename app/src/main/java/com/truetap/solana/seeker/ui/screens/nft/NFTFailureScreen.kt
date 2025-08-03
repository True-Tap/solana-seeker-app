package com.truetap.solana.seeker.ui.screens.nft

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
 * NFT Failure Screen - Jetpack Compose
 * Shows when no Genesis NFT is found with expandable info section
 * Converted from React Native TSX implementation
 */

@Composable
fun NFTFailureScreen(
    onRetry: () -> Unit,
    onContinueWithoutNFT: () -> Unit
) {
    val context = LocalContext.current
    
    // Screen dimensions for responsive design
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    // Calculate responsive sizes
    val baseUnit = minOf(screenWidth.value, screenHeight.value) * 0.01f
    val imageSize = minOf(screenWidth.value * 0.6f, screenHeight.value * 0.35f).dp
    
    val titleSize = maxOf(baseUnit * 3.2f, 28f).sp
    val subtitleSize = maxOf(baseUnit * 1.6f, 16f).sp
    val walletAddressSize = maxOf(baseUnit * 1.4f, 14f).sp
    val infoToggleSize = maxOf(baseUnit * 1.4f, 14f).sp
    val infoTextSize = maxOf(baseUnit * 1.3f, 13f).sp
    val buttonTextSize = maxOf(baseUnit * 1.8f, 18f).sp
    val continueButtonSize = maxOf(baseUnit * 1.6f, 16f).sp
    
    // State for expandable info
    var isInfoExpanded by remember { mutableStateOf(false) }
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "NFTFailureAnimations")
    
    // Main fade-in animation
    val fadeAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "FadeIn"
    )
    
    // Image entrance animations
    val imageScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(500, delayMillis = 200),
        label = "ImageScale"
    )
    
    // Image breathing animation
    val imageBreathing by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ImageBreathing"
    )
    
    // Image floating animation
    val imageFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -screenHeight.value * 0.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ImageFloat"
    )
    
    // Text fade-in animation
    val textFade by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(400, delayMillis = 600),
        label = "TextFade"
    )
    
    // Button press animation
    var buttonScale by remember { mutableFloatStateOf(1f) }
    val buttonScaleAnimation by animateFloatAsState(
        targetValue = buttonScale,
        animationSpec = tween(100),
        label = "ButtonScale"
    )
    
    // Arrow rotation for info toggle
    val arrowRotation by animateFloatAsState(
        targetValue = if (isInfoExpanded) 180f else 0f,
        animationSpec = tween(300),
        label = "ArrowRotation"
    )
    
    fun handleRetry() {
        // Vibration feedback
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(30)
        }
        
        // Button press animation
        buttonScale = 0.95f
        // Reset scale after animation
        buttonScale = 1f
        onRetry()
    }
    
    fun handleContinue() {
        try {
            // Safe vibration feedback
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    if (it.hasVibrator()) {
                        it.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    if (it.hasVibrator()) {
                        it.vibrate(30)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
        
        onContinueWithoutNFT()
    }
    
    fun toggleInfo() {
        try {
            // Safe vibration feedback
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    if (it.hasVibrator()) {
                        it.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    if (it.hasVibrator()) {
                        it.vibrate(30)
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
        
        isInfoExpanded = !isInfoExpanded
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueTapBackground)
            .graphicsLayer { alpha = fadeAnimation }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = screenWidth * 0.06f)
                .padding(
                    top = screenHeight * 0.08f,
                    bottom = screenHeight * 0.06f
                )
        ) {
            
            Spacer(modifier = Modifier.height(screenHeight * 0.05f))
            
            // NoPicasso Image Section
            Box(
                modifier = Modifier
                    .size(imageSize)
                    .graphicsLayer {
                        scaleX = imageScale * imageBreathing
                        scaleY = imageScale * imageBreathing
                        translationY = imageFloat
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nopicasso),
                    contentDescription = "No Picasso",
                    modifier = Modifier.size(imageSize)
                )
            }
            
            Spacer(modifier = Modifier.height(screenHeight * 0.05f))
            
            // Message Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer { alpha = textFade }
                    .weight(1f)
            ) {
                Text(
                    text = "No Picasso's Here",
                    fontSize = titleSize,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.015f))
                
                Text(
                    text = "We couldn't find a Genesis NFT in your wallet",
                    fontSize = subtitleSize,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.01f))
                
                Text(
                    text = "DYw8...hxGo",
                    fontSize = walletAddressSize,
                    fontWeight = FontWeight.Medium,
                    color = TrueTapPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(screenHeight * 0.04f))
                
                // Expandable Info Section
                Column(
                    modifier = Modifier.widthIn(max = 320.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { toggleInfo() },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = TrueTapPrimary.copy(alpha = 0.08f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TrueTapPrimary.copy(alpha = 0.2f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "What is a Genesis NFT?",
                                fontSize = infoToggleSize,
                                fontWeight = FontWeight.SemiBold,
                                color = TrueTapPrimary
                            )
                            
                            Text(
                                text = "▼",
                                fontSize = infoToggleSize * 0.85f,
                                color = TrueTapPrimary,
                                modifier = Modifier.rotate(arrowRotation)
                            )
                        }
                    }
                    
                    // Expandable content
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        shape = RoundedCornerShape(
                            topStart = 0.dp,
                            topEnd = 0.dp,
                            bottomStart = 12.dp,
                            bottomEnd = 12.dp
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = TrueTapPrimary.copy(alpha = 0.04f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            TrueTapPrimary.copy(alpha = 0.2f)
                        )
                    ) {
                        if (isInfoExpanded) {
                            Text(
                                text = "Genesis NFTs are exclusive tokens for early Solana Seeker adopters. They unlock premium features, early access to new capabilities, and exclusive rewards within the TrueTap ecosystem.",
                                fontSize = infoTextSize,
                                color = TrueTapTextSecondary,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
            
            // Action Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Retry Button
                Button(
                    onClick = { handleRetry() },
                    modifier = Modifier
                        .scale(buttonScaleAnimation)
                        .widthIn(min = 200.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TrueTapPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Text(
                        text = "Retry",
                        fontSize = buttonTextSize,
                        fontWeight = FontWeight.Bold,
                        color = TrueTapContainer,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(screenHeight * 0.02f))
                
                // Continue Without NFT Text
                Text(
                    text = "Continue Without Genesis NFT",
                    fontSize = continueButtonSize,
                    fontWeight = FontWeight.Medium,
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { handleContinue() }
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                )
            }
        }
    }
}