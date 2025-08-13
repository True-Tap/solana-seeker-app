package com.truetap.solana.seeker.seedvault

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing Seed Vault state in Jetpack Compose
 */
@HiltViewModel
class SeedVaultViewModel @Inject constructor(
    private val seedVaultManager: SeedVaultManager
) : ViewModel() {
    
    // Expose all state from SeedVaultManager
    val publicKey: StateFlow<String?> = seedVaultManager.publicKey
    val isAuthorized: StateFlow<Boolean> = seedVaultManager.isAuthorized
    val error: StateFlow<String?> = seedVaultManager.error
    val isLoading: StateFlow<Boolean> = seedVaultManager.isLoading
    val deviceValidation: StateFlow<SeekerDeviceValidator.DeviceValidationResult?> = seedVaultManager.deviceValidation
    val providerInfo: StateFlow<ProviderInfo?> = seedVaultManager.providerInfo
    
    // Additional UI state
    private val _walletConnected = MutableStateFlow(false)
    val walletConnected: StateFlow<Boolean> = _walletConnected.asStateFlow()
    
    private val _lastSignedTransaction = MutableStateFlow<String?>(null)
    val lastSignedTransaction: StateFlow<String?> = _lastSignedTransaction.asStateFlow()
    
    /**
     * Initialize Seed Vault connection
     */
    fun initializeSeedVault(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>
    ) {
        android.util.Log.d("SeedVaultViewModel", "initializeSeedVault called")
        viewModelScope.launch {
            try {
                android.util.Log.d("SeedVaultViewModel", "Checking if device supports Seed Vault...")
                // For development with FakeSeedVaultProvider, skip device validation
                val deviceSupported = seedVaultManager.isDeviceSupported(activity)
                android.util.Log.d("SeedVaultViewModel", "Device supports Seed Vault: $deviceSupported")
                
                // Note: We'll let the SeedVaultManager handle provider availability
                // The fake provider should be available even on non-Seeker devices
                
                android.util.Log.d("SeedVaultViewModel", "Device supports Seed Vault, calling initializeAndAuthorize...")
                seedVaultManager.initializeAndAuthorize(activity, activityResultLauncher)
                android.util.Log.d("SeedVaultViewModel", "initializeAndAuthorize completed")
            } catch (e: Exception) {
                android.util.Log.e("SeedVaultViewModel", "Exception in initializeSeedVault: ${e.message}", e)
                // Error handling is managed by SeedVaultManager
            }
        }
    }
    
    /**
     * Fetch user's public key
     */
    fun fetchPublicKey(
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>,
        accountIndex: Int = 0
    ) {
        viewModelScope.launch {
            val publicKey = seedVaultManager.getPublicKey(activity, activityResultLauncher, accountIndex)
            _walletConnected.value = publicKey != null
        }
    }
    
    /**
     * Sign a transaction
     */
    fun signTransaction(
        transactionBytes: ByteArray,
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>
    ) {
        viewModelScope.launch {
            val signedTransaction = seedVaultManager.signTransaction(
                transactionBytes, 
                activity, 
                activityResultLauncher
            )
            
            signedTransaction?.let { signed ->
                _lastSignedTransaction.value = "Transaction signed successfully"
            }
        }
    }
    
    /**
     * Sign a message
     */
    fun signMessage(
        message: String,
        activity: Activity,
        activityResultLauncher: ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>
    ) {
        viewModelScope.launch {
            val messageBytes = message.toByteArray(Charsets.UTF_8)
            seedVaultManager.signMessage(messageBytes, activity, activityResultLauncher)
        }
    }
    
    /**
     * Handle activity results
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        seedVaultManager.handleActivityResult(requestCode, resultCode, data)
    }
    
    /**
     * Clear error state
     */
    fun clearError() {
        seedVaultManager.clearError()
    }
    
    /**
     * Disconnect wallet
     */
    fun disconnectWallet() {
        _walletConnected.value = false
        _lastSignedTransaction.value = null
    }
}