package com.truetap.solana.seeker.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * TrueTap Design System
 * 
 * Comprehensive design system for consistent UI/UX across the entire app.
 * Includes colors, typography, spacing, and component styles.
 */

// Color System
data class TrueTapColors(
    // Primary Colors
    val primary: Color,           // Main brand color (Orange)
    val onPrimary: Color,         // Text/icons on primary
    val primaryContainer: Color,  // Lighter primary for containers
    val onPrimaryContainer: Color,
    
    // Secondary Colors  
    val secondary: Color,         // Accent color
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    
    // Background Colors
    val background: Color,        // Main background
    val onBackground: Color,      // Text on background
    val surface: Color,           // Card/surface background
    val onSurface: Color,         // Text on surface
    val surfaceVariant: Color,    // Alternative surface
    val onSurfaceVariant: Color,
    
    // Text Colors
    val textPrimary: Color,       // Primary text color
    val textSecondary: Color,     // Secondary text color
    val textTertiary: Color,      // Tertiary text color
    val textInactive: Color,      // Disabled/inactive text
    
    // Status Colors
    val success: Color,           // Success states
    val onSuccess: Color,
    val error: Color,             // Error states
    val onError: Color,
    val warning: Color,           // Warning states
    val onWarning: Color,
    val info: Color,              // Info states
    val onInfo: Color,
    
    // Border & Outline
    val outline: Color,           // Borders and dividers
    val outlineVariant: Color,    // Subtle borders
    
    // Background Gradient (optional)
    val backgroundBrush: Brush?   // For gradient backgrounds
)

// Light Theme Colors
val LightTrueTapColors = TrueTapColors(
    primary = Color(0xFFFF8B3D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE6D3),
    onPrimaryContainer = Color(0xFF8B4513),
    
    secondary = Color(0xFF6366F1),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF3730A3),
    
    background = Color(0xFFFFFBF7),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF666666),
    
    textPrimary = Color(0xFF1A1A1A),
    textSecondary = Color(0xFF666666),
    textTertiary = Color(0xFF999999),
    textInactive = Color(0xFFCCCCCC),
    
    success = Color(0xFF22C55E),
    onSuccess = Color.White,
    error = Color(0xFFEF4444),
    onError = Color.White,
    warning = Color(0xFFF59E0B),
    onWarning = Color.White,
    info = Color(0xFF3B82F6),
    onInfo = Color.White,
    
    outline = Color(0xFFE5E5E5),
    outlineVariant = Color(0xFFF0F0F0),
    
    backgroundBrush = null
)

// Dark Theme Colors
val DarkTrueTapColors = TrueTapColors(
    primary = Color(0xFFFF8B3D),
    onPrimary = Color(0xFF1A1A1A),
    primaryContainer = Color(0xFF8B4513),
    onPrimaryContainer = Color(0xFFFFE6D3),
    
    secondary = Color(0xFF818CF8),
    onSecondary = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFF3730A3),
    onSecondaryContainer = Color(0xFFE0E7FF),
    
    background = Color(0xFF121212),
    onBackground = Color(0xFFE5E5E5),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE5E5E5),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    
    textPrimary = Color(0xFFE5E5E5),
    textSecondary = Color(0xFFB0B0B0),
    textTertiary = Color(0xFF808080),
    textInactive = Color(0xFF555555),
    
    success = Color(0xFF22C55E),
    onSuccess = Color.White,
    error = Color(0xFFEF4444),
    onError = Color.White,
    warning = Color(0xFFF59E0B),
    onWarning = Color.White,
    info = Color(0xFF3B82F6),
    onInfo = Color.White,
    
    outline = Color(0xFF404040),
    outlineVariant = Color(0xFF2A2A2A),
    
    backgroundBrush = null
)

// High Contrast Light Theme
val HighContrastLightTrueTapColors = LightTrueTapColors.copy(
    primary = Color(0xFFFF6B00),
    textPrimary = Color.Black,
    textSecondary = Color(0xFF333333),
    outline = Color(0xFF666666)
)

// High Contrast Dark Theme
val HighContrastDarkTrueTapColors = DarkTrueTapColors.copy(
    primary = Color(0xFFFFAB5D),
    textPrimary = Color.White,
    textSecondary = Color(0xFFCCCCCC),
    outline = Color(0xFF808080)
)

