/**
 * Send Payment ViewModel - TrueTap
 * Manages state and business logic for the Send Payment screen
 */

package com.truetap.solana.seeker.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Data Classes
data class TokenInfo(
    val symbol: String,
    val name: String,
    val balance: Double
)

data class PaymentResult(
    val success: Boolean,
    val error: String? = null,
    val transactionHash: String? = null
)

data class SendPaymentUiState(
    val recipientAddress: String = "",
    val amount: String = "",
    val memo: String = "",
    val selectedToken: String = "SOL",
    val availableTokens: List<TokenInfo> = emptyList(),
    val isLoading: Boolean = false,
    val isWalletConnected: Boolean = false,
    val recipientError: String? = null,
    val amountError: String? = null,
    val errorMessage: String? = null,
    val paymentResult: PaymentResult? = null
) {
    val isFormValid: Boolean
        get() = recipientAddress.isNotBlank() && 
                amount.isNotBlank() && 
                amount.toDoubleOrNull()?.let { it > 0 } == true &&
                recipientError == null &&
                amountError == null
    
    fun getTokenBalance(tokenSymbol: String): Double {
        return availableTokens.find { it.symbol == tokenSymbol }?.balance ?: 0.0
    }
}

@HiltViewModel
class SendPaymentViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(SendPaymentUiState())
    val uiState: StateFlow<SendPaymentUiState> = _uiState.asStateFlow()
    
    // Sample token data
    private val sampleTokens = listOf(
        TokenInfo("SOL", "Solana", 125.4567),
        TokenInfo("USDC", "USD Coin", 1000.0),
        TokenInfo("BONK", "Bonk", 50000.0)
    )
    
    init {
        loadWalletInfo()
    }
    
    // TODO: Add navigation args support later
    
    fun updateRecipientAddress(address: String) {
        _uiState.update { currentState ->
            currentState.copy(
                recipientAddress = address,
                recipientError = null
            )
        }
        
        // Validate address format
        validateRecipientAddress(address)
    }
    
    fun updateAmount(amount: String) {
        // Allow only valid decimal input
        val sanitizedAmount = amount.filter { it.isDigit() || it == '.' }
        
        _uiState.update { currentState ->
            currentState.copy(
                amount = sanitizedAmount,
                amountError = null
            )
        }
        
        // Validate amount
        validateAmount(sanitizedAmount)
    }
    
    fun updateMemo(memo: String) {
        _uiState.update { it.copy(memo = memo) }
    }
    
    fun selectToken(tokenSymbol: String) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedToken = tokenSymbol,
                amountError = null
            )
        }
        
        // Re-validate amount with new token
        validateAmount(_uiState.value.amount)
    }
    
    fun sendPayment() {
        val currentState = _uiState.value
        
        if (!currentState.isFormValid) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                // TODO: Replace with actual wallet service integration
                // val walletService = WalletFactory.getInstance() as SolanaWalletService
                // val result = walletService.sendPayment(...)
                
                // Simulate payment processing
                delay(2000)
                
                // Simulate successful payment
                val result = PaymentResult(
                    success = true,
                    transactionHash = "5J7X...Y9Z1" // Mock transaction hash
                )
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        paymentResult = result
                    )
                }
                
                println("Payment sent successfully: ${currentState.amount} ${currentState.selectedToken} to ${currentState.recipientAddress}")
                
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to send payment: ${error.message}",
                        paymentResult = PaymentResult(
                            success = false,
                            error = error.message
                        )
                    )
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    private fun loadWalletInfo() {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual wallet service integration
                // val walletService = WalletFactory.getInstance() as SolanaWalletService
                // val connected = walletService.isWalletConnected()
                
                // Simulate loading wallet info
                delay(500)
                
                _uiState.update { currentState ->
                    currentState.copy(
                        isWalletConnected = true,
                        availableTokens = sampleTokens
                    )
                }
                
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isWalletConnected = false,
                        errorMessage = "Failed to load wallet info: ${error.message}"
                    )
                }
            }
        }
    }
    
    private fun validateRecipientAddress(address: String) {
        viewModelScope.launch {
            // Basic Solana address validation
            if (address.isBlank()) {
                return@launch
            }
            
            val error = when {
                address.length < 32 -> "Address too short"
                address.length > 44 -> "Address too long"
                !address.all { it.isLetterOrDigit() } -> "Invalid characters in address"
                else -> null
            }
            
            _uiState.update { it.copy(recipientError = error) }
        }
    }
    
    private fun validateAmount(amount: String) {
        viewModelScope.launch {
            if (amount.isBlank()) {
                return@launch
            }
            
            val amountValue = amount.toDoubleOrNull()
            val currentState = _uiState.value
            val availableBalance = currentState.getTokenBalance(currentState.selectedToken)
            
            val error = when {
                amountValue == null -> "Invalid amount"
                amountValue <= 0 -> "Amount must be greater than 0"
                amountValue > availableBalance -> "Insufficient balance"
                else -> null
            }
            
            _uiState.update { it.copy(amountError = error) }
        }
    }
}