package com.truetap.solana.seeker.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified Solana RPC client with failover and backoff using OkHttp
 */
@Singleton
open class UnifiedSolanaRpcService @Inject constructor(
    private val primaryEndpoint: String = com.truetap.solana.seeker.BuildConfig.RPC_PRIMARY,
    private val secondaryEndpoint: String = com.truetap.solana.seeker.BuildConfig.RPC_SECONDARY,
    private val tertiaryEndpoint: String = com.truetap.solana.seeker.BuildConfig.RPC_TERTIARY
) {
    private val jsonMedia = "application/json".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    open suspend fun call(method: String, params: Any, requestId: Int = 1): JSONObject =
        withContext(Dispatchers.IO) {
            val body = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", requestId)
                put("method", method)
                put("params", params)
            }.toString().toRequestBody(jsonMedia)

            var lastError: Exception? = null
            val endpoints = listOf(primaryEndpoint, secondaryEndpoint, tertiaryEndpoint)
                .filter { it.isNotBlank() }
            for (endpoint in endpoints) {
                var attempt = 0
                while (attempt < 3) {
                    try {
                        val resp = client.newCall(
                            Request.Builder()
                                .url(endpoint)
                                .post(body)
                                .header("Accept", "application/json")
                                .build()
                        ).execute()

                        val text = resp.body?.string().orEmpty()
                        // If response is empty or not JSON, throw to allow failover
                        if (text.isBlank()) throw IllegalStateException("Empty RPC response")
                        return@withContext JSONObject(text)
                    } catch (e: Exception) {
                        lastError = e
                        delay((500L shl attempt).coerceAtMost(4000))
                        attempt++
                    }
                }
            }
            throw Exception("RPC call failed: ${lastError?.message}", lastError)
        }

    open suspend fun sendTransaction(base64: String, skipPreflight: Boolean = false): String {
        val params = JSONArray().apply {
            put(base64)
            put(JSONObject().apply {
                put("skipPreflight", skipPreflight)
                put("preflightCommitment", "confirmed")
                put("encoding", "base64")
            })
        }
        val response = call("sendTransaction", params)
        val result = response.optString("result")
        if (result.isNotBlank()) return result
        val err = response.optJSONObject("error")?.optString("message") ?: "Unknown error"
        throw Exception("Transaction failed: $err")
    }

    open suspend fun simulateTransaction(base64: String): JSONObject {
        val params = JSONArray().apply {
            put(base64)
            put(JSONObject().apply {
                put("sigVerify", false)
                put("commitment", "confirmed")
                put("encoding", "base64")
            })
        }
        val response = call("simulateTransaction", params)
        val result = response.optJSONObject("result")
        if (result != null) return result
        val err = response.optJSONObject("error")?.optString("message") ?: "Unknown error"
        throw Exception("Simulation failed: $err")
    }

    open suspend fun getLatestBlockhash(): String {
        val params = JSONArray().apply {
            put(JSONObject().apply { put("commitment", "confirmed") })
        }
        val response = call("getLatestBlockhash", params)
        val value = response.optJSONObject("result")?.optJSONObject("value")
        return value?.optString("blockhash") ?: throw Exception("Failed to get latest blockhash")
    }

    /**
     * Wrapper to fetch SOL balance in SOL (Double).
     */
    open suspend fun getBalanceSol(publicKey: String): Double {
        val params = JSONArray().apply { put(publicKey) }
        val response = call("getBalance", params)
        val lamports = response.optJSONObject("result")?.optLong("value") ?: 0L
        return lamports / 1_000_000_000.0
    }
}


