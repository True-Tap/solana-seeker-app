package com.truetap.solana.seeker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.data.WalletTransaction
import com.truetap.solana.seeker.data.TransactionType as WalletTransactionType
import com.truetap.solana.seeker.data.models.TransactionType
import com.truetap.solana.seeker.data.models.Transaction as DataTransaction
import com.truetap.solana.seeker.data.models.TransactionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

// Display-specific Transaction model for UI
data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: String,
    val otherParty: String,
    val token: String,
    val timeAgo: String,
    val status: TransactionStatus? = null
)

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val walletRepository: com.truetap.solana.seeker.repositories.WalletRepository,
    private val mockData: com.truetap.solana.seeker.data.MockData
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    // Sorting and filtering state (keeping for backward compatibility)
    val sortOption: StateFlow<SortOption> = _uiState.map { it.sortOption }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), SortOption.DATE_NEWEST)

    val transactionTypeFilter: StateFlow<TransactionTypeFilter> = _uiState.map { it.transactionTypeFilter }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), TransactionTypeFilter.ALL)

    val timePeriodFilter: StateFlow<TimePeriodFilter> = _uiState.map { it.timePeriodFilter }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), TimePeriodFilter.ALL_TIME)

    val isFilterExpanded: StateFlow<Boolean> = _uiState.map { it.isFilterExpanded }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), false)

    val isSortExpanded: StateFlow<Boolean> = _uiState.map { it.isSortExpanded }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), false)

    // Combined filtered and sorted transactions using MockData
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        // Use reactive transactionsFlow for automatic updates when data changes
        mockData.transactionsFlow,
        _uiState
    ) { transactions, uiState ->
        // Use reactive transaction data directly
        val rawTransactions = transactions.map { dataTransaction ->
            dataTransaction.toDisplayTransaction()
        }
        
        val filteredTransactions = rawTransactions
            .filter { transaction -> matchesTypeFilter(transaction, uiState.transactionTypeFilter) }
            .filter { transaction -> matchesTimePeriodFilter(transaction, uiState.timePeriodFilter) }
            .filter { transaction -> matchesSearchQuery(transaction, uiState.searchQuery) }
        
        applySorting(filteredTransactions, uiState.sortOption)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Filter state helpers
    val hasActiveFilters: StateFlow<Boolean> = _uiState.map { uiState ->
        uiState.transactionTypeFilter != TransactionTypeFilter.ALL || 
        uiState.timePeriodFilter != TimePeriodFilter.ALL_TIME
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    // Transaction statistics based on filtered results
    val transactionStats: StateFlow<TransactionStats> = filteredTransactions.map { transactions ->
        val totalReceived = transactions
            .filter { it.type == TransactionType.RECEIVED }
            .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        
        val totalSent = transactions
            .filter { it.type == TransactionType.SENT }
            .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        
        val totalSwapped = transactions
            .filter { it.type == TransactionType.SWAPPED }
            .sumOf { it.amount.toDoubleOrNull() ?: 0.0 }
        
        TransactionStats(
            totalReceived = totalReceived,
            totalSent = totalSent,
            totalSwapped = totalSwapped,
            totalTransactions = transactions.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionStats()
    )

    // Actions
    fun setSortOption(option: SortOption) {
        _uiState.update { it.copy(
            sortOption = option,
            isSortExpanded = false
        )}
    }

    fun setTransactionTypeFilter(filter: TransactionTypeFilter) {
        _uiState.update { it.copy(
            transactionTypeFilter = filter,
            isFilterExpanded = false
        )}
    }

    fun setTimePeriodFilter(filter: TimePeriodFilter) {
        _uiState.update { it.copy(
            timePeriodFilter = filter,
            isFilterExpanded = false
        )}
    }

    fun toggleSortExpanded() {
        _uiState.update { it.copy(
            isSortExpanded = !it.isSortExpanded,
            isFilterExpanded = if (!it.isSortExpanded) false else it.isFilterExpanded
        )}
    }

    fun toggleFilterExpanded() {
        _uiState.update { it.copy(
            isFilterExpanded = !it.isFilterExpanded,
            isSortExpanded = if (!it.isFilterExpanded) false else it.isSortExpanded
        )}
    }

    fun clearAllFilters() {
        _uiState.update { it.copy(
            transactionTypeFilter = TransactionTypeFilter.ALL,
            timePeriodFilter = TimePeriodFilter.ALL_TIME,
            sortOption = SortOption.DATE_NEWEST,
            isFilterExpanded = false,
            isSortExpanded = false
        )}
    }

    fun collapseAll() {
        _uiState.update { it.copy(
            isFilterExpanded = false,
            isSortExpanded = false
        )}
    }

    // Additional helper methods
    fun refreshTransactions() {
        viewModelScope.launch {
            walletRepository.refreshWalletData()
        }
    }

    fun searchTransactions(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    // Helper functions for MockData  
    private fun DataTransaction.toDisplayTransaction(): Transaction {
        // Convert transaction type from MockData format
        val transactionType = when (this.type) {
            com.truetap.solana.seeker.data.models.TransactionType.SENT -> TransactionType.SENT
            com.truetap.solana.seeker.data.models.TransactionType.RECEIVED -> TransactionType.RECEIVED
            com.truetap.solana.seeker.data.models.TransactionType.SWAPPED -> TransactionType.SWAPPED
        }
        
        // Use the otherPartyName from MockData, with fallback to address
        val otherParty = this.otherPartyName ?: run {
            when (transactionType) {
                TransactionType.SENT -> this.otherPartyAddress?.take(8) ?: "Recipient"
                TransactionType.RECEIVED -> this.otherPartyAddress?.take(8) ?: "Sender"
                TransactionType.SWAPPED -> "Exchange"
            }
        }
        
        return Transaction(
            id = this.id,
            type = transactionType,
            amount = String.format("%.2f", this.amount),
            otherParty = otherParty,
            token = this.currency,
            timeAgo = formatTimeAgo(this.timestamp),
            status = this.status
        )
    }
    
    // Legacy helper function for WalletRepository transactions  
    private fun WalletTransaction.toDisplayTransaction(): Transaction {
        val walletState = walletRepository.walletState.value
        val currentWalletAddress = walletState.account?.publicKey
        
        // Improved transaction type detection based on wallet address comparison
        val transactionType = when {
            this.type == WalletTransactionType.SWAP -> TransactionType.SWAPPED
            this.toAddress == currentWalletAddress -> TransactionType.RECEIVED
            this.fromAddress == currentWalletAddress -> TransactionType.SENT
            this.type == WalletTransactionType.TRANSFER -> TransactionType.SENT
            this.type == WalletTransactionType.TOKEN_TRANSFER -> TransactionType.SENT
            this.type == WalletTransactionType.NFT_TRANSFER -> TransactionType.SENT
            else -> TransactionType.RECEIVED // Default fallback
        }
        
        // Determine other party address
        val otherPartyAddress = when (transactionType) {
            TransactionType.SENT -> this.toAddress
            TransactionType.RECEIVED -> this.fromAddress
            TransactionType.SWAPPED -> this.toAddress
        }
        
        // Determine token symbol
        val tokenSymbol = when {
            this.tokenMint != null -> {
                // Try to get token symbol from wallet state token balances
                walletState.balance?.tokenBalances?.find { it.mint == this.tokenMint }?.symbol ?: "TOKEN"
            }
            else -> "SOL"
        }
        
        return Transaction(
            id = this.signature,
            type = transactionType,
            amount = this.amount?.abs()?.toPlainString() ?: "0.00",
            otherParty = otherPartyAddress?.take(8) ?: "Unknown",
            token = tokenSymbol,
            timeAgo = formatTimeAgo(this.blockTime * 1000), // Convert to milliseconds
            status = TransactionStatus.COMPLETED // Legacy transactions are always completed
        )
    }

    private fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d ago"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
        }
    }

    private fun matchesTypeFilter(transaction: Transaction, filter: TransactionTypeFilter): Boolean {
        return when (filter) {
            TransactionTypeFilter.ALL -> true
            TransactionTypeFilter.SENT -> transaction.type == TransactionType.SENT
            TransactionTypeFilter.RECEIVED -> transaction.type == TransactionType.RECEIVED
            TransactionTypeFilter.SWAPPED -> transaction.type == TransactionType.SWAPPED
        }
    }

    private fun matchesTimePeriodFilter(transaction: Transaction, filter: TimePeriodFilter): Boolean {
        val now = System.currentTimeMillis()
        val transactionTime = parseTimeAgo(transaction.timeAgo)
        val dayInMs = 24 * 60 * 60 * 1000L
        
        return when (filter) {
            TimePeriodFilter.ALL_TIME -> true
            TimePeriodFilter.LAST_7_DAYS -> (now - transactionTime) <= (7 * dayInMs)
            TimePeriodFilter.LAST_30_DAYS -> (now - transactionTime) <= (30 * dayInMs)
            TimePeriodFilter.LAST_90_DAYS -> (now - transactionTime) <= (90 * dayInMs)
        }
    }

    private fun matchesSearchQuery(transaction: Transaction, query: String): Boolean {
        if (query.isBlank()) return true
        
        val searchQuery = query.lowercase()
        return transaction.otherParty.lowercase().contains(searchQuery) ||
               transaction.amount.contains(searchQuery) ||
               transaction.token.lowercase().contains(searchQuery) ||
               transaction.id.lowercase().contains(searchQuery) ||
               transaction.type.name.lowercase().contains(searchQuery)
    }

    private fun parseTimeAgo(timeAgo: String): Long {
        val now = System.currentTimeMillis()
        
        return when {
            timeAgo == "Just now" -> now
            timeAgo.endsWith("m ago") -> {
                val minutes = timeAgo.dropLast(5).toLongOrNull() ?: 0
                now - (minutes * 60 * 1000)
            }
            timeAgo.endsWith("h ago") -> {
                val hours = timeAgo.dropLast(5).toLongOrNull() ?: 0
                now - (hours * 60 * 60 * 1000)
            }
            timeAgo.endsWith("d ago") -> {
                val days = timeAgo.dropLast(5).toLongOrNull() ?: 0
                now - (days * 24 * 60 * 60 * 1000)
            }
            else -> {
                // Try to parse "MMM d" format
                try {
                    val format = SimpleDateFormat("MMM d", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val currentYear = calendar.get(Calendar.YEAR)
                    val date = format.parse(timeAgo)
                    if (date != null) {
                        calendar.time = date
                        calendar.set(Calendar.YEAR, currentYear)
                        calendar.timeInMillis
                    } else now
                } catch (e: Exception) {
                    now
                }
            }
        }
    }

    private fun applySorting(transactions: List<Transaction>, sortOption: SortOption): List<Transaction> {
        return when (sortOption) {
            SortOption.DATE_NEWEST -> transactions.sortedByDescending { parseTimeAgo(it.timeAgo) }
            SortOption.DATE_OLDEST -> transactions.sortedBy { parseTimeAgo(it.timeAgo) }
            SortOption.AMOUNT_HIGHEST -> transactions.sortedByDescending { it.amount.toDoubleOrNull() ?: 0.0 }
            SortOption.AMOUNT_LOWEST -> transactions.sortedBy { it.amount.toDoubleOrNull() ?: 0.0 }
        }
    }
}

// Data classes for sorting and filtering
enum class SortOption(val displayName: String) {
    DATE_NEWEST("Newest First"),
    DATE_OLDEST("Oldest First"),
    AMOUNT_HIGHEST("Highest Amount"),
    AMOUNT_LOWEST("Lowest Amount")
}

enum class TransactionTypeFilter(val displayName: String) {
    ALL("All"),
    SENT("Sent"),
    RECEIVED("Received"),
    SWAPPED("Swapped")
}

enum class TimePeriodFilter(val displayName: String) {
    ALL_TIME("All Time"),
    LAST_7_DAYS("Last 7 Days"),
    LAST_30_DAYS("Last 30 Days"),
    LAST_90_DAYS("Last 90 Days")
}

data class TransactionStats(
    val totalReceived: Double = 0.0,
    val totalSent: Double = 0.0,
    val totalSwapped: Double = 0.0,
    val totalTransactions: Int = 0
)

/**
 * Comprehensive UI state for transaction history screen
 */
data class TransactionHistoryUiState(
    val sortOption: SortOption = SortOption.DATE_NEWEST,
    val transactionTypeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val timePeriodFilter: TimePeriodFilter = TimePeriodFilter.ALL_TIME,
    val isFilterExpanded: Boolean = false,
    val isSortExpanded: Boolean = false,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)