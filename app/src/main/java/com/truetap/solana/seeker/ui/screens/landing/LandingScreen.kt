package com.truetap.solana.seeker.ui.screens.landing

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.offset
import kotlinx.coroutines.delay
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*

@Composable
fun LandingScreen(
    onConnectWallet: () -> Unit,
    onTryDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = EaseOutCubic),
        label = "landing_alpha"
    )
    
    val slideUpAnim = animateFloatAsState(
        targetValue = if (startAnimation) 0f else 30f,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "landing_slide"
    )
    
    LaunchedEffect(key1 = true) {
        delay(300) // Slight delay for smooth transition from splash
        startAnimation = true
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .clickable { onConnectWallet() }, // Entire screen is tappable
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // TrueTap Logo - larger
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.truetap_logo),
                contentDescription = "TrueTap Logo",
                modifier = Modifier
                    .size(140.dp)
                    .alpha(alphaAnim.value),
                contentScale = ContentScale.Fit
            )
            
            Spacer(modifier = Modifier.height(Spacing.large)) // Reduced from xlarge to large
            
            // Welcome Text - much larger
            Text(
                text = "Welcome to",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = TrueTapTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alphaAnim.value)
            )
            
            Spacer(modifier = Modifier.height(Spacing.small))
            
            // Brand Name - much larger
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
            
            Spacer(modifier = Modifier.height(Spacing.small))
            
            // Tagline - much larger
            Text(
                text = "Payments. Reimagined.",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = TrueTapTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alphaAnim.value)
            )
            
            Spacer(modifier = Modifier.height(Spacing.xlarge))
            
            // Tap to Continue - positioned in the middle
            Text(
                text = "Tap to Continue",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = TrueTapPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(alphaAnim.value)
                    .offset(y = slideUpAnim.value.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Simple feature text at bottom
            Text(
                text = "Secure. Fast. Decentralized.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = TrueTapTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(alphaAnim.value)
                    .offset(y = slideUpAnim.value.dp)
            )
            
            Spacer(modifier = Modifier.height(Spacing.large))
            
            // Simple Learn More text - no card, no shadow
            Text(
                text = "Learn more about TrueTap",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = TrueTapPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(alphaAnim.value * 0.8f)
                    .clickable { /* TODO: Learn more */ }
            )
        }
    }
}

 