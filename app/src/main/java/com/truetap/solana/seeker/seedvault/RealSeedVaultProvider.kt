package com.truetap.solana.seeker.seedvault

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.solanamobile.seedvault.WalletContractV1
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Real Seed Vault provider implementation using WalletContractV1
 */
class RealSeedVaultProvider @Inject constructor() : SeedVaultProvider {
    
    companion object {
        private const val TAG = "RealSeedVaultProvider"
        private const val REQUEST_CODE_AUTHORIZE_SEED = 1001
        private const val REQUEST_CODE_GET_PUBLIC_KEY = 1002
        private const val REQUEST_CODE_SIGN_TRANSACTION = 1003
        private const val REQUEST_CODE_SIGN_MESSAGE = 1004
    }
    
    private var pendingContinuation: kotlin.coroutines.Continuation<Any>? = null
    
    override fun isAvailable(activity: Activity): Boolean {
        return try {
            val intent = Intent(WalletContractV1.ACTION_AUTHORIZE_SEED_ACCESS)
            val resolveInfo = activity.packageManager.resolveActivity(intent, 0)
            val available = resolveInfo != null
            
            Log.d(TAG, "Real Seed Vault availability: $available")
            available
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Seed Vault availability", e)
            false
        }
    }
    
    override fun getProviderInfo(): ProviderInfo {
        return ProviderInfo(
            name = "Real Seed Vault",
            version = "Production",
            isFake = false,
            description = "Hardware-backed Solana Seeker Seed Vault"
        )
    }
    
