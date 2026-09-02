package com.grappim.taigamobile.core.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Headers
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResponseExtensionsTest {

    @Test
    fun `on hasNextPage with the pagination header should return true`() = runTest {
        val response = responseWith(headersOf("X-Pagination-Next", "https://example.com/?page=2"))

        assertTrue(response.hasNextPage())
    }

    @Test
    fun `on hasNextPage without the pagination header should return false`() = runTest {
        val response = responseWith(Headers.Empty)

        assertFalse(response.hasNextPage())
    }

    private suspend fun responseWith(headers: Headers): HttpResponse {
        val client = HttpClient(MockEngine { respond(content = "", headers = headers) })
        return client.get("https://example.com/")
    }
}
