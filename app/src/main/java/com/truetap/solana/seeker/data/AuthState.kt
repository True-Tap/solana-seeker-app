package com.truetap.solana.seeker.data

sealed class AuthState {
    object Idle : AuthState()
    object Connecting : AuthState()
    object Authenticating : AuthState()
    data class Connected(val account: WalletAccount) : AuthState()
    data class Error(val message: String) : AuthState()
}

sealed class WalletResult<out T> {
    data class Success<T>(val data: T) : WalletResult<T>()
    data class Error(val exception: Throwable, val message: String = exception.message ?: "Unknown error") : WalletResult<Nothing>()
}

data class SeedVaultInfo(
    val isAvailable: Boolean,
    val isBiometricAuthSupported: Boolean = false
)