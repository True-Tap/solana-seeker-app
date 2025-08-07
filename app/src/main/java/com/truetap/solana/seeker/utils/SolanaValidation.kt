package com.truetap.solana.seeker.utils

import org.bitcoinj.core.Base58

object SolanaValidation {
    fun isValidPublicKey(base58: String): Boolean {
        return try {
            val decoded = Base58.decode(base58)
            decoded.size == 32
        } catch (_: Exception) {
            false
        }
    }
}


