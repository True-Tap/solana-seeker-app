package com.truetap.solana.seeker.services

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SolanaPayService @Inject constructor() {
    private val client = OkHttpClient()

    data class TxRequest(
        val transactionBase64: String,
        val message: String? = null,
        val label: String? = null,
        val icon: String? = null,
        val redirect: String? = null
    )

    @Throws(Exception::class)
    fun fetchTransactionRequest(link: String): TxRequest {
        val req = Request.Builder()
            .url(link)
            .get()
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")
            val body = resp.body?.string() ?: throw Exception("Empty response")
            val json = JSONObject(body)
            val tx = json.optString("transaction")
            if (tx.isNullOrEmpty()) throw Exception("Missing transaction field")
            return TxRequest(
                transactionBase64 = tx,
                message = json.optString("message").takeIf { it.isNotEmpty() },
                label = json.optString("label").takeIf { it.isNotEmpty() },
                icon = json.optString("icon").takeIf { it.isNotEmpty() },
                redirect = json.optString("redirect").takeIf { it.isNotEmpty() }
            )
        }
    }
}


