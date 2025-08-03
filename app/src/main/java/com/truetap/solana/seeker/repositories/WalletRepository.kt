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

    suspend fun signAuthMessage(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        message: String = "Authenticate with True Tap"
    ): WalletResult<String> {
        return when (val result = seedVaultService.signMessage(activityResultLauncher, message)) {
            is WalletResult.Success -> {
                WalletResult.Success(android.util.Base64.encodeToString(result.data, android.util.Base64.NO_WRAP))
            }
            is WalletResult.Error -> result
        }
    }

    fun isConnected(): Flow<Boolean> {
        return authState.map { it is AuthState.Connected }
    }

    fun getCurrentAccount(): Flow<WalletAccount?> {
        return authState.map { 
            when (it) {
                is AuthState.Connected -> it.account
                else -> null
            }
        }
    }

    private suspend fun authorizeWallet(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        cluster: String
    ): WalletResult<WalletAccount> {
        // TODO: Fix Solana SDK integration
        return WalletResult.Error(
            RuntimeException("Solana SDK integration temporarily disabled"),
            "Wallet authorization not available"
        )
    }

    private suspend fun generateAuthToken(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): WalletResult<String> {
        // TODO: Fix Solana SDK integration
        return WalletResult.Error(
            RuntimeException("Solana SDK integration temporarily disabled"),
            "Auth token generation not available"
        )
    }

    private suspend fun saveWalletSession(account: WalletAccount, authToken: String) {
        try {
            context.dataStore.edit { prefs ->
                prefs[WALLET_PUBLIC_KEY] = account.publicKey
                prefs[WALLET_CLUSTER] = account.cluster
                prefs[WALLET_LABEL] = account.accountLabel ?: ""
                prefs[AUTH_TOKEN] = authToken
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}