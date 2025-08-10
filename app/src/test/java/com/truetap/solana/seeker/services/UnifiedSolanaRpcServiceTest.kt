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
        // Endpoints will be injected into the service under test
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

        val svc = UnifiedSolanaRpcService(
            primaryEndpoint = server1.url("/").toString(),
            secondaryEndpoint = server2.url("/").toString(),
            tertiaryEndpoint = ""
        )
        val res = svc.call("getHealth", JSONArray(), requestId = 1)
        assertEquals("ok", res.optString("result"))
    }

    @Test
    fun call_failover_to_secondary() = runBlocking {
        server1.enqueue(MockResponse().setResponseCode(500))
        val body = JSONObject().apply { put("result", "ok2") }.toString()
        server2.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val svc = UnifiedSolanaRpcService(
            primaryEndpoint = server1.url("/").toString(),
            secondaryEndpoint = server2.url("/").toString(),
            tertiaryEndpoint = ""
        )
        val res = svc.call("getHealth", JSONArray(), requestId = 1)
        assertEquals("ok2", res.optString("result"))
    }

    // No reflection needed after constructor injection change
}