// Typography System
data class TrueTapTypography(
    val displayLarge: TextStyle,    // 57sp - Largest display text
    val displayMedium: TextStyle,   // 45sp - Medium display text
    val displaySmall: TextStyle,    // 36sp - Small display text
    
    val headlineLarge: TextStyle,   // 32sp - Large headlines
    val headlineMedium: TextStyle,  // 28sp - Medium headlines
    val headlineSmall: TextStyle,   // 24sp - Small headlines
    
    val titleLarge: TextStyle,      // 22sp - Large titles
    val titleMedium: TextStyle,     // 16sp - Medium titles
    val titleSmall: TextStyle,      // 14sp - Small titles
    
    val bodyLarge: TextStyle,       // 16sp - Large body text
    val bodyMedium: TextStyle,      // 14sp - Medium body text
    val bodySmall: TextStyle,       // 12sp - Small body text
    
    val labelLarge: TextStyle,      // 14sp - Large labels
    val labelMedium: TextStyle,     // 12sp - Medium labels
    val labelSmall: TextStyle       // 11sp - Small labels
)

val BaseTrueTapTypography = TrueTapTypography(
    displayLarge = TextStyle(
        fontSize = 57.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.Bold
    ),
    displayMedium = TextStyle(
        fontSize = 45.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.Bold
    ),
    displaySmall = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Bold
    ),
    
    headlineLarge = TextStyle(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineSmall = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold
    ),
    
    titleLarge = TextStyle(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleMedium = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Medium
    ),
    titleSmall = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium
    ),
    
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal
    ),
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal
    ),
    bodySmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Normal
    ),
    
    labelLarge = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    )
)

// Accessibility-Enhanced Typography
val LargeTextTrueTapTypography = BaseTrueTapTypography.copy(
    displayLarge = BaseTrueTapTypography.displayLarge.copy(fontSize = 64.sp, lineHeight = 72.sp),
    displayMedium = BaseTrueTapTypography.displayMedium.copy(fontSize = 52.sp, lineHeight = 60.sp),
    displaySmall = BaseTrueTapTypography.displaySmall.copy(fontSize = 42.sp, lineHeight = 50.sp),
    headlineLarge = BaseTrueTapTypography.headlineLarge.copy(fontSize = 38.sp, lineHeight = 46.sp),
    headlineMedium = BaseTrueTapTypography.headlineMedium.copy(fontSize = 34.sp, lineHeight = 42.sp),
    headlineSmall = BaseTrueTapTypography.headlineSmall.copy(fontSize = 28.sp, lineHeight = 36.sp),
    titleLarge = BaseTrueTapTypography.titleLarge.copy(fontSize = 26.sp, lineHeight = 34.sp),
    titleMedium = BaseTrueTapTypography.titleMedium.copy(fontSize = 20.sp, lineHeight = 28.sp),
    titleSmall = BaseTrueTapTypography.titleSmall.copy(fontSize = 18.sp, lineHeight = 24.sp),
    bodyLarge = BaseTrueTapTypography.bodyLarge.copy(fontSize = 20.sp, lineHeight = 28.sp),
    bodyMedium = BaseTrueTapTypography.bodyMedium.copy(fontSize = 18.sp, lineHeight = 24.sp),
    bodySmall = BaseTrueTapTypography.bodySmall.copy(fontSize = 16.sp, lineHeight = 20.sp),
    labelLarge = BaseTrueTapTypography.labelLarge.copy(fontSize = 18.sp, lineHeight = 24.sp),
    labelMedium = BaseTrueTapTypography.labelMedium.copy(fontSize = 16.sp, lineHeight = 20.sp),
    labelSmall = BaseTrueTapTypography.labelSmall.copy(fontSize = 14.sp, lineHeight = 18.sp)
)

// CompositionLocal providers
val LocalTrueTapColors = staticCompositionLocalOf { LightTrueTapColors }
val LocalTrueTapTypography = staticCompositionLocalOf { BaseTrueTapTypography }

// Helper function to get colors based on theme and accessibility settings
@Composable
fun getTrueTapColors(
    themeMode: String = "system",
    highContrastMode: Boolean = false
): TrueTapColors {
    return when {
        themeMode == "dark" && highContrastMode -> HighContrastDarkTrueTapColors
        themeMode == "dark" -> DarkTrueTapColors
        themeMode == "light" && highContrastMode -> HighContrastLightTrueTapColors
        else -> LightTrueTapColors
    }
}

// Helper function to get typography based on accessibility settings
@Composable
fun getDynamicTypography(
    largeTextMode: Boolean = false
): TrueTapTypography {
    return if (largeTextMode) LargeTextTrueTapTypography else BaseTrueTapTypography
}

// Theme Extensions for backwards compatibility
// Namespaced to avoid conflicts with Color.kt definitions
object TrueTapDesignColors {
    val Primary = Color(0xFFFF8B3D)
    val Background = Color(0xFFFFFBF7)
    val Container = Color.White
    val TextPrimary = Color(0xFF1A1A1A)
    val TextSecondary = Color(0xFF666666)
    val Success = Color(0xFF22C55E)
    val Error = Color(0xFFEF4444)
}