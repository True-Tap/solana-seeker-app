package com.truetap.solana.seeker.seedvault

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

/**
 * Abstract interface for Seed Vault providers
 * Allows swapping between real and fake implementations
 */
interface SeedVaultProvider {
    
    /**
     * Check if the provider is available on this device
     */
    fun isAvailable(activity: Activity): Boolean
    
    /**
     * Get provider information for logging/debugging
     */
    fun getProviderInfo(): ProviderInfo
    
    /**
     * Request authorization to access seeds
     */
    suspend fun requestAuthorization(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): AuthResult
    
    /**
     * Get a public key for the specified derivation path
     */
    suspend fun getPublicKey(
        activity: Activity,
        derivationPath: ByteArray,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): PublicKeyResult
    
    /**
     * Sign a transaction with the specified derivation path
     */
    suspend fun signTransaction(
        activity: Activity,
        transactionBytes: ByteArray,
        derivationPath: ByteArray,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): SigningResult
    
    /**
     * Sign a message with the specified derivation path
     */
    suspend fun signMessage(
        activity: Activity,
        messageBytes: ByteArray,
        derivationPath: ByteArray,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): SigningResult
    
    /**
     * Handle activity results from Seed Vault operations
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?)
}

/**
 * Provider information for identification and debugging
 */
data class ProviderInfo(
    val name: String,
    val version: String,
    val isFake: Boolean,
    val description: String
)

/**
 * Result of authorization request
 */
sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
    object UserDenied : AuthResult()
    object NotAvailable : AuthResult()
}

/**
 * Result of public key request
 */
sealed class PublicKeyResult {
    data class Success(val publicKeyBytes: ByteArray, val base58: String) : PublicKeyResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Success
            if (!publicKeyBytes.contentEquals(other.publicKeyBytes)) return false
            if (base58 != other.base58) return false
            return true
        }
        
        override fun hashCode(): Int {
            var result = publicKeyBytes.contentHashCode()
            result = 31 * result + base58.hashCode()
            return result
        }
    }
    data class Error(val message: String) : PublicKeyResult()
    object UserDenied : PublicKeyResult()
}

/**
 * Result of signing request
 */
sealed class SigningResult {
    data class Success(val signatureBytes: ByteArray, val signedTransaction: ByteArray? = null) : SigningResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Success
            if (!signatureBytes.contentEquals(other.signatureBytes)) return false
            if (signedTransaction != null) {
                if (other.signedTransaction == null) return false
                if (!signedTransaction.contentEquals(other.signedTransaction)) return false
            } else if (other.signedTransaction != null) return false
            return true
        }
        
        override fun hashCode(): Int {
            var result = signatureBytes.contentHashCode()
            result = 31 * result + (signedTransaction?.contentHashCode() ?: 0)
            return result
        }
    }
    data class Error(val message: String) : SigningResult()
    object UserDenied : SigningResult()
}