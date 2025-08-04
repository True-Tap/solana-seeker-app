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

enum class WalletType(val id: String, val displayName: String, val usesMobileWalletAdapter: Boolean) {
    SOLFLARE("solflare", "Solflare", true),
    EXTERNAL("external", "External Wallet", true),
    SOLANA_SEEKER("solana", "Solana Seeker", false);
    
    companion object {
        fun fromId(id: String): WalletType? = values().find { it.id == id }
    }
}

sealed class ConnectionResult {
    data class Success(
        val publicKey: String,
        val accountLabel: String? = null,
        val walletType: WalletType,
        val authToken: String? = null
    ) : ConnectionResult()
    
    data class Failure(
        val error: String,
        val exception: Throwable? = null,
        val walletType: WalletType
    ) : ConnectionResult()
    
    data class Pending(
        val message: String,
        val walletType: WalletType
    ) : ConnectionResult()
}