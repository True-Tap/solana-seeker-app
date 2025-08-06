package com.truetap.solana.seeker.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.delay
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.TrueTapPrimary
import com.truetap.solana.seeker.ui.theme.TrueTapBackground
import com.truetap.solana.seeker.ui.theme.TrueTapTextPrimary
import com.truetap.solana.seeker.ui.theme.TrueTapTextSecondary
import com.truetap.solana.seeker.ui.theme.TrueTapShadow
import com.truetap.solana.seeker.ui.theme.Spacing

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startTextAnimation by remember { mutableStateOf(false) }
    
    // Text fade-in animation - starts after a short delay to sync with system splash
    val textAlphaAnim = animateFloatAsState(
        targetValue = if (startTextAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800, delayMillis = 400),
        label = "text_alpha"
    )
    
    // Subtle scale animation for logo (from system splash size to final size)
    val logoScaleAnim = animateFloatAsState(
        targetValue = if (startTextAnimation) 1f else 0.8f,
        animationSpec = tween(durationMillis = 600, delayMillis = 200),
        label = "logo_scale"
    )
    
    LaunchedEffect(key1 = true) {
        // Small delay to allow system splash to settle
        delay(200)
        startTextAnimation = true
        
        // Complete splash after animations finish
        delay(2000)
        onSplashComplete()
    }
    
    // Full screen beige background - no gaps
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TrueTapBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // TrueTap Logo - matches system splash screen size/position initially
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.truetap_logo),
                contentDescription = "TrueTap Logo",
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScaleAnim.value),
                contentScale = ContentScale.Fit
            )
            
            // Brand Name with gradient - fades in after system splash
            GradientText(
                text = "Welcome to TrueTap",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.alpha(textAlphaAnim.value)
            )
            
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            // Tagline - fades in with the brand name
            Text(
                text = "Payments. Reimagined.",
                color = TrueTapTextSecondary,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.alpha(textAlphaAnim.value)
            )
        }
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