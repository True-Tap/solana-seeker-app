package com.truetap.solana.seeker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.truetap.solana.seeker.domain.model.CryptoToken
import com.truetap.solana.seeker.domain.model.SwapQuote
import com.truetap.solana.seeker.domain.model.TransactionResult
import com.truetap.solana.seeker.services.JupiterSwapService
import com.truetap.solana.seeker.services.NftService
import com.truetap.solana.seeker.services.GenesisNFTTier
import dagger.hilt.android.lifecycle.HiltViewModel
import com.truetap.solana.seeker.services.FeePreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/**
 * Enhanced SwapViewModel with Jupiter V6 integration and Genesis NFT benefits
 */
@HiltViewModel
class SwapViewModel @Inject constructor(
    private val jupiterSwapService: JupiterSwapService,
    private val nftService: NftService,
    private val walletRepository: com.truetap.solana.seeker.repositories.WalletRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SwapUiState())
    val uiState: StateFlow<SwapUiState> = _uiState.asStateFlow()
    
    private var quoteJob: Job? = null
    private var priceUpdateJob: Job? = null
    
    init {
        startPriceUpdates()
        startNetworkMonitoring()
    }
    
    /**
     * Update input amount and get new quote
     */
    fun updateInputAmount(amount: String) {
        val cleanAmount = amount.filter { it.isDigit() || it == '.' }
        val decimalCount = cleanAmount.count { it == '.' }
        val validAmount = if (decimalCount <= 1) cleanAmount else _uiState.value.inputAmount
        
        _uiState.value = _uiState.value.copy(
            inputAmount = validAmount,
            errorMessage = null
        )
        
        // Get new quote if amount is valid
        if (validAmount.isNotEmpty() && validAmount.toBigDecimalOrNull() != null) {
            getSwapQuote()
        } else {
            _uiState.value = _uiState.value.copy(
                outputAmount = "0.00",
                currentQuote = null
            )
        }
    }
    
    /**
     * Update output amount (reverse calculation)
     */
    fun updateOutputAmount(amount: String) {
        val cleanAmount = amount.filter { it.isDigit() || it == '.' }
        val decimalCount = cleanAmount.count { it == '.' }
        val validAmount = if (decimalCount <= 1) cleanAmount else _uiState.value.outputAmount
        
        _uiState.value = _uiState.value.copy(
            outputAmount = validAmount,
            errorMessage = null
        )
        
        // For reverse quotes, we'd need to implement reverse quote logic
        // For now, just update the display amount
    }
    
    /**
     * Swap input and output tokens
     */
    fun swapTokens() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            inputToken = currentState.outputToken,
            outputToken = currentState.inputToken,
            inputAmount = currentState.outputAmount,
            outputAmount = currentState.inputAmount,
            currentQuote = null
        )
        
        // Get new quote for swapped tokens
        if (_uiState.value.inputAmount.isNotEmpty() && 
            _uiState.value.inputAmount.toBigDecimalOrNull() != null) {
            getSwapQuote()
        }
    }
    
    /**
     * Select input token
     */
    fun selectInputToken(token: CryptoToken) {
        if (token != _uiState.value.outputToken) {
            _uiState.value = _uiState.value.copy(
                inputToken = token,
                currentQuote = null
            )
            getSwapQuote()
        } else {
            // If selecting same as output token, swap them
            swapTokens()
        }
    }
    
    /**
     * Select output token
     */
    fun selectOutputToken(token: CryptoToken) {
        if (token != _uiState.value.inputToken) {
            _uiState.value = _uiState.value.copy(
                outputToken = token,
                currentQuote = null
            )
            getSwapQuote()
        } else {
            // If selecting same as input token, swap them
            swapTokens()
        }
    }
    
    /**
     * Update slippage tolerance
     */
    fun updateSlippage(slippagePercent: Double) {
        _uiState.value = _uiState.value.copy(slippagePercent = slippagePercent)
        getSwapQuote() // Get new quote with updated slippage
    }
    
    /**
     * Get swap quote from Jupiter
     */
    fun getSwapQuote() {
        val currentState = _uiState.value
        
        if (currentState.inputAmount.isEmpty() || 
            currentState.inputAmount.toBigDecimalOrNull() == null ||
            currentState.inputAmount.toBigDecimal() <= BigDecimal.ZERO) {
            return
        }
        
        // Check if wallet is connected
        if (currentState.walletAddress.isEmpty()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please connect your wallet to get swap quotes."
            )
            return
        }
        
        // Cancel previous quote request
        quoteJob?.cancel()
        
        quoteJob = viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(
                    isLoadingQuote = true,
                    errorMessage = null
                )
                
                // Auto-calculate optimal fees before getting quote
                val (networkFee, platformFeeRate) = jupiterSwapService.calculateOptimalFees(
                    currentState.transactionSpeed
                )
                
                val quote = jupiterSwapService.getBestQuote(
                    inputToken = currentState.inputToken.symbol,
                    outputToken = currentState.outputToken.symbol,
                    amount = currentState.inputAmount.toBigDecimal(),
                    slippageBps = (currentState.slippagePercent * 100).toInt()
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoadingQuote = false,
                    outputAmount = quote.outputAmount.toPlainString(),
                    currentQuote = quote,
                    priceImpact = quote.priceImpact,
                    estimatedFee = quote.exchangeFee,
                    route = quote.route,
                    estimatedTime = quote.estimatedTime
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingQuote = false,
                    errorMessage = "Failed to get quote: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Check if user has sufficient balance for the swap
     */
    fun checkSufficientBalance(): Boolean {
        val currentState = _uiState.value
        val inputAmount = currentState.inputAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        
        if (inputAmount <= BigDecimal.ZERO) return false
        
        return try {
            val balance = walletRepository.getTokenBalance(currentState.inputToken.symbol)
            balance >= inputAmount
        } catch (e: Exception) {
            android.util.Log.w("SwapViewModel", "Failed to check balance", e)
            false
        }
    }
    
    /**
     * Get wallet balance for a specific token
     */
    fun getWalletBalance(tokenSymbol: String): BigDecimal {
        return try {
            walletRepository.getTokenBalance(tokenSymbol)
        } catch (e: Exception) {
            android.util.Log.w("SwapViewModel", "Failed to get balance for $tokenSymbol", e)
            BigDecimal.ZERO
        }
    }
    
    /**
     * Execute the swap with comprehensive error handling and wallet integration
     */
    fun executeSwap(
        activity: android.app.Activity? = null,
        activityResultLauncher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>? = null
    ) {
        val currentState = _uiState.value
        
        // Check wallet connection
        if (currentState.walletAddress.isEmpty()) {
            _uiState.value = currentState.copy(
                errorMessage = "Please connect your wallet to execute swaps."
            )
            return
        }
        
        val quote = currentState.currentQuote ?: run {
            _uiState.value = currentState.copy(
                errorMessage = "No quote available. Please refresh and try again."
            )
            return
        }
        
        // Validate sufficient balance
        val inputAmount = currentState.inputAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        if (inputAmount <= BigDecimal.ZERO) {
            _uiState.value = currentState.copy(
                errorMessage = "Invalid amount. Please enter a valid amount."
            )
            return
        }
        
        // Check wallet balance
        if (!checkSufficientBalance()) {
            _uiState.value = currentState.copy(
                errorMessage = "Insufficient balance. Please check your ${currentState.inputToken.symbol} balance and try again."
            )
            return
        }
        
        viewModelScope.launch {
            try {
                _uiState.value = currentState.copy(
                    swapState = SwapState.PROCESSING,
                    errorMessage = null,
                    showSwapConfirmation = false
                )
                
                // Map fee preset to priority fee (lamports)
                val priorityFee = when (currentState.feePreset) {
                    FeePreset.EXPRESS -> 5000L
                    FeePreset.FAST -> 500L
                    FeePreset.NORMAL -> 0L
                }
                
                val result = if (activity != null && activityResultLauncher != null) {
                    jupiterSwapService.executeSwap(
                        quote = quote,
                        priorityFee = priorityFee,
                        activity = activity,
                        activityResultLauncher = activityResultLauncher
                    )
                } else {
                    // Fallback for testing - should not be used in production
                    TransactionResult.Error("Activity context required for transaction signing")
                }
                
                when (result) {
                    is TransactionResult.Success -> {
                        val successDetails = SwapSuccessDetails(
                            transactionSignature = result.signature,
                            inputAmount = inputAmount,
                            outputAmount = quote.outputAmount,
                            inputToken = quote.inputToken.symbol,
                            outputToken = quote.outputToken.symbol,
                            actualRate = quote.rate,
                            totalFees = quote.networkFee.add(quote.exchangeFee),
                            isGenesisHolder = currentState.isGenesisHolder
                        )
                        
                        _uiState.value = _uiState.value.copy(
                            swapState = SwapState.SUCCESS,
                            transactionSignature = result.signature,
                            swapSuccessDetails = successDetails
                        )
                        
                        // Refresh wallet data to update balances
                        try {
                            walletRepository.refreshWalletData()
                        } catch (e: Exception) {
                            android.util.Log.w("SwapViewModel", "Failed to refresh wallet data after swap", e)
                        }
                        
                        // Auto-reset after showing success for 5 seconds
                        delay(5000)
                        if (_uiState.value.swapState == SwapState.SUCCESS) {
                            resetSwap()
                        }
                    }
                    
                    is TransactionResult.Error -> {
                        val errorMessage = parseSwapError(result.message)
                        _uiState.value = _uiState.value.copy(
                            swapState = SwapState.ERROR,
                            errorMessage = errorMessage
                        )
                        
                        // Auto-clear error state after 5 seconds
                        delay(5000)
                        if (_uiState.value.swapState == SwapState.ERROR) {
                            _uiState.value = _uiState.value.copy(swapState = SwapState.IDLE)
                        }
                    }
                    
                    is TransactionResult.Processing -> {
                        // Transaction submitted but not confirmed yet
                        _uiState.value = _uiState.value.copy(
                            swapState = SwapState.PROCESSING,
                            errorMessage = "Transaction submitted, waiting for confirmation..."
                        )
                    }
                }
                
            } catch (e: Exception) {
                val errorMessage = parseSwapError(e.message ?: "Unknown error occurred")
                _uiState.value = _uiState.value.copy(
                    swapState = SwapState.ERROR,
                    errorMessage = errorMessage
                )
                
                // Auto-clear error state after 5 seconds
                delay(5000)
                if (_uiState.value.swapState == SwapState.ERROR) {
                    _uiState.value = _uiState.value.copy(swapState = SwapState.IDLE)
                }
            }
        }
    }
    
    /**
     * Parse swap errors to provide user-friendly messages
     */
    private fun parseSwapError(error: String): String {
        return when {
            error.contains("insufficient", ignoreCase = true) || 
            error.contains("balance", ignoreCase = true) -> 
                "Insufficient balance. Please check your token balance and try again."
                
            error.contains("slippage", ignoreCase = true) || 
            error.contains("price impact", ignoreCase = true) -> 
                "Price moved too much. Try increasing slippage tolerance or reducing amount."
                
            error.contains("congestion", ignoreCase = true) || 
            error.contains("network", ignoreCase = true) ||
            error.contains("timeout", ignoreCase = true) -> 
                "Network congestion detected. Try increasing transaction speed or wait a moment."
                
            error.contains("rejected", ignoreCase = true) || 
            error.contains("cancelled", ignoreCase = true) -> 
                "Transaction was rejected. Please try again."
                
            error.contains("route", ignoreCase = true) || 
            error.contains("liquidity", ignoreCase = true) -> 
                "No suitable route found. Try reducing the swap amount or try a different token pair."
                
            error.contains("wallet", ignoreCase = true) || 
            error.contains("connection", ignoreCase = true) -> 
                "Wallet connection issue. Please reconnect your wallet and try again."
                
            error.contains("fee", ignoreCase = true) -> 
                "Insufficient SOL for transaction fees. Please ensure you have enough SOL for gas."
                
            else -> "Swap failed: $error"
        }
    }
    
    /**
     * Set transaction speed (affects priority fee)
     */
    fun setTransactionSpeed(speed: String) {
        val mappedPreset = when (speed) {
            "Fast" -> FeePreset.FAST
            "Express" -> FeePreset.EXPRESS
            else -> FeePreset.NORMAL
        }
        _uiState.value = _uiState.value.copy(transactionSpeed = speed, feePreset = mappedPreset)
    }
    
    /**
     * Show/hide swap confirmation
     */
    fun showSwapConfirmation(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSwapConfirmation = show)
    }
    
    /**
     * Reset swap state
     */
    fun resetSwap() {
        _uiState.value = _uiState.value.copy(
            swapState = SwapState.IDLE,
            inputAmount = "",
            outputAmount = "0.00",
            currentQuote = null,
            transactionSignature = null,
            errorMessage = null,
            showSwapConfirmation = false,
            swapSuccessDetails = null
        )
    }
    
    /**
     * Set error message
     */
    fun setError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Toggle details expansion
     */
    fun toggleDetails() {
        _uiState.value = _uiState.value.copy(
            isDetailsExpanded = !_uiState.value.isDetailsExpanded
        )
    }
    
    /**
     * Update wallet address and check Genesis NFT status
     */
    fun updateWalletAddress(walletAddress: String) {
        _uiState.value = _uiState.value.copy(walletAddress = walletAddress)
        checkGenesisNFTStatus(walletAddress)
    }
    
    /**
     * Update Genesis NFT status directly from wallet state
     */
    fun updateGenesisNFTStatus(hasGenesis: Boolean, tier: String) {
        val genesisNFTTier = when (tier) {
            "LEGENDARY" -> GenesisNFTTier.LEGENDARY
            "RARE" -> GenesisNFTTier.RARE
            "COMMON" -> GenesisNFTTier.COMMON
            else -> GenesisNFTTier.NONE
        }
        
        _uiState.value = _uiState.value.copy(
            isGenesisHolder = hasGenesis,
            genesisNFTTier = genesisNFTTier
        )
        
        // Update the Jupiter service with Genesis status
        jupiterSwapService.updateGenesisStatus(hasGenesis)
    }
    
    /**
     * Check Genesis NFT status for fee benefits
     */
    private fun checkGenesisNFTStatus(walletAddress: String) {
        viewModelScope.launch {
            try {
                val hasGenesis = nftService.hasGenesisNFT(walletAddress)
                val tier = nftService.getGenesisNFTTier(walletAddress)
                
                _uiState.value = _uiState.value.copy(
                    isGenesisHolder = hasGenesis,
                    genesisNFTTier = tier
                )
                
            } catch (e: Exception) {
                // Silently fail Genesis check - user still gets standard rates
                android.util.Log.w("SwapViewModel", "Failed to check Genesis NFT status", e)
            }
        }
    }
    
    /**
     * Start periodic price updates
     */
    private fun startPriceUpdates() {
        priceUpdateJob = viewModelScope.launch {
            while (true) {
                try {
                    // Update token prices every 30 seconds
                    val solPrice = jupiterSwapService.getTokenPrice("SOL")
                    val usdcPrice = jupiterSwapService.getTokenPrice("USDC")
                    
                    _uiState.value = _uiState.value.copy(
                        tokenPrices = _uiState.value.tokenPrices.toMutableMap().apply {
                            put("SOL", solPrice)
                            put("USDC", usdcPrice)
                        }
                    )
                    
                    delay(30000) // 30 seconds
                } catch (e: Exception) {
                    android.util.Log.w("SwapViewModel", "Failed to update prices", e)
                    delay(60000) // Retry in 1 minute on error
                }
            }
        }
    }
    
    /**
     * Start network congestion monitoring
     */
    private fun startNetworkMonitoring() {
        viewModelScope.launch {
            while (true) {
                try {
                    val congestionStatus = jupiterSwapService.getNetworkCongestionStatus()
                    val (recommendedSpeed, message) = jupiterSwapService.getRecommendedSpeed()
                    
                    val congestionMessage = when (congestionStatus) {
                        com.truetap.solana.seeker.services.NetworkCongestionStatus.HIGH ->
                            "⚠️ Network congestion detected. Transactions may take longer or fail. Consider using Fast speed."
                        com.truetap.solana.seeker.services.NetworkCongestionStatus.MEDIUM ->
                            "🟡 Moderate network activity. Normal speed recommended."
                        com.truetap.solana.seeker.services.NetworkCongestionStatus.LOW ->
                            "🟢 Network is running smoothly. Eco speed available for lower fees."
                        else -> null
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        networkCongestionStatus = congestionStatus,
                        networkCongestionMessage = congestionMessage,
                        recommendedSpeed = recommendedSpeed
                    )
                    
                    delay(45000) // Check every 45 seconds
                } catch (e: Exception) {
                    android.util.Log.w("SwapViewModel", "Failed to check network status", e)
                    delay(120000) // Retry in 2 minutes on error
                }
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        quoteJob?.cancel()
        priceUpdateJob?.cancel()
    }
}

/**
 * UI State for Swap Screen
 */
data class SwapUiState(
    val inputToken: CryptoToken = CryptoToken.SOL,
    val outputToken: CryptoToken = CryptoToken.USDC,
    val inputAmount: String = "",
    val outputAmount: String = "0.00",
    val isLoadingQuote: Boolean = false,
    val currentQuote: SwapQuote? = null,
    val slippagePercent: Double = 0.5,
    val priceImpact: Double = 0.0,
    val estimatedFee: BigDecimal = BigDecimal.ZERO,
    val route: List<String> = emptyList(),
    val estimatedTime: Long = 30,
    val swapState: SwapState = SwapState.IDLE,
    val transactionSpeed: String = "Normal",
    val feePreset: FeePreset = FeePreset.NORMAL,
    val transactionSignature: String? = null,
    val errorMessage: String? = null,
    val showSwapConfirmation: Boolean = false,
    val isDetailsExpanded: Boolean = false,
    val isGenesisHolder: Boolean = false,
    val genesisNFTTier: GenesisNFTTier = GenesisNFTTier.NONE,
    val tokenPrices: Map<String, BigDecimal> = emptyMap(),
    val walletAddress: String = "",
    val networkCongestionStatus: com.truetap.solana.seeker.services.NetworkCongestionStatus = com.truetap.solana.seeker.services.NetworkCongestionStatus.UNKNOWN,
    val networkCongestionMessage: String? = null,
    val recommendedSpeed: String = "Normal",
    val swapSuccessDetails: SwapSuccessDetails? = null
)

/**
 * Swap execution states
 */
enum class SwapState {
    IDLE,
    PROCESSING,
    SUCCESS,
    ERROR
}

/**
 * Details for successful swap
 */
data class SwapSuccessDetails(
    val transactionSignature: String,
    val inputAmount: BigDecimal,
    val outputAmount: BigDecimal,
    val inputToken: String,
    val outputToken: String,
    val actualRate: Double,
    val totalFees: BigDecimal,
    val isGenesisHolder: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)