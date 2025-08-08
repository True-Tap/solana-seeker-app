package com.truetap.solana.seeker.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

interface AuthApi {
    suspend fun verifySiws(publicKey: String, message: String, signatureBase64: String): String?
}

@Singleton
class HttpAuthApi @Inject constructor(): AuthApi {
    private val client = OkHttpClient()
    private val jsonMedia = "application/json".toMediaType()
    private val baseUrl = System.getenv("TRUETAP_AUTH_URL") ?: "https://api.truetap.app/auth"

    override suspend fun verifySiws(publicKey: String, message: String, signatureBase64: String): String? = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("publicKey", publicKey)
            put("message", message)
            put("signatureBase64", signatureBase64)
        }.toString().toRequestBody(jsonMedia)

        val req = Request.Builder()
            .url("$baseUrl/siws/verify")
            .post(body)
            .header("Accept", "application/json")
            .build()

        client.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) return@withContext null
            val json = JSONObject(text)
            if (json.optBoolean("ok", false)) json.optString("token") else null
        }
    }
}


