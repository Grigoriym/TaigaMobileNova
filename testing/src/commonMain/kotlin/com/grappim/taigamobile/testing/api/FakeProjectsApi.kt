package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.projects.data.ProjectsApi
import com.grappim.taigamobile.feature.projects.dto.ProjectDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectDetailDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectResponseDTO
import com.grappim.taigamobile.feature.projects.dto.UpdateModulesRequestDTO
import com.grappim.taigamobile.feature.projects.dto.UpdateProjectRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.CreateTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.DeleteTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.EditTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.MixTagsRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.TagsColorsResponse
import com.grappim.taigamobile.testing.models.getProjectDetailDTO
import com.grappim.taigamobile.testing.models.getProjectResponseDTO
import io.ktor.client.statement.HttpResponse

class FakeProjectsApi : ProjectsApi {
    var projectResponseDTO: ProjectResponseDTO = getProjectResponseDTO()
    var projectDetailDTO: ProjectDetailDTO = getProjectDetailDTO()
    var getProjectsResult: List<ProjectDTO> = emptyList()
    var tagsColorsResult: TagsColorsResponse = emptyMap()

    var errorToThrow: Throwable? = null

    val getProjectsCalls: MutableList<Long?> = mutableListOf()
    val getProjectDetailCalls: MutableList<Long> = mutableListOf()
    val updateProjectCalls: MutableList<Pair<Long, UpdateProjectRequestDTO>> = mutableListOf()
    val updateModulesCalls: MutableList<Pair<Long, UpdateModulesRequestDTO>> = mutableListOf()
    val deleteTagCalls: MutableList<Pair<Long, DeleteTagRequestDTO>> = mutableListOf()
    val createTagCalls: MutableList<Pair<Long, CreateTagRequestDTO>> = mutableListOf()
    val editTagCalls: MutableList<Pair<Long, EditTagRequestDTO>> = mutableListOf()
    val mixTagsCalls: MutableList<Pair<Long, MixTagsRequestDTO>> = mutableListOf()

    override suspend fun getProjectsPaging(
        query: String?,
        page: Int?,
        memberId: Long?,
        pageSize: Int?,
        orderBy: String,
        slight: Boolean
    ): HttpResponse = error("not used in this test")

    override suspend fun getProjects(
        memberId: Long?,
        orderBy: String,
        slight: Boolean
    ): List<ProjectDTO> {
        errorToThrow?.let { throw it }
        getProjectsCalls += memberId
        return getProjectsResult
    }

    override suspend fun getProject(projectId: Long): ProjectResponseDTO {
        errorToThrow?.let { throw it }
        return projectResponseDTO
    }

    override suspend fun getProjectDetail(projectId: Long): ProjectDetailDTO {
        errorToThrow?.let { throw it }
        getProjectDetailCalls += projectId
        return projectDetailDTO
    }

    override suspend fun updateProject(projectId: Long, request: UpdateProjectRequestDTO): ProjectDetailDTO {
        errorToThrow?.let { throw it }
        updateProjectCalls += Pair(projectId, request)
        return projectDetailDTO
    }

    override suspend fun updateModules(projectId: Long, request: UpdateModulesRequestDTO): ProjectDetailDTO {
        errorToThrow?.let { throw it }
        updateModulesCalls += Pair(projectId, request)
        return projectDetailDTO
    }

    override suspend fun getProjectTagsColors(projectId: Long): TagsColorsResponse {
        errorToThrow?.let { throw it }
        return tagsColorsResult
    }

    override suspend fun editTag(projectId: Long, request: EditTagRequestDTO) {
        errorToThrow?.let { throw it }
        editTagCalls += Pair(projectId, request)
    }

    override suspend fun createTag(projectId: Long, request: CreateTagRequestDTO) {
        errorToThrow?.let { throw it }
        createTagCalls += Pair(projectId, request)
    }

    override suspend fun mixTags(projectId: Long, request: MixTagsRequestDTO) {
        errorToThrow?.let { throw it }
        mixTagsCalls += Pair(projectId, request)
    }

    override suspend fun deleteTag(projectId: Long, request: DeleteTagRequestDTO) {
        errorToThrow?.let { throw it }
        deleteTagCalls += Pair(projectId, request)
    }
}
