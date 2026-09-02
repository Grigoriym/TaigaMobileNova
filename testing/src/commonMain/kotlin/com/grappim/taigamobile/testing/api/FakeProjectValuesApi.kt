package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.projects.data.ProjectValuesApi
import com.grappim.taigamobile.feature.projects.dto.ProjectValueItemDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectValueRequestDTO

class FakeProjectValuesApi : ProjectValuesApi {

    var getProjectValuesResult: List<ProjectValueItemDTO> = emptyList()
    var createProjectValueResult: ProjectValueItemDTO? = null
    var updateProjectValueResult: ProjectValueItemDTO? = null

    var errorToThrow: Throwable? = null

    val getProjectValuesCalls: MutableList<Pair<String, Long>> = mutableListOf()
    val createProjectValueCalls: MutableList<Pair<String, ProjectValueRequestDTO>> = mutableListOf()
    val updateProjectValueCalls: MutableList<Triple<String, Long, ProjectValueRequestDTO>> = mutableListOf()
    val deleteProjectValueCalls: MutableList<Triple<String, Long, Long?>> = mutableListOf()

    override suspend fun getProjectValues(endpoint: String, projectId: Long): List<ProjectValueItemDTO> {
        errorToThrow?.let { throw it }
        getProjectValuesCalls += Pair(endpoint, projectId)
        return getProjectValuesResult
    }

    override suspend fun createProjectValue(
        endpoint: String,
        request: ProjectValueRequestDTO
    ): ProjectValueItemDTO {
        errorToThrow?.let { throw it }
        createProjectValueCalls += Pair(endpoint, request)
        return createProjectValueResult ?: error("createProjectValueResult not set")
    }

    override suspend fun updateProjectValue(
        endpoint: String,
        id: Long,
        request: ProjectValueRequestDTO
    ): ProjectValueItemDTO {
        errorToThrow?.let { throw it }
        updateProjectValueCalls += Triple(endpoint, id, request)
        return updateProjectValueResult ?: error("updateProjectValueResult not set")
    }

    override suspend fun deleteProjectValue(endpoint: String, id: Long, moveTo: Long?) {
        errorToThrow?.let { throw it }
        deleteProjectValueCalls += Triple(endpoint, id, moveTo)
    }
}
