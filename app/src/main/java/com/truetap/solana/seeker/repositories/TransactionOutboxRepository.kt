package com.truetap.solana.seeker.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.truetap.solana.seeker.services.FeePreset
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.outboxDataStore by preferencesDataStore(name = "tx_outbox")

data class PendingTransaction(
    val id: String,
    val toAddress: String,
    val amount: Double,
    val memo: String?,
    val feePreset: FeePreset,
    val createdAt: Long,
    val retries: Int = 0
)

@Singleton
class TransactionOutboxRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun key(id: String) = stringPreferencesKey("pt_" + id)
    private val retryKey = intPreferencesKey("retries_")

    suspend fun enqueue(tx: PendingTransaction) {
        context.outboxDataStore.edit { prefs ->
            val payload = listOf(
                tx.id,
                tx.toAddress,
                tx.amount.toString(),
                tx.memo ?: "",
                tx.feePreset.name,
                tx.createdAt.toString(),
                tx.retries.toString()
            ).joinToString("|")
            prefs[key(tx.id)] = payload
        }
    }

    suspend fun getAll(): List<PendingTransaction> {
        val prefs = context.outboxDataStore.data.first()
        return prefs.asMap().entries
            .filter { (k, _) -> k.name.startsWith("pt_") }
            .mapNotNull { (_, v) -> parse(v as String) }
            .sortedBy { it.createdAt }
    }

    suspend fun remove(id: String) {
        context.outboxDataStore.edit { it.remove(key(id)) }
    }

    suspend fun incrementRetries(id: String): PendingTransaction? {
        val all = getAll()
        val tx = all.find { it.id == id } ?: return null
        val updated = tx.copy(retries = tx.retries + 1)
        enqueue(updated)
        return updated
    }

    private fun parse(s: String): PendingTransaction? {
        val parts = s.split("|")
        return try {
            PendingTransaction(
                id = parts[0],
                toAddress = parts[1],
                amount = parts[2].toDouble(),
                memo = parts[3].ifBlank { null },
                feePreset = FeePreset.valueOf(parts[4]),
                createdAt = parts[5].toLong(),
                retries = parts[6].toInt()
            )
        } catch (_: Exception) { null }
    }
}


