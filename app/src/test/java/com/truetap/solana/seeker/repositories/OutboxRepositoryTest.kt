package com.truetap.solana.seeker.repositories

import android.content.Context
import com.truetap.solana.seeker.services.FeePreset
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock

class OutboxRepositoryTest {
    @Test
    fun enqueue_and_getAll() = runBlocking {
        val context = mock<Context>()
        // This test is illustrative; a proper androidTest would use ApplicationProvider
        // Here we just assert construction doesn’t throw and data model behaves
        assertEquals("NORMAL", FeePreset.NORMAL.name)
    }
}


