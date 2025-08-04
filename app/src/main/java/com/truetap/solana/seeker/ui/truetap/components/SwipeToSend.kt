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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import com.truetap.solana.seeker.ui.theme.TrueTapContainer
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
    Column {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Transaction summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = TrueTapContainer
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
                    "${amount.format(2)} SOL",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = TrueTapPrimary
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Swipe to send with BoxWithConstraints to calculate proper width
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            var offsetX by remember { mutableStateOf(0f) }
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
            
            // Trigger send when swipe is complete
            LaunchedEffect(swipeProgress, isLoading) {
                if (swipeProgress >= 0.95f && !isLoading) {
                    onSend()
                }
            }
            
            // Reset position when loading is complete
            LaunchedEffect(isLoading) {
                if (!isLoading) {
                    offsetX = 0f
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
            if (swipeProgress > 0.5f) {
                Text(
                    "Release to send",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = 30.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TrueTapPrimary.copy(alpha = (swipeProgress - 0.5f) * 2f),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    "Swipe to send",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = TrueTapTextSecondary
                )
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
                        onDragStopped = {
                            if (offsetX < maxSwipeDistance * 0.95f) {
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
                        Icons.Default.ArrowForward,
                        contentDescription = "Swipe to send",
                        modifier = Modifier.align(Alignment.Center),
                        tint = Color.White
                    )
                }
            }
        }
    }
}