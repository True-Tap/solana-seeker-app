package com.truetap.solana.seeker.repositories

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.truetap.solana.seeker.services.FeePreset
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class OutboxRepositoryTest {
    @Test
    fun enqueue_and_getAll() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = TransactionOutboxRepository(context)
        val tx = PendingTransaction(
            id = "id1",
            toAddress = "to",
            amount = 1.0,
            memo = null,
            feePreset = FeePreset.NORMAL,
            createdAt = System.currentTimeMillis()
        )
        repo.enqueue(tx)
        val all = repo.getAll()
        assertEquals(true, all.any { it.id == "id1" })
    }
}


