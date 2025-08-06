package com.truetap.solana.seeker.services

import com.truetap.solana.seeker.data.models.TokenAccountInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Solana RPC Service for blockchain interactions
 * Handles communication with Solana network for transaction and account data
 */
@Singleton
class SolanaRpcService @Inject constructor() {
    
    companion object {
        // Solana RPC endpoints - use the fastest/most reliable
        private const val MAINNET_RPC = "https://api.mainnet-beta.solana.com"
        private const val DEVNET_RPC = "https://api.devnet.solana.com"
        private const val HELIUS_RPC = "https://rpc.helius.xyz/?api-key=your-api-key"
        
        // Use mainnet for production, devnet for testing
        private const val CURRENT_RPC = MAINNET_RPC
    }
    
    private var requestId = 1
    
    /**
     * Send a signed transaction to the Solana network
     */
    suspend fun sendTransaction(
        signedTransaction: String,
        skipPreflight: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", requestId++)
            put("method", "sendTransaction")
            put("params", JSONArray().apply {
                put(signedTransaction)
                put(JSONObject().apply {
                    put("skipPreflight", skipPreflight)
                    put("preflightCommitment", "confirmed")
                    put("encoding", "base64")
                })
            })
        }
        
        val response = makeRpcCall(requestBody)
        val result = response.optString("result")
        
