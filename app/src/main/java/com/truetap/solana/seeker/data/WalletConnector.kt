package com.truetap.solana.seeker.data

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest

/**
 * Minimal wallet connector abstraction for unify MWA and Seed Vault integrations.
 */
interface WalletConnector {
    suspend fun connect(params: ConnectParams): ConnectionResult
    suspend fun signMessage(params: SignMessageParams): WalletResult<ByteArray>
    suspend fun signTransaction(params: SignTransactionParams): WalletResult<ByteArray>
    val walletType: WalletType
}

data class ConnectParams(
    val activityResultSender: com.solana.mobilewalletadapter.clientlib.ActivityResultSender? = null,
    val activity: Activity? = null,
    val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null,
    val targetWalletType: WalletType? = null
)

data class SignMessageParams(
    val message: ByteArray,
    val activity: Activity? = null,
    val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
)

data class SignTransactionParams(
    val transaction: ByteArray,
    val activity: Activity? = null,
    val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>? = null
)


