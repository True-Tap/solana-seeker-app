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

    override suspend fun signMessage(params: SignMessageParams): WalletResult<ByteArray> =
        WalletResult.Error(IllegalStateException("MWA signMessage not implemented"), "MWA signMessage not implemented")

    override suspend fun signTransaction(params: SignTransactionParams): WalletResult<ByteArray> =
        WalletResult.Error(IllegalStateException("MWA signTransaction not implemented"), "MWA signTransaction not implemented")
}


