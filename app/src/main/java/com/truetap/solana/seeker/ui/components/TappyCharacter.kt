package com.truetap.solana.seeker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.truetap.solana.seeker.R
import com.truetap.solana.seeker.data.models.NfcRole
import com.truetap.solana.seeker.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Tappy Character Component
 * Displays animated Tappy character based on NFC role and state
 */

enum class TappyState {
    IDLE, PROCESSING, SUCCESS, ERROR
}

@Composable
fun TappyCharacter(
    role: NfcRole,
    state: TappyState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            TappyState.IDLE -> {
                when (role) {
                    NfcRole.SENDER -> SenderTappy()
                    NfcRole.RECEIVER -> ReceiverTappy()
                }
            }
            TappyState.PROCESSING -> ProcessingTappy()
            TappyState.SUCCESS -> SuccessTappy()
            TappyState.ERROR -> ErrorTappy()
        }
    }
}

@Composable
private fun SenderTappy(
    modifier: Modifier = Modifier
) {
    // Floating animation for sender
    val floatAnimation by rememberInfiniteTransition(label = "float").animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.tappycomp),
            contentDescription = "Tappy ready to send",
            modifier = Modifier
                .size(120.dp)
                .offset(y = floatAnimation.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Ready to Send",
            style = MaterialTheme.typography.titleMedium,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Hold your device near another TrueTap device",
            style = MaterialTheme.typography.bodyMedium,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun ReceiverTappy(
    modifier: Modifier = Modifier
) {
    // Gentle pulse animation for receiver
    val pulseAnimation by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.tappycomp),
            contentDescription = "Tappy ready to receive",
            modifier = Modifier
                .size(120.dp)
                .scale(pulseAnimation)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Ready to Receive",
            style = MaterialTheme.typography.titleMedium,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Waiting for a payment...",
            style = MaterialTheme.typography.bodyMedium,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProcessingTappy(
    modifier: Modifier = Modifier
) {
    // Rotation animation for processing
    val rotationAnimation by rememberInfiniteTransition(label = "rotation").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Rotating circle background
            CircularProgressIndicator(
                modifier = Modifier
                    .size(140.dp),
                color = TrueTapPrimary,
                strokeWidth = 3.dp
            )
            
            Image(
                painter = painterResource(id = R.drawable.tappycomp),
                contentDescription = "Tappy processing",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Processing...",
            style = MaterialTheme.typography.titleMedium,
            color = TrueTapTextPrimary,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Please hold devices together",
            style = MaterialTheme.typography.bodyMedium,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuccessTappy(
    modifier: Modifier = Modifier
) {
    // Scale up animation for success
    val scaleAnimation by rememberInfiniteTransition(label = "success_scale").animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "success_scale"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Success indicator background
            Surface(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                color = TrueTapSuccess.copy(alpha = 0.1f)
            ) {}
            
            Image(
                painter = painterResource(id = R.drawable.tappycomp),
                contentDescription = "Tappy success",
                modifier = Modifier
                    .size(100.dp)
                    .scale(scaleAnimation)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Success!",
            style = MaterialTheme.typography.titleMedium,
            color = TrueTapSuccess,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Transaction completed",
            style = MaterialTheme.typography.bodyMedium,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
        
        // Confetti effect
        SuccessAnimation()
    }
}

@Composable
private fun ErrorTappy(
    modifier: Modifier = Modifier
) {
    // Shake animation for error
    val shakeAnimation by rememberInfiniteTransition(label = "shake").animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shake"
    )
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Error indicator background
            Surface(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                color = TrueTapError.copy(alpha = 0.1f)
            ) {}
            
            Image(
                painter = painterResource(id = R.drawable.tappycomp),
                contentDescription = "Tappy error",
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = shakeAnimation.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Error",
            style = MaterialTheme.typography.titleMedium,
            color = TrueTapError,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Please try again",
            style = MaterialTheme.typography.bodyMedium,
            color = TrueTapTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuccessAnimation() {
    // Simple confetti effect using circles
    var showConfetti by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        showConfetti = true
        delay(3000)
        showConfetti = false
    }
    
    AnimatedVisibility(
        visible = showConfetti,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(5) { index ->
                val colors = listOf(
                    TrueTapPrimary,
                    TrueTapSuccess,
                    Color.Yellow,
                    Color.Magenta,
                    Color.Cyan
                )
                
                Surface(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(y = (-10).dp),
                    shape = CircleShape,
                    color = colors[index % colors.size]
                ) {}
            }
        }
    }
}