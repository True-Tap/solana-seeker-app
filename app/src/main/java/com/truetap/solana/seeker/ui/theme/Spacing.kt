package com.truetap.solana.seeker.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

// Global Spacing System
object Spacing {
    // Screen padding
    val screenHorizontal = 24.dp
    
    // Button margins
    val buttonTop = 16.dp
    val buttonBottom = 24.dp
    
    // Vertical spacing
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val xlarge = 32.dp
    
    // Card elevation
    val cardElevation = 4.dp
}

// Common padding values
val screenPadding = PaddingValues(horizontal = Spacing.screenHorizontal)
val buttonPadding = PaddingValues(
    top = Spacing.buttonTop,
    bottom = Spacing.buttonBottom
) 