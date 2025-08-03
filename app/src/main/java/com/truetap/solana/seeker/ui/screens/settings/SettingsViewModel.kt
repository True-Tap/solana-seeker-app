package com.truetap.solana.seeker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings Screen
 * Manages state for app settings and preferences
 */

data class LinkedWallet(
    val id: String,
    val name: String,
    val connected: Boolean
)

data class DialogMessage(
    val title: String,
    val content: String? = null,
    val isError: Boolean = false,
    val onConfirm: (() -> Unit)? = null,
    val onCancel: (() -> Unit)? = null
)

enum class PinStep {
    INPUT,
    CONFIRM
}

enum class SelectionModalType {
    BIOMETRIC_METHOD,
    DEFAULT_CURRENCY,
    THEME,
    LANGUAGE
}

data class SettingsUiState(
    // Security settings
    val biometricEnabled: Boolean = false,
    val preferredBiometric: String = "face", // "face" or "fingerprint"
    val pinCode: String = "",
    
    // Wallet settings
    val linkedWallets: List<LinkedWallet> = listOf(
        LinkedWallet("phantom", "Phantom", true),
        LinkedWallet("solflare", "Solflare", false)
    ),
    val defaultCurrency: String = "USD",
    
    // Personalization settings
    val themeMode: String = "light",
    val language: String = "English",
    
    // Modal states
    val showPinModal: Boolean = false,
    val pinStep: PinStep = PinStep.INPUT,
    val pinInput: String = "",
    val confirmPin: String = "",
    val showSelectionModal: SelectionModalType? = null,
    val dialogMessage: DialogMessage? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    // TODO: Inject actual services when available
    // private val dataStoreService: DataStoreService,
    // private val biometricService: BiometricService,
    // private val walletService: WalletService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadSettings()
    }
    
    // Security Settings
    fun toggleBiometric(enabled: Boolean) {
        if (enabled) {
            showSelectionModal(SelectionModalType.BIOMETRIC_METHOD)
        } else {
            _uiState.value = _uiState.value.copy(
                dialogMessage = DialogMessage(
                    title = "Disable Biometric",
                    content = "Are you sure you want to disable biometric authentication?",
                    isError = false,
                    onConfirm = {
                        _uiState.value = _uiState.value.copy(biometricEnabled = false)
                        saveSettings()
                    }
                )
            )
        }
    }
    
    fun showPinModal() {
        if (_uiState.value.pinCode.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                dialogMessage = DialogMessage(
                    title = "Change PIN",
                    content = "Do you want to change your PIN code?",
                    onConfirm = {
                        _uiState.value = _uiState.value.copy(showPinModal = true)
                    }
                )
            )
        } else {
            _uiState.value = _uiState.value.copy(showPinModal = true)
        }
    }
    
    fun dismissPinModal() {
        _uiState.value = _uiState.value.copy(
            showPinModal = false,
            pinStep = PinStep.INPUT,
            pinInput = "",
            confirmPin = ""
        )
    }
    
    fun setPinInput(pin: String) {
        if (pin.length <= 6) {
            _uiState.value = _uiState.value.copy(pinInput = pin)
        }
    }
    
    fun setConfirmPin(pin: String) {
        if (pin.length <= 6) {
            _uiState.value = _uiState.value.copy(confirmPin = pin)
        }
    }
    
    fun submitPin() {
        val currentState = _uiState.value
        
        if (currentState.pinStep == PinStep.INPUT) {
            if (currentState.pinInput.length != 6) {
                _uiState.value = currentState.copy(
                    dialogMessage = DialogMessage(
                        title = "Invalid PIN",
                        content = "PIN must be exactly 6 digits",
                        isError = true
                    )
                )
                return
            }
            _uiState.value = currentState.copy(
                pinStep = PinStep.CONFIRM,
                confirmPin = ""
            )
        } else {
            if (currentState.pinInput != currentState.confirmPin) {
                _uiState.value = currentState.copy(
                    dialogMessage = DialogMessage(
                        title = "PIN Mismatch",
                        content = "PINs do not match. Please try again.",
                        isError = true,
                        onConfirm = {
                            _uiState.value = _uiState.value.copy(
                                pinStep = PinStep.INPUT,
                                pinInput = "",
                                confirmPin = ""
                            )
                        }
                    )
                )
                return
            }
            
            _uiState.value = currentState.copy(
                pinCode = currentState.pinInput,
                showPinModal = false,
                pinStep = PinStep.INPUT,
                pinInput = "",
                confirmPin = "",
                dialogMessage = DialogMessage(
                    title = "Success",
                    content = "PIN code has been set successfully"
                )
            )
            saveSettings()
        }
    }
    
    // Selection Modal
    fun showSelectionModal(type: SelectionModalType) {
        _uiState.value = _uiState.value.copy(showSelectionModal = type)
    }
    
    fun dismissSelectionModal() {
        _uiState.value = _uiState.value.copy(showSelectionModal = null)
    }
    
    fun selectOption(modalType: SelectionModalType, value: String) {
        when (modalType) {
            SelectionModalType.BIOMETRIC_METHOD -> {
                _uiState.value = _uiState.value.copy(
                    preferredBiometric = if (value == "Face ID") "face" else "fingerprint",
                    biometricEnabled = true
                )
            }
            SelectionModalType.DEFAULT_CURRENCY -> {
                _uiState.value = _uiState.value.copy(defaultCurrency = value)
            }
            SelectionModalType.THEME -> {
                _uiState.value = _uiState.value.copy(themeMode = value.lowercase())
            }
            SelectionModalType.LANGUAGE -> {
                _uiState.value = _uiState.value.copy(language = value)
            }
        }
        saveSettings()
    }
    
    // Wallet Settings
    fun showWalletDialog() {
        _uiState.value = _uiState.value.copy(
            dialogMessage = DialogMessage(
                title = "Linked Wallets",
                content = "Manage your connected wallets"
            )
        )
    }
    
    // Privacy & Data
    fun showPrivacyPolicy() {
        _uiState.value = _uiState.value.copy(
            dialogMessage = DialogMessage(
                title = "Privacy Policy",
                content = "Opening privacy policy..."
            )
        )
    }
    
    fun exportData() {
        _uiState.value = _uiState.value.copy(
            dialogMessage = DialogMessage(
                title = "Export Data",
                content = "Your activity log will be exported. This may take a few moments.",
                onConfirm = {
                    performDataExport()
                }
            )
        )
    }
    
    fun confirmDeleteData() {
        _uiState.value = _uiState.value.copy(
            dialogMessage = DialogMessage(
                title = "Delete All Data",
                content = "This action cannot be undone. All your data will be permanently deleted.",
                isError = true,
                onConfirm = {
                    performDataDeletion()
                }
            )
        )
    }
    
    private fun performDataExport() {
        viewModelScope.launch {
            try {
                // Simulate export process
                delay(2000)
                _uiState.value = _uiState.value.copy(
                    dialogMessage = DialogMessage(
                        title = "Export Complete",
                        content = "Your data has been exported successfully."
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    dialogMessage = DialogMessage(
                        title = "Export Failed",
                        content = "Failed to export data. Please try again.",
                        isError = true
                    )
                )
            }
        }
    }
    
    private fun performDataDeletion() {
        viewModelScope.launch {
            try {
                // Simulate deletion process
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    dialogMessage = DialogMessage(
                        title = "Data Deleted",
                        content = "All your data has been deleted."
                    )
                )
                // Reset all settings to defaults
                _uiState.value = SettingsUiState()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    dialogMessage = DialogMessage(
                        title = "Deletion Failed",
                        content = "Failed to delete data. Please try again.",
                        isError = true
                    )
                )
            }
        }
    }
    
    fun dismissDialog() {
        _uiState.value = _uiState.value.copy(dialogMessage = null)
    }
    
    private fun loadSettings() {
        viewModelScope.launch {
            try {
                // TODO: Load settings from DataStore
                /*
                val settings = dataStoreService.getSettings()
                _uiState.value = _uiState.value.copy(
                    biometricEnabled = settings.biometricEnabled,
                    preferredBiometric = settings.preferredBiometric,
                    pinCode = settings.pinCode,
                    sessionTimeout = settings.sessionTimeout,
                    defaultCurrency = settings.defaultCurrency,
                    preferredPaymentMode = settings.preferredPaymentMode,
                    themeMode = settings.themeMode,
                    language = settings.language
                )
                */
            } catch (e: Exception) {
                // Handle error silently, use default values
            }
        }
    }
    
    private fun saveSettings() {
        viewModelScope.launch {
            try {
                // TODO: Save settings to DataStore
                /*
                val currentState = _uiState.value
                dataStoreService.saveSettings(
                    Settings(
                        biometricEnabled = currentState.biometricEnabled,
                        preferredBiometric = currentState.preferredBiometric,
                        pinCode = currentState.pinCode,
                        sessionTimeout = currentState.sessionTimeout,
                        defaultCurrency = currentState.defaultCurrency,
                        preferredPaymentMode = currentState.preferredPaymentMode,
                        themeMode = currentState.themeMode,
                        language = currentState.language
                    )
                )
                */
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}