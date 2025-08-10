/**
 * Dashboard ViewModel - TrueTap
 * Manages state and business logic for the Dashboard screen
 */

package com.truetap.solana.seeker.ui.screens.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.truetap.solana.seeker.BuildConfig
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import com.truetap.solana.seeker.repositories.TransactionOutboxRepository
import com.truetap.solana.seeker.repositories.PendingTransaction
import com.truetap.solana.seeker.repositories.SocialRepository
import com.truetap.solana.seeker.repositories.SocialComment
import com.truetap.solana.seeker.repositories.SocialNote
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val isBalanceVisible: Boolean = false,
    val isRefreshing: Boolean = false,
    val isBalancePressed: Boolean = false,
    val animateTappy: Boolean = false,
    val walletBalance: Double = 0.0,
    val isWalletConnected: Boolean = false,
    val walletPublicKey: String = "",
    val tokenBalances: List<TokenBalance> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val selectedTransaction: Transaction? = null,
    val pendingOutbox: List<PendingTransaction> = emptyList(),
    val greeting: String = "",
    val subtitle: String = "",
    // Social aggregates
    val likesByTx: Map<String, Int> = emptyMap(),
    val commentsByTx: Map<String, List<SocialComment>> = emptyMap(),
    val notesByTx: Map<String, SocialNote> = emptyMap(),
    val defaultPrivate: Boolean = true,
    val weeklySpendSol: Double = 0.0,
    val categoryTotals: Map<String, Double> = emptyMap()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val outboxRepository: TransactionOutboxRepository,
    private val socialRepository: SocialRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    // No sample transactions - will load real data when wallet is connected
    
    init {
        updateGreeting()
        // Observe outbox live
        viewModelScope.launch {
            outboxRepository.flow.collect { pending ->
                _uiState.update { it.copy(pendingOutbox = pending) }
            }
        }

        // Observe social flows and aggregate by txId for quick UI
        viewModelScope.launch {
            socialRepository.likesFlow.collect { likes ->
                val aggregated = likes.groupBy { it.txId }.mapValues { it.value.size }
                _uiState.update { it.copy(likesByTx = aggregated) }
            }
        }
        viewModelScope.launch {
            socialRepository.commentsFlow.collect { comments ->
                val aggregated = comments.groupBy { it.txId }
                _uiState.update { it.copy(commentsByTx = aggregated) }
            }
        }
        viewModelScope.launch {
            socialRepository.notesFlow.collect { notes ->
                // latest note per tx
                val aggregated = notes.groupBy { it.txId }.mapValues { it.value.lastOrNull() }.filterValues { it != null } as Map<String, SocialNote>
                _uiState.update { it.copy(notesByTx = aggregated) }
            }
        }
    }
    
    fun startAnimations() {
        _uiState.update { it.copy(animateTappy = true) }
    }

    // Social actions
    fun likeTransaction(txId: String, user: String = "me") {
        socialRepository.like(txId, user)
    }

    fun addComment(txId: String, user: String = "me", text: String) {
        if (text.isNotBlank()) socialRepository.comment(txId, user, text)
    }

    fun addNote(txId: String, message: String, private: Boolean) {
        if (message.isNotBlank()) socialRepository.addNote(txId, message, private)
    }

    fun computeRewardSuggestion(amountText: String): String? {
        // Simple UX hook: suggest 0.25% SOL back for sends > 0.1 SOL
        return try {
            val amt = amountText.replace("+", "").replace("-", "").split(" ").firstOrNull()?.toDouble() ?: return null
            if (amt > 0.1) {
                val reward = (amt * 0.0025)
                "Earn ${"%.4f".format(reward)} SOL back on this send"
            } else null
        } catch (_: Exception) { null }
    }
    
    fun loadWalletData() {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual wallet service integration
                // val walletService = WalletFactory.getInstance() as SolanaWalletService
                // val isConnected = walletService.isWalletConnected()
                
                // TODO: Implement real wallet data loading
                // For now, just set connected state to false until real wallet integration
                _uiState.update { currentState ->
                    currentState.copy(
                        isWalletConnected = false,
                        walletBalance = 0.0,
                        walletPublicKey = "",
                        tokenBalances = emptyList(),
                        transactions = emptyList(),
                        weeklySpendSol = 0.0,
                        categoryTotals = emptyMap()
                    )
                }
                
                updateSubtitle()
                
            } catch (error: Exception) {
                // Handle error
                // Failed to load wallet data
            }
        }
    }

    // Naive spending aggregation for dashboard summaries
    fun computeSpendingSummaries(transactions: List<Transaction>) {
        val weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        val recentSends = transactions.filter { (it.timestamp ?: 0) >= weekAgo && it.type == TransactionType.SENT }
        val weekly = recentSends.sumOf { parseAmount(it.amount) }
        val cats = recentSends.groupBy { inferCategory(it) }.mapValues { (_, list) -> list.sumOf { parseAmount(it.amount) } }
        _uiState.update { it.copy(weeklySpendSol = weekly, categoryTotals = cats) }
    }

    private fun parseAmount(text: String): Double = try {
        val head = text.replace("+", "").replace("-", "").trim().split(" ").firstOrNull() ?: "0"
        head.toDouble()
    } catch (_: Exception) { 0.0 }

    private fun inferCategory(tx: Transaction): String {
        val note = tx.fee ?: tx.otherParty ?: ""
        return when {
            note.contains("food", true) || note.contains("coffee", true) -> "Food"
            note.contains("ride", true) || note.contains("uber", true) -> "Transport"
            note.contains("rent", true) || note.contains("bill", true) -> "Housing"
            else -> "Other"
        }
    }
    
    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            
            // Simulate refresh delay
            delay(1000)
            
            loadWalletData()
            
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
    
    fun toggleBalanceVisibility() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBalancePressed = true) }
            delay(100)
            _uiState.update { 
                it.copy(
                    isBalancePressed = false,
                    isBalanceVisible = !it.isBalanceVisible
                )
            }
            
            // Refresh wallet data when revealing balance
            if (!_uiState.value.isBalanceVisible) {
                loadWalletData()
            }
        }
    }
    
    fun selectTransaction(transaction: Transaction) {
        _uiState.update { it.copy(selectedTransaction = transaction) }
    }
    
    fun clearSelectedTransaction() {
        _uiState.update { it.copy(selectedTransaction = null) }
    }
    
    fun copyToClipboard(text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Transaction Hash", text)
            clipboard.setPrimaryClip(clip)
            
            // TODO: Show toast or snackbar confirmation
            // Copied to clipboard successfully
        } catch (error: Exception) {
            // Failed to copy to clipboard
        }
    }
    
    fun openExplorer(hash: String) {
        try {
            val clusterParam = if (BuildConfig.DEBUG) "?cluster=devnet" else ""
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://explorer.solana.com/tx/$hash$clusterParam"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (error: Exception) {
            // Failed to open blockchain explorer
        }
    }
    
    private fun updateGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when {
            hour < 12 -> "Good morning! Ready to tap!"
            hour < 18 -> "Good afternoon! Ready to tap!"
            else -> "Good evening! Ready to tap!"
        }
        
        _uiState.update { it.copy(greeting = greeting) }
    }
    
    private fun updateSubtitle() {
        val currentState = _uiState.value
        val subtitle = if (currentState.isWalletConnected && currentState.walletPublicKey.isNotEmpty()) {
            "${currentState.walletPublicKey.take(6)}...${currentState.walletPublicKey.takeLast(4)}"
        } else {
            getCurrentDate()
        }
        
        _uiState.update { it.copy(subtitle = subtitle) }
    }
    
    private fun getCurrentDate(): String {
        val date = Date()
        val formatter = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        return formatter.format(date)
    }
    
}