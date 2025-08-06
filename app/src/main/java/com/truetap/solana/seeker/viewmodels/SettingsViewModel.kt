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
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.collect

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
    val error: String? = null,
    // Additional UI state
    val showPinModal: Boolean = false,
    val pinStep: PinStep = PinStep.ENTER,
    val pinInput: String = "",
    val confirmPin: String = "",
    val linkedWallets: List<LinkedWallet> = emptyList(),
    val showWalletDialog: Boolean = false,
    val defaultCurrency: String = "USD",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: String = "English",
    val showPrivacyPolicy: Boolean = false,
    val confirmDeleteData: Boolean = false,
    val showSelectionModal: SelectionType? = null,
    val dialogMessage: String? = null
)

enum class PinStep { ENTER, CONFIRM, SUCCESS }
enum class ThemeMode { LIGHT, DARK, SYSTEM }
data class LinkedWallet(val name: String, val address: String, val connected: Boolean)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {
    
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
            
            try {
                // Clear all user preferences
                dataStore.edit { preferences ->
                    preferences.clear()
                }
                
                // Reset UI state to defaults
                _uiState.value = SettingsUiState(
                    dialogMessage = "All data has been cleared successfully."
                )
                
                // Simulate additional cleanup time
                kotlinx.coroutines.delay(500)
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        dialogMessage = "Failed to clear data: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun exportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Collect all user data
                val exportData = mutableMapOf<String, Any>()
                
                // Export preferences
                dataStore.data.collect { preferences ->
                    preferences.asMap().forEach { (key, value) ->
                        exportData[key.name] = value
                    }
                }
                
                // Add current settings
                exportData["theme_mode"] = _uiState.value.themeMode.name
                exportData["language"] = _uiState.value.language
                exportData["currency"] = _uiState.value.defaultCurrency
                exportData["biometric_enabled"] = _uiState.value.biometricEnabled
                exportData["large_button_mode"] = _uiState.value.largeButtonMode
                exportData["high_contrast_mode"] = _uiState.value.highContrastMode
                exportData["simplified_ui_mode"] = _uiState.value.simplifiedUIMode
                exportData["audio_confirmations"] = _uiState.value.audioConfirmations
                exportData["export_timestamp"] = System.currentTimeMillis()
                
                // In a real implementation, you would:
                // 1. Convert to JSON
                // 2. Save to file or share via intent
                // 3. Return file path or share result
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        dialogMessage = "Data exported successfully. ${exportData.size} settings exported."
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        dialogMessage = "Failed to export data: ${e.message}"
                    )
                }
            }
        }
    }
    
    // Pin management
    fun setPinInput(pin: String) {
        _uiState.update { it.copy(pinInput = pin) }
    }
    
    fun setConfirmPin(pin: String) {
        _uiState.update { it.copy(confirmPin = pin) }
    }
    
    fun submitPin() {
        when (_uiState.value.pinStep) {
            PinStep.ENTER -> {
                _uiState.update { it.copy(pinStep = PinStep.CONFIRM, pinInput = "") }
            }
            PinStep.CONFIRM -> {
                if (_uiState.value.pinInput == _uiState.value.confirmPin) {
                    setPinCode(_uiState.value.pinInput)
                    _uiState.update { it.copy(pinStep = PinStep.SUCCESS) }
                    viewModelScope.launch {
                        kotlinx.coroutines.delay(1500)
                        dismissPinModal()
                    }
                }
            }
            PinStep.SUCCESS -> dismissPinModal()
        }
    }
    
    fun dismissPinModal() {
        _uiState.update { it.copy(
            showPinModal = false,
            pinStep = PinStep.ENTER,
            pinInput = "",
            confirmPin = ""
        )}
    }
    
    // Selection modal
    fun selectOption(type: SelectionType, value: String) {
        when (type) {
            SelectionType.CURRENCY -> _uiState.update { it.copy(defaultCurrency = value) }
            SelectionType.THEME -> _uiState.update { it.copy(themeMode = ThemeMode.valueOf(value)) }
            SelectionType.LANGUAGE -> _uiState.update { it.copy(language = value) }
        }
        dismissSelectionModal()
    }
    
    fun dismissSelectionModal() {
        _uiState.update { it.copy(showSelectionModal = null) }
    }
    
    // Dialog management
    fun showWalletDialog() {
        _uiState.update { it.copy(showWalletDialog = true) }
    }
    
    fun dismissDialog() {
        _uiState.update { it.copy(dialogMessage = null) }
    }
    
    fun showPinModal() {
        _uiState.update { it.copy(showPinModal = true) }
    }
    
    fun showPrivacyPolicy() {
        // In a real app, this would open a WebView or external browser
        _uiState.update { it.copy(
            showPrivacyPolicy = true,
            dialogMessage = "Privacy Policy\n\nWe value your privacy. Your data is stored locally and encrypted. We do not collect personal information without your consent.\n\nFor the full privacy policy, visit: https://truetap.app/privacy"
        ) }
    }
    
    fun dismissPrivacyPolicy() {
        _uiState.update { it.copy(
            showPrivacyPolicy = false,
            dialogMessage = null
        ) }
    }
    
    fun confirmDeleteData() {
        // Show confirmation dialog
        _uiState.update { it.copy(
            dialogMessage = "Are you sure you want to delete all data? This action cannot be undone.",
            confirmDeleteData = true
        ) }
    }
    
    fun proceedWithDataDeletion() {
        // User confirmed deletion
        _uiState.update { it.copy(confirmDeleteData = false, dialogMessage = null) }
        clearAllData()
    }
    
    fun cancelDataDeletion() {
        // User cancelled deletion
        _uiState.update { it.copy(
            confirmDeleteData = false,
            dialogMessage = null
        ) }
    }
    
    fun selectCurrency() {
        _uiState.update { it.copy(showSelectionModal = SelectionType.CURRENCY) }
    }
    
    fun selectTheme() {
        _uiState.update { it.copy(showSelectionModal = SelectionType.THEME) }
    }
    
    fun selectLanguage() {
        _uiState.update { it.copy(showSelectionModal = SelectionType.LANGUAGE) }
    }
}

enum class SelectionType { CURRENCY, THEME, LANGUAGE }