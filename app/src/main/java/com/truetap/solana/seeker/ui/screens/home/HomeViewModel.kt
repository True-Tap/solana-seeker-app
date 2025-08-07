package com.truetap.solana.seeker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.data.MockData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mockData: MockData
) : ViewModel() {
    
    private val _recentTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val recentTransactions: StateFlow<List<Transaction>> = _recentTransactions.asStateFlow()
    
    init {
        loadRecentTransactions()
    }
    
    private fun loadRecentTransactions() {
        viewModelScope.launch {
            // Get recent transactions from MockData and convert to HomeScreen's Transaction format
            val transactions = mockData.getAllTransactions()
                .take(3) // Show only 3 most recent in home screen
                .map { dataTransaction ->
                    Transaction(
                        id = dataTransaction.id,
                        type = when (dataTransaction.type) {
                            com.truetap.solana.seeker.data.models.TransactionType.SENT -> TransactionType.SENT
                            com.truetap.solana.seeker.data.models.TransactionType.RECEIVED -> TransactionType.RECEIVED
                            else -> TransactionType.SENT
                        },
                        amount = String.format("%.2f", dataTransaction.amount),
                        otherParty = dataTransaction.otherPartyName ?: "Unknown",
                        timeAgo = formatTimeAgo(dataTransaction.timestamp),
                        token = dataTransaction.currency
                    )
                }
            _recentTransactions.value = transactions
        }
    }
    
    fun refreshTransactions() {
        loadRecentTransactions()
    }
    
    private fun formatTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            }
        }
    }
}