package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.core.storage.auth.AuthStateManager
import com.grappim.taigamobile.testing.storage.FakeAuthStorage
import com.grappim.taigamobile.testing.storage.FakeDatabaseWrapper
import com.grappim.taigamobile.testing.storage.FakeFiltersStorage
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class TokenRefresherImplTest {

    private val json = Json { ignoreUnknownKeys = true }

    private val filtersStorage = FakeFiltersStorage()
    private val sessionStorage = FakeTaigaSessionStorage()
    private val authStorage = FakeAuthStorage()
    private val databaseWrapper = FakeDatabaseWrapper()

    private var lastRequest: HttpRequestData? = null

    @Test
    fun `on refresh should post the refresh token and map the response`() = runTest {
        val client = createClient {
            respond(
                content = """{"auth_token":"newAuth","refresh":"newRefresh"}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", ContentType.Application.Json.toString())
            )
        }
        val sut = TokenRefresherImpl(client, createAuthStateManager(this))

        val actual = sut.refresh("oldRefresh")

        assertEquals(RefreshedTokens(authToken = "newAuth", refreshToken = "newRefresh"), actual)

        val request = requireNotNull(lastRequest)
        assertEquals(HttpMethod.Post, request.method)
        assertEquals("/auth/refresh", request.url.encodedPath)
        assertEquals("""{"refresh":"oldRefresh"}""", (request.body as TextContent).text)
    }

    @Test
    fun `on refresh failure should propagate the exception`() = runTest {
        val client = createClient { throw NetworkException(NetworkException.ERROR_TIMEOUT) }
        val sut = TokenRefresherImpl(client, createAuthStateManager(this))

        assertFailsWith<NetworkException> { sut.refresh("oldRefresh") }
    }

    @Test
    fun `on logout should delegate to the auth state manager`() = runTest {
        val client = createClient { respond(content = "") }
        val sut = TokenRefresherImpl(client, createAuthStateManager(this))

        sut.logout()
        runCurrent()

        assertTrue(filtersStorage.resetFiltersCalled)
        assertTrue(authStorage.clearCalled)
        assertTrue(databaseWrapper.clearAllTablesCalled)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createAuthStateManager(scope: TestScope) = AuthStateManager(
        filtersStorage = filtersStorage,
        taigaSessionStorage = sessionStorage,
        authStorage = authStorage,
        databaseWrapper = databaseWrapper,
        applicationScope = scope.backgroundScope
    )

    private fun createClient(handler: MockRequestHandleScope.() -> HttpResponseData): HttpClient = HttpClient(
        MockEngine { request ->
            lastRequest = request
            handler()
        }
    ) {
        install(ContentNegotiation) { json(json) }
    }
}
