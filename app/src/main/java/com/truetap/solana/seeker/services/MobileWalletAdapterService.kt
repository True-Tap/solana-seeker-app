package com.truetap.solana.seeker.services

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.mobilewalletadapter.clientlib.RpcCluster
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient
import com.solana.mobilewalletadapter.clientlib.protocol.MobileWalletAdapterClient.AuthorizationResult
import kotlinx.coroutines.delay
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
            
            // Check and launch wallet-specific app
            val targetResult = checkAndLaunchWallet(walletType)
            if (!targetResult) {
                return ConnectionResult.Failure(
                    error = "${walletType.displayName} is not installed. Install from Play Store?",
                    walletType = walletType
                )
            }
            
            // Create MWA instance
            val connectionIdentity = ConnectionIdentity(
                identityUri = Uri.parse("https://truetap.app"),
                iconUri = Uri.parse("favicon.ico"),
                identityName = "TrueTap"
            )
            val mobileWalletAdapter = MobileWalletAdapter(connectionIdentity)
            
            // Add delay after launch to ensure wallet is in foreground
            delay(1000L)
            
            // Use transact with authorize for consistent prompting
            var authResult: AuthorizationResult? = null
            val transactResult = mobileWalletAdapter.transact(activityResultSender) { client ->
                // Deauthorize any existing session to ensure fresh prompt
                try {
                    client.deauthorize()
                } catch (e: Exception) {
                    Log.d(TAG, "No existing session to deauthorize")
                }
                
                // Authorize for TrueTap
                authResult = client.authorize(
                    Uri.parse("https://truetap.app"),
                    Uri.parse("favicon.ico"),
                    "TrueTap",
                    RpcCluster.Devnet
                )
                
                // Return empty transaction
                byteArrayOf()
            }
            Log.d(TAG, "MWA transact completed for ${walletType.displayName}")
            
            // Handle MWA transact result
            when (transactResult) {
                is TransactionResult.Success<*> -> {
                    Log.d(TAG, "MWA transaction successful for ${walletType.displayName}")
                    
                    // Extract real auth data from authResult
                    authResult?.accounts?.firstOrNull()?.let { account ->
                        val publicKey = Base58.encode(account.publicKey)
                        val accountLabel = account.accountLabel ?: "${walletType.displayName} Wallet"
                        val authToken = authResult?.authToken ?: "auth_${System.currentTimeMillis()}"
                        
                        Log.d(TAG, "Real auth data - publicKey: $publicKey, label: $accountLabel")
                        
                        ConnectionResult.Success(
                            publicKey = publicKey,
                            accountLabel = accountLabel,
                            walletType = walletType,
                            authToken = authToken
                        )
                    } ?: run {
                        Log.e(TAG, "Authorization successful but no account data")
                        ConnectionResult.Failure(
                            error = "Authorization successful but no account data received",
                            walletType = walletType
                        )
                    }
                }
                is TransactionResult.Failure -> {
                    Log.e(TAG, "MWA transaction failed: ${transactResult.e.message}")
                    ConnectionResult.Failure(
                        error = "Authorization failed: ${transactResult.e.localizedMessage ?: transactResult.e.message}",
                        exception = transactResult.e,
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
    
    private fun checkAndLaunchWallet(walletType: WalletType): Boolean {
        val packageName = when (walletType) {
            WalletType.SOLFLARE -> "com.solflare.mobile"
            WalletType.PHANTOM -> "app.phantom"
            else -> return false
        }
        
        return try {
            // Use getLaunchIntentForPackage to check if wallet is launchable
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            
            if (launchIntent == null) {
                Log.w(TAG, "${walletType.displayName} not installed or not launchable")
                launchPlayStore(packageName)
                return false
            }
            
            // Launch wallet app
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            Log.d(TAG, "Successfully launched ${walletType.displayName}")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch ${walletType.displayName}", e)
            false
        }
    }
    
    private fun launchPlayStore(packageName: String) {
        try {
            val playStoreIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(playStoreIntent)
            Log.d(TAG, "Launched Play Store for $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch Play Store", e)
        }
    }
    
}