package com.truetap.solana.seeker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dynamic theme colors based on settings
data class DynamicColors(
    val background: Color,
    val backgroundBrush: Brush?,
    val surface: Color,
    val primary: Color,
    val onPrimary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textInactive: Color,
    val success: Color,
    val error: Color
)

val LocalDynamicColors = compositionLocalOf {
    DynamicColors(
        background = TrueTapBackground,
        backgroundBrush = null,
        surface = TrueTapContainer,
        primary = TrueTapPrimary,
        onPrimary = Color.White,
        textPrimary = TrueTapTextPrimary,
        textSecondary = TrueTapTextSecondary,
        textInactive = TrueTapTextInactive,
        success = TrueTapSuccess,
        error = TrueTapError
    )
}

@Composable
fun getDynamicColors(
    themeMode: String,
    highContrastMode: Boolean
): DynamicColors {
    val isSystemDark = isSystemInDarkTheme()
    val isDarkMode = when (themeMode.lowercase()) {
        "dark" -> true
        "light" -> false
        "system" -> isSystemDark
        else -> false
    }
    
    val baseColors = if (isDarkMode) {
        // Dark mode with your beautiful gradient and specified colors
        DynamicColors(
            background = Color(0xFF1A120D), // Deep Mocha Black
            backgroundBrush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1A120D), // Deep Mocha Black (0%)
                    Color(0xFF2A1A0D), // Charred Bronze (30%)
                    Color(0xFF3C2A20), // Dark Umber (60%)
                    Color(0xFFF28C38)  // Brand Ember (100%)
                ),
                center = androidx.compose.ui.geometry.Offset(0.5f, -0.4468f),
                radius = 1.6119f
            ),
            surface = Color(0xFF2A1A0D), // Charred Bronze for cards
            primary = Color(0xFFF28C38), // Brand Ember
            onPrimary = Color(0xFF1A120D), // Deep Mocha Black for text on primary
            textPrimary = Color(0xFFF5E9DA), // Pale Ivory
            textSecondary = Color(0xFFB8ADA4), // Warm Taupe
            textInactive = Color(0xFF3C2A20), // Dark Umber for borders/inactive
            success = Color(0xFF2ECC71), // Soft Moss
            error = Color(0xFFE74C3C) // Ember Red
        )
    } else {
        // Light mode (existing theme)
        DynamicColors(
            background = TrueTapBackground,
            backgroundBrush = null,
            surface = TrueTapContainer,
            primary = TrueTapPrimary,
            onPrimary = Color.White,
            textPrimary = TrueTapTextPrimary,
            textSecondary = TrueTapTextSecondary,
            textInactive = TrueTapTextInactive,
            success = TrueTapSuccess,
            error = TrueTapError
        )
    }
    
    // Apply high contrast modifications
    return if (highContrastMode) {
        baseColors.copy(
            primary = if (isDarkMode) Color.White else Color.Black,
            onPrimary = if (isDarkMode) Color.Black else Color.White,
            textPrimary = if (isDarkMode) Color.White else Color.Black,
            textSecondary = if (isDarkMode) Color(0xFFE0E0E0) else Color(0xFF333333),
            surface = if (isDarkMode) Color(0xFF000000) else Color.White, // Pure black for dark mode high contrast
            background = if (isDarkMode) Color(0xFF000000) else Color.White,
            backgroundBrush = null // Remove gradient in high contrast mode
        )
    } else {
        baseColors
    }
}

@Composable
fun DynamicBackground(
    colors: DynamicColors,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val backgroundModifier = if (colors.backgroundBrush != null) {
        modifier.background(colors.backgroundBrush)
    } else {
        modifier.background(colors.background)
    }
    
    androidx.compose.foundation.layout.Box(
        modifier = backgroundModifier
    ) {
        content()
    }
}