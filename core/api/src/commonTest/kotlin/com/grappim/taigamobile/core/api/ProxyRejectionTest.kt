package com.grappim.taigamobile.core.api

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProxyRejectionTest {

    private fun check(status: Int, wwwAuthenticate: String? = null, location: String? = null) =
        isProxyRejection(status, wwwAuthenticate, location, requestHost = "taiga.example.com")

    @Test
    fun `Taiga's own 401 with a Bearer challenge is not a proxy rejection`() {
        assertFalse(check(401, wwwAuthenticate = "Bearer realm=\"api\""))
        assertFalse(check(401, wwwAuthenticate = "bearer realm=\"api\""))
    }

    @Test
    fun `a 401 without a Bearer challenge is a proxy rejection`() {
        assertTrue(check(401))
        assertTrue(check(401, wwwAuthenticate = "Basic realm=\"proxy\""))
    }

    @Test
    fun `a 407 is always a proxy rejection`() {
        assertTrue(check(407, wwwAuthenticate = "Bearer realm=\"api\""))
    }

    @Test
    fun `a redirect to another host is a proxy rejection`() {
        assertTrue(check(302, location = "https://auth.example.com/login"))
        assertTrue(check(307, location = "//auth.example.com/login"))
    }

    @Test
    fun `a redirect on the same host or a relative one is not`() {
        assertFalse(check(301, location = "https://TAIGA.example.com/api/v1/"))
        assertFalse(check(302, location = "/api/v1/projects/"))
        assertFalse(check(302))
    }

    @Test
    fun `other statuses are not`() {
        assertFalse(check(200))
        assertFalse(check(403))
        assertFalse(check(500))
    }
}
