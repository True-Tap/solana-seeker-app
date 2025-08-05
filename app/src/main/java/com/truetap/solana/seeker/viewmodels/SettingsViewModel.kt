package com.truetap.solana.seeker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val biometricEnabled: Boolean = false,
    val pinCode: String = "",
    val largeButtonMode: Boolean = false,
    val highContrastMode: Boolean = false,
    val audioConfirmations: Boolean = false,
    val simplifiedUIMode: Boolean = false,
    val analyticsEnabled: Boolean = true,
    val crashReportingEnabled: Boolean = true,
    val personalizedAdsEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    fun toggleBiometric() {
        _uiState.update { it.copy(biometricEnabled = !it.biometricEnabled) }
    }
    
    fun setPinCode(pin: String) {
        _uiState.update { it.copy(pinCode = pin) }
    }
    
    fun toggleLargeButtons() {
        _uiState.update { it.copy(largeButtonMode = !it.largeButtonMode) }
    }
    
    fun toggleHighContrast() {
        _uiState.update { it.copy(highContrastMode = !it.highContrastMode) }
    }
    
    fun toggleAudioConfirmations() {
        _uiState.update { it.copy(audioConfirmations = !it.audioConfirmations) }
    }
    
    fun toggleSimplifiedUI() {
        _uiState.update { it.copy(simplifiedUIMode = !it.simplifiedUIMode) }
    }
    
    fun toggleAnalytics() {
        _uiState.update { it.copy(analyticsEnabled = !it.analyticsEnabled) }
    }
    
    fun toggleCrashReporting() {
        _uiState.update { it.copy(crashReportingEnabled = !it.crashReportingEnabled) }
    }
    
    fun togglePersonalizedAds() {
        _uiState.update { it.copy(personalizedAdsEnabled = !it.personalizedAdsEnabled) }
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // TODO: Implement data clearing logic
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    
    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // TODO: Implement data export logic
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}