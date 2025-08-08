package com.truetap.solana.seeker.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.truetap.solana.seeker.repositories.TransactionOutboxRepository
import com.truetap.solana.seeker.repositories.WalletRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TransactionSendWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val outbox: TransactionOutboxRepository,
    private val walletRepository: WalletRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val all = outbox.getAll()
        for (tx in all) {
            try {
                val res = walletRepository.sendTransactionWithPreset(
                    toAddress = tx.toAddress,
                    amount = tx.amount,
                    message = tx.memo,
                    feePreset = tx.feePreset,
                    activityResultSender = null
                )
                if (res.isSuccess) {
                    outbox.remove(tx.id)
                } else {
                    outbox.incrementRetries(tx.id)
                }
            } catch (_: Exception) {
                outbox.incrementRetries(tx.id)
            }
        }
        return Result.success()
    }
}


