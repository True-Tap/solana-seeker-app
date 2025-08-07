package com.truetap.solana.seeker.services

import com.truetap.solana.seeker.data.*
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
                walletType = walletType
            )
        // Delegate to existing service
        return mwaService.connectToWallet(walletType, sender)
    }

    override suspend fun signMessage(params: SignMessageParams): WalletResult<ByteArray> {
        // MWA signing to be implemented in Phase 2
        return WalletResult.Error(IllegalStateException("Not implemented"), "MWA signMessage not implemented")
    }

    override suspend fun signTransaction(params: SignTransactionParams): WalletResult<ByteArray> {
        // MWA signing to be implemented in Phase 2
        return WalletResult.Error(IllegalStateException("Not implemented"), "MWA signTransaction not implemented")
    }
}


