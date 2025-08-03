package com.truetap.solana.seeker.ui.screens.welcome

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.ui.theme.*
import kotlin.math.max as mathMax
import kotlin.math.min as mathMin

@Composable
fun WelcomeScreen(
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

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

    // Responsive sizing calculations
    val logoSize = mathMin(screenWidth.value * 0.4f, 200f).dp
    val titleFontSize = mathMax(screenWidth.value * 0.08f, 28f).sp
    val subtitleFontSize = mathMax(screenWidth.value * 0.045f, 16f).sp
    val featureFontSize = mathMax(screenWidth.value * 0.035f, 12f).sp

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // Full screen background with click handling
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
            .clickable { onNavigate() }
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(fadeAnim.value)
                    .scale(scaleAnim.value)
                    .padding(top = 40.dp)
            ) {
                Card(
                    modifier = Modifier
                        .size(logoSize)
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = TrueTapPrimary.copy(alpha = 0.3f),
                            spotColor = TrueTapPrimary.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.truetap_logo),
                            contentDescription = "TrueTap Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // Title Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(fadeAnim.value)
                    .padding(vertical = 40.dp)
            ) {
                Text(
                    text = "TrueTap",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = TrueTapPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "Tap to Pay with Solana",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = subtitleFontSize,
                        fontWeight = FontWeight.Light
                    ),
                    color = TrueTapTextSecondary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }

            // Tap to Continue
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(fadeAnim.value)
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = "Tap to Continue",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = subtitleFontSize,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TrueTapPrimary.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            // Features Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(fadeAnim.value)
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureItem(
                    text = "🔒 Secure",
                    fontSize = featureFontSize,
                    modifier = Modifier.weight(1f)
                )
                FeatureItem(
                    text = "⚡ Instant",
                    fontSize = featureFontSize,
                    modifier = Modifier.weight(1f)
                )
                FeatureItem(
                    text = "🌐 Decentralized",
                    fontSize = featureFontSize,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.Medium
            ),
            color = TrueTapTextSecondary.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}