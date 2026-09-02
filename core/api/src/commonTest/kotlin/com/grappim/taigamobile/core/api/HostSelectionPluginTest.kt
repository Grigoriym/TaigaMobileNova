package com.grappim.taigamobile.core.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HostSelectionPluginTest {

    private val baseUrlProvider = FakeBaseUrlProvider()

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `on non-empty base url should rewrite protocol host and port`() = runTest {
        baseUrlProvider.baseUrlToReturn = "https://taiga.example.com:8443/api/v1/"

        createClient().get("http://placeholder/projects")

        val url = requireNotNull(lastRequest).url
        assertEquals("https", url.protocol.name)
        assertEquals("taiga.example.com", url.host)
        assertEquals(8443, url.port)
        assertEquals("/projects", url.encodedPath)
    }

    @Test
    fun `on empty base url should leave the request url untouched`() = runTest {
        baseUrlProvider.baseUrlToReturn = ""

        createClient().get("http://placeholder:1234/projects")

        val url = requireNotNull(lastRequest).url
        assertEquals("http", url.protocol.name)
        assertEquals("placeholder", url.host)
        assertEquals(1234, url.port)
    }

    private fun createClient(): HttpClient = HttpClient(
        MockEngine { request ->
            lastRequest = request
            respond(content = "")
        }
    ) {
        install(HostSelectionPlugin) {
            this.baseUrlProvider = this@HostSelectionPluginTest.baseUrlProvider
        }
    }
}
