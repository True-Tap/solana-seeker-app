package com.truetap.solana.seeker.services

import android.content.Context
import android.net.Uri
import android.util.Log
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.truetap.solana.seeker.data.ConnectionResult
import com.truetap.solana.seeker.data.WalletType
import dagger.hilt.android.qualifiers.ApplicationContext
import org.bitcoinj.core.Base58
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MobileWalletAdapterService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "MobileWalletAdapterService"
    
    suspend fun connectToWallet(
        walletType: WalletType,
        activityResultSender: ActivityResultSender
    ): ConnectionResult {
        return try {
            Log.d(TAG, "Initiating MWA connection for ${walletType.displayName} at ${System.currentTimeMillis()}")
            
            // Create MWA instance with proper app identification  
            val connectionIdentity = ConnectionIdentity(
                identityUri = Uri.parse("https://truetap.app"),
                iconUri = Uri.parse("favicon.ico"), // MWA requires relative URI only
                identityName = "TrueTap"
            )
            val mobileWalletAdapter = MobileWalletAdapter(connectionIdentity)
            
            // Call MWA connect on Main dispatcher to ensure proper activity lifecycle integration
            Log.d(TAG, "Calling MWA connect() on Main thread - this should trigger wallet popup immediately")
            val connectResult = withContext(Dispatchers.Main) {
                mobileWalletAdapter.connect(activityResultSender)
            }
            Log.d(TAG, "MWA connect completed for ${walletType.displayName}")
            
            // Handle MWA connect result
            when (connectResult) {
                is TransactionResult.Success<*> -> {
                    Log.d(TAG, "MWA authorization successful for ${walletType.displayName}")
                    // Extract AuthorizationResult fields per MWA docs
                    val authResult = connectResult.authResult
                    val firstAccount = authResult.accounts.firstOrNull()
                    val publicKeyBase58 = firstAccount?.publicKey?.let { pk ->
                        try { org.bitcoinj.core.Base58.encode(pk) } catch (_: Exception) { null }
                    }
                    val accountLabel = "${walletType.displayName} Wallet"
                    val authToken = authResult.authToken

                    if (publicKeyBase58.isNullOrEmpty()) {
                        Log.w(TAG, "MWA auth success but no account public key returned")
                        return ConnectionResult.Failure(
                            error = "Wallet did not return a public key",
                            walletType = walletType
                        )
                    }

                    Log.d(TAG, "MWA auth successful - publicKey: $publicKeyBase58, label: $accountLabel")

                    ConnectionResult.Success(
                        publicKey = publicKeyBase58,
                        accountLabel = accountLabel,
                        walletType = walletType,
                        authToken = authToken
                    )
                }
                is TransactionResult.Failure -> {
                    Log.e(TAG, "MWA authorization failed: ${connectResult.e.message}")
                    ConnectionResult.Failure(
                        error = "Authorization failed: ${connectResult.e.localizedMessage ?: connectResult.e.message}",
                        exception = connectResult.e,
                        walletType = walletType
                    )
                }
                is TransactionResult.NoWalletFound -> {
                    Log.w(TAG, "No MWA-compatible wallet responding")
                    ConnectionResult.Failure(
                        error = "No compatible wallet responded. Ensure ${walletType.displayName} supports MWA.",
                        walletType = walletType
                    )
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "MWA connection failed", e)
            ConnectionResult.Failure(
                error = "Connection error: ${e.localizedMessage ?: e.message}",
                exception = e,
                walletType = walletType
            )
        }
    }
    
    // Removed stub key generation; now using actual account from MWA auth result
}