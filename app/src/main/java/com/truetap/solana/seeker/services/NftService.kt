package com.truetap.solana.seeker.services

import com.truetap.solana.seeker.data.models.TokenAccountInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * NFT Service for TrueTap Genesis NFT detection
 * Handles verification of Genesis NFT ownership for fee tier benefits
 */
@Singleton
class NftService @Inject constructor(
    private val solanaRpcService: SolanaRpcService
) {
    companion object {
        // TrueTap Genesis NFT Collection Address
        private const val GENESIS_COLLECTION_ADDRESS = "46pcSL5gmjBrPqGKFaLbbCmR6iVuLJbnQy13hAe7s6CC"
        
        // Seeker Genesis NFT individual mint addresses (add more as needed)
        private const val SEEKER_GENESIS_MINT = "46pcSL5gmjBrPqGKFaLbbCmR6iVuLJbnQy13hAe7s6CC"
        
        // Helius API for NFT data (alternative to Magic Eden)
        private const val HELIUS_API_BASE = "https://api.helius.xyz/v0"
        
        // Magic Eden API for NFT verification
        private const val MAGIC_EDEN_API = "https://api-mainnet.magiceden.dev/v2"
        
        // Timeout configurations
        private const val API_CONNECT_TIMEOUT = 10_000 // 10 seconds
        private const val API_READ_TIMEOUT = 15_000    // 15 seconds
        private const val GENESIS_CHECK_TIMEOUT = 30_000L // 30 seconds total for Genesis check
        private const val INDIVIDUAL_API_TIMEOUT = 10_000L // 10 seconds per API call
        private const val METADATA_FETCH_TIMEOUT = 8_000L  // 8 seconds for metadata
    }
    
    /**
     * Check if wallet holds a TrueTap Genesis NFT with comprehensive timeout handling
     */
    suspend fun hasGenesisNFT(walletAddress: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Overall timeout for the entire Genesis NFT check operation
            withTimeout(GENESIS_CHECK_TIMEOUT) {
                android.util.Log.d("NftService", "Starting Genesis NFT check for wallet: $walletAddress")
                
                // Try multiple methods for NFT detection with individual timeouts
                checkViaSolanaRpc(walletAddress) || 
                checkViaMagicEden(walletAddress) ||
                checkViaHelius(walletAddress)
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.w("NftService", "Genesis NFT check timed out after ${GENESIS_CHECK_TIMEOUT}ms for wallet: $walletAddress")
            // Fallback to test wallet check on timeout
            isTestWalletWithGenesis(walletAddress)
        } catch (e: Exception) {
            android.util.Log.e("NftService", "Error checking Genesis NFT for wallet: $walletAddress", e)
            // For demo purposes, return true for specific test wallets
            isTestWalletWithGenesis(walletAddress)
        }
    }
    
    /**
     * Get all Genesis NFTs owned by wallet with timeout handling
     */
    suspend fun getGenesisNFTs(walletAddress: String): List<GenesisNFT> = withContext(Dispatchers.IO) {
        try {
            withTimeout(GENESIS_CHECK_TIMEOUT) {
                android.util.Log.d("NftService", "Starting Genesis NFT collection for wallet: $walletAddress")
                val nfts = mutableListOf<GenesisNFT>()
                
                // Get NFTs from wallet using Solana RPC
                val tokenAccounts = solanaRpcService.getTokenAccountsByOwner(walletAddress)
                
                for (account in tokenAccounts) {
                    val tokenInfo = solanaRpcService.getTokenAccountInfo(account)
                    if (tokenInfo.amount == "1" && isGenesisNFT(tokenInfo.mint)) {
                        try {
                            // Individual timeout for metadata fetching
                            val metadata = withTimeout(METADATA_FETCH_TIMEOUT) {
                                getNFTMetadata(tokenInfo.mint)
                            }
                            nfts.add(GenesisNFT(
                                mint = tokenInfo.mint,
                                name = metadata.name,
                                image = metadata.image,
                                traits = metadata.traits,
                                tier = determineNFTTier(metadata.traits)
                            ))
                        } catch (metadataTimeout: kotlinx.coroutines.TimeoutCancellationException) {
                            android.util.Log.w("NftService", "Metadata fetch timed out for NFT: ${tokenInfo.mint}")
                            // Add NFT with basic info even if metadata fetch fails
                            nfts.add(GenesisNFT(
                                mint = tokenInfo.mint,
                                name = "Genesis NFT ${tokenInfo.mint.take(8)}...",
                                image = "",
                                traits = emptyMap(),
                                tier = GenesisNFTTier.COMMON
                            ))
                        }
                    }
                }
                
                android.util.Log.d("NftService", "Found ${nfts.size} Genesis NFTs for wallet: $walletAddress")
                nfts
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.w("NftService", "Genesis NFT collection timed out after ${GENESIS_CHECK_TIMEOUT}ms for wallet: $walletAddress")
            emptyList()
        } catch (e: Exception) {
            android.util.Log.e("NftService", "Error getting Genesis NFTs for wallet: $walletAddress", e)
            emptyList()
        }
    }
    
    /**
     * Get Genesis NFT tier for potential future tier-based benefits
     */
    suspend fun getGenesisNFTTier(walletAddress: String): GenesisNFTTier {
        return try {
            val nfts = getGenesisNFTs(walletAddress)
            when {
                nfts.any { it.tier == GenesisNFTTier.LEGENDARY } -> GenesisNFTTier.LEGENDARY
                nfts.any { it.tier == GenesisNFTTier.RARE } -> GenesisNFTTier.RARE
                nfts.any { it.tier == GenesisNFTTier.COMMON } -> GenesisNFTTier.COMMON
                else -> GenesisNFTTier.NONE
            }
        } catch (e: Exception) {
            GenesisNFTTier.NONE
        }
    }
    
    /**
     * Check NFT ownership via Solana RPC with timeout
     */
    private suspend fun checkViaSolanaRpc(walletAddress: String): Boolean {
        return try {
            withTimeout(INDIVIDUAL_API_TIMEOUT) {
                android.util.Log.d("NftService", "Checking Genesis NFT via Solana RPC for: $walletAddress")
                val tokenAccounts = solanaRpcService.getTokenAccountsByOwner(walletAddress)
                val hasGenesis = tokenAccounts.any { account ->
                    val tokenInfo = solanaRpcService.getTokenAccountInfo(account)
                    tokenInfo.amount == "1" && isGenesisNFT(tokenInfo.mint)
                }
                android.util.Log.d("NftService", "Solana RPC check result: $hasGenesis")
                hasGenesis
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.w("NftService", "Solana RPC check timed out after ${INDIVIDUAL_API_TIMEOUT}ms")
            false
        } catch (e: Exception) {
            android.util.Log.e("NftService", "Solana RPC check failed", e)
            false
        }
    }
    
    /**
     * Check NFT ownership via Magic Eden API with timeout
     */
    private suspend fun checkViaMagicEden(walletAddress: String): Boolean {
        return try {
            withTimeout(INDIVIDUAL_API_TIMEOUT) {
                android.util.Log.d("NftService", "Checking Genesis NFT via Magic Eden for: $walletAddress")
                val url = URL("$MAGIC_EDEN_API/wallets/$walletAddress/tokens?offset=0&limit=500")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = API_CONNECT_TIMEOUT
                connection.readTimeout = API_READ_TIMEOUT
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                    val jsonArray = JSONArray(response)
                    
                    for (i in 0 until jsonArray.length()) {
                        val nft = jsonArray.getJSONObject(i)
                        val collection = nft.optString("collection")
                        if (collection == GENESIS_COLLECTION_ADDRESS) {
                            android.util.Log.d("NftService", "Magic Eden found Genesis NFT!")
                            return@withTimeout true
                        }
                    }
                }
                android.util.Log.d("NftService", "Magic Eden check result: false")
                false
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.w("NftService", "Magic Eden API check timed out after ${INDIVIDUAL_API_TIMEOUT}ms")
            false
        } catch (e: Exception) {
            android.util.Log.e("NftService", "Magic Eden API check failed", e)
            false
        }
    }
    
    /**
     * Check NFT ownership via Helius API with timeout
     */
    private suspend fun checkViaHelius(walletAddress: String): Boolean {
        return try {
            withTimeout(INDIVIDUAL_API_TIMEOUT) {
                android.util.Log.d("NftService", "Checking Genesis NFT via Helius for: $walletAddress")
                // You would need a Helius API key for this
                val apiKey = "your-helius-api-key"
                val url = URL("$HELIUS_API_BASE/addresses/$walletAddress/nfts?api-key=$apiKey")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = API_CONNECT_TIMEOUT
                connection.readTimeout = API_READ_TIMEOUT
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                    val jsonObject = JSONObject(response)
                    val nfts = jsonObject.getJSONArray("nfts")
                    
                    for (i in 0 until nfts.length()) {
                        val nft = nfts.getJSONObject(i)
                        val grouping = nft.optJSONArray("grouping")
                        if (grouping != null) {
                            for (j in 0 until grouping.length()) {
                                val group = grouping.getJSONObject(j)
                                if (group.getString("group_key") == "collection" && 
                                    group.getString("group_value") == GENESIS_COLLECTION_ADDRESS) {
                                    android.util.Log.d("NftService", "Helius found Genesis NFT!")
                                    return@withTimeout true
                                }
                            }
                        }
                    }
                }
                android.util.Log.d("NftService", "Helius check result: false")
                false
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.w("NftService", "Helius API check timed out after ${INDIVIDUAL_API_TIMEOUT}ms")
            false
        } catch (e: Exception) {
            android.util.Log.e("NftService", "Helius API check failed", e)
            false
        }
    }
    
    /**
     * Check if a mint address is a Genesis NFT
     */
    private fun isGenesisNFT(mintAddress: String): Boolean {
        // Check against known Genesis NFT mint addresses
        return mintAddress == SEEKER_GENESIS_MINT || 
               mintAddress == GENESIS_COLLECTION_ADDRESS ||
               checkGenesisCollection(mintAddress)
    }
    
    private fun checkGenesisCollection(mintAddress: String): Boolean {
        // For additional Genesis NFT mints, check collection metadata via RPC
        // This would involve fetching the NFT's metadata and checking collection field
        return try {
            // TODO: Implement collection metadata check via Solana RPC
            // For now, fallback to pattern matching for backwards compatibility
            mintAddress.contains("TrueTap", ignoreCase = true) || 
            mintAddress.contains("Genesis", ignoreCase = true) ||
            mintAddress.contains("Seeker", ignoreCase = true)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get NFT metadata from mint address with timeout
     */
    private suspend fun getNFTMetadata(mintAddress: String): NFTMetadata {
        return try {
            withTimeout(METADATA_FETCH_TIMEOUT) {
                android.util.Log.d("NftService", "Fetching metadata for NFT: $mintAddress")
                
                // Get metadata from Solana RPC
                val metadataAccount = solanaRpcService.getMetadataAccount(mintAddress)
                val metadataUri = solanaRpcService.getMetadataUri(metadataAccount)
                
                // Fetch metadata JSON with timeout
                val url = URL(metadataUri)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = API_CONNECT_TIMEOUT
                connection.readTimeout = API_READ_TIMEOUT
                
                val response = BufferedReader(InputStreamReader(connection.inputStream)).use { it.readText() }
                val jsonObject = JSONObject(response)
                
                val attributes = jsonObject.optJSONArray("attributes") ?: JSONArray()
                val traits = mutableMapOf<String, String>()
                for (i in 0 until attributes.length()) {
                    val attribute = attributes.getJSONObject(i)
                    traits[attribute.getString("trait_type")] = attribute.getString("value")
                }
                
                val metadata = NFTMetadata(
                    name = jsonObject.optString("name", "Unknown NFT"),
                    image = jsonObject.optString("image", ""),
                    traits = traits
                )
                
                android.util.Log.d("NftService", "Successfully fetched metadata for: ${metadata.name}")
                metadata
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.w("NftService", "Metadata fetch timed out after ${METADATA_FETCH_TIMEOUT}ms for: $mintAddress")
            NFTMetadata("Genesis NFT ${mintAddress.take(8)}...", "", emptyMap())
        } catch (e: Exception) {
            android.util.Log.e("NftService", "Failed to fetch metadata for: $mintAddress", e)
            NFTMetadata("Unknown Genesis NFT", "", emptyMap())
        }
    }
    
    /**
     * Determine NFT tier based on traits
     */
    private fun determineNFTTier(traits: Map<String, String>): GenesisNFTTier {
        val rarity = traits["Rarity"]?.lowercase()
        return when (rarity) {
            "legendary" -> GenesisNFTTier.LEGENDARY
            "rare" -> GenesisNFTTier.RARE
            "common" -> GenesisNFTTier.COMMON
            else -> GenesisNFTTier.COMMON
        }
    }
    
    /**
     * For demo/testing purposes - check if wallet should have Genesis benefits
     */
    private fun isTestWalletWithGenesis(walletAddress: String): Boolean {
        // Add test wallet addresses that should have Genesis benefits during development
        val testGenesisWallets = listOf(
            "7xKBvf6Z1nP2QqXhJgY8sH3fK2dG1mVc9tB4wR5uE8pN", // Test wallet 1
            "9mKxA8rT1sE3pQ7vB2nF6jL4wG8cY5dH1xZ3kP9sM7tR"  // Test wallet 2
        )
        return testGenesisWallets.contains(walletAddress)
    }
}

/**
 * Data classes for Genesis NFT system
 */
data class GenesisNFT(
    val mint: String,
    val name: String,
    val image: String,
    val traits: Map<String, String>,
    val tier: GenesisNFTTier
)

data class NFTMetadata(
    val name: String,
    val image: String,
    val traits: Map<String, String>
)

enum class GenesisNFTTier {
    NONE,
    COMMON,
    RARE,
    LEGENDARY
}

