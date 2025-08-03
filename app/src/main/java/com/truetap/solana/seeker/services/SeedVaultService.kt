package com.truetap.solana.seeker.services

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.solana.mobile.seedvault.Wallet
import com.solana.mobile.seedvault.WalletContractV1
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
    private val wallet = Wallet.getInstance(context)

    suspend fun getSeedVaultInfo(): SeedVaultInfo {
        return try {
            val isAvailable = wallet.isAvailable()
            val isBiometricSupported = if (isAvailable) {
                wallet.isBiometricAuthenticationSupported()
            } else false
            
            SeedVaultInfo(
                isAvailable = isAvailable,
                isBiometricAuthSupported = isBiometricSupported
            )
        } catch (e: Exception) {
            SeedVaultInfo(isAvailable = false)
        }
    }

    suspend fun createSeed(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    ): WalletResult<ByteArray> = suspendCancellableCoroutine { continuation ->
        try {
            wallet.generateSeed(
                activityResultLauncher,
                WalletContractV1.PURPOSE_SIGN_SOLANA_TRANSACTION
            ) { result ->
                when (result) {
                    is Wallet.GenerateSeedResult.Success -> {
                        continuation.resume(WalletResult.Success(result.seed))
                    }
                    is Wallet.GenerateSeedResult.UserCanceled -> {
                        continuation.resume(
                            WalletResult.Error(
                                RuntimeException("Seed creation cancelled"),
                                "Seed creation was cancelled"
                            )
                        )
                    }
                    is Wallet.GenerateSeedResult.Failure -> {
                        continuation.resume(
                            WalletResult.Error(
                                result.exception,
                                "Failed to create seed: ${result.exception.message}"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            continuation.resume(
                WalletResult.Error(e, "Error initiating seed creation")
            )
        }
    }

    suspend fun signMessage(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        message: String
    ): WalletResult<ByteArray> = suspendCancellableCoroutine { continuation ->
        try {
            val messageBytes = message.toByteArray(StandardCharsets.UTF_8)
            
            wallet.signMessage(
                activityResultLauncher,
                messageBytes,
                WalletContractV1.PURPOSE_SIGN_SOLANA_TRANSACTION
            ) { result ->
                when (result) {
                    is Wallet.SignMessageResult.Success -> {
                        continuation.resume(WalletResult.Success(result.signature))
                    }
                    is Wallet.SignMessageResult.UserCanceled -> {
                        continuation.resume(
                            WalletResult.Error(
                                RuntimeException("Signing cancelled"),
                                "Message signing was cancelled"
                            )
                        )
                    }
                    is Wallet.SignMessageResult.Failure -> {
                        continuation.resume(
                            WalletResult.Error(
                                result.exception,
                                "Failed to sign message: ${result.exception.message}"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            continuation.resume(
                WalletResult.Error(e, "Error initiating message signing")
            )
        }
    }

    suspend fun signTransaction(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        transaction: ByteArray
    ): WalletResult<ByteArray> = suspendCancellableCoroutine { continuation ->
        try {
            wallet.signTransaction(
                activityResultLauncher,
                transaction,
                WalletContractV1.PURPOSE_SIGN_SOLANA_TRANSACTION
            ) { result ->
                when (result) {
                    is Wallet.SignTransactionResult.Success -> {
                        continuation.resume(WalletResult.Success(result.signatures[0]))
                    }
                    is Wallet.SignTransactionResult.UserCanceled -> {
                        continuation.resume(
                            WalletResult.Error(
                                RuntimeException("Transaction signing cancelled"),
                                "Transaction signing was cancelled"
                            )
                        )
                    }
                    is Wallet.SignTransactionResult.Failure -> {
                        continuation.resume(
                            WalletResult.Error(
                                result.exception,
                                "Failed to sign transaction: ${result.exception.message}"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            continuation.resume(
                WalletResult.Error(e, "Error initiating transaction signing")
            )
        }
    }

    suspend fun deriveAccount(
        activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>,
        derivationPath: String = "m/44'/501'/0'/0'"
    ): WalletResult<ByteArray> = suspendCancellableCoroutine { continuation ->
        try {
            wallet.derivePublicKey(
                activityResultLauncher,
                derivationPath,
                WalletContractV1.PURPOSE_SIGN_SOLANA_TRANSACTION
            ) { result ->
                when (result) {
                    is Wallet.DerivePublicKeyResult.Success -> {
                        continuation.resume(WalletResult.Success(result.publicKey))
                    }
                    is Wallet.DerivePublicKeyResult.UserCanceled -> {
                        continuation.resume(
                            WalletResult.Error(
                                RuntimeException("Key derivation cancelled"),
                                "Key derivation was cancelled"
                            )
                        )
                    }
                    is Wallet.DerivePublicKeyResult.Failure -> {
                        continuation.resume(
                            WalletResult.Error(
                                result.exception,
                                "Failed to derive key: ${result.exception.message}"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            continuation.resume(
                WalletResult.Error(e, "Error initiating key derivation")
            )
        }
    }
}