package com.truetap.solana.seeker.services

import com.truetap.solana.seeker.data.ConnectParams
import com.truetap.solana.seeker.data.ConnectionResult
import com.truetap.solana.seeker.data.SignMessageParams
import com.truetap.solana.seeker.data.SignTransactionParams
import com.truetap.solana.seeker.data.WalletConnector
import com.truetap.solana.seeker.data.WalletResult
import com.truetap.solana.seeker.data.WalletType
import javax.inject.Inject

/**
 * WalletConnector implementation backed by Mobile Wallet Adapter service.
 */
class MwaWalletConnector @Inject constructor(
    private val mwaService: MobileWalletAdapterService
) : WalletConnector {
    override val walletType: WalletType = WalletType.EXTERNAL

    override suspend fun connect(params: ConnectParams): ConnectionResult {
        val sender = params.activityResultSender
            ?: return ConnectionResult.Failure(
                error = "Missing ActivityResultSender for MWA connect",
                walletType = params.targetWalletType ?: walletType
            )
        // Delegate to existing service
        return mwaService.connectToWallet(params.targetWalletType ?: walletType, sender)
    }

    override suspend fun signMessage(params: SignMessageParams): WalletResult<ByteArray> {
        val sender = params.activityResultSender
            ?: return WalletResult.Error(IllegalStateException("Missing ActivityResultSender"), "Missing ActivityResultSender")
        return try {
            val adapter = MobileWalletAdapterServiceHelper.adapter
            val result = adapter.transact(sender) {
                signMessagesDetached(arrayOf(params.message), emptyArray())
            }
            when (result) {
                is com.solana.mobilewalletadapter.clientlib.TransactionResult.Success<*> -> {
                    val sigBytes = (result.payload as? ByteArray)
                    if (sigBytes != null) WalletResult.Success(sigBytes)
                    else WalletResult.Error(IllegalStateException("Invalid signature payload"), "Invalid signature payload")
                }
                is com.solana.mobilewalletadapter.clientlib.TransactionResult.NoWalletFound ->
                    WalletResult.Error(IllegalStateException("No compatible wallet found"), "No compatible wallet found")
                is com.solana.mobilewalletadapter.clientlib.TransactionResult.Failure ->
                    WalletResult.Error(result.e, result.e.message ?: "MWA signMessage error")
            }
        } catch (e: Exception) {
            WalletResult.Error(e, e.message ?: "MWA signMessage error")
        }
    }

    override suspend fun signTransaction(params: SignTransactionParams): WalletResult<ByteArray> {
        val sender = params.activityResultSender
            ?: return WalletResult.Error(IllegalStateException("Missing ActivityResultSender"), "Missing ActivityResultSender")
        return try {
            val adapter = MobileWalletAdapterServiceHelper.adapter
            val result = adapter.transact(sender) {
                // Use signTransactions (not send) to return signed bytes
                signTransactions(arrayOf(params.transaction))
            }
            when (result) {
                is com.solana.mobilewalletadapter.clientlib.TransactionResult.Success<*> -> {
                    val signed = (result.payload as? ByteArray)
                    if (signed != null) WalletResult.Success(signed)
                    else WalletResult.Error(IllegalStateException("No signed transaction returned"), "No signed transaction returned")
                }
                is com.solana.mobilewalletadapter.clientlib.TransactionResult.NoWalletFound ->
                    WalletResult.Error(IllegalStateException("No compatible wallet found"), "No compatible wallet found")
                is com.solana.mobilewalletadapter.clientlib.TransactionResult.Failure ->
                    WalletResult.Error(result.e, result.e.message ?: "MWA signTransaction error")
            }
        } catch (e: Exception) {
            WalletResult.Error(e, e.message ?: "MWA signTransaction error")
        }
    }
}


