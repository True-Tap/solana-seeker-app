package com.truetap.solana.seeker.auth

import org.junit.Assert.assertNotNull
import org.junit.Test

class AuthApiTest {
    @Test
    fun httpAuthApi_instantiates() {
        val api: AuthApi = HttpAuthApi()
        assertNotNull(api)
    }
}


