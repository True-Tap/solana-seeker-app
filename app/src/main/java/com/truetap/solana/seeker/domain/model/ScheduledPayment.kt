package com.truetap.solana.seeker.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Scheduled Payment Domain Models - TrueTap
 * Data models for scheduled payment functionality
 */

data class ScheduledPayment(
    val id: String,
    val recipientAddress: String,
    val recipientName: String? = null,
    val amount: BigDecimal,
    val token: String,
    val memo: String? = null,
    val startDate: LocalDateTime,
    val nextExecutionDate: LocalDateTime,
    val repeatInterval: RepeatInterval,
    val maxExecutions: Int? = null,
    val currentExecutions: Int = 0,
    val status: PaymentStatus,
    val createdAt: LocalDateTime,
    val lastExecutedAt: LocalDateTime? = null,
    val failureReason: String? = null
)

enum class RepeatInterval(val displayName: String, val value: String) {
    DAILY("Daily", "daily"),
    WEEKLY("Weekly", "weekly"), 
    MONTHLY("Monthly", "monthly"),
    NONE("One Time", "none")
}

enum class PaymentStatus(val displayName: String) {
    PENDING("Pending"),
    COMPLETED("Completed"), 
    FAILED("Failed"),
    CANCELLED("Cancelled")
}

data class ScheduledPaymentUiState(
    val selectedContact: Contact? = null,
    val recipientAddress: String = "",
    val amount: String = "",
    val selectedToken: String = "SOL",
    val memo: String = "",
    val selectedDate: LocalDateTime? = null,
    val selectedTime: LocalDateTime? = null,
    val repeatInterval: RepeatInterval = RepeatInterval.NONE,
    val maxExecutions: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showSuccessDialog: Boolean = false,
    val activeScheduledPayments: List<ScheduledPayment> = emptyList(),
    val contacts: List<Contact> = emptyList(),
    val tokenBalances: Map<String, BigDecimal> = emptyMap(),
    val searchQuery: String = "",
    val filteredContacts: List<Contact> = emptyList(),
    val showContactSelector: Boolean = false,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    val showPaymentScheduleDialog: Boolean = false
)

data class Contact(
    val id: String,
    val name: String,
    val walletAddress: String,
    val avatar: String? = null,
    val initials: String,
    val isFavorite: Boolean = false,
    val lastTransactionDate: LocalDateTime? = null,
    val totalTransactions: Int = 0,
    val preferredToken: String = "SOL",
    val tags: List<String> = emptyList(),
    val notes: String? = null
)