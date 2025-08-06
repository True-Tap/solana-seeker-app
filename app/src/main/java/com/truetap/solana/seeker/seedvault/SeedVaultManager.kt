package com.truetap.solana.seeker.seedvault

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solanamobile.seedvault.WalletContractV1
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager class for interacting with the Solana Seeker Seed Vault
 * Provides secure access to user's Solana keys and transaction signing
 */
@Singleton
class SeedVaultManager @Inject constructor(
    private val seedVaultProvider: SeedVaultProvider,
    private val deviceValidator: SeekerDeviceValidator
) {
    
    private val _publicKey = MutableStateFlow<String?>(null)
    val publicKey: StateFlow<String?> = _publicKey.asStateFlow()
    
    private val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _deviceValidation = MutableStateFlow<SeekerDeviceValidator.DeviceValidationResult?>(null)
    val deviceValidation: StateFlow<SeekerDeviceValidator.DeviceValidationResult?> = _deviceValidation.asStateFlow()
    
    private val _providerInfo = MutableStateFlow<ProviderInfo?>(null)
    val providerInfo: StateFlow<ProviderInfo?> = _providerInfo.asStateFlow()
    
    companion object {
        private const val REQUEST_CODE_AUTHORIZE_SEED = 1001
        private const val REQUEST_CODE_GET_PUBLIC_KEY = 1002
        private const val REQUEST_CODE_SIGN_TRANSACTION = 1003
        
        // Default derivation path for Solana (m/44'/501'/0'/0')
        private val DEFAULT_DERIVATION_PATH = byteArrayOf(
            0x80.toByte(), 0x00, 0x00, 0x2C, // 44'
            0x80.toByte(), 0x00, 0x01, 0xF5.toByte(), // 501' (Solana)
            0x80.toByte(), 0x00, 0x00, 0x00, // 0'
            0x00, 0x00, 0x00, 0x00           // 0'
        )
    }
    
    /**
     * Initialize connection to Seed Vault and authorize access
     */
    suspend fun initializeAndAuthorize(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        _isLoading.value = true
        _error.value = null
        
        try {
            // Get provider information
            val providerInfo = seedVaultProvider.getProviderInfo()
            _providerInfo.value = providerInfo
            
            Log.d("SeedVault", "Using provider: ${providerInfo.name} v${providerInfo.version} (fake: ${providerInfo.isFake})")
            
            // Validate device and Seed Vault capability
            val validation = deviceValidator.validateDevice(activity)
            _deviceValidation.value = validation
            
            // Check if provider is available
            if (!seedVaultProvider.isAvailable(activity)) {
                _error.value = "Seed Vault provider not available: ${providerInfo.description}"
                _isLoading.value = false
                return
            }
            
            Log.d("SeedVault", "Provider available: ${providerInfo.name}")
            
            // Request authorization using the provider
            val authResult = seedVaultProvider.requestAuthorization(activity, activityResultLauncher)
            when (authResult) {
                is AuthResult.Success -> {
                    _isAuthorized.value = true
                    _error.value = null
                    
                    // After successful authorization, automatically fetch the public key
                    Log.d("SeedVault", "✅ Authorization successful, fetching public key...")
                    val publicKeyResult = getPublicKey(activity, activityResultLauncher, 0)
                    if (publicKeyResult == null) {
                        _error.value = "Failed to retrieve public key after authorization"
                    }
                }
                is AuthResult.Error -> {
                    _error.value = authResult.message
                }
                is AuthResult.UserDenied -> {
                    _error.value = "Authorization denied by user"
                }
                is AuthResult.NotAvailable -> {
                    _error.value = "Authorization not available"
                }
            }
            
        } catch (e: Exception) {
            _error.value = "Failed to initialize Seed Vault: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Request authorization to access seeds from the Seed Vault
     */
    private suspend fun requestSeedAuthorization(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        try {
            // Create intent using WalletContractV1
            val authIntent = Intent(WalletContractV1.ACTION_AUTHORIZE_SEED_ACCESS).apply {
                data = Uri.parse("https://solanamobile.com")
            }
            
            // Check if Seed Vault is available
            val resolveInfo = activity.packageManager.resolveActivity(authIntent, 0)
            if (resolveInfo == null) {
                throw Exception("Seed Vault not available on this device")
            }
            
            activity.startActivityForResult(authIntent, REQUEST_CODE_AUTHORIZE_SEED)
            
        } catch (e: Exception) {
            _error.value = "Authorization failed: ${e.message}"
            _isLoading.value = false
        }
    }
    
    /**
     * Fetch the user's public key from the Seed Vault
     * @param accountIndex The account index (default: 0)
     * @return Base58 encoded public key string
     */
    suspend fun getPublicKey(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        accountIndex: Int = 0
    ): String? {
        _isLoading.value = true
        _error.value = null
        
        return try {
            // Create derivation path for the specific account
            val derivationPath = createDerivationPath(accountIndex)
            
            // Use provider to get public key
            val result = seedVaultProvider.getPublicKey(activity, derivationPath, activityResultLauncher)
            
            when (result) {
                is PublicKeyResult.Success -> {
                    _publicKey.value = result.base58
                    Log.d("SeedVault", "Public key retrieved: ${result.base58}")
                    result.base58
                }
                is PublicKeyResult.Error -> {
                    _error.value = result.message
                    null
                }
                is PublicKeyResult.UserDenied -> {
                    _error.value = "Public key request denied by user"
                    null
                }
            }
        } catch (e: Exception) {
            _error.value = "Public key retrieval failed: ${e.message}"
            null
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Sign a transaction using the Seed Vault
     * @param transactionBytes The serialized transaction to sign
     * @param activity The calling activity
     * @return Signed transaction bytes
     */
    suspend fun signTransaction(
        transactionBytes: ByteArray,
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): ByteArray? {
        _isLoading.value = true
        _error.value = null
        
        return try {
            // Create intent to sign transaction
            val signTransactionIntent = Intent(WalletContractV1.ACTION_SIGN_TRANSACTION).apply {
                data = Uri.parse("https://solanamobile.com")
                putExtra("transaction", transactionBytes)
                putExtra("derivation_path", DEFAULT_DERIVATION_PATH)
            }
            
            // Check if Seed Vault is available
            val resolveInfo = activity.packageManager.resolveActivity(signTransactionIntent, 0)
            if (resolveInfo == null) {
                throw Exception("Seed Vault not available on this device")
            }
            
            activity.startActivityForResult(signTransactionIntent, REQUEST_CODE_SIGN_TRANSACTION)
            
            null // Will be set in handleActivityResult
        } catch (e: Exception) {
            _error.value = "Signing error: ${e.message}"
            _isLoading.value = false
            null
        }
    }
    
    /**
     * Sign a message using the Seed Vault
     * @param messageBytes The message to sign
     * @param activity The calling activity
     * @return Message signature
     */
    suspend fun signMessage(
        messageBytes: ByteArray,
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): ByteArray? {
        _isLoading.value = true
        _error.value = null
        
        return try {
            // Create intent to sign message
            val signMessageIntent = Intent(WalletContractV1.ACTION_SIGN_MESSAGE).apply {
                data = Uri.parse("https://solanamobile.com")
                putExtra("message", messageBytes)
                putExtra("derivation_path", DEFAULT_DERIVATION_PATH)
            }
            
            // Check if Seed Vault is available
            val resolveInfo = activity.packageManager.resolveActivity(signMessageIntent, 0)
            if (resolveInfo == null) {
                throw Exception("Seed Vault not available on this device")
            }
            
            activity.startActivityForResult(signMessageIntent, REQUEST_CODE_SIGN_TRANSACTION)
            
            null // Will be set in handleActivityResult
        } catch (e: Exception) {
            _error.value = "Message signing error: ${e.message}"
            _isLoading.value = false
            null
        }
    }
    
    /**
     * Handle activity results from Seed Vault operations
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Delegate to the provider
        seedVaultProvider.handleActivityResult(requestCode, resultCode, data)
    }
    
    /**
     * Clear any stored error state
     */
    fun clearError() {
        _error.value = null
    }
    
    /**
     * Check if the device supports Seed Vault
     */
    fun isDeviceSupported(activity: Activity): Boolean {
        return try {
            val seedVaultIntent = Intent(WalletContractV1.ACTION_AUTHORIZE_SEED_ACCESS)
            activity.packageManager.resolveActivity(seedVaultIntent, 0) != null
        } catch (e: Exception) {
            false
        }
    }
    
    // Helper methods
    
    private fun createDerivationPath(accountIndex: Int): ByteArray {
        // Create BIP-44 derivation path: m/44'/501'/accountIndex'/0'
        return byteArrayOf(
            0x80.toByte(), 0x00, 0x00, 0x2C, // 44'
            0x80.toByte(), 0x00, 0x01, 0xF5.toByte(), // 501' (Solana)
            0x80.toByte(), 0x00, 0x00, accountIndex.toByte(), // accountIndex'
            0x00, 0x00, 0x00, 0x00           // 0'
        )
    }
    
    private fun encodeBase58(bytes: ByteArray): String {
        // Simple Base58 encoding implementation
        // Note: In production, use a proper Base58 library like bitcoinj
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
        
        // Add leading zeros
        repeat(zeros) {
            encoded.insert(0, '1')
        }
        
        return encoded.reverse().toString()
    }
}