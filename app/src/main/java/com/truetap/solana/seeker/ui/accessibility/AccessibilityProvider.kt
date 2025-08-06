package com.truetap.solana.seeker.ui.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.truetap.solana.seeker.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class AccessibilitySettings(
    val highContrastMode: Boolean = false,
    val largeButtonMode: Boolean = false,
    val audioConfirmations: Boolean = false,
    val simplifiedUIMode: Boolean = false,
    val themeMode: String = "light",
    val language: String = "English",
    val biometricEnabled: Boolean = false,
    val defaultCurrency: String = "USD"
)

val LocalAccessibilitySettings = compositionLocalOf { AccessibilitySettings() }

@HiltViewModel
class AccessibilityViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    val settings = settingsRepository.settings
}

@Composable
fun AccessibilityProvider(
    content: @Composable () -> Unit
) {
    val viewModel: AccessibilityViewModel = hiltViewModel()
    val settings by viewModel.settings.collectAsState()
    
    val accessibilitySettings = AccessibilitySettings(
        highContrastMode = settings.highContrastMode,
        largeButtonMode = settings.largeButtonMode,
        audioConfirmations = settings.audioConfirmations,
        simplifiedUIMode = settings.simplifiedUIMode,
        themeMode = settings.themeMode,
        language = settings.language,
        biometricEnabled = settings.biometricEnabled,
        defaultCurrency = settings.defaultCurrency
    )
    
    CompositionLocalProvider(
        LocalAccessibilitySettings provides accessibilitySettings
    ) {
        content()
    }
}