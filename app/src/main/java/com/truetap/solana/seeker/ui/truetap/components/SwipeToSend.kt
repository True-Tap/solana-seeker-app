package com.truetap.solana.seeker.ui.truetap.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.truetap.solana.seeker.repositories.TrueTapContact
import com.truetap.solana.seeker.ui.theme.TrueTapPrimary
import com.truetap.solana.seeker.ui.theme.TrueTapBackground
import com.truetap.solana.seeker.ui.theme.TrueTapTextPrimary
import com.truetap.solana.seeker.ui.theme.TrueTapTextSecondary
import com.truetap.solana.seeker.ui.truetap.components.format
import kotlin.math.roundToInt

@Composable
fun SwipeToSend(
    recipient: TrueTapContact,
    amount: Double,
    message: String,
    isLoading: Boolean,
    onSend: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Transaction summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = TrueTapBackground
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Sending to",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TrueTapTextSecondary
                )
                
                Text(
                    recipient.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapTextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "${amount.format(4)} SOL",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapPrimary
                )
                
                // USD equivalent
                val solPrice = 150.0 // Mock SOL price - should match AmountInput
                val usdEquivalent = amount * solPrice
                Text(
                    "≈ $${usdEquivalent.format(2)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TrueTapTextSecondary
                )
                
                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        message,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TrueTapTextPrimary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Swipe to send with BoxWithConstraints to calculate proper width
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            var offsetX by remember { mutableStateOf(0f) }
            var hasTriggered by remember { mutableStateOf(false) }
            val density = LocalDensity.current
            val buttonSize = with(density) { 60.dp.toPx() }
            val containerWidth = with(density) { maxWidth.toPx() }
            // Calculate max swipe distance - full width minus button size
            val maxSwipeDistance = containerWidth - buttonSize
            
            // Calculate progress from 0 to 1
            val swipeProgress = (offsetX / maxSwipeDistance).coerceIn(0f, 1f)
            
            // Animate the button position
            val animatedOffset by animateFloatAsState(
                targetValue = if (isLoading) maxSwipeDistance else offsetX,
                label = "button_offset"
            )
            
            // Trigger send when swipe is complete (Apple-style threshold)
            LaunchedEffect(swipeProgress) {
                if (swipeProgress >= 0.85f && !isLoading && !hasTriggered) {
                    hasTriggered = true
                    onSend()
                }
            }
            
            // Reset position when loading is complete
            LaunchedEffect(isLoading) {
                if (!isLoading) {
                    offsetX = 0f
                    hasTriggered = false // Reset for next swipe
                }
            }
            
            // Track with gradient opacity based on progress
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(30.dp))
                    .background(TrueTapPrimary.copy(alpha = 0.2f + (0.3f * swipeProgress)))
            )
            
            // Success text that fades in as swipe progresses
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (swipeProgress > 0.4f) {
                    Text(
                        "Release to send",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TrueTapPrimary.copy(alpha = (swipeProgress - 0.4f) * 1.67f),
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        "Swipe to send",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TrueTapTextSecondary
                    )
                }
            }
            
            // Swipeable button
            Box(
                modifier = Modifier
                    .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            if (!isLoading) {
                                offsetX = (offsetX + delta).coerceIn(0f, maxSwipeDistance)
                            }
                        },
                        onDragStopped = { velocity ->
                            // Apple-style swipe: if velocity is high enough or distance is far enough, complete the action
                            if (velocity > 300f || offsetX > maxSwipeDistance * 0.6f) {
                                offsetX = maxSwipeDistance
                            } else {
                                offsetX = 0f
                            }
                        }
                    )
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(TrueTapPrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Swipe to send",
                        modifier = Modifier.align(Alignment.Center),
                        tint = Color.White
                    )
                }
            }
        }
    }
}