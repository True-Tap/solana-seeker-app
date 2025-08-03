package com.truetap.solana.seeker.ui.screens.genesis

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import kotlin.math.max as maxOf
import kotlin.math.min as minOf

@Composable
fun GenesisTokenScreen(
    onLinkGenesis: () -> Unit,
    onContinueWithout: () -> Unit,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val hapticFeedback = LocalHapticFeedback.current

    // Animation values - start with true as fallback to prevent blank screen
    var startAnimation by remember { mutableStateOf(true) }
    val fadeAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "fade_animation"
    )
    val textFade = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 200),
        label = "text_fade"
    )
    val buttonScale = remember { Animatable(1f) }
    
    // Swaying animation for questiontap
    val infiniteTransition = rememberInfiniteTransition(label = "sway")
    val swayAnimation by infiniteTransition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    // Responsive sizing calculations - even bigger question tappy (increased from 0.6f to 0.65f)
    val iconSize = minOf(screenWidth.value * 0.65f, screenHeight.value * 0.35f).dp
    val titleSize = maxOf(screenWidth.value * 0.08f, 36f).sp
    val subtitleSize = maxOf(screenWidth.value * 0.045f, 18f).sp
    val pillTextSize = maxOf(screenWidth.value * 0.035f, 13f).sp
    val buttonTextSize = maxOf(screenWidth.value * 0.045f, 18f).sp
    val secondaryButtonTextSize = maxOf(screenWidth.value * 0.04f, 16f).sp

    // Responsive spacing
    val containerPaddingHorizontal = screenWidth * 0.06f
    val containerPaddingTop = screenHeight * 0.07f
    val containerPaddingBottom = screenHeight * 0.05f
    val headerMarginTop = screenHeight * 0.02f
    val titleMarginBottom = screenHeight * 0.015f
    val iconSectionMarginVertical = screenHeight * 0.05f
    val iconMarginBottom = screenHeight * 0.04f
    val featurePillsMaxWidth = screenWidth * 0.8f
    val pillPaddingHorizontal = screenWidth * 0.04f
    val pillPaddingVertical = screenHeight * 0.01f
    val primaryButtonMarginBottom = screenHeight * 0.02f
    val primaryButtonPaddingVertical = screenHeight * 0.022f
    val primaryButtonPaddingHorizontal = screenWidth * 0.08f
    val secondaryButtonPaddingVertical = screenHeight * 0.02f

    // Animation trigger removed - content starts visible to prevent blank screen

    val handleLinkGenesis: () -> Unit = {
        onLinkGenesis()
    }

    // Handle back button
    BackHandler {
        onBack?.invoke()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .alpha(fadeAnim.value)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top spacing for camera - further reduced to move Tappy higher
            Spacer(modifier = Modifier.height(screenHeight * 0.04f))
            
            // Header Section - moved to top
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textFade.value)
            ) {
                Text(
                    text = "Genesis Token",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = titleSize,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = TrueTapTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = titleMarginBottom)
                )
                Text(
                    text = "Connect your Genesis Token for\nexclusive features",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = subtitleSize,
                        lineHeight = 26.sp
                    ),
                    color = TrueTapTextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(screenHeight * 0.08f))

            // Icon Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // QuestionTap Image with sway animation
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .graphicsLayer {
                            rotationZ = swayAnimation
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.question),
                        contentDescription = "QuestionTap",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                
                Spacer(modifier = Modifier.height(iconMarginBottom))

                // Feature Pills
                Row(
                    modifier = Modifier.width(featurePillsMaxWidth),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    FeaturePill(
                        text = "Early Access",
                        fontSize = pillTextSize,
                        paddingHorizontal = pillPaddingHorizontal,
                        paddingVertical = pillPaddingVertical
                    )
                    FeaturePill(
                        text = "Exclusive Rewards",
                        fontSize = pillTextSize,
                        paddingHorizontal = pillPaddingHorizontal,
                        paddingVertical = pillPaddingVertical
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FeaturePill(
                    text = "Premium Support",
                    fontSize = pillTextSize,
                    paddingHorizontal = pillPaddingHorizontal,
                    paddingVertical = pillPaddingVertical
                )
            }

            // Push buttons to bottom area but keep them together
            Spacer(modifier = Modifier.weight(1f))
            
            // Primary Button - Connect Wallet style
            Button(
                onClick = handleLinkGenesis,
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
                    text = "Link Genesis Token",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Reduced spacing between buttons to move Link Genesis Token slightly above
            Spacer(modifier = Modifier.height(screenHeight * 0.015f))

            // Secondary Button - near navigation bar
            TextButton(
                onClick = onContinueWithout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = screenHeight * 0.02f),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TrueTapTextSecondary
                )
            ) {
                Text(
                    text = "Continue without Genesis Token",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = secondaryButtonTextSize,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}

@Composable
private fun FeaturePill(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    paddingHorizontal: androidx.compose.ui.unit.Dp,
    paddingVertical: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = TrueTapPrimary.copy(alpha = 0.1f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold
            ),
            color = TrueTapPrimary,
            modifier = Modifier.padding(
                horizontal = paddingHorizontal,
                vertical = paddingVertical
            ),
            textAlign = TextAlign.Center
        )
    }
}