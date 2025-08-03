package com.truetap.solana.seeker.extensions

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.truetap.solana.seeker.data.WalletResult
import kotlinx.coroutines.launch

/**
 * Extension functions for ComponentActivity to simplify wallet operations
 */

fun ComponentActivity.connectWallet(
    cluster: String = "devnet",
    onResult: (WalletResult<*>) -> Unit
) {
    lifecycleScope.launch {
        try {
            // This would be called from the UI layer with proper repository injection
            // For now, this is a placeholder for the integration pattern
            onResult(WalletResult.Error(
                RuntimeException("Use WalletViewModel.connectWallet() instead"),
                "Direct activity wallet connection not implemented"
            ))
        } catch (e: Exception) {
            onResult(WalletResult.Error(e, "Failed to connect wallet: ${e.message}"))
        }
    }
}

/**
 * Helper function to handle common wallet operation patterns
 */
inline fun <T> ComponentActivity.handleWalletOperation(
    crossinline operation: suspend () -> WalletResult<T>,
    crossinline onSuccess: (T) -> Unit,
    crossinline onError: (String) -> Unit
) {
    lifecycleScope.launch {
        try {
            when (val result = operation()) {
                is WalletResult.Success -> onSuccess(result.data)
                is WalletResult.Error -> onError(result.message)
            }
        } catch (e: Exception) {
            onError(e.message ?: "Unknown error occurred")
        }
    }
}