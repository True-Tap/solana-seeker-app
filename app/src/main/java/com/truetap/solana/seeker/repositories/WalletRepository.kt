package com.truetap.solana.seeker.repositories

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
// import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
// import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient.SignTransactionsResult
// import com.solana.mobilewalletadapter.common.ProtocolContract
import com.truetap.solana.seeker.data.AuthState
import com.truetap.solana.seeker.data.WalletAccount
import com.truetap.solana.seeker.data.WalletResult
import com.truetap.solana.seeker.services.SeedVaultService
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

private val Context.dataStore by preferencesDataStore(name = "wallet_prefs")

@Singleton
class WalletRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val seedVaultService: SeedVaultService
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // private val mobileWalletAdapter = MobileWalletAdapter()

    companion object {
        private val WALLET_PUBLIC_KEY = stringPreferencesKey("wallet_public_key")
        private val WALLET_CLUSTER = stringPreferencesKey("wallet_cluster")
        private val WALLET_LABEL = stringPreferencesKey("wallet_label")
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        
        private const val APP_IDENTITY = "True Tap"
        private const val ICON_URI = "favicon.ico"
        private const val IDENTITY_URI = "https://truetap.solana"
    }

    suspend fun connectAndAuthWallet(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        cluster: String = "mainnet-beta" // ProtocolContract.CLUSTER_MAINNET_BETA
    ): WalletResult<WalletAccount> {
        return try {
            _authState.value = AuthState.Connecting
            
            // TODO: Fix Solana SDK integration
            val errorMsg = "Solana SDK integration temporarily disabled"
            _authState.value = AuthState.Error(errorMsg)
            WalletResult.Error(RuntimeException(errorMsg), errorMsg)
        } catch (e: Exception) {
            val errorMsg = "Failed to connect wallet: ${e.message}"
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
            val authToken = prefs[AUTH_TOKEN]

            if (publicKey != null && cluster != null && authToken != null) {
                val account = WalletAccount(publicKey, cluster, label)
                _authState.value = AuthState.Connected(account)
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
                prefs.remove(AUTH_TOKEN)
            }
            _authState.value = AuthState.Idle
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Failed to disconnect: ${e.message}")
        }
    }

    private suspend fun saveSession(account: WalletAccount, authToken: String) {
        context.dataStore.edit { prefs ->
            prefs[WALLET_PUBLIC_KEY] = account.publicKey
            prefs[WALLET_CLUSTER] = account.cluster
            prefs[WALLET_LABEL] = account.accountLabel ?: ""
            prefs[AUTH_TOKEN] = authToken
        }
    }
    
    // TrueTap specific implementation
    private val isMockMode = true // TODO: Toggle via BuildConfig for production
    
    suspend fun getTrueTapContacts(): List<TrueTapContact> {
        return if (isMockMode) {
            // Storytelling contacts that showcase our three phases
            listOf(
                TrueTapContact("1", "Sarah 🌟", "7xKp9Zf3nQmL4dRt8sFg3nFd"),
                TrueTapContact("2", "Mike 🚀", "9mNqBxR4vTpL5sYh2kJw8kLp"),
                TrueTapContact("3", "Crypto Coffee ☕", "3bVnMkZ8xQpR7tLs4wHy9mKl", isMerchant = true),
                TrueTapContact("4", "Lunch Squad 🍕", "5tReFgY7bNmK3pQr8vXz2nMl", isGroup = true)
            )
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