        if (result.isNotEmpty()) {
            result
        } else {
            val error = response.optJSONObject("error")
            throw Exception("Transaction failed: ${error?.getString("message") ?: "Unknown error"}")
        }
    }
    
    /**
     * Get account balance in SOL
     */
    suspend fun getBalance(publicKey: String): Double = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", requestId++)
            put("method", "getBalance")
            put("params", JSONArray().apply {
                put(publicKey)
            })
        }
        
        val response = makeRpcCall(requestBody)
        val result = response.optJSONObject("result")
        val lamports = result?.optLong("value") ?: 0L
        lamports / 1_000_000_000.0 // Convert lamports to SOL
    }
    
    /**
     * Get token accounts owned by a wallet
     */
    suspend fun getTokenAccountsByOwner(ownerPublicKey: String): List<String> = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", requestId++)
            put("method", "getTokenAccountsByOwner")
            put("params", JSONArray().apply {
                put(ownerPublicKey)
                put(JSONObject().apply {
                    put("programId", "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA") // SPL Token Program
                })
                put(JSONObject().apply {
                    put("encoding", "jsonParsed")
                })
            })
        }
        
        val response = makeRpcCall(requestBody)
        val result = response.optJSONObject("result")
        val accounts = result?.optJSONArray("value") ?: JSONArray()
        
        val tokenAccounts = mutableListOf<String>()
        for (i in 0 until accounts.length()) {
            val account = accounts.getJSONObject(i)
            tokenAccounts.add(account.getString("pubkey"))
        }
        tokenAccounts
    }
    
    /**
     * Get token account information
     */
    suspend fun getTokenAccountInfo(accountPublicKey: String): TokenAccountInfo = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", requestId++)
            put("method", "getAccountInfo")
            put("params", JSONArray().apply {
                put(accountPublicKey)
                put(JSONObject().apply {
                    put("encoding", "jsonParsed")
                })
            })
        }
        
        val response = makeRpcCall(requestBody)
        val result = response.optJSONObject("result")
        val value = result?.optJSONObject("value")
        val data = value?.optJSONObject("data")
        val parsed = data?.optJSONObject("parsed")
        val info = parsed?.optJSONObject("info")
        
        TokenAccountInfo(
            mint = info?.optString("mint") ?: "",
            amount = info?.optString("tokenAmount")?.let { 
                JSONObject(it).optString("amount") 
            } ?: "0",
            decimals = info?.optString("tokenAmount")?.let { 
                JSONObject(it).optInt("decimals") 
            } ?: 0
        )
    }
    
    /**
     * Get transaction details
     */
    suspend fun getTransaction(signature: String): JSONObject? = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", requestId++)
            put("method", "getTransaction")
            put("params", JSONArray().apply {
                put(signature)
                put(JSONObject().apply {
                    put("encoding", "jsonParsed")
                    put("maxSupportedTransactionVersion", 0)
                })
            })
        }
        
        val response = makeRpcCall(requestBody)
        response.optJSONObject("result")
    }
    
    /**
     * Get recent blockhash for transaction building
     */
    suspend fun getLatestBlockhash(): String = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", requestId++)
            put("method", "getLatestBlockhash")
            put("params", JSONArray().apply {
                put(JSONObject().apply {
                    put("commitment", "confirmed")
                })
            })
        }
        
        val response = makeRpcCall(requestBody)
        val result = response.optJSONObject("result")
        val value = result?.optJSONObject("value")
        value?.optString("blockhash") ?: throw Exception("Failed to get latest blockhash")
    }
    
    /**
     * Get minimum rent exemption for account
     */
    suspend fun getMinimumBalanceForRentExemption(dataLength: Int): Long = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", requestId++)
            put("method", "getMinimumBalanceForRentExemption")
            put("params", JSONArray().apply {
                put(dataLength)
            })
        }
        
        val response = makeRpcCall(requestBody)
        response.optLong("result")
    }
    
    /**
     * Get metadata account for NFT
     */
    suspend fun getMetadataAccount(mintAddress: String): String = withContext(Dispatchers.IO) {
        // For NFTs, metadata account is derived from mint address
        // This is a simplified version - in reality you'd use Metaplex SDK
        // to properly derive the metadata PDA
        "${mintAddress}_metadata"
    }
    
    /**
     * Get metadata URI from metadata account
     */
    suspend fun getMetadataUri(metadataAccount: String): String = withContext(Dispatchers.IO) {
        // This would typically involve parsing the metadata account data
        // For demo purposes, return a placeholder
        "https://arweave.net/example-metadata-uri"
    }
    
    /**
     * Confirm transaction status
     */
    suspend fun confirmTransaction(signature: String, commitment: String = "confirmed"): Boolean = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", requestId++)
            put("method", "getSignatureStatuses")
            put("params", JSONArray().apply {
                put(JSONArray().apply { put(signature) })
                put(JSONObject().apply {
                    put("searchTransactionHistory", true)
                })
            })
        }
        
        val response = makeRpcCall(requestBody)
        val result = response.optJSONObject("result")
        val value = result?.optJSONArray("value")
        val status = value?.optJSONObject(0)
        
        status != null && !status.isNull("confirmationStatus")
    }
    
    /**
     * Get token supply information
     */
    suspend fun getTokenSupply(mintAddress: String): JSONObject? = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", requestId++)
            put("method", "getTokenSupply")
            put("params", JSONArray().apply {
                put(mintAddress)
            })
        }
        
        val response = makeRpcCall(requestBody)
        response.optJSONObject("result")?.optJSONObject("value")
    }
    
    /**
     * Get recent performance samples for network congestion analysis
     */
    suspend fun getRecentPerformanceSamples(limit: Int = 20): List<NetworkPerformanceSample> = withContext(Dispatchers.IO) {
        try {
            val requestBody = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", requestId++)
                put("method", "getRecentPerformanceSamples")
                put("params", JSONArray().apply {
                    put(limit)
                })
            }
            
            val response = makeRpcCall(requestBody)
            val result = response.optJSONArray("result")
            val samples = mutableListOf<NetworkPerformanceSample>()
            
            if (result != null) {
                for (i in 0 until result.length()) {
                    val sample = result.getJSONObject(i)
                    samples.add(
                        NetworkPerformanceSample(
                            slot = sample.getLong("slot"),
                            numTransactions = sample.getInt("numTransactions"),
                            avgTransactionTime = sample.optDouble("samplePeriodSecs", 60.0),
                            samplePeriodSecs = sample.getInt("samplePeriodSecs")
                        )
                    )
                }
            }
            
            samples
        } catch (e: Exception) {
            android.util.Log.w("SolanaRpcService", "Failed to get performance samples", e)
            // Return mock data for fallback
            listOf(
                NetworkPerformanceSample(
                    slot = System.currentTimeMillis() / 400, // Approximate slot
                    numTransactions = 2500, // Moderate activity
                    avgTransactionTime = 12.0,
                    samplePeriodSecs = 60
                )
            )
        }
    }
    
    /**
     * Make RPC call to Solana network
     */
    private suspend fun makeRpcCall(requestBody: JSONObject): JSONObject = withContext(Dispatchers.IO) {
        try {
            val url = URL(CURRENT_RPC)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true
            
            // Write request body
            connection.outputStream.use { os ->
                os.write(requestBody.toString().toByteArray())
            }
            
            val responseCode = connection.responseCode
            val inputStream = if (responseCode >= 200 && responseCode < 300) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            
            val response = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
            JSONObject(response)
            
        } catch (e: Exception) {
            android.util.Log.e("SolanaRpcService", "RPC call failed", e)
            throw Exception("Solana RPC call failed: ${e.message}")
        }
    }
}