    override suspend fun requestAuthorization(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ): AuthResult = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "Requesting Seed Vault authorization")
            
            val authIntent = Intent(WalletContractV1.ACTION_AUTHORIZE_SEED_ACCESS).apply {
                data = Uri.parse("https://solanamobile.com")
            }
            
            val resolveInfo = activity.packageManager.resolveActivity(authIntent, 0)
            if (resolveInfo == null) {
                continuation.resume(AuthResult.NotAvailable)
                return@suspendCancellableCoroutine
            }
            
            @Suppress("UNCHECKED_CAST")
            pendingContinuation = continuation as kotlin.coroutines.Continuation<Any>
            activityResultLauncher.launch(authIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Authorization request failed", e)
            continuation.resume(AuthResult.Error("Authorization failed: ${e.message}"))
        }
    }
    
    override suspend fun getPublicKey(
        activity: Activity,
        derivationPath: ByteArray,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ): PublicKeyResult = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "Requesting public key")
            
            val getPublicKeyIntent = Intent(WalletContractV1.ACTION_GET_PUBLIC_KEY).apply {
                data = Uri.parse("https://solanamobile.com")
                putExtra("derivation_path", derivationPath)
            }
            
            val resolveInfo = activity.packageManager.resolveActivity(getPublicKeyIntent, 0)
            if (resolveInfo == null) {
                continuation.resume(PublicKeyResult.Error("Seed Vault not available"))
                return@suspendCancellableCoroutine
            }
            
            @Suppress("UNCHECKED_CAST")
            pendingContinuation = continuation as kotlin.coroutines.Continuation<Any>
            activityResultLauncher.launch(getPublicKeyIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Public key request failed", e)
            continuation.resume(PublicKeyResult.Error("Public key request failed: ${e.message}"))
        }
    }
    
    override suspend fun signTransaction(
        activity: Activity,
        transactionBytes: ByteArray,
        derivationPath: ByteArray,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ): SigningResult = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "Requesting transaction signing")
            
            val signTransactionIntent = Intent(WalletContractV1.ACTION_SIGN_TRANSACTION).apply {
                data = Uri.parse("https://solanamobile.com")
                putExtra("transaction", transactionBytes)
                putExtra("derivation_path", derivationPath)
            }
            
            val resolveInfo = activity.packageManager.resolveActivity(signTransactionIntent, 0)
            if (resolveInfo == null) {
                continuation.resume(SigningResult.Error("Seed Vault not available"))
                return@suspendCancellableCoroutine
            }
            
            @Suppress("UNCHECKED_CAST")
            pendingContinuation = continuation as kotlin.coroutines.Continuation<Any>
            activityResultLauncher.launch(signTransactionIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Transaction signing request failed", e)
            continuation.resume(SigningResult.Error("Transaction signing failed: ${e.message}"))
        }
    }
    
    override suspend fun signMessage(
        activity: Activity,
        messageBytes: ByteArray,
        derivationPath: ByteArray,
        activityResultLauncher: ActivityResultLauncher<Intent>
    ): SigningResult = suspendCancellableCoroutine { continuation ->
        try {
            Log.d(TAG, "Requesting message signing")
            
            val signMessageIntent = Intent(WalletContractV1.ACTION_SIGN_MESSAGE).apply {
                data = Uri.parse("https://solanamobile.com")
                putExtra("message", messageBytes)
                putExtra("derivation_path", derivationPath)
            }
            
            val resolveInfo = activity.packageManager.resolveActivity(signMessageIntent, 0)
            if (resolveInfo == null) {
                continuation.resume(SigningResult.Error("Seed Vault not available"))
                return@suspendCancellableCoroutine
            }
            
            @Suppress("UNCHECKED_CAST")
            pendingContinuation = continuation as kotlin.coroutines.Continuation<Any>
            activityResultLauncher.launch(signMessageIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Message signing request failed", e)
            continuation.resume(SigningResult.Error("Message signing failed: ${e.message}"))
        }
    }
    
    override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val continuation = pendingContinuation
        pendingContinuation = null
        
        when (requestCode) {
            REQUEST_CODE_AUTHORIZE_SEED -> {
                val result = if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "Authorization successful")
                    AuthResult.Success
                } else {
                    Log.w(TAG, "Authorization denied or cancelled")
                    AuthResult.UserDenied
                }
                continuation?.resume(result)
            }
            
            REQUEST_CODE_GET_PUBLIC_KEY -> {
                val result = if (resultCode == Activity.RESULT_OK && data != null) {
                    val publicKeyBytes = data.getByteArrayExtra("public_key")
                    if (publicKeyBytes != null) {
                        val base58 = encodeBase58(publicKeyBytes)
                        Log.d(TAG, "Public key retrieved successfully")
                        PublicKeyResult.Success(publicKeyBytes, base58)
                    } else {
                        Log.e(TAG, "Public key data missing from result")
                        PublicKeyResult.Error("Public key data missing")
                    }
                } else {
                    Log.w(TAG, "Public key request denied or cancelled")
                    PublicKeyResult.UserDenied
                }
                continuation?.resume(result)
            }
            
            REQUEST_CODE_SIGN_TRANSACTION -> {
                val result = if (resultCode == Activity.RESULT_OK && data != null) {
                    val signedTransaction = data.getByteArrayExtra("signed_transaction")
                    val signature = data.getByteArrayExtra("signature")
                    
                    when {
                        signature != null -> {
                            Log.d(TAG, "Transaction signed successfully")
                            SigningResult.Success(signature, signedTransaction)
                        }
                        else -> {
                            Log.e(TAG, "Signature data missing from result")
                            SigningResult.Error("Signature data missing")
                        }
                    }
                } else {
                    Log.w(TAG, "Transaction signing denied or cancelled")
                    SigningResult.UserDenied
                }
                continuation?.resume(result)
            }
            
            REQUEST_CODE_SIGN_MESSAGE -> {
                val result = if (resultCode == Activity.RESULT_OK && data != null) {
                    val signature = data.getByteArrayExtra("signature")
                    
                    if (signature != null) {
                        Log.d(TAG, "Message signed successfully")
                        SigningResult.Success(signature)
                    } else {
                        Log.e(TAG, "Signature data missing from result")
                        SigningResult.Error("Signature data missing")
                    }
                } else {
                    Log.w(TAG, "Message signing denied or cancelled")
                    SigningResult.UserDenied
                }
                continuation?.resume(result)
            }
        }
    }
    
    private fun encodeBase58(bytes: ByteArray): String {
        // Simple Base58 encoding - in production use a proper library
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
}