package com.truetap.solana.seeker.services

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UnifiedSolanaRpcServiceTest {
    private lateinit var server1: MockWebServer
    private lateinit var server2: MockWebServer

    @Before
    fun setup() {
        server1 = MockWebServer()
        server2 = MockWebServer()
        server1.start()
        server2.start()
        // Override BuildConfig endpoints via reflection for tests
        setBuildField("RPC_PRIMARY", server1.url("/").toString())
        setBuildField("RPC_SECONDARY", server2.url("/").toString())
        setBuildField("RPC_TERTIARY", "")
    }

    @After
    fun teardown() {
        server1.shutdown()
        server2.shutdown()
    }

    @Test
    fun call_success_on_primary() = runBlocking {
        val body = JSONObject().apply { put("result", "ok") }.toString()
        server1.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val svc = UnifiedSolanaRpcService()
        val res = svc.call("getHealth", JSONArray())
        assertEquals("ok", res.optString("result"))
    }

    @Test
    fun call_failover_to_secondary() = runBlocking {
        server1.enqueue(MockResponse().setResponseCode(500))
        val body = JSONObject().apply { put("result", "ok2") }.toString()
        server2.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val svc = UnifiedSolanaRpcService()
        val res = svc.call("getHealth", JSONArray())
        assertEquals("ok2", res.optString("result"))
    }

    private fun setBuildField(name: String, value: String) {
        val clazz = com.truetap.solana.seeker.BuildConfig::class.java
        val field = clazz.getDeclaredField(name)
        field.isAccessible = true
        field.set(null, value)
    }
}


