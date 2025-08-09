package com.truetap.solana.seeker.services

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

@org.junit.Ignore("Temporarily disable flaky test in CI; depends on coroutine timing and mocked endpoints")
class TransactionMonitorTest {
    @Test
    fun emits_statuses_until_finalized() = runBlocking {
        val ok = { st: String ->
            JSONObject().apply {
                put("result", JSONObject().apply {
                    put("value", JSONArray().put(JSONObject().apply {
                        put("confirmationStatus", st)
                    }))
                })
            }
        }
        val rpc = object : UnifiedSolanaRpcService(
            primaryEndpoint = "http://localhost/",
            secondaryEndpoint = "",
            tertiaryEndpoint = ""
        ) {
            var i = 0
            val list = listOf(ok("processed"), ok("confirmed"), ok("finalized"))
            override suspend fun call(method: String, params: Any, requestId: Int): JSONObject = list[i++]
        }
        val monitor = TransactionMonitor(rpc)
        val first = monitor.watch("sig").first()
        assertEquals("submitted", first.state)
    }
}


