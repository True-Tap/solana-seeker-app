package com.truetap.solana.seeker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.data.MockData
import com.truetap.solana.seeker.repositories.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mockData: MockData,
    private val walletRepository: WalletRepository
) : ViewModel() {
    
    // Reactive recent transactions that automatically update when MockData changes
    val recentTransactions: StateFlow<List<Transaction>> = walletRepository.walletState
        .map { state ->
            val currentWallet = state.account?.publicKey
            val live = state.transactions.take(3).map { tx ->
                Transaction(
                    id = tx.signature,
                    type = when {
                        tx.toAddress == currentWallet -> TransactionType.RECEIVED
                        else -> TransactionType.SENT
                    },
                    amount = tx.amount?.toPlainString() ?: "0.00",
                    otherParty = (if (tx.toAddress == currentWallet) tx.fromAddress else tx.toAddress)?.take(8) ?: "Unknown",
                    timeAgo = formatTimeAgo(tx.blockTime * 1000),
                    token = if (tx.tokenMint != null) (state.balance?.tokenBalances?.find { it.mint == tx.tokenMint }?.symbol ?: "TOKEN") else "SOL"
                )
            }
            if (live.isNotEmpty()) live else mockData.transactionsFlow.value
                .take(3)
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
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )
    
    fun refreshTransactions() {
        // No longer needed - transactions update automatically via reactive flow
        // Kept for compatibility if needed elsewhere
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