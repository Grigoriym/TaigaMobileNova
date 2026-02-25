package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.sprint.data.CreateSprintRequest
import com.grappim.taigamobile.feature.sprint.data.EditSprintRequest
import com.grappim.taigamobile.feature.sprint.data.SprintApi
import com.grappim.taigamobile.feature.sprint.data.SprintResponseDTO
import io.ktor.client.statement.HttpResponse

class FakeSprintsApi : SprintApi {
    override suspend fun getSprintsPaging(
        project: Long,
        page: Int,
        isClosed: Boolean
    ): HttpResponse {
        TODO("Not yet implemented")
    }

    override suspend fun getSprints(
        project: Long,
        isClosed: Boolean
    ): List<SprintResponseDTO> {
        TODO("Not yet implemented")
    }

    override suspend fun getSprint(sprintId: Long): SprintResponseDTO {
        TODO("Not yet implemented")
    }

    override suspend fun createSprint(request: CreateSprintRequest) {
        TODO("Not yet implemented")
    }

    override suspend fun editSprint(
        id: Long,
        request: EditSprintRequest
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSprint(id: Long) {
        TODO("Not yet implemented")
    }
}
