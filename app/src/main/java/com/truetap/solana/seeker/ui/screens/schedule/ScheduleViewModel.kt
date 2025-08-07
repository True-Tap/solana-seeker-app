/**
 * Schedule ViewModel - TrueTap
 * Manages state and business logic for the Schedule screen
 */

package com.truetap.solana.seeker.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.data.MockData
import com.truetap.solana.seeker.domain.model.Contact
import com.truetap.solana.seeker.domain.model.RepeatInterval
import com.truetap.solana.seeker.domain.model.ScheduledPayment
import com.truetap.solana.seeker.domain.model.PaymentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

// Data Classes for Schedule Screen
data class ScheduleUiState(
    val scheduledPayments: List<ScheduledPayment> = emptyList(),
    val filteredPayments: List<ScheduledPayment> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: PaymentStatusFilter = PaymentStatusFilter.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val paymentToDelete: ScheduledPayment? = null,
    val showEditDialog: Boolean = false,
    val paymentToEdit: ScheduledPayment? = null
)

enum class PaymentStatusFilter(val displayName: String) {
    ALL("All"),
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed")
}

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val mockData: MockData
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()
    
    init {
        loadScheduledPayments()
    }
    
    /**
     * Refresh scheduled payments - public method for UI to call
     */
    fun refreshScheduledPayments() {
        loadScheduledPayments()
    }
    
    /**
     * Load all scheduled payments from MockData
     */
    private fun loadScheduledPayments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val payments = mockData.getScheduledPayments()
                _uiState.update { currentState ->
                    currentState.copy(
                        scheduledPayments = payments,
                        filteredPayments = filterPayments(payments, currentState.searchQuery, currentState.selectedFilter),
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (error: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load scheduled payments: ${error.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Update search query and filter payments
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            val filteredPayments = filterPayments(currentState.scheduledPayments, query, currentState.selectedFilter)
            currentState.copy(
                searchQuery = query,
                filteredPayments = filteredPayments
            )
        }
    }
    
    /**
     * Update status filter and filter payments
     */
    fun updateStatusFilter(filter: PaymentStatusFilter) {
        _uiState.update { currentState ->
            val filteredPayments = filterPayments(currentState.scheduledPayments, currentState.searchQuery, filter)
            currentState.copy(
                selectedFilter = filter,
                filteredPayments = filteredPayments
            )
        }
    }
    
    /**
     * Show delete confirmation dialog
     */
    fun showDeleteConfirmation(payment: ScheduledPayment) {
        _uiState.update { 
            it.copy(
                showDeleteConfirmation = true,
                paymentToDelete = payment
            )
        }
    }
    
    /**
     * Hide delete confirmation dialog
     */
    fun hideDeleteConfirmation() {
        _uiState.update { 
            it.copy(
                showDeleteConfirmation = false,
                paymentToDelete = null
            )
        }
    }
    
    /**
     * Delete a scheduled payment
     */
    fun deleteScheduledPayment() {
        val paymentToDelete = _uiState.value.paymentToDelete
        if (paymentToDelete != null) {
            viewModelScope.launch {
                try {
                    val success = mockData.removeScheduledPayment(paymentToDelete.id)
                    if (success) {
                        hideDeleteConfirmation()
                        loadScheduledPayments() // Refresh the list
                    } else {
                        _uiState.update { 
                            it.copy(errorMessage = "Failed to delete scheduled payment")
                        }
                    }
                } catch (error: Exception) {
                    _uiState.update { 
                        it.copy(errorMessage = "Error deleting payment: ${error.message}")
                    }
                }
            }
        }
    }
    
    /**
     * Show edit dialog for a scheduled payment
     */
    fun showEditDialog(payment: ScheduledPayment) {
        _uiState.update { 
            it.copy(
                showEditDialog = true,
                paymentToEdit = payment
            )
        }
    }
    
    /**
     * Hide edit dialog
     */
    fun hideEditDialog() {
        _uiState.update { 
            it.copy(
                showEditDialog = false,
                paymentToEdit = null
            )
        }
    }
    
    /**
     * Cancel a scheduled payment (mark as cancelled)
     */
    fun cancelScheduledPayment(payment: ScheduledPayment) {
        viewModelScope.launch {
            try {
                // For now, we'll delete it since MockData doesn't support status updates
                // In a real implementation, this would update the status to CANCELLED
                val success = mockData.removeScheduledPayment(payment.id)
                if (success) {
                    loadScheduledPayments() // Refresh the list
                } else {
                    _uiState.update { 
                        it.copy(errorMessage = "Failed to cancel scheduled payment")
                    }
                }
            } catch (error: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Error cancelling payment: ${error.message}")
                }
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    /**
     * Filter payments based on search query and status filter
     */
    private fun filterPayments(
        payments: List<ScheduledPayment>, 
        query: String, 
        filter: PaymentStatusFilter
    ): List<ScheduledPayment> {
        var filtered = payments
        
        // Apply status filter
        if (filter != PaymentStatusFilter.ALL) {
            filtered = filtered.filter { payment ->
                when (filter) {
                    PaymentStatusFilter.PENDING -> payment.status == PaymentStatus.PENDING
                    PaymentStatusFilter.COMPLETED -> payment.status == PaymentStatus.COMPLETED
                    PaymentStatusFilter.FAILED -> payment.status == PaymentStatus.FAILED
                    else -> true
                }
            }
        }
        
        // Apply search query
        if (query.isNotBlank()) {
            filtered = filtered.filter { payment ->
                payment.recipientName?.contains(query, ignoreCase = true) == true ||
                payment.recipientAddress.contains(query, ignoreCase = true) ||
                payment.token.contains(query, ignoreCase = true) ||
                payment.memo?.contains(query, ignoreCase = true) == true
            }
        }
        
        // Sort by next execution date, then by creation date
        return filtered.sortedWith(
            compareBy<ScheduledPayment> { it.status != PaymentStatus.PENDING }
                .thenBy { it.nextExecutionDate }
                .thenByDescending { it.createdAt }
        )
    }
    
    /**
     * Get available contacts for new scheduled payment
     */
    fun getAvailableContacts(): List<Contact> {
        return mockData.getDomainContacts()
    }
}