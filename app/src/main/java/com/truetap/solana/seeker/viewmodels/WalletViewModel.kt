package com.truetap.solana.seeker.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.truetap.solana.seeker.BuildConfig
import com.truetap.solana.seeker.data.AuthState
import com.truetap.solana.seeker.data.ConnectionResult
import com.truetap.solana.seeker.data.SeedVaultInfo
import com.truetap.solana.seeker.data.WalletAccount
import com.truetap.solana.seeker.data.WalletResult
import com.truetap.solana.seeker.data.WalletState
import com.truetap.solana.seeker.repositories.WalletRepository
import com.truetap.solana.seeker.repositories.TrueTapContact
import com.truetap.solana.seeker.repositories.TransactionResult
import com.truetap.solana.seeker.services.SeedVaultService
import com.truetap.solana.seeker.services.MwaWalletConnector
import com.truetap.solana.seeker.services.SeedVaultWalletConnector
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import android.app.Activity
import com.truetap.solana.seeker.services.NftService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val seedVaultService: SeedVaultService,
    val nftService: NftService,
    private val mwaWalletConnector: MwaWalletConnector,
    private val seedVaultWalletConnector: SeedVaultWalletConnector
) : ViewModel() {

    val authState: StateFlow<AuthState> = walletRepository.authState
    val walletState: StateFlow<WalletState> = walletRepository.walletState

    private val _seedVaultInfo = MutableStateFlow<SeedVaultInfo?>(null)
    val seedVaultInfo: StateFlow<SeedVaultInfo?> = _seedVaultInfo.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // TrueTap specific state
    private val _trueTapState = MutableStateFlow(TrueTapState())
    val trueTapState: StateFlow<TrueTapState> = _trueTapState.asStateFlow()
    
    private val _trueTapContacts = MutableStateFlow<List<TrueTapContact>>(emptyList())
    val trueTapContacts: StateFlow<List<TrueTapContact>> = _trueTapContacts.asStateFlow()
    
    private val _recentActivity = MutableStateFlow<List<TransactionResult>>(emptyList())
    val recentActivity: StateFlow<List<TransactionResult>> = _recentActivity.asStateFlow()
    
    val balance: StateFlow<Double> = flow {
        while (true) {
            emit(walletRepository.getBalance())
            delay(5000) // Refresh every 5 seconds in mock
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 42.5)

    init {
        checkSeedVaultAvailability()
        attemptSessionRestore()
        loadTrueTapContacts()
        startAutoRefresh()
    }

    fun saveWalletConnection(
        connectionResult: ConnectionResult.Success,
        cluster: String = if (BuildConfig.DEBUG) "devnet" else "mainnet-beta"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = walletRepository.saveConnectionResult(connectionResult, cluster)) {
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
            walletRepository.disconnectWallet()
            _errorMessage.value = null
        }
    }

    fun signAuthMessage(
        activity: android.app.Activity?,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>?,
        activityResultSender: ActivityResultSender?,
        message: String = "Authenticate with True Tap"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = walletRepository.signAuthMessage(
                activity = activity as? androidx.activity.ComponentActivity,
                activityResultLauncher = activityResultLauncher,
                activityResultSender = activityResultSender,
                message = message
            )
            when (result) {
                is WalletResult.Success -> _errorMessage.value = null
                is WalletResult.Error -> _errorMessage.value = result.message
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
    
    fun refreshWalletData() {
        viewModelScope.launch {
            walletRepository.refreshWalletData()
        }
    }
    
    fun connectWallet() {
        // Alias for connect() to maintain compatibility
        connect()
    }
    
    fun connect() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            
            try {
                // Since connect() doesn't exist in the repository, 
                // we'll just restore the session or clear the loading state
                walletRepository.restoreSession()
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to connect wallet"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Connect to a wallet via the appropriate connector (MWA or Seed Vault) and persist session on success.
     */
    suspend fun connectWithWallet(
        walletType: com.truetap.solana.seeker.data.WalletType,
        activity: Activity?,
        activityResultLauncher: androidx.activity.result.ActivityResultLauncher<androidx.activity.result.IntentSenderRequest>?,
        activityResultSender: ActivityResultSender?
    ): com.truetap.solana.seeker.data.ConnectionResult {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val connector = when (walletType) {
                com.truetap.solana.seeker.data.WalletType.SOLANA_SEEKER -> seedVaultWalletConnector
                else -> mwaWalletConnector
            }

            val result = connector.connect(
                com.truetap.solana.seeker.data.ConnectParams(
                    activityResultSender = activityResultSender,
                    activity = activity,
                    activityResultLauncher = activityResultLauncher
                )
            )

            if (result is com.truetap.solana.seeker.data.ConnectionResult.Success) {
                // Persist and update state
                saveWalletConnection(result)
            } else if (result is com.truetap.solana.seeker.data.ConnectionResult.Failure) {
                _errorMessage.value = result.error
            }
            result
        } catch (e: Exception) {
            _errorMessage.value = e.message
            com.truetap.solana.seeker.data.ConnectionResult.Failure(
                error = e.message ?: "Wallet connection failed",
                exception = e,
                walletType = walletType
            )
        } finally {
            _isLoading.value = false
        }
    }
    
    /**
     * Check if connected wallet has Genesis NFT
     */
    fun checkGenesisNFT(): StateFlow<Boolean> = flow {
        val currentWallet = walletState.value.account?.publicKey
        if (currentWallet != null) {
            emit(nftService.hasGenesisNFT(currentWallet))
        } else {
            emit(false)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    
    /**
     * Get Genesis NFT tier for connected wallet
     */
    fun getGenesisNFTTier(): StateFlow<String> = flow {
        val currentWallet = walletState.value.account?.publicKey
        if (currentWallet != null) {
            val tier = nftService.getGenesisNFTTier(currentWallet)
            emit(tier.name)
        } else {
            emit("NONE")
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), "NONE")
    
    /**
     * Get all Genesis NFTs for connected wallet
     */
    fun getGenesisNFTs() = flow {
        val currentWallet = walletState.value.account?.publicKey
        if (currentWallet != null) {
            emit(nftService.getGenesisNFTs(currentWallet))
        } else {
            emit(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun retry() {
        when (val currentState = authState.value) {
            is AuthState.Error -> {
                // Clear error state - connection retry handled by PairingScreen
                clearError()
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
    
    // Auto-refresh live wallet data when connected (Phase 3 prep)
    private var autoRefreshJob: Job? = null
    private fun startAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                try {
                    val account = walletState.value.account
                    if (account != null) {
                        walletRepository.refreshWalletData()
                    }
                } catch (_: Exception) {
                }
                delay(30_000)
            }
        }
    }
    
    // TrueTap specific methods
    private fun loadTrueTapContacts() {
        viewModelScope.launch {
            _trueTapContacts.value = walletRepository.getTrueTapContacts()
        }
    }
    
    fun selectRecipient(contact: TrueTapContact) {
        _trueTapState.update { it.copy(selectedRecipient = contact) }
    }
    
    fun setAmount(amount: Double, emoji: String) {
        _trueTapState.update { it.copy(amount = amount, emojiMessage = emoji) }
    }
    
    fun executeTrueTap() {
        viewModelScope.launch {
            val state = _trueTapState.value
            val recipient = state.selectedRecipient ?: return@launch
            
            // Prevent concurrent executions
            if (state.isLoading) {
                return@launch
            }
            
            _trueTapState.update { it.copy(isLoading = true) }
            
            val result = walletRepository.sendTransaction(
                toAddress = recipient.address,
                amount = state.amount,
                message = state.emojiMessage
            )
            
            result.onSuccess { txn ->
                _recentActivity.update { it + txn }
                _trueTapState.update { 
                    it.copy(
                        isLoading = false,
                        lastTransaction = txn
                    )
                }
            }.onFailure { error ->
                _trueTapState.update { 
                    it.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            }
        }
    }
    
    fun resetTrueTap() {
        _trueTapState.value = TrueTapState()
    }
}

data class TrueTapState(
    val selectedRecipient: TrueTapContact? = null,
    val amount: Double = 0.0,
    val emojiMessage: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastTransaction: TransactionResult? = null
)