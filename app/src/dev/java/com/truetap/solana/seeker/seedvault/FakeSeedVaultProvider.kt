package com.truetap.solana.seeker.seedvault

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.delay
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject

/**
 * Fake Seed Vault provider for development and testing
 * Simulates the Seed Vault interface without requiring real hardware
 */
class FakeSeedVaultProvider @Inject constructor() : SeedVaultProvider {
    
    companion object {
        private const val TAG = "FakeSeedVaultProvider"
        
        // Fake seed for consistent key generation during development
        private val FAKE_SEED = byteArrayOf(
            0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
            0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10,
            0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18,
            0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x20
        )
    }
    
    private var isAuthorized = false
    private val secureRandom = SecureRandom()
    
    override fun isAvailable(activity: Activity): Boolean {
        Log.d(TAG, "Fake Seed Vault is always available for development")
        return true
    }
    
    override fun getProviderInfo(): ProviderInfo {
        return ProviderInfo(
            name = "Fake Seed Vault",
            version = "Development",
            isFake = true,
            description = "Simulated Seed Vault for development and testing"
        )
    }
    
    override suspend fun requestAuthorization(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): AuthResult {
        Log.d(TAG, "Simulating authorization request...")
        
        // Simulate network/processing delay
        delay(1000)
        
        // For better UX testing, let's make it more reliable but add some realism
        try {
            // Simulate user interaction - in development, always approve after delay
            delay(500) // Additional simulation delay
            isAuthorized = true
            
            Log.d(TAG, "✅ Fake authorization granted successfully")
            return AuthResult.Success
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error in fake authorization", e)
            return AuthResult.Error("Authorization simulation failed: ${e.message}")
        }
    }
    
    override suspend fun getPublicKey(
        activity: Activity,
        derivationPath: ByteArray,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): PublicKeyResult {
        Log.d(TAG, "Generating fake public key for derivation path")
        
        if (!isAuthorized) {
            return PublicKeyResult.Error("Not authorized")
        }
        
        // Simulate processing delay
        delay(500)
        
        try {
            // Generate deterministic public key based on derivation path
            val publicKeyBytes = generateFakePublicKey(derivationPath)
            val base58 = encodeBase58(publicKeyBytes)
            
            Log.d(TAG, "Generated fake public key: $base58")
            return PublicKeyResult.Success(publicKeyBytes, base58)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate fake public key", e)
            return PublicKeyResult.Error("Key generation failed: ${e.message}")
        }
    }
    
    override suspend fun signTransaction(
        activity: Activity,
        transactionBytes: ByteArray,
        derivationPath: ByteArray,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): SigningResult {
        Log.d(TAG, "Simulating transaction signing...")
        
        if (!isAuthorized) {
            return SigningResult.Error("Not authorized")
        }
        
        // Simulate user confirmation and signing delay
        delay(1500)
        
        try {
            // Generate fake signature
            val signature = generateFakeSignature(transactionBytes, derivationPath)
            
            // For transactions, also return the "signed" transaction
            val signedTransaction = transactionBytes + signature.takeLast(8).toByteArray()
            
            Log.d(TAG, "Transaction signed with fake signature")
            return SigningResult.Success(signature, signedTransaction)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign transaction", e)
            return SigningResult.Error("Signing failed: ${e.message}")
        }
    }
    
    override suspend fun signMessage(
        activity: Activity,
        messageBytes: ByteArray,
        derivationPath: ByteArray,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): SigningResult {
        Log.d(TAG, "Simulating message signing...")
        
        if (!isAuthorized) {
            return SigningResult.Error("Not authorized")
        }
        
        // Simulate user confirmation and signing delay
        delay(1200)
        
        try {
            // Generate fake signature
            val signature = generateFakeSignature(messageBytes, derivationPath)
            
            Log.d(TAG, "Message signed with fake signature")
            return SigningResult.Success(signature)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign message", e)
            return SigningResult.Error("Signing failed: ${e.message}")
        }
    }
    
    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        // Fake provider doesn't use activity results since it simulates everything internally
        Log.d(TAG, "Fake provider received activity result (ignored)")
    }
    
    /**
     * Generate a deterministic fake public key based on the derivation path
     */
    private fun generateFakePublicKey(derivationPath: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(FAKE_SEED)
        digest.update(derivationPath)
        digest.update("public_key".toByteArray())
        
        // Take first 32 bytes for Ed25519 public key
        return digest.digest().take(32).toByteArray()
    }
    
    /**
     * Generate a fake signature for the given data and derivation path
     */
    private fun generateFakeSignature(data: ByteArray, derivationPath: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(FAKE_SEED)
        digest.update(derivationPath)
        digest.update(data)
        digest.update("signature".toByteArray())
        
        // Generate 64-byte signature (Ed25519 signature size)
        val hash1 = digest.digest()
        digest.reset()
        digest.update(hash1)
        digest.update("signature_part2".toByteArray())
        val hash2 = digest.digest()
        
        return hash1 + hash2.take(32).toByteArray()
    }
    
    /**
     * Simple Base58 encoding for fake public keys
     */
    private fun encodeBase58(bytes: ByteArray): String {
        val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        if (bytes.isEmpty()) return ""
        
        val input = bytes.copyOf()
        var zeros = 0
        while (zeros < input.size && input[zeros] == 0.toByte()) {
            zeros++
        }
        
        val encoded = StringBuilder()
        var i = zeros
        while (i < input.size) {
            var carry = input[i].toInt() and 0xFF
            var j = 0
            while (carry != 0 || j < encoded.length) {
                if (j >= encoded.length) {
                    encoded.append('1')
                }
                carry += (alphabet.indexOf(encoded[j]) * 256)
                encoded[j] = alphabet[carry % 58]
                carry /= 58
                j++
            }
            i++
        }
        
        repeat(zeros) {
            encoded.insert(0, '1')
        }
        
        return encoded.reverse().toString()
    }
    
    /**
     * Reset authorization state (useful for testing)
     */
    fun resetAuthorization() {
        Log.d(TAG, "Resetting fake authorization state")
        isAuthorized = false
    }
    
    /**
     * Simulate user denying authorization (for testing error flows)
     */
    suspend fun simulateAuthorizationDenial(): AuthResult {
        Log.d(TAG, "Simulating authorization denial")
        delay(1000)
        return AuthResult.UserDenied
    }
    
    /**
     * Get development statistics
     */
    fun getDevelopmentStats(): Map<String, Any> {
        return mapOf(
            "provider" to "fake",
            "authorized" to isAuthorized,
            "version" to "dev-1.0",
            "capabilities" to listOf("auth", "public_key", "sign_transaction", "sign_message")
        )
    }
}