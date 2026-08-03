package com.grappim.taigamobile.core.api.errors

import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.core.domain.ProjectLimitInfo
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class ErrorMappingPluginTest {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val networkErrorMapper = NetworkErrorMapper()
    private val errorResponseParser = ErrorResponseParser(json)

    @Test
    fun `on successful response should pass it through untouched`() = runTest {
        val client = createClient { respond(content = "payload", status = HttpStatusCode.OK) }

        val response = client.get("https://example.com/ok")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("payload", response.bodyAsText())
    }

    @Test
    fun `on error response should throw NetworkException with the mapped code and parsed error`() = runTest {
        val client = createClient {
            respond(
                content = """{"_error_message":"Not there","_error_type":"NotFound"}""",
                status = HttpStatusCode.NotFound
            )
        }

        val thrown = assertFailsWith<NetworkException> { client.get("https://example.com/missing") }

        assertEquals(NetworkException.ERROR_NOT_FOUND, thrown.errorCode)
        assertEquals("https://example.com/missing", thrown.request)
        assertEquals("Not there", thrown.taigaError?.message)
        assertEquals("NotFound", thrown.taigaError?.type)
        assertEquals(HttpStatusCode.NotFound.value, thrown.taigaError?.statusCode)
    }

    @Test
    fun `on error response with both project limit headers should attach ProjectLimitInfo`() = runTest {
        val client = createClient {
            respond(
                content = """{"_error_message":"Limit reached"}""",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(
                    "Taiga-Info-Project-Memberships" to listOf("7"),
                    "Taiga-Info-Project-Is-Private" to listOf("true")
                )
            )
        }

        val thrown = assertFailsWith<NetworkException> { client.get("https://example.com/limit") }

        assertEquals(NetworkException.ERROR_VALIDATION, thrown.errorCode)
        assertEquals(
            ProjectLimitInfo(totalMemberships = 7, isPrivate = true),
            thrown.taigaError?.projectLimitInfo
        )
    }

    @Test
    fun `on error response with only the memberships header should not attach ProjectLimitInfo`() = runTest {
        val client = createClient {
            respond(
                content = """{"_error_message":"Limit reached"}""",
                status = HttpStatusCode.BadRequest,
                headers = headersOf("Taiga-Info-Project-Memberships", "7")
            )
        }

        val thrown = assertFailsWith<NetworkException> { client.get("https://example.com/limit") }

        assertNull(thrown.taigaError?.projectLimitInfo)
    }

    @Test
    fun `on error response with an unparseable memberships header should not attach ProjectLimitInfo`() = runTest {
        val client = createClient {
            respond(
                content = """{"_error_message":"Limit reached"}""",
                status = HttpStatusCode.BadRequest,
                headers = headersOf(
                    "Taiga-Info-Project-Memberships" to listOf("not-a-number"),
                    "Taiga-Info-Project-Is-Private" to listOf("true")
                )
            )
        }

        val thrown = assertFailsWith<NetworkException> { client.get("https://example.com/limit") }

        assertNull(thrown.taigaError?.projectLimitInfo)
    }

    @Test
    fun `on server error response should map to the internal server error code`() = runTest {
        val client = createClient {
            respond(content = "not json at all", status = HttpStatusCode.InternalServerError)
        }

        val thrown = assertFailsWith<NetworkException> { client.get("https://example.com/boom") }

        assertEquals(NetworkException.ERROR_INTERNAL_SERVER, thrown.errorCode)
        assertNull(thrown.taigaError)
    }

    @Test
    fun `on NetworkException thrown downstream should rethrow the same instance`() = runTest {
        val original = NetworkException(NetworkException.ERROR_TIMEOUT)
        val client = createClient { throw original }

        val thrown = assertFailsWith<NetworkException> { client.get("https://example.com/downstream") }

        assertSame(original, thrown)
    }

    @Test
    fun `on unexpected exception downstream should map it through the error mapper`() = runTest {
        val client = createClient { throw IllegalStateException("engine blew up") }

        val thrown = assertFailsWith<NetworkException> { client.get("https://example.com/downstream") }

        assertEquals(NetworkException.ERROR_UNDEFINED, thrown.errorCode)
    }

    private fun createClient(handler: MockRequestHandleScope.() -> HttpResponseData): HttpClient =
        HttpClient(MockEngine { handler() }) {
            install(ErrorMappingPlugin) {
                this.json = this@ErrorMappingPluginTest.json
                this.errorMapper = networkErrorMapper
                this.errorResponseParser = this@ErrorMappingPluginTest.errorResponseParser
            }
        }
}
