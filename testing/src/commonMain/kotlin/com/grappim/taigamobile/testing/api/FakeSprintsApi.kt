package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.sprint.data.CreateSprintRequest
import com.grappim.taigamobile.feature.sprint.data.EditSprintRequest
import com.grappim.taigamobile.feature.sprint.data.SprintApi
import com.grappim.taigamobile.feature.sprint.data.SprintResponseDTO
import io.ktor.client.statement.HttpResponse

class FakeSprintsApi : SprintApi {

    var sprintsResult: List<SprintResponseDTO> = emptyList()
    var sprintResult: SprintResponseDTO? = null
    var lastCreateRequest: CreateSprintRequest? = null
    var lastEditId: Long? = null
    var lastEditRequest: EditSprintRequest? = null
    var lastDeleteId: Long? = null

    override suspend fun getSprintsPaging(
        project: Long,
        page: Int,
        isClosed: Boolean
    ): HttpResponse = error("not used in this test")

    override suspend fun getSprints(
        project: Long,
        isClosed: Boolean
    ): List<SprintResponseDTO> = sprintsResult

    override suspend fun getSprint(sprintId: Long): SprintResponseDTO =
        sprintResult ?: error("sprintResult not set")

    override suspend fun createSprint(request: CreateSprintRequest) {
        lastCreateRequest = request
    }

    override suspend fun editSprint(id: Long, request: EditSprintRequest) {
        lastEditId = id
        lastEditRequest = request
    }

    override suspend fun deleteSprint(id: Long) {
        lastDeleteId = id
    }
}
