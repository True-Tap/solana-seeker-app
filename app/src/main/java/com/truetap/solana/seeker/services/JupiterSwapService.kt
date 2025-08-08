package com.truetap.solana.seeker.services

import com.truetap.solana.seeker.domain.model.CryptoToken
import com.truetap.solana.seeker.domain.model.SwapQuote
import com.truetap.solana.seeker.domain.model.TransactionResult
import com.truetap.solana.seeker.seedvault.SeedVaultProvider
import com.truetap.solana.seeker.seedvault.SigningResult
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import androidx.activity.ComponentActivity
import com.solana.mobilewalletadapter.clientlib.TransactionResult as MwaTransactionResult
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient
import org.bitcoinj.core.Base58
import javax.inject.Singleton

/**
 * Jupiter V6 Swap API Integration for TrueTap
 * Provides best-price swaps across all Solana DEXs with tiered fee structure
 */
@Singleton
class JupiterSwapService @Inject constructor(
    private val seedVaultProvider: SeedVaultProvider,
    private val nftService: NftService,
    private val solanaRpcService: SolanaRpcService,
    private val walletRepository: com.truetap.solana.seeker.repositories.WalletRepository
) {
    companion object {
        private const val JUPITER_API_BASE = "https://quote-api.jup.ag/v6"
        private const val JUPITER_SWAP_API = "https://quote-api.jup.ag/v6/swap"
        
        // TrueTap fee structure
        private const val STANDARD_FEE_BPS = 50  // 0.5%
        private const val GENESIS_FEE_BPS = 25   // 0.25%
        
        // Token mint addresses on Solana
        private val TOKEN_MINTS = mapOf(
            "SOL" to "So11111111111111111111111111111111111111112",
            "USDC" to "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v",
            "USDT" to "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB",
            "RAY" to "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R",
            "SRM" to "SRMuApVNdxXokk5GT7XD5cUUgXMBCoAz2LHeuAoKWRt",
            "ORCA" to "orcaEKTdK7LKz57vaAYr9QeNsVEPfiu6QeMU1kektZE",
            "BONK" to "DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263",
            "JUP" to "JUPyiwrYJFskUPiHa7hkeR8VUtAeFoSYbKedZNsDvCN"
        )
        
        private val TOKEN_DECIMALS = mapOf(
            "SOL" to 9,
            "USDC" to 6,
            "USDT" to 6,
            "RAY" to 6,
            "SRM" to 6,
            "ORCA" to 6,
            "BONK" to 5,
            "JUP" to 6
        )
    }
    
    /**
     * Get the best swap quote with TrueTap fee structure
     */
    suspend fun getBestQuote(
        inputToken: String,
        outputToken: String,
        amount: BigDecimal,
        slippageBps: Int = 50 // 0.5% default slippage
    ): SwapQuote = withContext(Dispatchers.IO) {
        try {
            // Get real wallet address from repository
            val userWallet = walletRepository.getCurrentWalletAddress()
                ?: throw IllegalStateException("No wallet connected")
            
            val isGenesisHolder = nftService.hasGenesisNFT(userWallet)
            updateGenesisStatus(isGenesisHolder)
            
            // Auto-calculate optimal fees
            val (networkFee, platformFeeMultiplier) = calculateOptimalFees("Normal")
            val platformFeeBps = if (isGenesisHolder) GENESIS_FEE_BPS else STANDARD_FEE_BPS
            
            val inputMint = TOKEN_MINTS[inputToken] ?: throw IllegalArgumentException("Unsupported input token: $inputToken")
            val outputMint = TOKEN_MINTS[outputToken] ?: throw IllegalArgumentException("Unsupported output token: $outputToken")
            val inputDecimals = TOKEN_DECIMALS[inputToken] ?: 9
            
            // Convert amount to token's smallest unit (lamports for SOL)
            val amountInSmallestUnit = amount.movePointRight(inputDecimals).toLong()
            
            // Build Jupiter quote request
            val queryParams = buildString {
                append("inputMint=${URLEncoder.encode(inputMint, "UTF-8")}")
                append("&outputMint=${URLEncoder.encode(outputMint, "UTF-8")}")
                append("&amount=$amountInSmallestUnit")
                append("&slippageBps=$slippageBps")
                append("&platformFeeBps=$platformFeeBps")
                // Use TrueTap fee account - you'll need to create this
                append("&feeAccount=${URLEncoder.encode("TrueTapFeeAccount123...", "UTF-8")}")
            }
            
            val url = URL("$JUPITER_API_BASE/quote?$queryParams")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val jsonResponse = JSONObject(response)
                
                parseJupiterQuote(jsonResponse, inputToken, outputToken, amount, isGenesisHolder, platformFeeBps)
            } else {
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.let { 
                    BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() }
                } ?: "Unknown error"
                throw Exception("Jupiter API error: $responseCode - $errorResponse")
            }
        } catch (e: Exception) {
            android.util.Log.e("JupiterSwapService", "Error getting quote", e)
            throw Exception("Failed to get swap quote: ${e.message}")
        }
    }
    
    /**
     * Execute the swap transaction
     */
    suspend fun executeSwap(
        quote: SwapQuote,
        priorityFee: Long = 0, // Optional priority fee in lamports
        activity: android.app.Activity,
        activityResultLauncher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>
    ): TransactionResult = withContext(Dispatchers.IO) {
        try {
            // Get real wallet address from repository
            val userWallet = walletRepository.getCurrentWalletAddress()
                ?: throw IllegalStateException("No wallet connected")
            val walletType = walletRepository.walletState.value.account?.walletType
            
            // Request swap transaction from Jupiter
            val swapRequest = JSONObject().apply {
                put("quoteResponse", quote.jupiterQuoteResponse)
                put("userPublicKey", userWallet)
                put("wrapAndUnwrapSol", true)
                if (priorityFee > 0) {
                    put("prioritizationFeeLamports", priorityFee)
                }
            }
            
            val url = URL(JUPITER_SWAP_API)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            
            connection.outputStream.use { os ->
                os.write(swapRequest.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val jsonResponse = JSONObject(response)
                
                val swapTransaction = jsonResponse.getString("swapTransaction")
                val transactionBytes = android.util.Base64.decode(swapTransaction, android.util.Base64.DEFAULT)

                // Choose signing route based on wallet type
                return@withContext when (walletType) {
                    com.truetap.solana.seeker.data.WalletType.SOLFLARE,
                    com.truetap.solana.seeker.data.WalletType.PHANTOM,
                    com.truetap.solana.seeker.data.WalletType.EXTERNAL -> {
                        // MWA sign and send
                        val componentActivity = activity as? ComponentActivity
                            ?: throw IllegalStateException("ComponentActivity required for MWA transact")
                        val sender = ActivityResultSender(componentActivity)
                        val adapter = MobileWalletAdapterServiceHelper.adapter
                        val mwaResult: MwaTransactionResult<*> = adapter.transact(sender) {
                            signAndSendTransactions(arrayOf(transactionBytes))
                        }
                        when (mwaResult) {
                            is MwaTransactionResult.Success<*> -> {
                                val payload = (mwaResult as MwaTransactionResult.Success<*>).payload
                                val signatures = (payload as? MobileWalletAdapterClient.SignAndSendTransactionsResult)?.signatures
                                val signatureBase58 = signatures?.firstOrNull()?.let { bytes -> Base58.encode(bytes) }
                                if (signatureBase58 != null) {
                                    TransactionResult.Success(signatureBase58)
                                } else {
                                    TransactionResult.Error("No signature returned from wallet")
                                }
                            }
                            is MwaTransactionResult.NoWalletFound -> TransactionResult.Error("No compatible wallet found")
                            is MwaTransactionResult.Failure -> TransactionResult.Error("MWA error: ${mwaResult.e.message}")
                        }
                    }
                    else -> {
                        // Seed Vault sign then submit
                        val derivationPath = byteArrayOf(
                            0x80.toByte(), 0x00, 0x00, 0x2C, // 44'
                            0x80.toByte(), 0x00, 0x01, 0xF5.toByte(), // 501' (Solana)
                            0x80.toByte(), 0x00, 0x00, 0x00, // 0'
                            0x00, 0x00, 0x00, 0x00           // 0'
                        )
                        val signingResult = seedVaultProvider.signTransaction(
                            activity,
                            transactionBytes,
                            derivationPath,
                            activityResultLauncher
                        )
                        val signedTransaction = when (signingResult) {
                            is SigningResult.Success -> android.util.Base64.encodeToString(signingResult.signedTransaction, android.util.Base64.DEFAULT)
                            is SigningResult.Error -> throw Exception("Transaction signing failed: ${signingResult.message}")
                            is SigningResult.UserDenied -> throw Exception("Transaction signing was denied by user")
                        }
                        val signature = solanaRpcService.sendTransaction(signedTransaction)
                        TransactionResult.Success(signature)
                    }
                }
            } else {
                val errorStream = connection.errorStream
                val errorResponse = errorStream?.let { 
                    BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() }
                } ?: "Unknown error"
                
                // Parse specific Jupiter API errors
                val errorMessage = parseJupiterError(responseCode, errorResponse)
                TransactionResult.Error(errorMessage)
            }
        } catch (e: Exception) {
            android.util.Log.e("JupiterSwapService", "Error executing swap", e)
            val errorMessage = when {
                e.message?.contains("timeout", ignoreCase = true) == true -> 
                    "Network timeout. Please check your connection and try again."
                e.message?.contains("connection", ignoreCase = true) == true -> 
                    "Connection failed. Please check your internet and try again."
                e.message?.contains("insufficient", ignoreCase = true) == true -> 
                    "Insufficient balance for this transaction."
                else -> "Swap execution failed: ${e.message}"
            }
            TransactionResult.Error(errorMessage)
        }
    }
    
    /**
     * Get current token price from Jupiter
     */
    suspend fun getTokenPrice(tokenSymbol: String): BigDecimal = withContext(Dispatchers.IO) {
        try {
            val tokenMint = TOKEN_MINTS[tokenSymbol] ?: throw IllegalArgumentException("Unsupported token: $tokenSymbol")
            
            val url = URL("$JUPITER_API_BASE/price?ids=$tokenMint")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val jsonResponse = JSONObject(response)
                val priceData = jsonResponse.getJSONObject("data").getJSONObject(tokenMint)
                BigDecimal(priceData.getString("price"))
            } else {
                BigDecimal.ZERO
            }
        } catch (e: Exception) {
            android.util.Log.e("JupiterSwapService", "Error getting token price", e)
            BigDecimal.ZERO
        }
    }
    
    /**
     * Parse Jupiter API errors to provide user-friendly messages
     */
    private fun parseJupiterError(responseCode: Int, errorResponse: String): String {
        return when (responseCode) {
            400 -> {
                when {
                    errorResponse.contains("No routes found", ignoreCase = true) ->
                        "No swap routes found for this token pair. Try a different amount or token."
                    errorResponse.contains("Insufficient liquidity", ignoreCase = true) ->
                        "Not enough liquidity for this swap. Try reducing the amount."
                    errorResponse.contains("Invalid amount", ignoreCase = true) ->
                        "Invalid swap amount. Please check your input."
                    errorResponse.contains("slippage", ignoreCase = true) ->
                        "Slippage tolerance too low. Try increasing slippage or reducing amount."
                    else -> "Invalid swap request. Please check your inputs and try again."
                }
            }
            429 -> "Too many requests. Please wait a moment and try again."
            500, 502, 503 -> "Jupiter API is experiencing issues. Please try again in a moment."
            else -> "Swap failed with error $responseCode. Please try again."
        }
    }
    
    /**
     * Auto-calculate optimal fees based on network conditions
     */
    suspend fun calculateOptimalFees(speed: String = "Normal"): Pair<BigDecimal, BigDecimal> = withContext(Dispatchers.IO) {
        try {
            // Get recent blockhash to estimate network fee
            val blockhash = solanaRpcService.getLatestBlockhash()
            
            // Base network fee (typical Solana transaction)
            val baseNetworkFee = BigDecimal("0.000005") // 5,000 lamports
            
            // Calculate platform fee based on Genesis NFT status
            // This would be calculated based on the actual swap amount
            val platformFeeRate = if (isGenesisHolder) GENESIS_FEE_BPS else STANDARD_FEE_BPS
            
            // Priority fees based on speed
            val priorityFee = when (speed) {
                "Fast" -> BigDecimal("0.0001") // Higher priority for faster confirmation
                "Normal" -> BigDecimal("0.00005") // Standard priority
                else -> BigDecimal("0.000025") // Lower priority for eco mode
            }
            
            val totalNetworkFee = baseNetworkFee.add(priorityFee)
            val platformFeeMultiplier = BigDecimal(platformFeeRate).divide(BigDecimal(10000)) // Convert BPS to decimal
            
            Pair(totalNetworkFee, platformFeeMultiplier)
            
        } catch (e: Exception) {
            android.util.Log.w("JupiterSwapService", "Failed to calculate optimal fees, using defaults", e)
            // Return default fees
            val defaultNetworkFee = BigDecimal("0.00001")
            val defaultPlatformRate = BigDecimal(if (isGenesisHolder) GENESIS_FEE_BPS else STANDARD_FEE_BPS)
                .divide(BigDecimal(10000))
            Pair(defaultNetworkFee, defaultPlatformRate)
        }
    }
    
    /**
     * Check network congestion status
     */
    suspend fun getNetworkCongestionStatus(): NetworkCongestionStatus = withContext(Dispatchers.IO) {
        try {
            // Get current slot and recent performance samples
            val recentPerformance = solanaRpcService.getRecentPerformanceSamples()
            
            // Analyze average transaction times and fees
            val avgConfirmationTime = recentPerformance.map { it.avgTransactionTime }.average()
            val avgTransactionsPerSlot = recentPerformance.map { it.numTransactions }.average()
            
            when {
                avgConfirmationTime > 30.0 || avgTransactionsPerSlot > 3000 -> 
                    NetworkCongestionStatus.HIGH
                avgConfirmationTime > 15.0 || avgTransactionsPerSlot > 2000 -> 
                    NetworkCongestionStatus.MEDIUM
                else -> NetworkCongestionStatus.LOW
            }
        } catch (e: Exception) {
            android.util.Log.w("JupiterSwapService", "Failed to get congestion status", e)
            NetworkCongestionStatus.UNKNOWN
        }
    }
    
    /**
     * Get recommended transaction speed based on network conditions
     */
    suspend fun getRecommendedSpeed(): Pair<String, String> = withContext(Dispatchers.IO) {
        when (getNetworkCongestionStatus()) {
            NetworkCongestionStatus.HIGH -> 
                Pair("Fast", "Network is congested. Consider using Fast speed for better success rate.")
            NetworkCongestionStatus.MEDIUM -> 
                Pair("Normal", "Network has moderate activity. Normal speed should work well.")
            NetworkCongestionStatus.LOW -> 
                Pair("Eco", "Network is clear. Eco speed will save on fees.")
            NetworkCongestionStatus.UNKNOWN -> 
                Pair("Normal", "Unable to determine network status. Normal speed recommended.")
        }
    }
    
    // Helper property to check Genesis NFT status (this would be injected from NftService)
    private var isGenesisHolder: Boolean = false
    
    /**
     * Update Genesis NFT holder status
     */
    fun updateGenesisStatus(isHolder: Boolean) {
        isGenesisHolder = isHolder
    }
    
    private fun parseJupiterQuote(
        jsonResponse: JSONObject,
        inputToken: String,
        outputToken: String,
        inputAmount: BigDecimal,
        isGenesisHolder: Boolean,
        platformFeeBps: Int
    ): SwapQuote {
        val outputAmount = BigDecimal(jsonResponse.getString("outAmount"))
        val outputDecimals = TOKEN_DECIMALS[outputToken] ?: 9
        val outputAmountDecimal = outputAmount.movePointLeft(outputDecimals)
        
        val routePlan = jsonResponse.getJSONArray("routePlan")
        val route = mutableListOf<String>()
        for (i in 0 until routePlan.length()) {
            val step = routePlan.getJSONObject(i)
            val swapInfo = step.getJSONObject("swapInfo")
            route.add(swapInfo.getString("label"))
        }
        
        val priceImpactPct = jsonResponse.optDouble("priceImpactPct", 0.0)
        val slippageBps = jsonResponse.optInt("slippageBps", 50)
        
        // Calculate fees
        val platformFeeAmount = inputAmount * BigDecimal(platformFeeBps) / BigDecimal(10000)
        val networkFee = BigDecimal("0.000005") // ~5000 lamports
        
        return SwapQuote(
            inputAmount = inputAmount,
            outputAmount = outputAmountDecimal,
            inputToken = CryptoToken.fromSymbol(inputToken)!!,
            outputToken = CryptoToken.fromSymbol(outputToken)!!,
            rate = outputAmountDecimal.divide(inputAmount, 6, BigDecimal.ROUND_HALF_UP).toDouble(),
            slippage = slippageBps / 10000.0,
            networkFee = networkFee,
            exchangeFee = platformFeeAmount,
            route = route,
            estimatedTime = 30, // seconds
            priceImpact = priceImpactPct,
            isGenesisHolder = isGenesisHolder,
            platformFeeBps = platformFeeBps,
            jupiterQuoteResponse = jsonResponse.toString()
        )
    }
    
    fun getTokenMint(symbol: String): String {
        return TOKEN_MINTS[symbol] ?: throw IllegalArgumentException("Unsupported token: $symbol")
    }
    
    fun getTokenDecimals(symbol: String): Int {
        return TOKEN_DECIMALS[symbol] ?: 9
    }
}

/**
 * Network congestion status levels
 */
enum class NetworkCongestionStatus {
    LOW,      // Network is clear, low fees recommended
    MEDIUM,   // Moderate activity, normal fees
    HIGH,     // Heavy congestion, high priority fees recommended
    UNKNOWN   // Unable to determine status
}

/**
 * Network performance data
 */
data class NetworkPerformanceSample(
    val slot: Long,
    val numTransactions: Int,
    val avgTransactionTime: Double,
    val samplePeriodSecs: Int
)