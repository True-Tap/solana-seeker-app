package com.truetap.solana.seeker.ui.screens.nfc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.data.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for NFC Tap to Pay Screen
 * Manages NFC payment state and interactions
 */

@HiltViewModel
class NfcViewModel @Inject constructor(
    // TODO: Inject actual services when available
    // private val nfcService: NfcService,
    // private val walletService: WalletService,
    // private val solanaService: SolanaService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NfcUiState())
    val uiState: StateFlow<NfcUiState> = _uiState.asStateFlow()
    
    init {
        checkNfcAvailability()
    }
    
    /**
     * Toggle between sender and receiver roles
     */
    fun toggleRole() {
        val currentRole = _uiState.value.role
        val newRole = when (currentRole) {
            NfcRole.SENDER -> NfcRole.RECEIVER
            NfcRole.RECEIVER -> NfcRole.SENDER
        }
        
        _uiState.value = _uiState.value.copy(
            role = newRole,
            amount = "", // Clear amount when switching roles
            recipient = "",
            transactionResult = null,
            errorMessage = null,
            showSuccessAnimation = false,
            showErrorAnimation = false
        )
    }
    
    /**
     * Update the amount for sending
     */
    fun updateAmount(amount: String) {
        // Validate amount input (numbers and decimal point only)
        val cleanAmount = amount.filter { it.isDigit() || it == '.' }
        
        // Prevent multiple decimal points
        val decimalCount = cleanAmount.count { it == '.' }
        val validAmount = if (decimalCount <= 1) cleanAmount else _uiState.value.amount
        
        _uiState.value = _uiState.value.copy(
            amount = validAmount,
            errorMessage = null
        )
    }
    
    /**
     * Update recipient address
     */
    fun updateRecipient(recipient: String) {
        _uiState.value = _uiState.value.copy(
            recipient = recipient,
            errorMessage = null
        )
    }
    
    /**
     * Process NFC tag interaction
     */
    fun processNfcTag(nfcData: String? = null) {
        val currentState = _uiState.value
        
        // Validate input based on role
        when (currentState.role) {
            NfcRole.SENDER -> {
                if (currentState.amount.isEmpty() || currentState.amount.toDoubleOrNull() == null) {
                    _uiState.value = currentState.copy(
                        errorMessage = "Please enter a valid amount",
                        showErrorAnimation = true
                    )
                    return
                }
                
                val amountValue = currentState.amount.toDouble()
                if (amountValue <= 0) {
                    _uiState.value = currentState.copy(
                        errorMessage = "Amount must be greater than 0",
                        showErrorAnimation = true
                    )
                    return
                }
            }
            NfcRole.RECEIVER -> {
                // For receiver, we don't need amount validation
                // We'll receive the amount from the sender's NFC tag
            }
        }
        
        // Start processing
        _uiState.value = currentState.copy(
            isProcessing = true,
            transactionResult = TransactionResult.Processing,
            errorMessage = null,
            showSuccessAnimation = false,
            showErrorAnimation = false
        )
        
        viewModelScope.launch {
            try {
                // TODO: Implement actual NFC processing
                /*
                when (currentState.role) {
                    NfcRole.SENDER -> {
                        // Create payment data to write to NFC tag
                        val paymentData = PaymentData(
                            amount = currentState.amount.toDouble(),
                            token = "SOL", // or selected token
                            senderAddress = walletService.getCurrentWalletAddress(),
                            timestamp = System.currentTimeMillis()
                        )
                        
                        // Write to NFC tag
                        nfcService.writePaymentData(paymentData)
                        
                        // Process the payment
                        val result = solanaService.sendPayment(
                            recipientAddress = nfcData ?: currentState.recipient,
                            amount = currentState.amount.toDouble(),
                            token = "SOL"
                        )
                        
                        if (result.success) {
                            _uiState.value = currentState.copy(
                                isProcessing = false,
                                transactionResult = TransactionResult.Success,
                                showSuccessAnimation = true
                            )
                        } else {
                            throw Exception(result.error ?: "Transaction failed")
                        }
                    }
                    
                    NfcRole.RECEIVER -> {
                        // Read payment data from NFC tag
                        val paymentData = nfcService.readPaymentData(nfcData)
                        
                        // Update UI with received payment info
                        _uiState.value = currentState.copy(
                            amount = paymentData.amount.toString(),
                            recipient = paymentData.senderAddress,
                            isProcessing = false,
                            transactionResult = TransactionResult.Success,
                            showSuccessAnimation = true
                        )
                    }
                }
                */
                
                // Simulate NFC processing for demonstration
                delay(2000)
                
                when (currentState.role) {
                    NfcRole.SENDER -> {
                        delay(1000) // Additional delay for transaction processing
                        _uiState.value = currentState.copy(
                            isProcessing = false,
                            transactionResult = TransactionResult.Success,
                            showSuccessAnimation = true
                        )
                    }
                    
                    NfcRole.RECEIVER -> {
                        // Simulate receiving payment data
                        val receivedAmount = "0.5" // Simulated received amount
                        val senderAddress = "7xKBvf6Z1nP2QqXhJgY8sH3fK2dG1mVc9tB4wR5uE8pN"
                        
                        _uiState.value = currentState.copy(
                            amount = receivedAmount,
                            recipient = senderAddress,
                            isProcessing = false,
                            transactionResult = TransactionResult.Success,
                            showSuccessAnimation = true
                        )
                    }
                }
                
                // Hide success animation after 3 seconds
                delay(3000)
                if (_uiState.value.showSuccessAnimation) {
                    _uiState.value = _uiState.value.copy(showSuccessAnimation = false)
                }
                
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isProcessing = false,
                    transactionResult = TransactionResult.Error(e.message ?: "Unknown error"),
                    errorMessage = e.message ?: "NFC processing failed",
                    showErrorAnimation = true
                )
                
                // Hide error animation after 3 seconds
                delay(3000)
                if (_uiState.value.showErrorAnimation) {
                    _uiState.value = _uiState.value.copy(showErrorAnimation = false)
                }
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            showErrorAnimation = false
        )
    }
    
    /**
     * Reset transaction state
     */
    fun resetTransaction() {
        _uiState.value = _uiState.value.copy(
            amount = if (_uiState.value.role == NfcRole.SENDER) "" else _uiState.value.amount,
            recipient = "",
            isProcessing = false,
            transactionResult = null,
            errorMessage = null,
            showSuccessAnimation = false,
            showErrorAnimation = false
        )
    }
    
    /**
     * Check NFC availability
     */
    private fun checkNfcAvailability() {
        viewModelScope.launch {
            try {
                // TODO: Check actual NFC availability
                /*
                val isNfcAvailable = nfcService.isNfcAvailable()
                val isNfcEnabled = nfcService.isNfcEnabled()
                
                _uiState.value = _uiState.value.copy(
                    isNfcEnabled = isNfcAvailable && isNfcEnabled
                )
                
                if (!isNfcAvailable) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "NFC is not available on this device"
                    )
                } else if (!isNfcEnabled) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "NFC is disabled. Please enable NFC in settings."
                    )
                }
                */
                
                // For demonstration, assume NFC is available and enabled
                _uiState.value = _uiState.value.copy(isNfcEnabled = true)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isNfcEnabled = false,
                    errorMessage = "Failed to check NFC availability: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Enable NFC (navigate to settings)
     */
    fun enableNfc() {
        // TODO: Navigate to NFC settings or show instructions
        /*
        nfcService.openNfcSettings()
        */
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up any NFC listeners or resources
    }
}