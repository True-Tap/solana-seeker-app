package com.truetap.solana.seeker.repositories

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.mobilewalletadapter.clientlib.scenario.Scenario
import com.solana.mobilewalletadapter.common.ProtocolContract
import com.truetap.solana.seeker.data.AuthState
import com.truetap.solana.seeker.data.WalletAccount
import com.truetap.solana.seeker.data.WalletResult
import com.truetap.solana.seeker.services.SeedVaultService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
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
        activity: ComponentActivity,
        cluster: String = ProtocolContract.CLUSTER_DEVNET
    ): WalletResult<WalletAccount> {
        return try {
            _authState.value = AuthState.Connecting
            
            val authResult = authorizeWallet(activity, cluster)
            when (authResult) {
                is WalletResult.Success -> {
                    _authState.value = AuthState.Authenticating
                    
                    val authToken = generateAuthToken(activity)
                    when (authToken) {
                        is WalletResult.Success -> {
                            val account = authResult.data
                            saveWalletSession(account, authToken.data)
                            _authState.value = AuthState.Connected(account)
                            WalletResult.Success(account)
                        }
                        is WalletResult.Error -> {
                            _authState.value = AuthState.Error(authToken.message)
                            authToken
                        }
                    }
                }
                is WalletResult.Error -> {
                    _authState.value = AuthState.Error(authResult.message)
                    authResult
                }
            }
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
                WalletResult.Error(
                    RuntimeException("No saved session"),
                    "No wallet session found"
                )
            }
        } catch (e: Exception) {
            WalletResult.Error(e, "Failed to restore session")
        }
    }

    suspend fun disconnect() {
        try {
            context.dataStore.edit { prefs ->
                prefs.clear()
            }
            _authState.value = AuthState.Idle
        } catch (e: Exception) {
            // Log error but don't throw
        }
    }

    suspend fun signAuthMessage(
        activity: ComponentActivity,
        message: String = "Authenticate with True Tap"
    ): WalletResult<String> {
        return when (val result = seedVaultService.signMessage(activity, message)) {
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
        activity: ComponentActivity,
        cluster: String
    ): WalletResult<WalletAccount> = suspendCancellableCoroutine { continuation ->
        try {
            val identityUri = Uri.parse(IDENTITY_URI)
            val iconUri = Uri.parse(ICON_URI)
            
            val connectionIdentity = ConnectionIdentity(
                identityUri = identityUri,
                iconUri = iconUri,
                identityName = APP_IDENTITY
            )
            
            val adapter = MobileWalletAdapter(
                connectionIdentity = connectionIdentity,
                ioDispatcher = Dispatchers.IO
            )
            
            val activityResultSender = ActivityResultSender(activity)
            
            // Note: Real MWA integration requires proper coroutine handling
            // For now, use a simplified approach that works with current API
            try {
                // This would be the proper way to use MWA in production:
                // adapter.transact(activityResultSender) { scenario ->
                //     val authResult = scenario.connect()
                //     // Handle auth result
                // }
                
                // For development purposes, create a realistic account structure
                val mockPublicKey = "${System.currentTimeMillis()}${cluster.hashCode()}".toByteArray()
                val account = WalletAccount(
                    publicKey = android.util.Base64.encodeToString(mockPublicKey, android.util.Base64.NO_WRAP),
                    cluster = cluster,
                    accountLabel = "$APP_IDENTITY Wallet"
                )
                
                continuation.resume(WalletResult.Success(account))
            } catch (e: Exception) {
                continuation.resume(
                    WalletResult.Error(e, "MWA integration error: ${e.message}")
                )
            }
        } catch (e: Exception) {
            continuation.resume(
                WalletResult.Error(e, "Failed to start wallet adapter: ${e.message}")
            )
        }
    }

    private suspend fun generateAuthToken(
        activity: ComponentActivity
    ): WalletResult<String> {
        val timestamp = System.currentTimeMillis()
        val authMessage = "Login to True Tap - $timestamp"
        
        return when (val signResult = seedVaultService.signMessage(activity, authMessage)) {
            is WalletResult.Success -> {
                val token = android.util.Base64.encodeToString(signResult.data, android.util.Base64.NO_WRAP)
                WalletResult.Success(token)
            }
            is WalletResult.Error -> signResult
        }
    }

    private suspend fun saveWalletSession(account: WalletAccount, authToken: String) {
        context.dataStore.edit { prefs ->
            prefs[WALLET_PUBLIC_KEY] = account.publicKey
            prefs[WALLET_CLUSTER] = account.cluster
            account.accountLabel?.let { prefs[WALLET_LABEL] = it }
            prefs[AUTH_TOKEN] = authToken
        }
    }
}