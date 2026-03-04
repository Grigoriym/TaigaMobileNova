package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.projects.data.ProjectsApi
import com.grappim.taigamobile.feature.projects.dto.ProjectDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectResponseDTO
import com.grappim.taigamobile.feature.projects.dto.tags.CreateTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.DeleteTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.EditTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.MixTagsRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.TagsColorsResponse
import com.grappim.taigamobile.testing.models.getProjectResponseDTO
import io.ktor.client.statement.HttpResponse

class FakeProjectsApi : ProjectsApi {
    var projectResponseDTO: ProjectResponseDTO = getProjectResponseDTO()
    var getProjectsResult: List<ProjectDTO> = emptyList()
    var tagsColorsResult: TagsColorsResponse = emptyMap()

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
    ): List<ProjectDTO> = getProjectsResult

    override suspend fun getProject(projectId: Long): ProjectResponseDTO = projectResponseDTO

    override suspend fun getProjectTagsColors(projectId: Long): TagsColorsResponse = tagsColorsResult

    override suspend fun editTag(projectId: Long, request: EditTagRequestDTO) {
        editTagCalls += Pair(projectId, request)
    }

    override suspend fun createTag(projectId: Long, request: CreateTagRequestDTO) {
        createTagCalls += Pair(projectId, request)
    }

    override suspend fun mixTags(projectId: Long, request: MixTagsRequestDTO) {
        mixTagsCalls += Pair(projectId, request)
    }

    override suspend fun deleteTag(projectId: Long, request: DeleteTagRequestDTO) {
        deleteTagCalls += Pair(projectId, request)
    }
}
