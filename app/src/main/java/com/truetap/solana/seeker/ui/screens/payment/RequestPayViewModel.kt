package com.truetap.solana.seeker.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.repositories.TransactionOutboxRepository
import com.truetap.solana.seeker.repositories.RequestsRepository
import com.truetap.solana.seeker.repositories.PaymentRequest
import com.truetap.solana.seeker.repositories.RequestStatus
import com.truetap.solana.seeker.repositories.WalletRepository
import com.truetap.solana.seeker.services.FeePreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RequestPayUiState(
    val recipientAddress: String = "",
    val amount: String = "",
    val memo: String = "",
    val isPrivate: Boolean = true,
    val feePreset: FeePreset = FeePreset.NORMAL,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val info: String? = "Request money? Add a note for context. Private by default — hide from feed."
)

@HiltViewModel
class RequestPayViewModel @Inject constructor(
    private val outboxRepository: TransactionOutboxRepository,
    private val walletRepository: WalletRepository,
    private val requestsRepository: RequestsRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RequestPayUiState())
    val uiState: StateFlow<RequestPayUiState> = _uiState.asStateFlow()

    fun setRecipient(address: String) { _uiState.value = _uiState.value.copy(recipientAddress = address) }
    fun setAmount(amount: String) { _uiState.value = _uiState.value.copy(amount = amount.filter { it.isDigit() || it == '.' }) }
    fun setMemo(memo: String) { _uiState.value = _uiState.value.copy(memo = memo) }
    fun togglePrivate(isPrivate: Boolean) { _uiState.value = _uiState.value.copy(isPrivate = isPrivate) }
    fun setFeePreset(preset: FeePreset) { _uiState.value = _uiState.value.copy(feePreset = preset) }

    fun useDemoContact() {
        viewModelScope.launch {
            try {
                val contacts = walletRepository.getTrueTapContacts()
                val first = contacts.firstOrNull()
                if (first != null) setRecipient(first.address)
            } catch (_: Exception) { }
        }
    }

    fun submitRequest() {
        if (_uiState.value.isSubmitting) return
        val amt = _uiState.value.amount.toDoubleOrNull() ?: 0.0
        val to = _uiState.value.recipientAddress
        if (to.isBlank() || amt <= 0.0) {
            _uiState.value = _uiState.value.copy(error = "Enter recipient and valid amount")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                // For requests, add an outbox placeholder entry with memo; no send
                outboxRepository.enqueue(
                    com.truetap.solana.seeker.repositories.PendingTransaction(
                        id = java.util.UUID.randomUUID().toString(),
                        toAddress = to,
                        amount = 0.0,
                        memo = "Request: ${_uiState.value.amount} SOL • ${_uiState.value.memo}",
                        feePreset = _uiState.value.feePreset,
                        createdAt = System.currentTimeMillis()
                    )
                )
                // Add a pending request for demo inbox (from recipient's perspective)
                val me = walletRepository.walletState.value.account?.publicKey ?: "me"
                requestsRepository.addRequest(
                    PaymentRequest(
                        id = java.util.UUID.randomUUID().toString(),
                        fromAddress = me,
                        toAddress = to,
                        amount = amt,
                        memo = _uiState.value.memo.takeIf { it.isNotBlank() },
                        status = RequestStatus.PENDING,
                        createdAt = System.currentTimeMillis()
                    )
                )
                _uiState.value = _uiState.value.copy(isSubmitting = false, info = "Request sent — your contact will be notified")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = e.message ?: "Request failed")
            }
        }
    }
}


