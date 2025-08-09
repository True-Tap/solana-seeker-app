package com.truetap.solana.seeker.utils

import android.net.Uri
import android.util.Base64
import com.truetap.solana.seeker.data.ConnectionResult
import com.truetap.solana.seeker.data.WalletType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Utility for parsing Solflare deep link responses
 * Handles the wallet connection data returned from Solflare app
 */
object SolflareDeepLinkParser {
    private const val TAG = "SolflareDeepLinkParser"
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    /**
     * Parse a Solflare deep link response URI
     * Expected format: truetap://wallet-connected?data=<base64_encoded_json>&public_key=<public_key>
     */
    fun parseWalletConnectionResponse(uri: Uri): ConnectionResult {
        return try {
            Logx.d(TAG) { "Parsing Solflare deep link" }
            
            // Extract parameters from URI
            val publicKey = uri.getQueryParameter("public_key")
            val encodedData = uri.getQueryParameter("data")
            val errorParam = uri.getQueryParameter("error")
            val phantomPublicKey = uri.getQueryParameter("phantom_encryption_public_key")
            val solflarePublicKey = uri.getQueryParameter("solflare_encryption_public_key")
            
            // Log all parameters for debugging
            Logx.d(TAG) { "public_key=${Logx.redact(publicKey)} error=${Logx.redact(errorParam)}" }
            
            // Log all query parameters
            // Avoid logging raw query parameters in release builds
            
            // Check for error response
            if (errorParam != null) {
                Logx.e(TAG, { "Solflare returned error" })
                return ConnectionResult.Failure(
                    error = "Solflare connection failed: $errorParam",
                    walletType = WalletType.SOLFLARE
                )
            }
            
            // Try to extract public key from different possible sources
            var extractedPublicKey = publicKey
            var extractedAccountLabel: String? = null
            
            // If public_key is missing, try to extract from encoded data
            if (extractedPublicKey.isNullOrEmpty() && !encodedData.isNullOrEmpty()) {
                Logx.d(TAG) { "public_key missing, attempting to extract from data parameter" }
                try {
                    val decodedData = Base64.decode(encodedData, Base64.DEFAULT)
                    val dataString = String(decodedData)
                    
                    val solflareResponse = json.decodeFromString<SolflareResponse>(dataString)
                    extractedPublicKey = solflareResponse.publicKey
                    extractedAccountLabel = solflareResponse.accountLabel ?: solflareResponse.walletName
                    Logx.d(TAG) { "Extracted publicKey from data: ${Logx.redact(extractedPublicKey)}" }
                } catch (e: Exception) {
                    Logx.w(TAG) { "Failed to extract public key from data: ${e.message}" }
                }
            }
            
            // Try to extract from other possible parameter names
            if (extractedPublicKey.isNullOrEmpty()) {
                extractedPublicKey = uri.getQueryParameter("publicKey") 
                    ?: uri.getQueryParameter("public-key")
                    ?: uri.getQueryParameter("wallet_public_key")
                    ?: uri.getQueryParameter("address")
                    ?: uri.getQueryParameter("account")
                Logx.d(TAG) { "Tried alternative parameter names" }
            }
            
            // Final validation
            if (extractedPublicKey.isNullOrEmpty()) {
                Logx.e(TAG, { "Could not find public key in any expected parameter or data" })
                return ConnectionResult.Failure(
                    error = "Invalid response from Solflare: missing public key. Please try connecting again or use a different wallet.",
                    walletType = WalletType.SOLFLARE
                )
            }
            
            // Use extracted account label if available
            val finalAccountLabel = extractedAccountLabel ?: "Solflare Wallet"
            
            // Validate public key format (basic Solana public key validation)
            if (!isValidSolanaPublicKey(extractedPublicKey)) {
                Logx.e(TAG, { "Invalid Solana public key format" })
                return ConnectionResult.Failure(
                    error = "Invalid public key format received from Solflare: $extractedPublicKey",
                    walletType = WalletType.SOLFLARE
                )
            }
            
            Logx.d(TAG) { "Successfully parsed Solflare connection" }
            
            ConnectionResult.Success(
                publicKey = extractedPublicKey,
                accountLabel = finalAccountLabel,
                walletType = WalletType.SOLFLARE
            )
            
        } catch (e: Exception) {
            Logx.e(TAG, { "Error parsing Solflare deep link: ${e.message}" }, e)
            ConnectionResult.Failure(
                error = "Failed to process Solflare response: ${e.message}",
                exception = e,
                walletType = WalletType.SOLFLARE
            )
        }
    }
    
    /**
     * Basic validation for Solana public key format
     * Solana public keys are base58 encoded and typically 44 characters long
     */
    private fun isValidSolanaPublicKey(publicKey: String): Boolean {
        // Basic checks for Solana public key format
        if (publicKey.length < 32 || publicKey.length > 48) return false
        
        // Check if it contains only valid base58 characters
        val base58Chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        return publicKey.all { it in base58Chars }
    }
}

/**
 * Data classes for parsing Solflare response data
 */
@Serializable
private data class SolflareResponse(
    val publicKey: String? = null,
    val accountLabel: String? = null,
    val walletName: String? = null,
    val cluster: String? = null,
    val timestamp: Long? = null
)