package com.truetap.solana.seeker.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.repositories.WalletRepository
import com.truetap.solana.seeker.repositories.TrueTapContact
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
    val amount: BigDecimal = BigDecimal.ZERO,
    val isFirstPayment: Boolean = false,
    val riskLevel: RiskLevel = RiskLevel.LOW
)

enum class RiskLevel {
    LOW, MEDIUM, HIGH
}

data class SplitPayUiState(
    val participants: List<SplitParticipant> = emptyList(),
    val availableContacts: List<TrueTapContact> = emptyList(),
    val totalAmount: String = "",
    val splitEvenly: Boolean = true,
    val feePreset: FeePreset = FeePreset.NORMAL,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val info: String? = "Divide evenly or set custom shares — requests sent automatically. Fees may change during congestion.",
    val showSummary: Boolean = false,
    val totalPercent: BigDecimal = BigDecimal.ZERO,
    val validationError: String? = null
) {
    val isFormValid: Boolean
        get() = totalAmount.isNotBlank() && 
                totalAmount.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } == true &&
                participants.isNotEmpty() &&
                validationError == null
}

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
        validateSplit()
    }

    fun loadContacts() {
        viewModelScope.launch {
            try {
                val contacts = walletRepository.getTrueTapContacts()
                _uiState.value = _uiState.value.copy(availableContacts = contacts)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Failed to load contacts")
            }
        }
    }

    fun toggleContactSelection(contact: TrueTapContact) {
        val current = _uiState.value.participants.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == contact.id }
        if (existingIndex >= 0) {
            current.removeAt(existingIndex)
        } else {
            // Check if this is a first payment to this contact
            val isFirstPayment = !hasPaymentHistory(contact.address)
            val riskLevel = assessContactRisk(contact.address)
            current.add(SplitParticipant(
                id = contact.id, 
                name = contact.name, 
                address = contact.address,
                isFirstPayment = isFirstPayment,
                riskLevel = riskLevel
            ))
        }
        setParticipants(current)
    }

    private fun hasPaymentHistory(address: String): Boolean {
        // TODO: Implement actual payment history check
        // For now, return false to show first-payment warnings
        return false
    }

    private fun assessContactRisk(address: String): RiskLevel {
        // TODO: Implement actual risk assessment
        // For now, return MEDIUM for demo purposes
        return RiskLevel.MEDIUM
    }

    fun addDemoContacts(count: Int = 3) {
        viewModelScope.launch {
            try {
                val contacts = walletRepository.getTrueTapContacts().take(count)
                val added = contacts.map { c ->
                    val isFirstPayment = !hasPaymentHistory(c.address)
                    val riskLevel = assessContactRisk(c.address)
                    SplitParticipant(
                        id = c.id, 
                        name = c.name, 
                        address = c.address,
                        isFirstPayment = isFirstPayment,
                        riskLevel = riskLevel
                    )
                }
                setParticipants(added)
            } catch (_: Exception) { }
        }
    }

    fun toggleEvenSplit(even: Boolean) {
        _uiState.value = _uiState.value.copy(splitEvenly = even)
        recalcSplits()
        validateSplit()
    }

    fun setTotalAmount(amount: String) {
        _uiState.value = _uiState.value.copy(totalAmount = amount)
        recalcSplits()
        validateSplit()
    }

    fun setFeePreset(preset: FeePreset) {
        _uiState.value = _uiState.value.copy(feePreset = preset)
    }

    fun showSummary() {
        if (validateSplit()) {
            _uiState.value = _uiState.value.copy(showSummary = true)
        }
    }

    fun hideSummary() {
        _uiState.value = _uiState.value.copy(showSummary = false)
    }

    private fun validateSplit(): Boolean {
        val current = _uiState.value
        val total = current.totalAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        
        if (total <= BigDecimal.ZERO) {
            _uiState.value = current.copy(validationError = "Enter a valid total amount")
            return false
        }
        
        if (current.participants.isEmpty()) {
            _uiState.value = current.copy(validationError = "Select at least one participant")
            return false
        }

        if (!current.splitEvenly) {
            val totalPercent = current.participants.sumOf { it.sharePercent }
            if (totalPercent != BigDecimal(100)) {
                _uiState.value = current.copy(
                    validationError = "Total percentage must equal 100% (currently ${totalPercent}%)",
                    totalPercent = totalPercent
                )
                return false
            }
        }

        _uiState.value = current.copy(validationError = null)
        return true
    }

    private fun recalcSplits() {
        val total = _uiState.value.totalAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val participants = _uiState.value.participants
        if (participants.isEmpty()) return

        val updated = if (_uiState.value.splitEvenly) {
            val per = if (participants.isNotEmpty()) total.divide(BigDecimal(participants.size), 9, java.math.RoundingMode.HALF_UP) else BigDecimal.ZERO
            val percent = BigDecimal(100).divide(BigDecimal(participants.size), 2, java.math.RoundingMode.HALF_UP)
            participants.map { it.copy(amount = per, sharePercent = percent) }
        } else {
            // Respect existing sharePercent, compute amounts
            participants.map { p -> 
                p.copy(amount = total.multiply(p.sharePercent).divide(BigDecimal(100), 9, java.math.RoundingMode.HALF_UP)) 
            }
        }
        _uiState.value = _uiState.value.copy(participants = updated)
    }

    fun submitSplitSendOrRequests(sendNow: Boolean) {
        if (_uiState.value.isSubmitting) return
        if (!validateSplit()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val preset = _uiState.value.feePreset
                val total = _uiState.value.totalAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
                
                // Iterate participants: either send transactions now or enqueue requests
                _uiState.value.participants.forEach { p ->
                    val amt = p.amount.toDouble()
                    if (amt <= 0.0) return@forEach
                    
                    if (sendNow) {
                        // Best-effort send; on IO exception WalletRepository enqueues to outbox automatically
                        walletRepository.sendTransactionWithPreset(
                            toAddress = p.address,
                            amount = amt,
                            message = "Split: ${_uiState.value.totalAmount} SOL",
                            feePreset = preset,
                            activityResultSender = null
                        )
                    } else {
                        // Enqueue as pending request
                        outboxRepository.enqueue(
                            com.truetap.solana.seeker.repositories.PendingTransaction(
                                id = java.util.UUID.randomUUID().toString(),
                                toAddress = p.address,
                                amount = 0.0,
                                memo = "Request: ${amt} SOL",
                                feePreset = preset,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false, 
                    info = if (sendNow) "Split sent — watch feed for confirmations" else "Requests queued — recipients will be notified",
                    showSummary = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSubmitting = false, error = e.message ?: "Split failed")
            }
        }
    }
}


