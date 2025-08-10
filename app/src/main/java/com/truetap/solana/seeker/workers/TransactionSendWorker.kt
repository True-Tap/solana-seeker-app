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
                // Stop after 5 retries
                if (tx.retries >= 5) {
                    continue
                }
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

object TransactionWorkScheduler {
    fun enqueue(context: Context) {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val work = androidx.work.OneTimeWorkRequestBuilder<TransactionSendWorker>()
            .setConstraints(constraints)
            .build()

        androidx.work.WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "tx_outbox_sender",
                androidx.work.ExistingWorkPolicy.KEEP,
                work
            )
    }
}


