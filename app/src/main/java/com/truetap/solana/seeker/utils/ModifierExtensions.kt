package com.truetap.solana.seeker.utils

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Utility extension functions for common animations and modifiers
 */

/**
 * Animate rotation of a composable
 */
@Composable
fun Modifier.animateRotation(
    isRotating: Boolean,
    duration: Int = 300
): Modifier = this.then(
    if (isRotating) {
        val infiniteTransition = rememberInfiniteTransition(label = "rotation")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
        Modifier.rotate(rotation)
    } else {
        Modifier
    }
)

/**
 * Add floating animation to a composable
 */
@Composable
fun Modifier.floatingAnimation(
    isEnabled: Boolean = true,
    amplitude: Dp = 10.dp,
    duration: Int = 2000
): Modifier = this.then(
    if (isEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "floating")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = amplitude.value,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "floating"
        )
        Modifier.offset(y = offsetY.dp)
    } else {
        Modifier
    }
)

/**
 * Add pulse animation to a composable
 */
@Composable
fun Modifier.pulseAnimation(
    isEnabled: Boolean = true,
    minScale: Float = 0.9f,
    maxScale: Float = 1.1f,
    duration: Int = 1000
): Modifier = this.then(
    if (isEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = minScale,
            targetValue = maxScale,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
        Modifier.scale(scale)
    } else {
        Modifier
    }
)

/**
 * Add shake animation to a composable (useful for error states)
 */
@Composable
fun Modifier.shakeAnimation(
    isEnabled: Boolean = false,
    amplitude: Dp = 10.dp,
    duration: Int = 100
): Modifier = this.then(
    if (isEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "shake")
        val offsetX by infiniteTransition.animateFloat(
            initialValue = -amplitude.value,
            targetValue = amplitude.value,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "shake"
        )
        Modifier.offset(x = offsetX.dp)
    } else {
        Modifier
    }
)

/**
 * Add bounce animation to a composable
 */
@Composable
fun Modifier.bounceAnimation(
    isEnabled: Boolean = true,
    amplitude: Dp = 15.dp,
    duration: Int = 1200
): Modifier = this.then(
    if (isEnabled) {
        val infiniteTransition = rememberInfiniteTransition(label = "bounce")
        val bounceProgress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bounce"
        )
        val currentHeight = bounceProgress
        Modifier
            .graphicsLayer {
                scaleX = 1f - (1f - currentHeight) * 0.02f
                scaleY = 1f + (1f - currentHeight) * 0.01f
            }
            .offset(y = -(currentHeight * amplitude.value).dp)
    } else {
        Modifier
    }
)

/**
 * Execute an action after animation completes
 */
@Composable
fun Modifier.onAnimationEnd(
    duration: Int = 300,
    onEnd: () -> Unit
): Modifier {
    LaunchedEffect(Unit) {
        delay(duration.toLong())
        onEnd()
    }
    return this
}