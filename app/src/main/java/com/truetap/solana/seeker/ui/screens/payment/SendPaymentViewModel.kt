/**
 * Send Payment ViewModel - TrueTap
 * Manages state and business logic for the Send Payment screen
 */

package com.truetap.solana.seeker.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.data.MockData
import com.truetap.solana.seeker.data.models.Contact
import com.truetap.solana.seeker.data.models.TransactionType
import com.truetap.solana.seeker.domain.model.RepeatInterval
import dagger.hilt.android.lifecycle.HiltViewModel
import com.truetap.solana.seeker.repositories.WalletRepository
import kotlinx.coroutines.flow.collectLatest
import com.truetap.solana.seeker.utils.SolanaValidation
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.truetap.solana.seeker.services.FeePreset

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

data class ScheduleResult(
    val success: Boolean,
    val error: String? = null,
    val scheduledPaymentId: String? = null
)

data class SendPaymentUiState(
    val recipientAddress: String = "",
    val amount: String = "",
    val memo: String = "",
    val selectedToken: String = "SOL",
    val availableTokens: List<TokenInfo> = emptyList(),
    val recentContacts: List<Contact> = emptyList(),
    val selectedContact: Contact? = null,
    val isLoading: Boolean = false,
    val isWalletConnected: Boolean = false,
    val recipientError: String? = null,
    val amountError: String? = null,
    val errorMessage: String? = null,
    val paymentResult: PaymentResult? = null,
    val scheduleResult: ScheduleResult? = null,
    val feePreset: com.truetap.solana.seeker.services.FeePreset = com.truetap.solana.seeker.services.FeePreset.NORMAL,
    val riskWarning: String? = null
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
class SendPaymentViewModel @Inject constructor(
    private val mockData: MockData,
    private val walletRepository: WalletRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SendPaymentUiState())
    val uiState: StateFlow<SendPaymentUiState> = _uiState.asStateFlow()
    
    init {
        loadWalletInfo()
        loadContacts()
        observeWalletState()
    }

    fun updateFeePreset(preset: FeePreset) {
        _uiState.update { it.copy(feePreset = preset) }
    }
    
    // TODO: Add navigation args support later
    
    fun updateRecipientAddress(address: String) {
        _uiState.update { currentState ->
            currentState.copy(
                recipientAddress = address,
                recipientError = null,
                // Clear selected contact if address is manually changed
                selectedContact = if (address != currentState.selectedContact?.walletAddress) null else currentState.selectedContact
            )
        }
        
        // Validate address format
        validateRecipientAddress(address)
        // Trigger background risk assessment
        viewModelScope.launch {
            val warning = walletRepository.assessRecipientRisk(address)
            _uiState.update { it.copy(riskWarning = warning) }
        }
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
    
    fun sendPayment(activityResultSender: com.solana.mobilewalletadapter.clientlib.ActivityResultSender? = null) {
        val currentState = _uiState.value
        
        if (!currentState.isFormValid) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val amountDouble = currentState.amount.toDoubleOrNull() ?: 0.0
                val result = walletRepository.sendTransactionWithPreset(
                    toAddress = currentState.recipientAddress,
                    amount = amountDouble,
                    message = currentState.memo.takeIf { it.isNotBlank() },
                    feePreset = currentState.feePreset,
                    activityResultSender = activityResultSender
                )
                // UX note: fees may change while queued
                result.onSuccess { tx ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            paymentResult = PaymentResult(success = true, transactionHash = tx.txId)
                        )
                    }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Fees may change while queued.",
                            paymentResult = PaymentResult(success = false, error = error.message)
                        )
                    }
                }
                
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
    
    fun selectContact(contact: Contact) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedContact = contact,
                recipientAddress = contact.walletAddress,
                recipientError = null
            )
        }
    }
    
    fun clearSelectedContact() {
        _uiState.update { currentState ->
            currentState.copy(
                selectedContact = null,
                recipientAddress = ""
            )
        }
    }
    
    fun schedulePayment(startDate: LocalDateTime, recurrence: RepeatInterval, maxExecutions: Int?) {
        val currentState = _uiState.value
        
        if (!currentState.isFormValid) {
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val amount = currentState.amount.toDoubleOrNull() ?: 0.0
                val scheduledPayment = mockData.createScheduledPayment(
                    recipientAddress = currentState.recipientAddress,
                    amount = BigDecimal.valueOf(amount),
                    token = currentState.selectedToken,
                    memo = currentState.memo.takeIf { it.isNotBlank() },
                    startDate = startDate,
                    repeatInterval = recurrence,
                    maxExecutions = maxExecutions
                )
                
                // Add to mock data
                mockData.addScheduledPayment(scheduledPayment)
                
                val result = ScheduleResult(
                    success = true,
                    scheduledPaymentId = scheduledPayment.id
                )
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        scheduleResult = result
                    )
                }
                
                // Payment scheduled successfully - UI state updated with result
                
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to schedule payment: ${error.message}",
                        scheduleResult = ScheduleResult(
                            success = false,
                            error = error.message
                        )
                    )
                }
            }
        }
    }
    
    fun clearScheduleResult() {
        _uiState.update { it.copy(scheduleResult = null) }
    }
    
    private fun loadWalletInfo() {
        viewModelScope.launch {
            val isConnected = walletRepository.walletState.value.account != null
            _uiState.update { currentState ->
                currentState.copy(
                    isWalletConnected = isConnected
                )
            }
        }
    }
    
    private fun loadContacts() {
        viewModelScope.launch {
            try {
                val recentContacts = mockData.getRecentContacts()
                _uiState.update { currentState ->
                    currentState.copy(recentContacts = recentContacts)
                }
            } catch (error: Exception) {
                // Silently fail for contacts - not critical for payment functionality
                // Contacts loading failed - not critical for payment functionality
            }
        }
    }
    
    private fun observeWalletState() {
        viewModelScope.launch {
            walletRepository.walletState.collectLatest { state ->
                val isConnected = state.account != null
                val solBalance = state.balance?.solBalance?.toDouble() ?: 0.0
                val tokenInfos = buildList {
                    add(TokenInfo(symbol = "SOL", name = "Solana", balance = solBalance))
                    state.balance?.tokenBalances?.forEach { tb ->
                        add(
                            TokenInfo(
                                symbol = tb.symbol,
                                name = tb.name,
                                balance = tb.uiAmount
                            )
                        )
                    }
                }
                _uiState.update { current ->
                    current.copy(
                        isWalletConnected = isConnected,
                        availableTokens = tokenInfos
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
                !SolanaValidation.isValidPublicKey(address) -> "Invalid Solana address"
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