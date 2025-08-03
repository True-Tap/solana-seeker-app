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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 2000),
        label = "splash_alpha"
    )
    
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.3f,
        animationSpec = tween(durationMillis = 1000),
        label = "splash_scale"
    )
    
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500)
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
            // TrueTap Logo - no box, no shadow, clean
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.truetap_logo),
                contentDescription = "TrueTap Logo",
                modifier = Modifier.size(160.dp),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(Spacing.xlarge))
            
            // Brand Name with gradient
            Text(
                text = "TrueTap",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alphaAnim.value),
                color = TrueTapPrimary
            )
            
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            // Tagline - bigger and more prominent
            Text(
                text = "Payments. Reimagined.",
                color = TrueTapTextSecondary,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
    }
} 