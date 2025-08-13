package com.truetap.solana.seeker.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.requestsDataStore by preferencesDataStore(name = "requests_store")

data class PaymentRequest(
    val id: String,
    val fromAddress: String,
    val toAddress: String,
    val amount: Double,
    val memo: String?,
    val status: RequestStatus,
    val createdAt: Long
)

enum class RequestStatus { PENDING, ACCEPTED, DECLINED }

@Singleton
class RequestsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun key(id: String) = stringPreferencesKey("req_" + id)

    suspend fun addRequest(request: PaymentRequest) {
        context.requestsDataStore.edit { prefs ->
            prefs[key(request.id)] = serialize(request)
        }
    }

    suspend fun updateStatus(id: String, status: RequestStatus) {
        val existing = getById(id) ?: return
        addRequest(existing.copy(status = status))
    }

    suspend fun remove(id: String) {
        context.requestsDataStore.edit { it.remove(key(id)) }
    }

    suspend fun getById(id: String): PaymentRequest? {
        val prefs = context.requestsDataStore.data.first()
        val s = prefs[key(id)] ?: return null
        return deserialize(s)
    }

    val flow: Flow<List<PaymentRequest>> = context.requestsDataStore.data.map { prefs ->
        prefs.asMap().entries
            .filter { (it.key as? androidx.datastore.preferences.core.Preferences.Key<*>)?.name?.startsWith("req_") == true }
            .mapNotNull { (_, v) -> (v as? String)?.let { deserialize(it) } }
            .sortedByDescending { it.createdAt }
    }

    private fun serialize(r: PaymentRequest): String = listOf(
        r.id,
        r.fromAddress,
        r.toAddress,
        r.amount.toString(),
        r.memo ?: "",
        r.status.name,
        r.createdAt.toString()
    ).joinToString("|")

    private fun deserialize(s: String): PaymentRequest? = try {
        val parts = s.split("|")
        PaymentRequest(
            id = parts[0],
            fromAddress = parts[1],
            toAddress = parts[2],
            amount = parts[3].toDoubleOrNull() ?: 0.0,
            memo = parts[4].ifBlank { null },
            status = RequestStatus.valueOf(parts[5]),
            createdAt = parts[6].toLongOrNull() ?: System.currentTimeMillis()
        )
    } catch (_: Exception) { null }
}


