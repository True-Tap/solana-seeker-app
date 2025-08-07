package com.truetap.solana.seeker.services

import com.truetap.solana.seeker.data.*
import com.truetap.solana.seeker.seedvault.SeedVaultManager
import javax.inject.Inject

/**
 * WalletConnector implementation backed by SeedVaultManager.
 */
class SeedVaultWalletConnector @Inject constructor(
    private val seedVaultManager: SeedVaultManager
) : WalletConnector {
    override val walletType: WalletType = WalletType.SOLANA_SEEKER

    override suspend fun connect(params: ConnectParams): ConnectionResult {
        val activity = params.activity
            ?: return ConnectionResult.Failure(
                error = "Missing Activity for Seed Vault connect",
                walletType = walletType
            )
        val launcher = params.activityResultLauncher
            ?: return ConnectionResult.Failure(
                error = "Missing ActivityResultLauncher for Seed Vault connect",
                walletType = walletType
            )

        seedVaultManager.initializeAndAuthorize(activity, launcher)
        val publicKey = seedVaultManager.publicKey.value
        return if (publicKey != null) {
            ConnectionResult.Success(
                publicKey = publicKey,
                accountLabel = "Seed Vault",
                walletType = walletType,
                authToken = null
            )
        } else {
            ConnectionResult.Failure(
                error = seedVaultManager.error.value ?: "Unknown Seed Vault error",
                walletType = walletType
            )
        }
    }

    override suspend fun signMessage(params: SignMessageParams): WalletResult<ByteArray> {
        val activity = params.activity
            ?: return WalletResult.Error(IllegalStateException("Missing Activity"), "Missing Activity")
        val launcher = params.activityResultLauncher
            ?: return WalletResult.Error(IllegalStateException("Missing ActivityResultLauncher"), "Missing ActivityResultLauncher")
        val signature = seedVaultManager.signMessage(params.message, activity, launcher)
        return if (signature != null) WalletResult.Success(signature)
        else WalletResult.Error(IllegalStateException(seedVaultManager.error.value ?: "Signing failed"))
    }

    override suspend fun signTransaction(params: SignTransactionParams): WalletResult<ByteArray> {
        val activity = params.activity
            ?: return WalletResult.Error(IllegalStateException("Missing Activity"), "Missing Activity")
        val launcher = params.activityResultLauncher
            ?: return WalletResult.Error(IllegalStateException("Missing ActivityResultLauncher"), "Missing ActivityResultLauncher")
        val signed = seedVaultManager.signTransaction(params.transaction, activity, launcher)
        return if (signed != null) WalletResult.Success(signed)
        else WalletResult.Error(IllegalStateException(seedVaultManager.error.value ?: "Signing failed"))
    }
}


