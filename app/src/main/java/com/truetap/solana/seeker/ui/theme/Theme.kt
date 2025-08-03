package com.truetap.solana.seeker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.truetap.solana.seeker.ui.theme.TrueTapTypography

private val DarkColorScheme = darkColorScheme(
    primary = TrueTapPrimary,
    secondary = TrueTapPrimary,
    tertiary = TrueTapTextInactive,
    background = TrueTapBackground,
    surface = TrueTapContainer,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = TrueTapTextSecondary,
    onBackground = TrueTapTextPrimary,
    onSurface = TrueTapTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = TrueTapPrimary,
    secondary = TrueTapPrimary,
    tertiary = TrueTapTextInactive,
    background = TrueTapBackground,
    surface = TrueTapContainer,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = TrueTapTextSecondary,
    onBackground = TrueTapTextPrimary,
    onSurface = TrueTapTextPrimary
)

@Composable
fun SolanaseekerappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TrueTapTypography,
        content = content
    )
}