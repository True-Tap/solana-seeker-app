package com.truetap.solana.seeker.auth

import org.junit.Assert.assertTrue
import org.junit.Test

class SiwsMessageTest {
    @Test
    fun canonical_contains_required_fields() {
        val msg = SiwsMessage.create(
            domain = "truetap.app",
            statement = "Sign in",
            uri = "https://truetap.app",
            isDevnet = true
        )
        val s = msg.toCanonicalString()
        assertTrue(s.contains("Nonce:"))
        assertTrue(s.contains("Issued At:"))
        assertTrue(s.contains("Chain ID:"))
    }
}


