package com.truetap.solana.seeker.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.repositories.TransactionOutboxRepository
import com.truetap.solana.seeker.repositories.PendingTransaction
import com.truetap.solana.seeker.services.FeePreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecurringUiState(
    val recipient: String = "",
    val amount: String = "",
    val memo: String = "",
    val cadenceDays: Int = 7,
    val feePreset: FeePreset = FeePreset.NORMAL,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val info: String? = "Recurring? Fees may vary; transactions will queue if offline."
)

@HiltViewModel
class RecurringPaymentViewModel @Inject constructor(
    private val outbox: TransactionOutboxRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(RecurringUiState())
    val ui: StateFlow<RecurringUiState> = _ui.asStateFlow()

    fun setRecipient(addr: String) { _ui.value = _ui.value.copy(recipient = addr) }
    fun setAmount(a: String) { _ui.value = _ui.value.copy(amount = a.filter { it.isDigit() || it == '.' }) }
    fun setMemo(m: String) { _ui.value = _ui.value.copy(memo = m) }
    fun setCadence(days: Int) { _ui.value = _ui.value.copy(cadenceDays = days) }
    fun setFeePreset(p: FeePreset) { _ui.value = _ui.value.copy(feePreset = p) }

    fun schedule() {
        if (_ui.value.isSubmitting) return
        val amt = _ui.value.amount.toDoubleOrNull() ?: 0.0
        if (_ui.value.recipient.isBlank() || amt <= 0.0) {
            _ui.value = _ui.value.copy(error = "Enter recipient and valid amount")
            return
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(isSubmitting = true, error = null)
            try {
                // Persist first occurrence into outbox; scheduler would enqueue on cadence
                outbox.enqueue(
                    PendingTransaction(
                        id = java.util.UUID.randomUUID().toString(),
                        toAddress = _ui.value.recipient,
                        amount = amt,
                        memo = _ui.value.memo,
                        feePreset = _ui.value.feePreset,
                        createdAt = System.currentTimeMillis()
                    )
                )
                _ui.value = _ui.value.copy(isSubmitting = false, info = "Scheduled and queued when due")
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(isSubmitting = false, error = e.message ?: "Failed to schedule")
            }
        }
    }
}


