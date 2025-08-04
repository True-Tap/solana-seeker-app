package com.truetap.solana.seeker.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class AppSettings(
    // Security settings
    val biometricEnabled: Boolean = false,
    val preferredBiometric: String = "face", // "face" or "fingerprint"
    val pinCode: String = "",
    
    // Wallet settings
    val defaultCurrency: String = "USD",
    
    // Personalization settings
    val themeMode: String = "light",
    val language: String = "English",
    
    // Accessibility settings
    val highContrastMode: Boolean = false,
    val largeButtonMode: Boolean = false,
    val audioConfirmations: Boolean = false,
    val simplifiedUIMode: Boolean = false
)

@Singleton
class SettingsRepository @Inject constructor() {
    
    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    
    fun updateSettings(update: AppSettings.() -> AppSettings) {
        _settings.value = _settings.value.update()
    }
    
    // Individual update methods
    fun updateThemeMode(themeMode: String) {
        _settings.value = _settings.value.copy(themeMode = themeMode)
    }
    
    fun updateHighContrastMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(highContrastMode = enabled)
    }
    
    fun updateLargeButtonMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(largeButtonMode = enabled)
    }
    
    fun updateAudioConfirmations(enabled: Boolean) {
        _settings.value = _settings.value.copy(audioConfirmations = enabled)
    }
    
    fun updateSimplifiedUIMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(simplifiedUIMode = enabled)
    }
    
    fun updateLanguage(language: String) {
        _settings.value = _settings.value.copy(language = language)
    }
    
    fun updateDefaultCurrency(currency: String) {
        _settings.value = _settings.value.copy(defaultCurrency = currency)
    }
    
    fun updateBiometricEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(biometricEnabled = enabled)
    }
    
    fun updatePreferredBiometric(biometric: String) {
        _settings.value = _settings.value.copy(preferredBiometric = biometric)
    }
    
    fun updatePinCode(pinCode: String) {
        _settings.value = _settings.value.copy(pinCode = pinCode)
    }
}