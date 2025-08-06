package com.truetap.solana.seeker.services

import android.util.Log
import com.truetap.solana.seeker.BuildConfig
import com.truetap.solana.seeker.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for interacting with Solana blockchain
 * Handles balance fetching, NFT metadata, and transaction history
 */
@Singleton
class SolanaService @Inject constructor() {
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Network configuration - use BuildConfig to switch between networks
    private val rpcUrl = if (BuildConfig.DEBUG) {
        "https://api.devnet.solana.com"  // Devnet for development/testing
    } else {
        "https://api.mainnet-beta.solana.com"  // Mainnet for production
    }
    private val heliusApiKey = "YOUR_HELIUS_API_KEY" // You'll need to get this for NFT metadata
    
    companion object {
        private const val TAG = "SolanaService"
        private const val LAMPORTS_PER_SOL = 1_000_000_000L
        
        // Individual RPC call timeout (longer than HTTP client timeout)
        private const val RPC_OPERATION_TIMEOUT = 45_000L // 45 seconds per RPC call
    }
    
    /**
     * Fetch SOL balance for a wallet with timeout handling
     */
    suspend fun getBalance(publicKey: String): WalletResult<WalletBalance> = withContext(Dispatchers.IO) {
        try {
            withTimeout(RPC_OPERATION_TIMEOUT) {
                Log.d(TAG, "Starting balance fetch for: $publicKey")
            val requestBody = """
                {
                    "jsonrpc": "2.0",
                    "id": 1,
                    "method": "getBalance",
                    "params": ["$publicKey"]
                }
            """.trimIndent()
            
            val request = Request.Builder()
                .url(rpcUrl)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                val balanceResponse = json.decodeFromString<BalanceResponse>(responseBody)
                val lamports = balanceResponse.result?.value ?: 0L
                val solBalance = BigDecimal(lamports).divide(BigDecimal(LAMPORTS_PER_SOL))
                
                val walletBalance = WalletBalance(
                    publicKey = publicKey,
                    solBalance = solBalance,
                    tokenBalances = emptyList(), // Will implement token balance fetching separately
                    lastUpdated = System.currentTimeMillis()
                )
                
                Log.d(TAG, "Successfully fetched balance for $publicKey: $solBalance SOL")
                WalletResult.Success(walletBalance)
            } else {
                val errorMessage = "Failed to fetch balance: HTTP ${response.code}"
                Log.e(TAG, errorMessage)
                WalletResult.Error(RuntimeException(errorMessage), errorMessage)
            }
            } // End of withTimeout
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            val errorMessage = "Balance fetch timed out after ${RPC_OPERATION_TIMEOUT}ms for: $publicKey"
            Log.w(TAG, errorMessage)
            WalletResult.Error(RuntimeException(errorMessage), errorMessage)
        } catch (e: Exception) {
            val errorMessage = "Error fetching balance: ${e.message}"
            Log.e(TAG, errorMessage, e)
            WalletResult.Error(e, errorMessage)
        }
    }
    
    /**
     * Fetch token balances for a wallet
     */
    suspend fun getTokenBalances(publicKey: String): WalletResult<List<TokenBalance>> = withContext(Dispatchers.IO) {
        try {
            val requestBody = """
                {
                    "jsonrpc": "2.0",
                    "id": 1,
                    "method": "getTokenAccountsByOwner",
                    "params": [
                        "$publicKey",
                        { "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA" },
                        { "encoding": "jsonParsed" }
                    ]
                }
            """.trimIndent()
            
            val request = Request.Builder()
                .url(rpcUrl)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                val tokenResponse = json.decodeFromString<TokenAccountsResponse>(responseBody)
                val tokenBalances = tokenResponse.result?.value?.mapNotNull { account ->
                    try {
                        val info = account.account.data.parsed.info
                        val tokenAmount = info.tokenAmount
                        
                        if (tokenAmount.uiAmount > 0.0) {
                            TokenBalance(
                                mint = info.mint,
                                symbol = "Unknown", // Would need token registry for symbols
                                name = "Unknown Token",
                                amount = BigDecimal(tokenAmount.amount),
                                decimals = tokenAmount.decimals,
                                uiAmount = tokenAmount.uiAmount
                            )
                        } else null
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse token account: ${e.message}")
                        null
                    }
                } ?: emptyList()
                
                Log.d(TAG, "Successfully fetched ${tokenBalances.size} token balances for $publicKey")
                WalletResult.Success(tokenBalances)
            } else {
                val errorMessage = "Failed to fetch token balances: HTTP ${response.code}"
                Log.e(TAG, errorMessage)
                WalletResult.Error(RuntimeException(errorMessage), errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = "Error fetching token balances: ${e.message}"
            Log.e(TAG, errorMessage, e)
            WalletResult.Error(e, errorMessage)
        }
    }
    
    /**
     * Fetch NFTs for a wallet (simplified implementation)
     */
    suspend fun getNFTs(publicKey: String): WalletResult<List<WalletNFT>> = withContext(Dispatchers.IO) {
        try {
            // First get token accounts that could be NFTs (supply = 1, decimals = 0)
            val requestBody = """
                {
                    "jsonrpc": "2.0",
                    "id": 1,
                    "method": "getTokenAccountsByOwner",
                    "params": [
                        "$publicKey",
                        { "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA" },
                        { "encoding": "jsonParsed" }
                    ]
                }
            """.trimIndent()
            
            val request = Request.Builder()
                .url(rpcUrl)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                val tokenResponse = json.decodeFromString<TokenAccountsResponse>(responseBody)
                val nfts = tokenResponse.result?.value?.mapNotNull { account ->
                    try {
                        val info = account.account.data.parsed.info
                        val tokenAmount = info.tokenAmount
                        
                        // Check if this could be an NFT (amount = 1, decimals = 0)
                        if (tokenAmount.decimals == 0 && tokenAmount.uiAmount == 1.0) {
                            WalletNFT(
                                mint = info.mint,
                                name = "NFT ${info.mint.take(8)}...",
                                description = "NFT from wallet",
                                image = null, // Would need metadata fetching
                                lastUpdated = System.currentTimeMillis()
                            )
                        } else null
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse potential NFT: ${e.message}")
                        null
                    }
                } ?: emptyList()
                
                Log.d(TAG, "Successfully fetched ${nfts.size} NFTs for $publicKey")
                WalletResult.Success(nfts)
            } else {
                val errorMessage = "Failed to fetch NFTs: HTTP ${response.code}"
                Log.e(TAG, errorMessage)
                WalletResult.Error(RuntimeException(errorMessage), errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = "Error fetching NFTs: ${e.message}"
            Log.e(TAG, errorMessage, e)
            WalletResult.Error(e, errorMessage)
        }
    }
    
    /**
     * Fetch recent transactions for a wallet
     */
    suspend fun getTransactionHistory(publicKey: String, limit: Int = 10): WalletResult<List<WalletTransaction>> = withContext(Dispatchers.IO) {
        try {
            val requestBody = """
                {
                    "jsonrpc": "2.0",
                    "id": 1,
                    "method": "getConfirmedSignaturesForAddress2",
                    "params": [
                        "$publicKey",
                        { "limit": $limit }
                    ]
                }
            """.trimIndent()
            
            val request = Request.Builder()
                .url(rpcUrl)
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()
            
            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            
            if (response.isSuccessful) {
                val signaturesResponse = json.decodeFromString<SignaturesResponse>(responseBody)
                val transactions = signaturesResponse.result?.mapNotNull { sigInfo ->
                    try {
                        WalletTransaction(
                            signature = sigInfo.signature,
                            blockTime = sigInfo.blockTime ?: 0L,
                            slot = sigInfo.slot,
                            status = if (sigInfo.err != null) TransactionStatus.FAILED else TransactionStatus.SUCCESS,
                            type = TransactionType.UNKNOWN, // Would need detailed transaction parsing
                            fee = BigDecimal.ZERO, // Would get from transaction details
                            memo = sigInfo.memo
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse transaction signature: ${e.message}")
                        null
                    }
                } ?: emptyList()
                
                Log.d(TAG, "Successfully fetched ${transactions.size} transactions for $publicKey")
                WalletResult.Success(transactions)
            } else {
                val errorMessage = "Failed to fetch transactions: HTTP ${response.code}"
                Log.e(TAG, errorMessage)
                WalletResult.Error(RuntimeException(errorMessage), errorMessage)
            }
        } catch (e: Exception) {
            val errorMessage = "Error fetching transactions: ${e.message}"
            Log.e(TAG, errorMessage, e)
            WalletResult.Error(e, errorMessage)
        }
    }
}

// Data classes for JSON responses
@Serializable
private data class BalanceResponse(
    val jsonrpc: String,
    val result: BalanceResult?,
    val id: Int
)

@Serializable
private data class BalanceResult(
    val context: Context,
    val value: Long
) {
    @Serializable
    data class Context(val slot: Long)
}

@Serializable
private data class TokenAccountsResponse(
    val jsonrpc: String,
    val result: TokenAccountsResult?,
    val id: Int
)

@Serializable
private data class TokenAccountsResult(
    val context: BalanceResult.Context,
    val value: List<TokenAccount>
)

@Serializable
private data class TokenAccount(
    val account: TokenAccountData,
    val pubkey: String
)

@Serializable
private data class TokenAccountData(
    val data: TokenAccountDataParsed,
    val executable: Boolean,
    val lamports: Long,
    val owner: String,
    val rentEpoch: Long
)

@Serializable
private data class TokenAccountDataParsed(
    val parsed: TokenAccountParsed,
    val program: String,
    val space: Long
)

@Serializable
private data class TokenAccountParsed(
    val info: ApiTokenAccountInfo,
    val type: String
)

@Serializable
private data class ApiTokenAccountInfo(
    val isNative: Boolean,
    val mint: String,
    val owner: String,
    val state: String,
    val tokenAmount: TokenAmount
)

@Serializable
private data class TokenAmount(
    val amount: String,
    val decimals: Int,
    val uiAmount: Double,
    val uiAmountString: String
)

@Serializable
private data class SignaturesResponse(
    val jsonrpc: String,
    val result: List<SignatureInfo>?,
    val id: Int
)

@Serializable
private data class SignatureInfo(
    val signature: String,
    val slot: Long,
    val err: String? = null,
    val memo: String? = null,
    val blockTime: Long? = null
)