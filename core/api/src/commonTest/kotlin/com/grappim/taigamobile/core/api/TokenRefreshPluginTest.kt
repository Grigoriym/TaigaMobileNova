package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.storage.auth.AuthStorage
import com.grappim.taigamobile.testing.storage.FakeAuthStorage
import com.grappim.taigamobile.testing.utils.testException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TokenRefreshPluginTest {

    private val authStorage = FakeAuthStorage()
    private val tokenRefresher = FakeTokenRefresher()

    private val requests = mutableListOf<HttpRequestData>()

    @Test
    fun `on non-401 response should pass it through without refreshing`() = runTest {
        val client = createClient(authStorage) { respond(content = "", status = HttpStatusCode.OK) }

        val response = client.get("https://example.com/") { header(ApiConstants.AUTHORIZATION, bearer("tokenA")) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1, requests.size)
        assertEquals(0, tokenRefresher.refreshCallCount)
        assertEquals(0, tokenRefresher.logoutCallCount)
    }

    @Test
    fun `on 401 without an Authorization header should log out and return the response`() = runTest {
        val client = createClient(authStorage) { unauthorized() }

        val response = client.get("https://example.com/")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(1, tokenRefresher.logoutCallCount)
        assertEquals(0, tokenRefresher.refreshCallCount)
        assertEquals(1, requests.size)
    }

    @Test
    fun `on 401 with the retry count already at the maximum should log out and return the response`() = runTest {
        val client = createClient(authStorage) { unauthorized() }

        val response = client.get("https://example.com/") {
            header(ApiConstants.AUTHORIZATION, bearer("tokenA"))
            attributes.put(RETRY_COUNT_KEY, MAX_RETRIES)
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(1, tokenRefresher.logoutCallCount)
        assertEquals(0, tokenRefresher.refreshCallCount)
        assertEquals(1, requests.size)
    }

    @Test
    fun `on 401 after another coroutine refreshed should retry with the stored token and not refresh`() = runTest {
        authStorage.tokenToReturn = "tokenB"
        val client = createClient(authStorage) { unauthorized() }

        client.get("https://example.com/") { header(ApiConstants.AUTHORIZATION, bearer("tokenA")) }

        assertEquals(2, requests.size)
        assertEquals(bearer("tokenB"), requests[1].headers[ApiConstants.AUTHORIZATION])
        assertEquals(0, tokenRefresher.refreshCallCount)
        assertEquals(0, tokenRefresher.logoutCallCount)
    }

    @Test
    fun `on 401 with a token refreshed while waiting for the lock should retry without refreshing`() = runTest {
        // getToken() is read twice: once before the mutex, once as the double-check inside it.
        // Returning a different value the second time is the only way to reach the double-check
        // branch, which exists for the case where a concurrent call refreshed while we waited.
        val storage = QueuedAuthStorage(listOf("tokenA", "tokenB"))
        val client = createClient(storage) { unauthorized() }

        client.get("https://example.com/") { header(ApiConstants.AUTHORIZATION, bearer("tokenA")) }

        assertEquals(2, requests.size)
        assertEquals(bearer("tokenB"), requests[1].headers[ApiConstants.AUTHORIZATION])
        assertEquals(0, tokenRefresher.refreshCallCount)
        assertEquals(0, tokenRefresher.logoutCallCount)
    }

    @Test
    fun `on 401 should refresh store the new credentials and retry with the new token`() = runTest {
        authStorage.tokenToReturn = "tokenA"
        authStorage.refreshTokenToReturn = "refreshA"
        var callCount = 0
        val client = createClient(authStorage) {
            callCount++
            if (callCount == 1) unauthorized() else respond(content = "", status = HttpStatusCode.OK)
        }

        val response = client.get("https://example.com/") { header(ApiConstants.AUTHORIZATION, bearer("tokenA")) }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(1, tokenRefresher.refreshCallCount)
        assertEquals("refreshA", tokenRefresher.refreshArgument)
        assertEquals("newToken", authStorage.setCredentialsToken)
        assertEquals("newRefresh", authStorage.setCredentialsRefreshToken)
        assertEquals(2, requests.size)
        assertEquals(bearer("newToken"), requests[1].headers[ApiConstants.AUTHORIZATION])
        assertEquals(0, tokenRefresher.logoutCallCount)
    }

    @Test
    fun `on refresh failure should log out and return the original 401 response`() = runTest {
        authStorage.tokenToReturn = "tokenA"
        tokenRefresher.refreshThrows = testException
        val client = createClient(authStorage) { unauthorized() }

        val response = client.get("https://example.com/") { header(ApiConstants.AUTHORIZATION, bearer("tokenA")) }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(1, tokenRefresher.refreshCallCount)
        assertEquals(1, tokenRefresher.logoutCallCount)
        assertEquals(1, requests.size)
        assertNull(authStorage.setCredentialsToken)
    }

    private fun bearer(token: String) = "${ApiConstants.BEARER} $token"

    private fun MockRequestHandleScope.unauthorized() = respond(content = "", status = HttpStatusCode.Unauthorized)

    private fun createClient(storage: AuthStorage, handler: MockRequestHandleScope.() -> HttpResponseData): HttpClient =
        HttpClient(
            MockEngine { request ->
                requests += request
                handler()
            }
        ) {
            install(TokenRefreshPlugin) {
                this.authStorage = storage
                this.tokenRefresher = this@TokenRefreshPluginTest.tokenRefresher
            }
        }

    private companion object {
        const val MAX_RETRIES = 3

        /**
         * Mirrors the plugin's private key. `AttributeKey` compares by name, so this resolves to the
         * same attribute.
         */
        val RETRY_COUNT_KEY = AttributeKey<Int>("TokenRefreshRetryCount")
    }
}

private class QueuedAuthStorage(tokens: List<String>) : AuthStorage {

    private val remaining = tokens.toMutableList()

    override val isLoggedIn: Flow<Boolean> = flowOf(true)

    override suspend fun getToken(): String = if (remaining.size > 1) remaining.removeAt(0) else remaining.first()

    override suspend fun getRefreshToken(): String = "refresh"

    override suspend fun setAuthCredentials(token: String, refreshToken: String) = Unit

    override suspend fun clear() = Unit
}
