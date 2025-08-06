package com.truetap.solana.seeker.presentation.screens.scheduled

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Scheduled Payment functionality
 * Manages state for scheduling payments and contact management
 */
@HiltViewModel
class ScheduledPaymentViewModel @Inject constructor(
    // TODO: Inject actual repositories when available
    // private val scheduledPaymentRepository: ScheduledPaymentRepository,
    // private val contactRepository: ContactRepository,
    // private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduledPaymentUiState())
    val uiState: StateFlow<ScheduledPaymentUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    // Contact Management
    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredContacts = filterContacts(query, _uiState.value.contacts)
        )
    }

    fun selectContact(contact: Contact) {
        _uiState.value = _uiState.value.copy(
            selectedContact = contact,
            recipientAddress = contact.walletAddress,
            selectedToken = contact.preferredToken,
            showContactSelector = false
        )
    }

    fun showContactSelector() {
        _uiState.value = _uiState.value.copy(
            showContactSelector = true,
            filteredContacts = _uiState.value.contacts
        )
    }

    fun hideContactSelector() {
        _uiState.value = _uiState.value.copy(showContactSelector = false)
    }

    // Payment Details
    fun setRecipientAddress(address: String) {
        _uiState.value = _uiState.value.copy(
            recipientAddress = address,
            selectedContact = null // Clear selected contact if manually entering address
        )
    }

    fun setAmount(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun setSelectedToken(token: String) {
        _uiState.value = _uiState.value.copy(selectedToken = token)
    }

    fun setMemo(memo: String) {
        _uiState.value = _uiState.value.copy(memo = memo)
    }

    // Date and Time Management
    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = true)
    }

    fun hideDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }

    fun setSelectedDate(date: LocalDateTime) {
        _uiState.value = _uiState.value.copy(
            selectedDate = date,
            showDatePicker = false
        )
    }

    fun showTimePicker() {
        _uiState.value = _uiState.value.copy(showTimePicker = true)
    }

    fun hideTimePicker() {
        _uiState.value = _uiState.value.copy(showTimePicker = false)
    }

    fun setSelectedTime(time: LocalDateTime) {
        _uiState.value = _uiState.value.copy(
            selectedTime = time,
            showTimePicker = false
        )
    }

    // Repeat Interval Management
    fun setRepeatInterval(interval: RepeatInterval) {
        _uiState.value = _uiState.value.copy(repeatInterval = interval)
    }

    fun setMaxExecutions(maxExecutions: String) {
        _uiState.value = _uiState.value.copy(maxExecutions = maxExecutions)
    }

    // Payment Scheduling
    fun showPaymentScheduleDialog() {
        if (validatePaymentDetails()) {
            _uiState.value = _uiState.value.copy(showPaymentScheduleDialog = true)
        }
    }

    fun hidePaymentScheduleDialog() {
        _uiState.value = _uiState.value.copy(showPaymentScheduleDialog = false)
    }

    fun schedulePayment() {
        val currentState = _uiState.value
        
        if (!validatePaymentDetails()) {
            return
        }

        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null,
            showPaymentScheduleDialog = false
        )

        viewModelScope.launch {
            try {
                // Simulate payment scheduling
                delay(2000)

                val scheduledPayment = createScheduledPayment(currentState)
                
                // TODO: Replace with actual repository call
                // val result = scheduledPaymentRepository.createScheduledPayment(scheduledPayment)
                
                // Simulate success
                val updatedPayments = currentState.activeScheduledPayments + scheduledPayment
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showSuccessDialog = true,
                    activeScheduledPayments = updatedPayments,
                    // Reset form
                    selectedContact = null,
                    recipientAddress = "",
                    amount = "",
                    memo = "",
                    selectedDate = null,
                    selectedTime = null,
                    repeatInterval = RepeatInterval.NONE,
                    maxExecutions = ""
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to schedule payment. Please try again."
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

    // Scheduled Payment Management
    fun cancelScheduledPayment(paymentId: String) {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual repository call
                // scheduledPaymentRepository.cancelScheduledPayment(paymentId)
                
                val updatedPayments = _uiState.value.activeScheduledPayments.map { payment ->
                    if (payment.id == paymentId) {
                        payment.copy(status = PaymentStatus.CANCELLED)
                    } else {
                        payment
                    }
                }
                
                _uiState.value = _uiState.value.copy(activeScheduledPayments = updatedPayments)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to cancel scheduled payment."
                )
            }
        }
    }

    fun executeScheduledPayment(paymentId: String) {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual payment execution logic
                // paymentExecutionService.executePayment(paymentId)
                
                val updatedPayments = _uiState.value.activeScheduledPayments.map { payment ->
                    if (payment.id == paymentId) {
                        payment.copy(
                            status = PaymentStatus.COMPLETED,
                            currentExecutions = payment.currentExecutions + 1,
                            lastExecutedAt = LocalDateTime.now(),
                            nextExecutionDate = calculateNextExecutionDate(payment)
                        )
                    } else {
                        payment
                    }
                }
                
                _uiState.value = _uiState.value.copy(activeScheduledPayments = updatedPayments)
                
            } catch (e: Exception) {
                val updatedPayments = _uiState.value.activeScheduledPayments.map { payment ->
                    if (payment.id == paymentId) {
                        payment.copy(
                            status = PaymentStatus.FAILED,
                            failureReason = "Payment execution failed"
                        )
                    } else {
                        payment
                    }
                }
                
                _uiState.value = _uiState.value.copy(activeScheduledPayments = updatedPayments)
            }
        }
    }

    // Private Helper Methods
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual data loading
                // val contacts = contactRepository.getAllContacts()
                // val tokenBalances = walletRepository.getTokenBalances()
                // val scheduledPayments = scheduledPaymentRepository.getActiveScheduledPayments()
                
                // Mock data for now
                val mockContacts = generateMockContacts()
                val mockTokenBalances = mapOf(
                    "SOL" to BigDecimal("25.4567"),
                    "USDC" to BigDecimal("1250.00"),
                    "BONK" to BigDecimal("1000000.0")
                )
                val mockScheduledPayments = generateMockScheduledPayments()
                
                _uiState.value = _uiState.value.copy(
                    contacts = mockContacts,
                    filteredContacts = mockContacts,
                    tokenBalances = mockTokenBalances,
                    activeScheduledPayments = mockScheduledPayments
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load data. Please try again."
                )
            }
        }
    }

    private fun filterContacts(query: String, contacts: List<Contact>): List<Contact> {
        if (query.isBlank()) return contacts
        
        return contacts.filter { contact ->
            contact.name.contains(query, ignoreCase = true) ||
            contact.walletAddress.contains(query, ignoreCase = true) ||
            contact.tags.any { it.contains(query, ignoreCase = true) }
        }.sortedBy { contact ->
            // Prioritize favorites and recent transactions
            when {
                contact.isFavorite -> 0
                contact.lastTransactionDate != null -> 1
                else -> 2
            }
        }
    }

    private fun validatePaymentDetails(): Boolean {
        val currentState = _uiState.value
        
        if (currentState.recipientAddress.trim().isEmpty()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter a recipient address or select a contact."
            )
            return false
        }
        
        val amount = currentState.amount.toBigDecimalOrNull()
        if (amount == null || amount <= BigDecimal.ZERO) {
            _uiState.value = currentState.copy(
                errorMessage = "Please enter a valid amount."
            )
            return false
        }
        
        val availableBalance = currentState.tokenBalances[currentState.selectedToken] ?: BigDecimal.ZERO
        if (amount > availableBalance) {
            _uiState.value = currentState.copy(
                errorMessage = "Insufficient ${currentState.selectedToken} balance."
            )
            return false
        }
        
        if (currentState.selectedDate == null) {
            _uiState.value = currentState.copy(
                errorMessage = "Please select a start date."
            )
            return false
        }
        
        if (currentState.selectedTime == null) {
            _uiState.value = currentState.copy(
                errorMessage = "Please select a start time."
            )
            return false
        }
        
        if (currentState.repeatInterval != RepeatInterval.NONE) {
            val maxExec = currentState.maxExecutions.toIntOrNull()
            if (maxExec == null || maxExec <= 0 || maxExec > 100) {
                _uiState.value = currentState.copy(
                    errorMessage = "Please enter a valid number of executions (1-100)."
                )
                return false
            }
        }
        
        return true
    }

    private fun createScheduledPayment(state: ScheduledPaymentUiState): ScheduledPayment {
        val startDateTime = combineDateTime(state.selectedDate!!, state.selectedTime!!)
        
        return ScheduledPayment(
            id = UUID.randomUUID().toString(),
            recipientAddress = state.recipientAddress.trim(),
            recipientName = state.selectedContact?.name,
            amount = state.amount.toBigDecimal(),
            token = state.selectedToken,
            memo = state.memo.trim().ifEmpty { null },
            startDate = startDateTime,
            nextExecutionDate = startDateTime,
            repeatInterval = state.repeatInterval,
            maxExecutions = if (state.repeatInterval != RepeatInterval.NONE) 
                state.maxExecutions.toIntOrNull() else null,
            currentExecutions = 0,
            status = PaymentStatus.PENDING,
            createdAt = LocalDateTime.now()
        )
    }

    private fun combineDateTime(date: LocalDateTime, time: LocalDateTime): LocalDateTime {
        return date.withHour(time.hour).withMinute(time.minute)
    }

    private fun calculateNextExecutionDate(payment: ScheduledPayment): LocalDateTime {
        return when (payment.repeatInterval) {
            RepeatInterval.DAILY -> payment.nextExecutionDate.plusDays(1)
            RepeatInterval.WEEKLY -> payment.nextExecutionDate.plusWeeks(1)
            RepeatInterval.MONTHLY -> payment.nextExecutionDate.plusMonths(1)
            RepeatInterval.NONE -> payment.nextExecutionDate
        }
    }

    private fun generateMockContacts(): List<Contact> {
        return listOf(
            Contact(
                id = "1",
                name = "Alice Cooper",
                walletAddress = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU",
                initials = "AC",
                isFavorite = true,
                lastTransactionDate = LocalDateTime.now().minusDays(2),
                totalTransactions = 15,
                preferredToken = "SOL",
                tags = listOf("Friend", "DeFi")
            ),
            Contact(
                id = "2", 
                name = "Bob Johnson",
                walletAddress = "FNNvb1AFDnDVPkocEri8mWbzKfhBFz2G8wW8J8tKGbN",
                initials = "BJ",
                isFavorite = false,
                lastTransactionDate = LocalDateTime.now().minusDays(7),
                totalTransactions = 8,
                preferredToken = "USDC",
                tags = listOf("Business")
            ),
            Contact(
                id = "3",
                name = "Charlie Davis",
                walletAddress = "2HgPw6NVcKoGGYnMxSr3E4axZ5LVY8xLv6W4NGBtv9n",
                initials = "CD",
                isFavorite = true,
                lastTransactionDate = LocalDateTime.now().minusDays(1),
                totalTransactions = 25,
                preferredToken = "SOL",
                tags = listOf("Family", "Regular")
            )
        )
    }

    private fun generateMockScheduledPayments(): List<ScheduledPayment> {
        return listOf(
            ScheduledPayment(
                id = "sp1",
                recipientAddress = "7xKXtg2CW87d97TXJSDpbD5jBkheTqA83TZRuJosgAsU",
                recipientName = "Alice Cooper",
                amount = BigDecimal("5.0"),
                token = "SOL",
                startDate = LocalDateTime.now().minusDays(5),
                nextExecutionDate = LocalDateTime.now().plusDays(2),
                repeatInterval = RepeatInterval.WEEKLY,
                maxExecutions = 12,
                currentExecutions = 2,
                status = PaymentStatus.PENDING,
                createdAt = LocalDateTime.now().minusDays(5),
                memo = "Weekly allowance"
            ),
            ScheduledPayment(
                id = "sp2",
                recipientAddress = "FNNvb1AFDnDVPkocEri8mWbzKfhBFz2G8wW8J8tKGbN",
                recipientName = "Bob Johnson",
                amount = BigDecimal("100.0"),
                token = "USDC",
                startDate = LocalDateTime.now().minusDays(15),
                nextExecutionDate = LocalDateTime.now().plusDays(15),
                repeatInterval = RepeatInterval.MONTHLY,
                maxExecutions = 6,
                currentExecutions = 1,
                status = PaymentStatus.PENDING,
                createdAt = LocalDateTime.now().minusDays(15),
                memo = "Monthly rent split"
            )
        )
    }
}