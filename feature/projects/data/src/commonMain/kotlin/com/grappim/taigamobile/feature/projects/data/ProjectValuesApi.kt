package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.feature.projects.dto.ProjectValueItemDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectValueRequestDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.discardRemaining
import org.koin.core.annotation.Single

interface ProjectValuesApi {
    suspend fun getProjectValues(endpoint: String, projectId: Long): List<ProjectValueItemDTO>
    suspend fun createProjectValue(endpoint: String, request: ProjectValueRequestDTO): ProjectValueItemDTO
    suspend fun updateProjectValue(endpoint: String, id: Long, request: ProjectValueRequestDTO): ProjectValueItemDTO
    suspend fun deleteProjectValue(endpoint: String, id: Long, moveTo: Long?)
}

@Single(binds = [ProjectValuesApi::class])
class ProjectValuesApiImpl(private val httpClient: HttpClient) : ProjectValuesApi {

    override suspend fun getProjectValues(endpoint: String, projectId: Long): List<ProjectValueItemDTO> =
        httpClient.get(endpoint) {
            url { parameters.append("project", projectId.toString()) }
        }.body()

    override suspend fun createProjectValue(endpoint: String, request: ProjectValueRequestDTO): ProjectValueItemDTO =
        httpClient.post(endpoint) { setBody(request) }.body()

    override suspend fun updateProjectValue(endpoint: String, id: Long, request: ProjectValueRequestDTO): ProjectValueItemDTO =
        httpClient.patch("$endpoint/$id") { setBody(request) }.body()

    override suspend fun deleteProjectValue(endpoint: String, id: Long, moveTo: Long?) {
        httpClient.delete("$endpoint/$id") {
            url { if (moveTo != null) parameters.append("moveTo", moveTo.toString()) }
        }.discardRemaining()
    }
}
