package com.truetap.solana.seeker.auth

import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class SiwsMessage(
    val domain: String,
    val statement: String,
    val uri: String,
    val nonce: String,
    val issuedAt: String,
    val expirationTime: String?,
    val chainId: String
) {
    fun toCanonicalString(): String = buildString {
        appendLine("$domain wants you to sign in with your Solana account:")
        appendLine()
        appendLine(statement)
        appendLine()
        appendLine("URI: $uri")
        appendLine("Nonce: $nonce")
        appendLine("Issued At: $issuedAt")
        expirationTime?.let { appendLine("Expiration Time: $it") }
        appendLine("Chain ID: $chainId")
    }

    companion object {
        fun create(domain: String, statement: String, uri: String, isDevnet: Boolean, ttlSeconds: Long = 300): SiwsMessage {
            val now = Instant.now()
            return SiwsMessage(
                domain = domain,
                statement = statement,
                uri = uri,
                nonce = UUID.randomUUID().toString(),
                issuedAt = now.toString(),
                expirationTime = now.plusSeconds(ttlSeconds).toString(),
                chainId = if (isDevnet) "solana:devnet" else "solana:mainnet"
            )
        }
    }
}


