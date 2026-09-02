package com.grappim.taigamobile.core.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DebugLocalhostPluginTest {

    private val appInfoProvider = FakeAppInfoProvider()

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `on localhost request with a debug host should rewrite protocol host and port`() = runTest {
        appInfoProvider.debugLocalHostToReturn = "http://10.0.2.2:8000/"

        createClient().get("https://localhost/projects")

        val url = requireNotNull(lastRequest).url
        assertEquals("http", url.protocol.name)
        assertEquals("10.0.2.2", url.host)
        assertEquals(8000, url.port)
    }

    @Test
    fun `on localhost request with an empty debug host should leave the url untouched`() = runTest {
        appInfoProvider.debugLocalHostToReturn = ""

        createClient().get("https://localhost:1234/projects")

        val url = requireNotNull(lastRequest).url
        assertEquals("localhost", url.host)
        assertEquals(1234, url.port)
    }

    @Test
    fun `on non-localhost request should leave the url untouched even with a debug host`() = runTest {
        appInfoProvider.debugLocalHostToReturn = "http://10.0.2.2:8000/"

        createClient().get("https://taiga.example.com:1234/projects")

        val url = requireNotNull(lastRequest).url
        assertEquals("taiga.example.com", url.host)
        assertEquals(1234, url.port)
    }

    private fun createClient(): HttpClient = HttpClient(
        MockEngine { request ->
            lastRequest = request
            respond(content = "")
        }
    ) {
        install(DebugLocalhostPlugin) {
            this.appInfoProvider = this@DebugLocalhostPluginTest.appInfoProvider
        }
    }
}
