package com.truetap.solana.seeker.services

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
// import com.solana.mobile.seedvault.Wallet
// import com.solana.mobile.seedvault.WalletContractV1
import com.truetap.solana.seeker.data.SeedVaultInfo
import com.truetap.solana.seeker.data.WalletResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class SeedVaultService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // private val wallet = Wallet.getInstance(context)

    suspend fun getSeedVaultInfo(): SeedVaultInfo {
        return try {
            // TODO: Fix Solana SDK integration
            // val isAvailable = wallet.isAvailable()
            // val isBiometricSupported = if (isAvailable) {
            //     wallet.isBiometricAuthenticationSupported()
            // } else false
            
            SeedVaultInfo(
                isAvailable = false, // Temporarily disabled
                isBiometricAuthSupported = false
            )
        } catch (e: Exception) {
            SeedVaultInfo(isAvailable = false)
        }
    }

    suspend fun createSeed(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): WalletResult<ByteArray> {
        // TODO: Fix Solana SDK integration
        return WalletResult.Error(
            RuntimeException("Solana SDK integration temporarily disabled"),
            "Seed creation not available"
        )
    }

    suspend fun signMessage(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        message: String
    ): WalletResult<ByteArray> {
        // TODO: Fix Solana SDK integration
        return WalletResult.Error(
            RuntimeException("Solana SDK integration temporarily disabled"),
            "Message signing not available"
        )
    }

    suspend fun signTransaction(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        transaction: ByteArray
    ): WalletResult<ByteArray> {
        // TODO: Fix Solana SDK integration
        return WalletResult.Error(
            RuntimeException("Solana SDK integration temporarily disabled"),
            "Transaction signing not available"
        )
    }

    suspend fun deriveAccount(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        derivationPath: String = "m/44'/501'/0'/0'"
    ): WalletResult<ByteArray> {
        // TODO: Fix Solana SDK integration
        return WalletResult.Error(
            RuntimeException("Solana SDK integration temporarily disabled"),
            "Key derivation not available"
        )
    }
}