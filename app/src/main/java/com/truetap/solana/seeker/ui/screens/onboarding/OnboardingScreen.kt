package com.truetap.solana.seeker.ui.screens.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

data class OnboardingSlide(
    val id: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val animationType: String,
    val icon: ImageVector? = null
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val hapticFeedback = LocalHapticFeedback.current

    val slides = remember {
        listOf(
            OnboardingSlide(
                id = 1,
                title = "Welcome to TrueTap",
                subtitle = "Meet your payment companion, Tappy",
                description = "I'm Tappy, your friendly guide to effortless Solana payments. I'll help you send, receive, and manage your crypto with confidence. Together, we'll make every transaction smooth and secure.",
                animationType = "mascot"
            ),
            OnboardingSlide(
                id = 2,
                title = "Secure by Design",
                subtitle = "Built on Solana blockchain",
                description = "Every transaction is cryptographically secured and settled instantly on the Solana network.",
                animationType = "security",
                icon = Icons.Default.Security
            ),
            OnboardingSlide(
                id = 3,
                title = "Lightning Fast",
                subtitle = "Payments in seconds",
                description = "Experience the speed of Solana with instant confirmations and minimal fees.",
                animationType = "tap-demo",
                icon = Icons.Default.Speed
            ),
            OnboardingSlide(
                id = 4,
                title = "Smart Contacts",
                subtitle = "Stay connected",
                description = "Easily manage contacts, schedule payments, and track your transaction history.",
                animationType = "icon",
                icon = Icons.Default.Contacts
            )
        )
    }

    var currentSlide by remember { mutableIntStateOf(0) }
    val isLastSlide = currentSlide == slides.size - 1
    val currentSlideData = slides[currentSlide]

    // Animation values
    val textFade = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 600, delayMillis = 200),
        label = "text_fade"
    )

    // Continuous animations for mascot
    val mascotFloat = remember { Animatable(0f) }
    val mascotBreathing = remember { Animatable(1f) }

    // Responsive dimensions
    val isSmallScreen = screenHeight < 700.dp
    val mascotSize = kotlin.math.min(screenWidth.value * 0.4f, screenHeight.value * 0.25f).dp
    val titleSize = kotlin.math.max(screenWidth.value * 0.06f, if (isSmallScreen) 24f else 28f).sp
    val subtitleSize = kotlin.math.max(screenWidth.value * 0.045f, if (isSmallScreen) 18f else 20f).sp
    val descriptionSize = kotlin.math.max(screenWidth.value * 0.035f, if (isSmallScreen) 14f else 16f).sp

    // Responsive spacing
    val containerPadding = screenWidth * 0.06f
    val contentPadding = screenWidth * 0.08f

    LaunchedEffect(Unit) {
        // Start continuous mascot animations
        launch {
            mascotFloat.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
        launch {
            mascotBreathing.animateTo(
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500),
                    repeatMode = RepeatMode.Reverse
                )
            )
        }
    }

    val handleNext: () -> Unit = {
        if (currentSlide < slides.size - 1) {
            currentSlide++
        } else {
            onComplete()
        }
    }

    val handlePrevious: () -> Unit = {
        if (currentSlide > 0) {
            currentSlide--
        }
    }

    // Handle back button
    BackHandler {
        if (currentSlide > 0) {
            handlePrevious()
        }
        // If on first slide, let system handle back (exit app)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TrueTapBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button (only show if not first slide)
                if (currentSlide > 0) {
                    Text(
                        text = "Back",
                        fontSize = 16.sp,
                        color = TrueTapTextSecondary,
                        modifier = Modifier.clickable { handlePrevious() }
                    )
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
                
                // Progress Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(slides.size) { index ->
                        Box(
                            modifier = Modifier
                                .size(width = 24.dp, height = 4.dp)
                                .background(
                                    color = if (index <= currentSlide) TrueTapPrimary else TrueTapTextSecondary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
                
                // Skip Button
                Text(
                    text = "Skip",
                    fontSize = 16.sp,
                    color = TrueTapTextSecondary,
                    modifier = Modifier.clickable { onComplete() }
                )
            }

            // Main Content Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // Content Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(textFade.value),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = TrueTapContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Icon or Tappy - Standardized container size
                        Box(
                            modifier = Modifier
                                .size(120.dp), // Standardized container size for all slides
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentSlideData.animationType == "mascot") {
                                // Tappy for first slide
                                Image(
                                    painter = painterResource(id = R.drawable.truetap_logo),
                                    contentDescription = "Tappy Mascot",
                                    modifier = Modifier
                                        .size(100.dp) // Slightly smaller than container
                                        .offset(
                                            y = (mascotFloat.value * (-screenHeight.value * 0.02f)).dp
                                        )
                                        .scale(mascotBreathing.value),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                // Icon for other slides
                                currentSlideData.icon?.let { icon ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp) // Consistent with Tappy size
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        TrueTapPrimary.copy(alpha = 0.8f),
                                                        TrueTapPrimary
                                                    )
                                                ),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(50.dp) // Proportional to container
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Title
                        Text(
                            text = currentSlideData.title,
                            fontSize = titleSize,
                            fontWeight = FontWeight.Bold,
                            color = TrueTapTextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Subtitle
                        Text(
                            text = currentSlideData.subtitle,
                            fontSize = subtitleSize,
                            fontWeight = FontWeight.SemiBold,
                            color = TrueTapPrimary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Description
                        Text(
                            text = currentSlideData.description,
                            fontSize = descriptionSize,
                            color = TrueTapTextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Bottom Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Button(
                    onClick = handleNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
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
                        text = if (isLastSlide) "Get Started" else "Continue",
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
}