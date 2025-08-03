package com.truetap.solana.seeker.data

data class WalletAccount(
    val publicKey: String,
    val cluster: String,
    val accountLabel: String? = null
)