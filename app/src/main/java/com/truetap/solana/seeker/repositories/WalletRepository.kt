package com.truetap.solana.seeker.repositories

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.truetap.solana.seeker.BuildConfig
import com.truetap.solana.seeker.data.*
import com.truetap.solana.seeker.services.SeedVaultService
import com.truetap.solana.seeker.services.SolanaService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.random.Random
import com.truetap.solana.seeker.data.MockData

private val Context.dataStore by preferencesDataStore(name = "wallet_prefs")

@Singleton
class WalletRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val seedVaultService: SeedVaultService,
    private val solanaService: SolanaService,
    private val mockData: MockData
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _walletState = MutableStateFlow(WalletState(null, null))
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    companion object {
        private val WALLET_PUBLIC_KEY = stringPreferencesKey("wallet_public_key")
        private val WALLET_CLUSTER = stringPreferencesKey("wallet_cluster")
        private val WALLET_LABEL = stringPreferencesKey("wallet_label")
        private val WALLET_TYPE = stringPreferencesKey("wallet_type")
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }
    
    /**
     * Get the current connected wallet address
     */
    suspend fun getCurrentWalletAddress(): String? {
        return walletState.value.account?.publicKey
    }
    

    suspend fun saveConnectionResult(
        connectionResult: ConnectionResult.Success,
        cluster: String = if (BuildConfig.DEBUG) "devnet" else "mainnet-beta"
    ): WalletResult<WalletAccount> {
        return try {
            // Create WalletAccount from ConnectionResult
            val account = WalletAccount(
                publicKey = connectionResult.publicKey,
                cluster = cluster,
                accountLabel = connectionResult.accountLabel,
                walletType = connectionResult.walletType
            )
            
            // Generate auth token for session
            val authToken = "${connectionResult.publicKey}_${System.currentTimeMillis()}"
            
            // Save session data with wallet type
            saveSessionWithWalletType(account, authToken, connectionResult.walletType.id)
            
            // Update auth state
            _authState.value = AuthState.Connected(account)
            
            // Update wallet state and fetch data
            _walletState.value = _walletState.value.copy(
                account = account,
                isLoading = false,
                error = null
            )
            
            // Fetch wallet data in background
            fetchWalletData(account.publicKey)
            
            WalletResult.Success(account)
        } catch (e: Exception) {
            val errorMsg = "Failed to save wallet connection: ${e.message}"
            _authState.value = AuthState.Error(errorMsg)
            WalletResult.Error(e, errorMsg)
        }
    }

    suspend fun restoreSession(): WalletResult<WalletAccount> {
        return try {
            val prefs = context.dataStore.data.first()
            val publicKey = prefs[WALLET_PUBLIC_KEY]
            val cluster = prefs[WALLET_CLUSTER]
            val label = prefs[WALLET_LABEL]
            val walletType = prefs[WALLET_TYPE]
            val authToken = prefs[AUTH_TOKEN]

            if (publicKey != null && cluster != null && authToken != null) {
                val walletTypeEnum = walletType?.let { WalletType.fromId(it) }
                val account = WalletAccount(publicKey, cluster, label, walletTypeEnum)
                _authState.value = AuthState.Connected(account)
                
                // Update wallet state and fetch data
                _walletState.value = _walletState.value.copy(
                    account = account,
                    isLoading = false,
                    error = null
                )
                
                // Fetch wallet data in background
                fetchWalletData(account.publicKey)
                
                WalletResult.Success(account)
            } else {
                _authState.value = AuthState.Idle
                WalletResult.Error(
                    RuntimeException("No saved session found"),
                    "No saved session found"
                )
            }
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Failed to restore session: ${e.message}")
            WalletResult.Error(e, "Failed to restore session")
        }
    }

    suspend fun disconnectWallet() {
        try {
            context.dataStore.edit { prefs ->
                prefs.remove(WALLET_PUBLIC_KEY)
                prefs.remove(WALLET_CLUSTER)
                prefs.remove(WALLET_LABEL)
                prefs.remove(WALLET_TYPE)
                prefs.remove(AUTH_TOKEN)
            }
            _authState.value = AuthState.Idle
            _walletState.value = WalletState(null, null)
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Failed to disconnect: ${e.message}")
        }
    }
    
    /**
     * Fetch all wallet data (balance, NFTs, transactions)
     */
    suspend fun fetchWalletData(publicKey: String) {
        try {
            _walletState.value = _walletState.value.copy(isLoading = true)
            
            // Fetch balance
            val balanceResult = solanaService.getBalance(publicKey)
            val balance = when (balanceResult) {
                is WalletResult.Success -> balanceResult.data
                is WalletResult.Error -> {
                    _walletState.value = _walletState.value.copy(
                        isLoading = false,
                        error = "Failed to fetch balance: ${balanceResult.message}"
                    )
                    return
                }
            }
            
            // Fetch token balances
            val tokenBalancesResult = solanaService.getTokenBalances(publicKey)
            val tokenBalances = when (tokenBalancesResult) {
                is WalletResult.Success -> tokenBalancesResult.data
                is WalletResult.Error -> emptyList()
            }
            
            // Update balance with token balances
            val updatedBalance = balance.copy(tokenBalances = tokenBalances)
            
            // Fetch NFTs
            val nftsResult = solanaService.getNFTs(publicKey)
            val nfts = when (nftsResult) {
                is WalletResult.Success -> nftsResult.data
                is WalletResult.Error -> emptyList()
            }
            
            // Fetch recent transactions
            val transactionsResult = solanaService.getTransactionHistory(publicKey, 10)
            val transactions = when (transactionsResult) {
                is WalletResult.Success -> transactionsResult.data
                is WalletResult.Error -> emptyList()
            }
            
            // Update wallet state with all data
            _walletState.value = _walletState.value.copy(
                balance = updatedBalance,
                nfts = nfts,
                transactions = transactions,
                isLoading = false,
                lastUpdated = System.currentTimeMillis(),
                error = null
            )
            
        } catch (e: Exception) {
            _walletState.value = _walletState.value.copy(
                isLoading = false,
                error = "Failed to fetch wallet data: ${e.message}"
            )
        }
    }
    
    /**
     * Refresh wallet data
     */
    suspend fun refreshWalletData() {
        val currentAccount = _walletState.value.account
        if (currentAccount != null) {
            fetchWalletData(currentAccount.publicKey)
        }
    }

    private suspend fun saveSessionWithWalletType(
        account: WalletAccount, 
        authToken: String, 
        walletTypeId: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[WALLET_PUBLIC_KEY] = account.publicKey
            prefs[WALLET_CLUSTER] = account.cluster
            prefs[WALLET_LABEL] = account.accountLabel ?: ""
            prefs[WALLET_TYPE] = walletTypeId
            prefs[AUTH_TOKEN] = authToken
        }
    }
    
    // TrueTap specific implementation
    private val isMockMode = BuildConfig.DEBUG // Use mock data only in debug builds
    
    suspend fun getTrueTapContacts(): List<TrueTapContact> {
        return if (isMockMode) {
            // Use centralized MockData for consistent contacts across the app
            val dataContacts = mockData.getDataContacts()
            // Convert first 4 contacts to TrueTapContacts format for TrueTap button
            dataContacts.take(4).map { contact ->
                TrueTapContact(
                    id = contact.id,
                    name = contact.name,
                    address = contact.walletAddress,
                    isMerchant = false,
                    isGroup = false
                )
            }
        } else {
            TODO("Implement real contact fetching with MWA 2.2.2")
        }
    }
    
    suspend fun getBalance(): Double {
        return if (isMockMode) {
            // Mock balance with slight variation for realism
            42.5 + Random.nextDouble(-0.5, 0.5)
        } else {
            TODO("Fetch real balance via Solana RPC")
        }
    }
    
    /**
     * Get balance for a specific token
     */
    fun getTokenBalance(tokenSymbol: String): java.math.BigDecimal {
        val currentState = _walletState.value
        return when (tokenSymbol) {
            "SOL" -> currentState.solBalance
            else -> currentState.tokenBalances[tokenSymbol] ?: java.math.BigDecimal.ZERO
        }
    }
    
    suspend fun sendTransaction(
        toAddress: String, 
        amount: Double, 
        message: String? = null
    ): Result<TransactionResult> {
        return if (isMockMode) {
            delay(1000) // Simulate network delay
            
            val currentBalance = getBalance()
            if (amount > currentBalance) {
                Result.failure(Exception("Insufficient balance"))
            } else {
                Result.success(
                    TransactionResult(
                        txId = generateMockTransactionId(),
                        status = "confirmed",
                        message = message,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        } else {
            TODO("Implement real transaction with MWA 2.2.2")
        }
    }
    
    /**
     * Generates a mock transaction ID that resembles a real Solana transaction signature.
     * Real Solana signatures are Base58 encoded and typically 88 characters long.
     */
    private fun generateMockTransactionId(): String {
        val base58Chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        return buildString {
            repeat(88) {
                append(base58Chars.random())
            }
        }
    }
}

// TrueTap data classes
data class TrueTapContact(
    val id: String,
    val name: String,
    val address: String,
    val isMerchant: Boolean = false,
    val isGroup: Boolean = false
)

data class TransactionResult(
    val txId: String,
    val status: String,
    val message: String?,
    val timestamp: Long
) 