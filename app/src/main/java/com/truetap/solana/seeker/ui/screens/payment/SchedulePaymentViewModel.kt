package com.truetap.solana.seeker.ui.screens.payment

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
 * ViewModel for Schedule Payment Screen
 * Manages state for scheduling recurring payments
 */

@HiltViewModel
class SchedulePaymentViewModel @Inject constructor(
    private val walletRepository: com.truetap.solana.seeker.repositories.WalletRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SchedulePaymentUiState())
    val uiState: StateFlow<SchedulePaymentUiState> = _uiState.asStateFlow()
    
    init {
        loadWalletInfo()
    }
    
    fun setRecipientAddress(address: String) {
        _uiState.value = _uiState.value.copy(recipientAddress = address)
    }
    
    fun setAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }
    
    fun setMemo(memo: String) {
        _uiState.value = _uiState.value.copy(memo = memo)
    }
    
    fun setSelectedToken(token: String) {
        _uiState.value = _uiState.value.copy(selectedToken = token)
    }
    
    fun setSelectedFrequency(frequency: String) {
        _uiState.value = _uiState.value.copy(selectedFrequency = frequency)
    }
    
    fun setMaxExecutions(maxExecutions: String) {
        _uiState.value = _uiState.value.copy(maxExecutions = maxExecutions)
    }
    
    fun schedulePayment() {
        val currentState = _uiState.value
        
        // Validation
        if (!currentState.walletConnected) {
            _uiState.value = currentState.copy(
                errorMessage = "Wallet not connected. Please connect your wallet first."
            )
            return
        }
        
        if (currentState.recipientAddress.trim().isEmpty()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter a recipient address."
            )
            return
        }
        
        val amount = currentState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter a valid amount."
            )
            return
        }
        
        val maxExec = currentState.maxExecutions.toIntOrNull()
        if (maxExec == null || maxExec <= 0 || maxExec > 100) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter a valid number of executions (1-100)."
            )
            return
        }
        
        _uiState.value = currentState.copy(isLoading = true, errorMessage = null)
        
        viewModelScope.launch {
            try {
                // Simulate payment scheduling
                delay(2000)
                
                // TODO: Replace with actual service call
                /*
                val result = schedulePaymentService.createSchedule(
                    SchedulePaymentRequest(
                        recipientPublicKey = currentState.recipientAddress.trim(),
                        amount = amount,
                        tokenSymbol = currentState.selectedToken,
                        memo = currentState.memo.trim().ifEmpty { null },
                        frequency = currentState.selectedFrequency,
                        maxExecutions = maxExec
                    )
                )
                
                if (result.success) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showSuccessDialog = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.error ?: "Unknown error occurred"
                    )
                }
                */
                
                // Simulate success for now
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showSuccessDialog = true
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to create payment schedule. Please try again."
                )
            }
        }
    }
    
    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(showSuccessDialog = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private fun loadWalletInfo() {
        viewModelScope.launch {
            walletRepository.walletState.collect { state ->
                val isConnected = state.account != null
                val sol = state.balance?.solBalance?.toDouble() ?: 0.0
                val balances = buildMap<String, Double> {
                    put("SOL", sol)
                    state.balance?.tokenBalances?.forEach { tb ->
                        put(tb.symbol, tb.uiAmount)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    walletConnected = isConnected,
                    tokenBalances = balances
                )
            }
        }
    }
}