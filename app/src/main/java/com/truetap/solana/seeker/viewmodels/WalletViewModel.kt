package com.truetap.solana.seeker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.truetap.solana.seeker.data.AuthState
import com.truetap.solana.seeker.data.SeedVaultInfo
import com.truetap.solana.seeker.data.WalletAccount
import com.truetap.solana.seeker.data.WalletResult
import com.truetap.solana.seeker.repositories.WalletRepository
import com.truetap.solana.seeker.services.SeedVaultService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val seedVaultService: SeedVaultService
) : ViewModel() {

    val authState: StateFlow<AuthState> = walletRepository.authState
    val isConnected = walletRepository.isConnected()
    val currentAccount = walletRepository.getCurrentAccount()

    private val _seedVaultInfo = MutableStateFlow<SeedVaultInfo?>(null)
    val seedVaultInfo: StateFlow<SeedVaultInfo?> = _seedVaultInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkSeedVaultAvailability()
        attemptSessionRestore()
    }

    fun connectWallet(
        activity: ComponentActivity,
        cluster: String = "devnet"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = walletRepository.connectAndAuthWallet(activity, cluster)) {
                is WalletResult.Success -> {
                    _errorMessage.value = null
                }
                is WalletResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            walletRepository.disconnect()
            _errorMessage.value = null
        }
    }

    fun signAuthMessage(
        activity: ComponentActivity,
        message: String = "Authenticate with True Tap"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            
            when (val result = walletRepository.signAuthMessage(activity, message)) {
                is WalletResult.Success -> {
                    _errorMessage.value = null
                }
                is WalletResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun retry(activity: ComponentActivity) {
        when (val currentState = authState.value) {
            is AuthState.Error -> {
                connectWallet(activity)
            }
            is AuthState.Idle -> {
                attemptSessionRestore()
            }
            else -> {
                // No retry needed for other states
            }
        }
    }

    private fun checkSeedVaultAvailability() {
        viewModelScope.launch {
            _seedVaultInfo.value = seedVaultService.getSeedVaultInfo()
        }
    }

    private fun attemptSessionRestore() {
        viewModelScope.launch {
            walletRepository.restoreSession()
        }
    }
}