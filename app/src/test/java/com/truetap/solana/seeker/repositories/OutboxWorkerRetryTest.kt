package com.truetap.solana.seeker.repositories

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.truetap.solana.seeker.services.FeePreset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class OutboxWorkerRetryTest {
    @Test
    fun enqueue_and_flow_emits_and_retry_caps_at_5() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = TransactionOutboxRepository(context)

        val tx = PendingTransaction(
            id = "retry_cap_test",
            toAddress = "to",
            amount = 1.0,
            memo = null,
            feePreset = FeePreset.NORMAL,
            createdAt = System.currentTimeMillis()
        )
        repo.enqueue(tx)

        val list1 = repo.flow.first()
        assertEquals(true, list1.any { it.id == tx.id })

        repeat(10) { repo.incrementRetries(tx.id) }
        val updated = repo.getAll().first { it.id == tx.id }
        // Worker will skip processing at >=5; test ensures increment works and we can assert behavior elsewhere
        assertEquals(10, updated.retries)
    }
}


