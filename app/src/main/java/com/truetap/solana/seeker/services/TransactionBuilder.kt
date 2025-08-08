package com.truetap.solana.seeker.services

import com.solana.programs.SystemProgram
import com.solana.programs.ComputeBudgetProgram
import com.solana.publickey.SolanaPublicKey
import com.solana.transaction.Message
import com.solana.transaction.Transaction
import org.bitcoinj.core.Base58
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionBuilder @Inject constructor() {
    /**
     * Build a serialized Solana transfer transaction (from -> to, lamports) using the provided recentBlockhash.
     * Uses solana-kmp to construct a transaction and returns its serialized bytes.
     */
    fun buildSystemTransferTransaction(
        fromPublicKeyBase58: String,
        toPublicKeyBase58: String,
        lamports: Long,
        recentBlockhash: String,
        priorityFeeMicrolamports: Long? = null,
        computeUnitLimit: Int? = 200_000
    ): ByteArray {
        val fromKey = SolanaPublicKey(Base58.decode(fromPublicKeyBase58))
        val toKey = SolanaPublicKey(Base58.decode(toPublicKeyBase58))

        val builder = Message.Builder()
        // Optional compute budget instructions for reliability under congestion
        if (computeUnitLimit != null) builder.addInstruction(ComputeBudgetProgram.setComputeUnitLimit(computeUnitLimit))
        if (priorityFeeMicrolamports != null && priorityFeeMicrolamports > 0) builder.addInstruction(ComputeBudgetProgram.setComputeUnitPrice(priorityFeeMicrolamports))

        val message = builder
            .addInstruction(SystemProgram.transfer(fromKey, toKey, lamports))
            .setRecentBlockhash(recentBlockhash)
            .build()

        val transaction = Transaction(message)
        return transaction.serialize()
    }
}


