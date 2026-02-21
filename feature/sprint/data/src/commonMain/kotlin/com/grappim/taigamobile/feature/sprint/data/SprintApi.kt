package com.grappim.taigamobile.feature.sprint.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.discardRemaining
import org.koin.core.annotation.Single

@Single
class SprintApi(private val httpClient: HttpClient) {

    suspend fun getSprintsPaging(project: Long, page: Int, isClosed: Boolean): HttpResponse =
        httpClient.get("milestones") {
            url {
                parameters.append("project", project.toString())
                parameters.append("page", page.toString())
                parameters.append("closed", isClosed.toString())
            }
        }

    suspend fun getSprints(project: Long, isClosed: Boolean): List<SprintResponseDTO> = httpClient.get("milestones") {
        url {
            parameters.append("project", project.toString())
            parameters.append("closed", isClosed.toString())
        }
    }.body()

    suspend fun getSprint(sprintId: Long): SprintResponseDTO = httpClient.get("milestones/$sprintId").body()

    suspend fun createSprint(request: CreateSprintRequest) {
        httpClient.post("milestones") {
            setBody(request)
        }.discardRemaining()
    }

    suspend fun editSprint(id: Long, request: EditSprintRequest) {
        httpClient.patch("milestones/$id") {
            setBody(request)
        }.discardRemaining()
    }

    suspend fun deleteSprint(id: Long) {
        httpClient.delete("milestones/$id").discardRemaining()
    }
}
