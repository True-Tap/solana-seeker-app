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
import com.truetap.solana.seeker.services.UnifiedSolanaRpcService
import com.truetap.solana.seeker.services.TransactionMonitor
import com.truetap.solana.seeker.auth.AuthApi
import com.truetap.solana.seeker.security.SecureStorage
import com.truetap.solana.seeker.services.FeePreset
import com.truetap.solana.seeker.services.MwaWalletConnector
import com.truetap.solana.seeker.services.SeedVaultWalletConnector
import com.truetap.solana.seeker.services.TransactionBuilder
import com.truetap.solana.seeker.workers.TransactionWorkScheduler
import com.solana.mobilewalletadapter.clientlib.TransactionResult as MwaTransactionResult
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient
import org.bitcoinj.core.Base58
import androidx.activity.ComponentActivity
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
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
    private val solanaRpcService: UnifiedSolanaRpcService,
    private val transactionBuilder: TransactionBuilder,
    private val mockData: MockData,
    private val mwaWalletConnector: MwaWalletConnector,
    private val seedVaultWalletConnector: SeedVaultWalletConnector,
    private val transactionMonitor: TransactionMonitor,
    private val authApi: AuthApi,
    private val secureStorage: SecureStorage,
    private val outboxRepository: TransactionOutboxRepository
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
        private val LAST_WALLET_CHOICE = stringPreferencesKey("last_wallet_choice")
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
            
            // Use wallet-provided auth token when available (MWA wallets)
            val authToken = connectionResult.authToken ?: "${connectionResult.publicKey}_${System.currentTimeMillis()}"
            
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
            
            // If using MWA, set adapter authToken for seamless subsequent requests
            if (connectionResult.walletType.usesMobileWalletAdapter && connectionResult.authToken != null) {
                try {
                    com.truetap.solana.seeker.services.MobileWalletAdapterServiceHelper.adapter.authToken = connectionResult.authToken
                } catch (_: Exception) { }
            }

            // Fetch wallet data in background
            fetchWalletData(account.publicKey)
            
            WalletResult.Success(account)
        } catch (e: Exception) {
            val errorMsg = "Failed to save wallet connection: ${e.message}"
            _authState.value = AuthState.Error(errorMsg)
            WalletResult.Error(e, errorMsg)
        }
    }

    private suspend fun trackConfirmation(signature: String, message: String?): TransactionResult {
        var lastStatus = "submitted"
        var ts = System.currentTimeMillis()
        transactionMonitor.watch(signature).collect { status ->
            lastStatus = status.state
            ts = System.currentTimeMillis()
        }
        return TransactionResult(
            txId = signature,
            status = lastStatus,
            message = message,
            timestamp = ts
        )
    }

    // Dev-only fallback for SIWS verification
    private fun verifySiwsBackend(publicKey: String, message: String, signatureBase64: String): Boolean {
        // In production, replace with AuthApi.verify(publicKey, message, signature)
        // For dev, accept non-empty signature and valid message
        return signatureBase64.isNotBlank() && message.contains("Nonce:")
    }
    /**
     * Sign an authentication message using the connected wallet
     */
    suspend fun signAuthMessage(
        activity: ComponentActivity?,
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>?,
        activityResultSender: com.solana.mobilewalletadapter.clientlib.ActivityResultSender?,
        message: String
    ): WalletResult<ByteArray> {
        val account = _walletState.value.account
        if (account == null) {
            return WalletResult.Error(IllegalStateException("No connected wallet"), "No connected wallet")
        }

        return when (account.walletType) {
            WalletType.SOLANA_SEEKER -> {
                if (activity == null || activityResultLauncher == null) {
                    return WalletResult.Error(IllegalStateException("Missing activity or launcher"), "Missing activity or launcher")
                }
                seedVaultWalletConnector.signMessage(
                    com.truetap.solana.seeker.data.SignMessageParams(
                        message = message.toByteArray(),
                        activity = activity,
                        activityResultLauncher = activityResultLauncher
                    )
                )
            }
            WalletType.SOLFLARE, WalletType.PHANTOM, WalletType.EXTERNAL, null -> {
                // TODO: Implement MWA signMessage via MwaWalletConnector
                WalletResult.Error(IllegalStateException("MWA signMessage not implemented"), "MWA signMessage not implemented")
            }
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
                // If using MWA, set adapter token for session reuse
                if (walletTypeEnum?.usesMobileWalletAdapter == true) {
                    try {
                        com.truetap.solana.seeker.services.MobileWalletAdapterServiceHelper.adapter.authToken = authToken
                    } catch (_: Exception) { }
                }
                
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

    /**
     * Deauthorize MWA wallet session (if applicable) to invalidate authToken.
     */
    suspend fun deauthorizeIfMwa(activityResultSender: ActivityResultSender?): WalletResult<Unit> {
        val account = _walletState.value.account
        if (account?.walletType?.usesMobileWalletAdapter != true) {
            return WalletResult.Success(Unit)
        }
        if (activityResultSender == null) {
            return WalletResult.Error(IllegalStateException("Missing ActivityResultSender"), "Missing ActivityResultSender")
        }
        return try {
            val adapter = com.truetap.solana.seeker.services.MobileWalletAdapterServiceHelper.adapter
            val result: MwaTransactionResult<*> = adapter.disconnect(activityResultSender)
            when (result) {
                is MwaTransactionResult.Success<*> -> {
                    // Clear in-memory token for safety
                    adapter.authToken = null
                    WalletResult.Success(Unit)
                }
                is MwaTransactionResult.NoWalletFound -> WalletResult.Error(IllegalStateException("No compatible wallet found"), "No compatible wallet found")
                is MwaTransactionResult.Failure -> WalletResult.Error(result.e, result.e.message ?: "MWA disconnect error")
            }
        } catch (e: Exception) {
            WalletResult.Error(e, e.message ?: "MWA disconnect error")
        }
    }

    /**
     * Sign-In With Solana via MWA. Returns success if wallet completed sign-in.
     */
    suspend fun signInWithSolana(
        activityResultSender: ActivityResultSender?,
        domain: String = "truetap.app",
        statement: String = "Sign in to TrueTap"
    ): WalletResult<Unit> {
        val account = _walletState.value.account
        if (account?.walletType?.usesMobileWalletAdapter != true) {
            return WalletResult.Error(IllegalStateException("MWA wallet required"), "MWA wallet required")
        }
        if (activityResultSender == null) {
            return WalletResult.Error(IllegalStateException("Missing ActivityResultSender"), "Missing ActivityResultSender")
        }
        return try {
            val adapter = com.truetap.solana.seeker.services.MobileWalletAdapterServiceHelper.adapter
            val siws = com.truetap.solana.seeker.auth.SiwsMessage.create(
                domain = domain,
                statement = statement,
                uri = "https://truetap.app",
                isDevnet = BuildConfig.DEBUG
            )
            val messageBytes = siws.toCanonicalString().toByteArray(Charsets.UTF_8)
            val result: MwaTransactionResult<*> = adapter.transact(activityResultSender) {
                val pkBytes = Base58.decode(account.publicKey)
                signMessagesDetached(arrayOf(messageBytes), arrayOf(pkBytes))
            }
            when (result) {
                is MwaTransactionResult.Success<*> -> {
                    val sigBytes = (result.payload as? ByteArray)
                        ?: return WalletResult.Error(IllegalStateException("Invalid signature payload"), "Invalid signature payload")
                    val token = authApi.verifySiws(
                        publicKey = account.publicKey,
                        message = siws.toCanonicalString(),
                        signatureBase64 = android.util.Base64.encodeToString(sigBytes, android.util.Base64.NO_WRAP)
                    )
                    return if (!token.isNullOrBlank()) {
                        // Persist in secure storage and DataStore for convenience
                        secureStorage.putString("siws_token", token)
                        saveSessionWithWalletType(account, token, account.walletType?.id ?: "")
                        _authState.value = AuthState.Message("Signed in securely—your wallet is ready for Solana!")
                        WalletResult.Success(Unit)
                    } else {
                        WalletResult.Error(IllegalStateException("SIWS verification failed"), "Sign-in verification failed")
                    }
                }
                is MwaTransactionResult.NoWalletFound -> WalletResult.Error(IllegalStateException("No compatible wallet found"), "No compatible wallet found")
                is MwaTransactionResult.Failure -> WalletResult.Error(result.e, result.e.message ?: "MWA sign-in error")
            }
        } catch (e: Exception) {
            WalletResult.Error(e, e.message ?: "MWA sign-in error")
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
                // Do not clear LAST_WALLET_CHOICE on disconnect; keep it for convenience
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
            // Record last selected wallet for future preselection
            prefs[LAST_WALLET_CHOICE] = walletTypeId
        }
    }

    /**
     * Persist the last wallet choice selected by the user.
     */
    suspend fun setLastWalletChoice(walletId: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_WALLET_CHOICE] = walletId
        }
    }

    /**
     * Retrieve the last wallet choice if available.
     */
    suspend fun getLastWalletChoice(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[LAST_WALLET_CHOICE]
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
            val publicKey = _walletState.value.account?.publicKey
            if (publicKey.isNullOrBlank()) 0.0 else {
                when (val res = solanaService.getBalance(publicKey)) {
                    is WalletResult.Success -> res.data.solBalance.toDouble()
                    is WalletResult.Error -> 0.0
                }
            }
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
        message: String? = null,
        activityResultSender: com.solana.mobilewalletadapter.clientlib.ActivityResultSender? = null
    ): Result<TransactionResult> {
        return sendTransactionWithPreset(toAddress, amount, message, FeePreset.NORMAL, activityResultSender)
    }

    suspend fun sendTransactionWithPreset(
        toAddress: String,
        amount: Double,
        message: String? = null,
        feePreset: FeePreset = FeePreset.NORMAL,
        activityResultSender: com.solana.mobilewalletadapter.clientlib.ActivityResultSender? = null
    ): Result<TransactionResult> {
        return if (isMockMode) {
            delay(1000) // Simulate network delay
            
            val currentBalance = getBalance()
            if (amount > currentBalance) {
                Result.failure(Exception("Insufficient balance"))
            } else {
                val timestamp = System.currentTimeMillis()
                
                // Add transaction to MockData so it appears in Recent Activity and Transaction History
                val transactionId = mockData.addTransaction(
                    type = com.truetap.solana.seeker.data.models.TransactionType.SENT,
                    amount = amount,
                    currency = "SOL", // TrueTap uses SOL by default
                    otherPartyAddress = toAddress,
                    otherPartyName = null, // Let MockData resolve from contacts
                    memo = message
                )
                
                Result.success(
                    TransactionResult(
                        txId = transactionId, // Use the ID from MockData for consistency
                        status = "confirmed",
                        message = message,
                        timestamp = timestamp
                    )
                )
            }
        } else {
            try {
                val account = _walletState.value.account ?: return Result.failure(IllegalStateException("No connected wallet"))
                val lamports = (amount * 1_000_000_000L).toLong()
                // Always use a fresh recentBlockhash to avoid expiry
                val blockhash = solanaRpcService.getLatestBlockhash()
                // Build placeholder serialized transfer (to be replaced with real serialization)
                val payload = transactionBuilder.buildSystemTransferTransaction(
                    fromPublicKeyBase58 = account.publicKey,
                    toPublicKeyBase58 = toAddress,
                    lamports = lamports,
                    recentBlockhash = blockhash,
                    priorityFeeMicrolamports = feePreset.microLamportsPerCU,
                    computeUnitLimit = feePreset.computeUnits
                )
                if (account.walletType == WalletType.SOLFLARE || account.walletType == WalletType.PHANTOM || account.walletType == WalletType.EXTERNAL) {
                    val sender = activityResultSender ?: return Result.failure(IllegalStateException("Missing ActivityResultSender for MWA"))
                    val adapter = com.truetap.solana.seeker.services.MobileWalletAdapterServiceHelper.adapter
                    val mwaResult: MwaTransactionResult<*> = adapter.transact(sender) {
                        signAndSendTransactions(arrayOf(payload))
                    }
                    return when (mwaResult) {
                        is MwaTransactionResult.Success<*> -> {
                            val payload = (mwaResult as MwaTransactionResult.Success<*>).let { it.payload }
                            val signatures = (payload as? MobileWalletAdapterClient.SignAndSendTransactionsResult)?.signatures
                            val signatureBase58 = signatures?.firstOrNull()?.let { bytes -> Base58.encode(bytes) }
                            if (signatureBase58 != null) {
                                val tracked = trackConfirmation(signatureBase58, message)
                                Result.success(tracked)
                            } else {
                                Result.failure(IllegalStateException("No signature returned from wallet"))
                            }
                        }
                        is MwaTransactionResult.NoWalletFound -> Result.failure(IllegalStateException("No compatible wallet found"))
                        is MwaTransactionResult.Failure -> Result.failure(mwaResult.e)
                    }
                } else {
                    val signedPayloadResult = seedVaultWalletConnector.signTransaction(
                        com.truetap.solana.seeker.data.SignTransactionParams(
                            transaction = payload,
                            activity = null,
                            activityResultLauncher = null,
                            activityResultSender = null
                        )
                    )
                    return when (signedPayloadResult) {
                        is WalletResult.Success -> {
                            val signedBase64 = android.util.Base64.encodeToString(signedPayloadResult.data, android.util.Base64.NO_WRAP)
                            val signature = solanaRpcService.sendTransaction(signedBase64)
                            val tracked = trackConfirmation(signature, message)
                            Result.success(tracked)
                        }
                        is WalletResult.Error -> Result.failure(Exception(signedPayloadResult.message))
                    }
                }
            } catch (e: Exception) {
                // Network/offline handling: enqueue to outbox
                if (e is java.io.IOException) {
                    val pending = PendingTransaction(
                        id = java.util.UUID.randomUUID().toString(),
                        toAddress = toAddress,
                        amount = amount,
                        memo = message,
                        feePreset = feePreset,
                        createdAt = System.currentTimeMillis()
                    )
                    try {
                        outboxRepository.enqueue(pending)
                        // Schedule constrained background send
                        TransactionWorkScheduler.enqueue(context)
                        _authState.value = AuthState.Message("Queued—will send when online")
                        return Result.failure(e)
                    } catch (_: Exception) {
                        return Result.failure(e)
                    }
                }
                Result.failure(e)
            }
        }
    }
    
    /**
     * Generates a deterministic mock transaction ID that resembles a real Solana transaction signature.
     * Uses transaction parameters to create consistent IDs for demo purposes.
     * Real Solana signatures are Base58 encoded and typically 88 characters long.
     */
    private fun generateMockTransactionId(toAddress: String, amount: Double, timestamp: Long): String {
        val base58Chars = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        
        // Create a seed from transaction parameters for deterministic generation
        val seed = "${toAddress}_${amount}_${timestamp / 1000}" // Use seconds for stability
        val hash = seed.hashCode().toLong()
        
        // Use seeded random for consistent but varied output
        val random = kotlin.random.Random(hash)
        
        return buildString {
            repeat(88) {
                append(base58Chars[random.nextInt(base58Chars.length)])
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