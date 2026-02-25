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

    override suspend fun getProjectsPaging(
        query: String?,
        page: Int?,
        memberId: Long?,
        pageSize: Int?,
        orderBy: String,
        slight: Boolean
    ): HttpResponse {
        TODO("Not yet implemented")
    }

    override suspend fun getProjects(
        memberId: Long?,
        orderBy: String,
        slight: Boolean
    ): List<ProjectDTO> {
        TODO("Not yet implemented")
    }

    override suspend fun getProject(projectId: Long): ProjectResponseDTO = projectResponseDTO

    override suspend fun getProjectTagsColors(projectId: Long): TagsColorsResponse {
        TODO("Not yet implemented")
    }

    override suspend fun editTag(
        projectId: Long,
        request: EditTagRequestDTO
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun createTag(
        projectId: Long,
        request: CreateTagRequestDTO
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun mixTags(
        projectId: Long,
        request: MixTagsRequestDTO
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTag(
        projectId: Long,
        request: DeleteTagRequestDTO
    ) {
        TODO("Not yet implemented")
    }
}