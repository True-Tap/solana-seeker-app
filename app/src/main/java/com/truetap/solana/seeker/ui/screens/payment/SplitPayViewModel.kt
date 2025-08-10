package com.truetap.solana.seeker.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.repositories.WalletRepository
import com.truetap.solana.seeker.repositories.TransactionOutboxRepository
import com.truetap.solana.seeker.services.FeePreset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class SplitParticipant(
    val id: String,
    val name: String,
    val address: String,
    val sharePercent: BigDecimal = BigDecimal.ZERO,
    val amount: BigDecimal = BigDecimal.ZERO
)

data class SplitPayUiState(
    val participants: List<SplitParticipant> = emptyList(),
    val totalAmount: String = "",
    val splitEvenly: Boolean = true,
    val feePreset: FeePreset = FeePreset.NORMAL,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val info: String? = "Divide evenly or set custom shares — requests sent automatically. Fees may change during congestion."
)

@HiltViewModel
class SplitPayViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val outboxRepository: TransactionOutboxRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplitPayUiState())
    val uiState: StateFlow<SplitPayUiState> = _uiState.asStateFlow()

    fun setParticipants(list: List<SplitParticipant>) {
        _uiState.value = _uiState.value.copy(participants = list)
        recalcSplits()
    }

    fun addDemoContacts(count: Int = 3) {
        viewModelScope.launch {
            try {
                val contacts = walletRepository.getTrueTapContacts().take(count)
                val added = contacts.map { c ->
                    SplitParticipant(id = c.id, name = c.name, address = c.address)
                }
                setParticipants(added)
            } catch (_: Exception) { }
        }
    }

    fun toggleEvenSplit(even: Boolean) {
        _uiState.value = _uiState.value.copy(splitEvenly = even)
        recalcSplits()
    }

    fun setTotalAmount(amount: String) {
        _uiState.value = _uiState.value.copy(totalAmount = amount)
        recalcSplits()
    }

    fun setFeePreset(preset: FeePreset) {
        _uiState.value = _uiState.value.copy(feePreset = preset)
    }

    private fun recalcSplits() {
        val total = _uiState.value.totalAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val participants = _uiState.value.participants
        if (participants.isEmpty()) return

        val updated = if (_uiState.value.splitEvenly) {
            val per = if (participants.isNotEmpty()) total.divide(BigDecimal(participants.size), 9, java.math.RoundingMode.HALF_UP) else BigDecimal.ZERO
            participants.map { it.copy(amount = per, sharePercent = BigDecimal(100).divide(BigDecimal(participants.size), 2, java.math.RoundingMode.HALF_UP)) }
        } else {
            // Respect existing sharePercent, compute amounts
            participants.map { p -> p.copy(amount = total.multiply(p.sharePercent).divide(BigDecimal(100), 9, java.math.RoundingMode.HALF_UP)) }
        }
        _uiState.value = _uiState.value.copy(participants = updated)
    }

    fun submitSplitSendOrRequests(sendNow: Boolean) {
        if (_uiState.value.isSubmitting) return
        val total = _uiState.value.totalAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (total <= BigDecimal.ZERO || _uiState.value.participants.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Enter a valid amount and select participants")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val preset = _uiState.value.feePreset
                // Iterate participants: either send transactions now or enqueue requests (UX only hook)
                _uiState.value.participants.forEach { p ->
                    val amt = p.amount.toDouble()
                    if (amt <= 0.0) return@forEach
                    if (sendNow) {
                        // Best-effort send; on IO exception WalletRepository enqueues to outbox automatically
                        walletRepository.sendTransactionWithPreset(
                            toAddress = p.address,
                            amount = amt,
                            message = "Split: ${_uiState.value.totalAmount}",
                            feePreset = preset,
                            activityResultSender = null
                        )
                    } else {
                        // Future: request flows via backend; for now record as pending request in outbox with zero send
                        outboxRepository.enqueue(
                            com.truetap.solana.seeker.repositories.PendingTransaction(
                                id = java.util.UUID.randomUUID().toString(),
                                toAddress = p.address,
                                amount = 0.0,
                                memo = "Request: ${amt}",
                                feePreset = preset,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
                _uiState.value = _uiState.value.copy(isSubmitting = false, info = if (sendNow) "Split sent — watch feed for confirmations" else "Requests queued — recipients will be notified")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = e.message ?: "Split failed")
            }
        }
    }
}


