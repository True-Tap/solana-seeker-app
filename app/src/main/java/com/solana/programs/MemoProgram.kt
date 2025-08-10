package com.solana.programs

import com.solana.publickey.SolanaPublicKey
import com.solana.transaction.AccountMeta
import com.solana.transaction.TransactionInstruction
import org.bitcoinj.core.Base58

/**
 * Minimal local implementation of Memo Program.
 * Program ID: MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr
 * Instruction data is UTF-8 bytes of the memo string, no accounts.
 */
object MemoProgram {
    private val PROGRAM_ID: SolanaPublicKey = SolanaPublicKey(Base58.decode("MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr"))

    fun memo(text: String): TransactionInstruction {
        val data = text.toByteArray(Charsets.UTF_8)
        return TransactionInstruction(
            PROGRAM_ID,
            emptyList<AccountMeta>(),
            data
        )
    }
}


