package com.solana.programs

import com.solana.publickey.SolanaPublicKey
import org.bitcoinj.core.Base58
import com.solana.transaction.AccountMeta
import com.solana.transaction.TransactionInstruction
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Minimal local implementation of Compute Budget Program instructions.
 * Program ID: ComputeBudget111111111111111111111111111111
 *
 * Instruction encodings (LE):
 * - 0x02 SetComputeUnitLimit { u32 units }
 * - 0x03 SetComputeUnitPrice { u64 microLamportsPerCU }
 */
object ComputeBudgetProgram {
    private val PROGRAM_ID: SolanaPublicKey = SolanaPublicKey(Base58.decode("ComputeBudget111111111111111111111111111111"))

    fun setComputeUnitLimit(units: Int): TransactionInstruction {
        val data = ByteBuffer.allocate(1 + 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(0x02.toByte())
            .putInt(units)
            .array()
        return TransactionInstruction(
            PROGRAM_ID,
            emptyList<AccountMeta>(),
            data
        )
    }

    fun setComputeUnitPrice(microLamportsPerCU: Long): TransactionInstruction {
        val data = ByteBuffer.allocate(1 + 8)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(0x03.toByte())
            .putLong(microLamportsPerCU)
            .array()
        return TransactionInstruction(
            PROGRAM_ID,
            emptyList<AccountMeta>(),
            data
        )
    }
}


