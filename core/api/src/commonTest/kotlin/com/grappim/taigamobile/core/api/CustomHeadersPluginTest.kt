package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.storage.customheaders.CustomHeader
import com.grappim.taigamobile.testing.storage.FakeCustomHeadersStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CustomHeadersPluginTest {

    private val storage = FakeCustomHeadersStorage(listOf(CustomHeader("X-Proxy-Token", "secret")))
    private val requests = mutableListOf<HttpRequestData>()
    private val logLines = mutableListOf<String>()

    @Test
    fun `adds the headers to a request on the server's origin`() = runTest {
        createClient(SERVER).get("$SERVER/api/v1/projects")

        assertEquals("secret", requests.single().headers["X-Proxy-Token"])
    }

    @Test
    fun `leaves out the headers for another host, scheme or port`() = runTest {
        val client = createClient(SERVER)

        client.get("https://www.gravatar.com/avatar/1")
        client.get("http://taiga.example.com/api/v1/projects")
        client.get("https://taiga.example.com:8443/api/v1/projects")

        assertEquals(3, requests.size)
        requests.forEach { assertNull(it.headers["X-Proxy-Token"], it.url.toString()) }
    }

    @Test
    fun `strips a same-named header set by the caller on another host`() = runTest {
        createClient(SERVER).get("https://elsewhere.example.com/") { header("X-Proxy-Token", "caller") }

        assertNull(requests.single().headers["X-Proxy-Token"])
    }

    @Test
    fun `sends nothing while no server is stored`() = runTest {
        createClient("").get("$SERVER/api/v1/projects")

        assertNull(requests.single().headers["X-Proxy-Token"])
    }

    @Test
    fun `does not carry the headers across a redirect off the server`() = runTest {
        createClient(SERVER, redirectTo = "https://login.proxy.example.com/").get("$SERVER/api/v1/projects")

        assertEquals(2, requests.size)
        assertEquals("secret", requests[0].headers["X-Proxy-Token"])
        assertEquals("login.proxy.example.com", requests[1].url.host)
        assertNull(requests[1].headers["X-Proxy-Token"])
    }

    @Test
    fun `the header value never reaches the log`() = runTest {
        createClient(SERVER).get("$SERVER/api/v1/projects")

        val log = logLines.joinToString("\n")
        assertTrue(log.contains("X-Proxy-Token", ignoreCase = true), log)
        assertFalse(log.contains("secret"), log)
    }

    @Test
    fun `isServerOrigin compares the host ignoring case`() {
        assertTrue(isServerOrigin(Url("https://TAIGA.example.com/api/v1/"), SERVER))
        assertFalse(isServerOrigin(Url("https://taiga.example.com/"), "not a url"))
    }

    private fun createClient(server: String, redirectTo: String? = null): HttpClient = HttpClient(
        MockEngine { request ->
            requests += request
            if (redirectTo != null && requests.size == 1) {
                respond("", HttpStatusCode.Found, headersOf(HttpHeaders.Location, redirectTo))
            } else {
                respond("")
            }
        }
    ) {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    logLines += message
                }
            }
            level = LogLevel.HEADERS
            sanitizeCustomHeaders(storage)
        }
        install(CustomHeaders) {
            storage = this@CustomHeadersPluginTest.storage
            serverUrl = { server }
        }
    }

    private companion object {
        const val SERVER = "https://taiga.example.com"
    }
}
