package com.truetap.solana.seeker.services

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Polling-based transaction confirmation monitor.
 * Emits submitted -> processed/confirmed/finalized or failed states.
 */
@Singleton
class TransactionMonitor @Inject constructor(
    private val rpc: UnifiedSolanaRpcService
) {
    data class Status(val signature: String, val state: String, val error: String? = null)

    fun watch(signature: String, timeoutMs: Long = 60_000L, pollMs: Long = 2_000L): Flow<Status> = flow {
        val start = System.currentTimeMillis()
        emit(Status(signature, "submitted"))
        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                val params = JSONArray().apply {
                    put(JSONArray().put(signature))
                    put(JSONObject().apply { put("searchTransactionHistory", true) })
                }
                val resp = rpc.call("getSignatureStatuses", params)
                val value = resp.optJSONObject("result")?.optJSONArray("value")?.optJSONObject(0)
                val confirmationStatus = value?.optString("confirmationStatus") // processed, confirmed, finalized
                val err = value?.opt("err")?.toString()
                if (confirmationStatus != null) {
                    emit(Status(signature, confirmationStatus, err))
                    if (confirmationStatus == "finalized" || err != null) break
                }
            } catch (_: Exception) {
                // keep polling silently to avoid noisy UX
            }
            delay(pollMs)
        }
    }
}


