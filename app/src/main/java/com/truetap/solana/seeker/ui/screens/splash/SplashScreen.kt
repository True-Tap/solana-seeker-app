package com.truetap.solana.seeker.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // TrueTap Logo with shadow
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .shadow(
                        elevation = Spacing.cardElevation,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = TrueTapShadow.copy(alpha = 0.1f)
                    )
                    .clip(RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.truetap_logo),
                    contentDescription = "TrueTap Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.xlarge))
            
            // Brand Name
            Text(
                text = "TrueTap",
                color = TrueTapPrimary,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.alpha(alphaAnim.value)
            )
            
            Spacer(modifier = Modifier.height(Spacing.medium))
            
            // Tagline
            Text(
                text = "Payments. Reimagined.",
                color = TrueTapTextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.alpha(alphaAnim.value)
            )
        }
    }
} 