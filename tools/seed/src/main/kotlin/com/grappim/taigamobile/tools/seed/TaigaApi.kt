package com.grappim.taigamobile.tools.seed

import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class TaigaApi(
    private val client: HttpClient,
    private val baseUrl: String
) {
    private var authToken: String? = null

    private val apiUrl get() = "$baseUrl/api/v1"

    private val json = Json { ignoreUnknownKeys = true }

    private suspend inline fun <reified T> HttpResponse.parse(endpoint: String): T {
        val bodyText = bodyAsText()
        if (!status.isSuccess()) {
            error("$endpoint failed ($status): $bodyText")
        }
        return json.decodeFromString<T>(bodyText)
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        val response: LoginResponse = client.post("$apiUrl/auth") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.parse("POST /auth")
        authToken = response.authToken
        return response
    }

    suspend fun createProject(request: CreateProjectRequest): CreateProjectResponse =
        client.post("$apiUrl/projects") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(request)
        }.parse("POST /projects")

    suspend fun getProjectDetail(projectId: Int): ProjectDetailResponse =
        client.get("$apiUrl/projects/$projectId") {
            bearerAuth(authToken!!)
        }.parse("GET /projects/$projectId")

    suspend fun createMilestone(request: CreateMilestoneRequest): MilestoneResponse =
        client.post("$apiUrl/milestones") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(request)
        }.parse("POST /milestones")

    suspend fun createEpic(request: CreateEpicRequest): EpicResponse =
        client.post("$apiUrl/epics") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(request)
        }.parse("POST /epics")

    suspend fun createUserStory(request: CreateUserStoryRequest): UserStoryResponse =
        client.post("$apiUrl/userstories") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(request)
        }.parse("POST /userstories")

    suspend fun linkUserStoryToEpic(epicId: Int, request: EpicRelatedUserStoryRequest) {
        val httpResponse = client.post("$apiUrl/epics/$epicId/related_userstories") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(request)
        }
        if (!httpResponse.status.isSuccess()) {
            error("POST /epics/$epicId/related_userstories failed (${httpResponse.status}): ${httpResponse.bodyAsText()}")
        }
    }

    suspend fun createTask(request: CreateTaskRequest): TaskResponse =
        client.post("$apiUrl/tasks") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(request)
        }.parse("POST /tasks")

    suspend fun createIssue(request: CreateIssueRequest): IssueResponse =
        client.post("$apiUrl/issues") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(request)
        }.parse("POST /issues")

    suspend fun createWikiPage(request: CreateWikiPageRequest): WikiPageResponse =
        client.post("$apiUrl/wiki") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(request)
        }.parse("POST /wiki")

    suspend fun createWikiLink(request: CreateWikiLinkRequest): WikiLinkResponse =
        client.post("$apiUrl/wiki-links") {
            contentType(ContentType.Application.Json)
            bearerAuth(authToken!!)
            setBody(request)
        }.parse("POST /wiki-links")
}
