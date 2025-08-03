package com.truetap.solana.seeker.services

import android.content.Context
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.seedvault.SeedVault
import com.solana.seedvault.WalletContractV1
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
    private val seedVault = SeedVault.getInstance(context)

    suspend fun getSeedVaultInfo(): SeedVaultInfo {
        return try {
            val isAvailable = seedVault.isAvailable()
            val isBiometricSupported = if (isAvailable) {
                seedVault.isBiometricAuthenticationSupported()
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
        activityResultSender: ActivityResultSender
    ): WalletResult<ByteArray> = suspendCancellableCoroutine { continuation ->
        try {
            seedVault.createSeed(
                activityResultSender,
                WalletContractV1.PURPOSE_SIGN_SOLANA_TRANSACTION
            ) { result ->
                when (result) {
                    is SeedVault.SeedResult.Success -> {
                        continuation.resume(WalletResult.Success(result.seed))
                    }
                    is SeedVault.SeedResult.Cancelled -> {
                        continuation.resume(
                            WalletResult.Error(
                                RuntimeException("Seed creation cancelled"),
                                "Seed creation was cancelled"
                            )
                        )
                    }
                    is SeedVault.SeedResult.Failure -> {
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
        activityResultSender: ActivityResultSender,
        message: String
    ): WalletResult<ByteArray> = suspendCancellableCoroutine { continuation ->
        try {
            val messageBytes = message.toByteArray(StandardCharsets.UTF_8)
            
            seedVault.signMessage(
                activityResultSender,
                messageBytes,
                WalletContractV1.PURPOSE_SIGN_SOLANA_TRANSACTION
            ) { result ->
                when (result) {
                    is SeedVault.SignResult.Success -> {
                        continuation.resume(WalletResult.Success(result.signature))
                    }
                    is SeedVault.SignResult.Cancelled -> {
                        continuation.resume(
                            WalletResult.Error(
                                RuntimeException("Signing cancelled"),
                                "Message signing was cancelled"
                            )
                        )
                    }
                    is SeedVault.SignResult.Failure -> {
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
        activityResultSender: ActivityResultSender,
        transaction: ByteArray
    ): WalletResult<ByteArray> = suspendCancellableCoroutine { continuation ->
        try {
            seedVault.signTransaction(
                activityResultSender,
                transaction,
                WalletContractV1.PURPOSE_SIGN_SOLANA_TRANSACTION
            ) { result ->
                when (result) {
                    is SeedVault.SignResult.Success -> {
                        continuation.resume(WalletResult.Success(result.signature))
                    }
                    is SeedVault.SignResult.Cancelled -> {
                        continuation.resume(
                            WalletResult.Error(
                                RuntimeException("Transaction signing cancelled"),
                                "Transaction signing was cancelled"
                            )
                        )
                    }
                    is SeedVault.SignResult.Failure -> {
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
        activityResultSender: ActivityResultSender,
        derivationPath: String = "m/44'/501'/0'/0'"
    ): WalletResult<ByteArray> = suspendCancellableCoroutine { continuation ->
        try {
            seedVault.deriveKey(
                activityResultSender,
                derivationPath,
                WalletContractV1.PURPOSE_SIGN_SOLANA_TRANSACTION
            ) { result ->
                when (result) {
                    is SeedVault.KeyResult.Success -> {
                        continuation.resume(WalletResult.Success(result.publicKey))
                    }
                    is SeedVault.KeyResult.Cancelled -> {
                        continuation.resume(
                            WalletResult.Error(
                                RuntimeException("Key derivation cancelled"),
                                "Key derivation was cancelled"
                            )
                        )
                    }
                    is SeedVault.KeyResult.Failure -> {
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