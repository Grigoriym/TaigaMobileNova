package com.grappim.taigamobile.testing.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Builds a real [HttpResponse] over Ktor's `MockEngine`, carrying [json] as an
 * `application/json` body and — when [hasNextPage] is true — the `X-Pagination-Next` header that
 * `HttpResponse.hasNextPage()` reads.
 *
 * Paging endpoints (`getSprintsPaging`, `getWorkItemsPaging`, …) return a raw [HttpResponse], and a
 * `RemoteMediator` calls **both** `body()` and `hasNextPage()` on it. Neither can be stubbed, so a
 * fake for such an endpoint has to hand back a genuine response; this is how one is made. Content
 * negotiation is installed on the mock client, which is what makes `response.body<T>()` deserialize.
 *
 * The client is deliberately not closed — `MockEngine` holds no resources, and the response must
 * outlive this call.
 */
suspend fun jsonHttpResponse(json: String, hasNextPage: Boolean = false): HttpResponse {
    val headers = if (hasNextPage) {
        headersOf(
            HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString()),
            PAGINATION_NEXT_HEADER to listOf("https://example.com/?page=2")
        )
    } else {
        headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    }
    val client = HttpClient(MockEngine { respond(content = json, headers = headers) }) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    return client.get("https://example.com/")
}

private const val PAGINATION_NEXT_HEADER = "X-Pagination-Next"
