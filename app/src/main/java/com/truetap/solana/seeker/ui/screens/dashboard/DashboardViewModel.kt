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
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val greeting: String = "",
    val subtitle: String = ""
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    // Sample transactions for demo
    private val sampleTransactions = listOf(
        Transaction(
            id = "1",
            type = TransactionType.RECEIVED,
            amount = "+25.5 SOL",
            recipient = "From Les Grossman",
            time = "2 min ago"
        ),
        Transaction(
            id = "2",
            type = TransactionType.SENT,
            amount = "-12 SOL",
            recipient = "To Sarah Kim",
            time = "1 hour ago"
        ),
        Transaction(
            id = "3",
            type = TransactionType.RECEIVED,
            amount = "+8.75 SOL",
            recipient = "From Mike Johnson",
            time = "3 hours ago"
        )
    )
    
    init {
        updateGreeting()
        loadSampleData()
    }
    
    fun startAnimations() {
        _uiState.update { it.copy(animateTappy = true) }
    }
    
    fun loadWalletData() {
        viewModelScope.launch {
            try {
                // TODO: Replace with actual wallet service integration
                // val walletService = WalletFactory.getInstance() as SolanaWalletService
                // val isConnected = walletService.isWalletConnected()
                
                // Simulate wallet data loading
                delay(500)
                
                _uiState.update { currentState ->
                    currentState.copy(
                        isWalletConnected = true,
                        walletBalance = 125.4567,
                        walletPublicKey = "9WzDXwBbkk8QkbwvQp2cK3xYzYtAWWM",
                        tokenBalances = listOf(
                            TokenBalance("USDC", 1000.0, 1000.0),
                            TokenBalance("BONK", 50000.0, 50000.0),
                            TokenBalance("ORCA", 25.5, 25.5)
                        ),
                        transactions = sampleTransactions
                    )
                }
                
                updateSubtitle()
                
            } catch (error: Exception) {
                // Handle error
                println("Failed to load wallet data: ${error.message}")
            }
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
            println("Copied to clipboard: $text")
        } catch (error: Exception) {
            println("Failed to copy to clipboard: ${error.message}")
        }
    }
    
    fun openExplorer(hash: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://explorer.solana.com/tx/$hash"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (error: Exception) {
            println("Failed to open explorer: ${error.message}")
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
    
    private fun loadSampleData() {
        _uiState.update { 
            it.copy(
                transactions = sampleTransactions,
                subtitle = getCurrentDate()
            )
        }
    }
}