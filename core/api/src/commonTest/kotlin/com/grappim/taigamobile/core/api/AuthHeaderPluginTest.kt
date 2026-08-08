package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.testing.storage.FakeAuthStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.get
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AuthHeaderPluginTest {

    private val authStorage = FakeAuthStorage()
    private val appInfoProvider = FakeAppInfoProvider()

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `on request should set the User-Agent header from the app version`() = runTest {
        appInfoProvider.versionNameToReturn = "9.9.9"

        createClient().get("https://example.com/")

        assertEquals(
            "TaigaMobileNova/9.9.9",
            lastRequest?.headers?.get(ApiConstants.USER_AGENT)
        )
    }

    @Test
    fun `on request with a stored token should set the Authorization header`() = runTest {
        authStorage.tokenToReturn = "storedToken"

        createClient().get("https://example.com/")

        assertEquals(
            "Bearer storedToken",
            lastRequest?.headers?.get(ApiConstants.AUTHORIZATION)
        )
    }

    @Test
    fun `on request with an empty token should not set the Authorization header`() = runTest {
        authStorage.tokenToReturn = ""

        createClient().get("https://example.com/")

        assertNotNull(lastRequest)
        assertNull(lastRequest?.headers?.get(ApiConstants.AUTHORIZATION))
    }

    private fun createClient(): HttpClient = HttpClient(
        MockEngine { request ->
            lastRequest = request
            respond(content = "")
        }
    ) {
        install(AuthHeaderPlugin) {
            this.authStorage = this@AuthHeaderPluginTest.authStorage
            this.appInfoProvider = this@AuthHeaderPluginTest.appInfoProvider
        }
    }
}
