package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.feature.projects.dto.ProjectDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectResponseDTO
import com.grappim.taigamobile.feature.projects.dto.tags.CreateTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.DeleteTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.EditTagRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.MixTagsRequestDTO
import com.grappim.taigamobile.feature.projects.dto.tags.TagsColorsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.discardRemaining
import org.koin.core.annotation.Single

/**
 * the slight parameter in functions switches between two serializers for list endpoints
 * with true it makes fewer database queries - lighter/faster response
 */
interface ProjectsApi {

    suspend fun getProjectsPaging(
        query: String? = null,
        page: Int? = null,
        memberId: Long? = null,
        pageSize: Int? = null,
        orderBy: String = "user_order",
        slight: Boolean = true
    ): HttpResponse

    suspend fun getProjects(
        memberId: Long? = null,
        orderBy: String = "user_order",
        slight: Boolean = true
    ): List<ProjectDTO>

    suspend fun getProject(projectId: Long): ProjectResponseDTO

    suspend fun getProjectTagsColors(projectId: Long): TagsColorsResponse

    suspend fun editTag(projectId: Long, request: EditTagRequestDTO)

    suspend fun createTag(projectId: Long, request: CreateTagRequestDTO)

    suspend fun mixTags(projectId: Long, request: MixTagsRequestDTO)

    suspend fun deleteTag(projectId: Long, request: DeleteTagRequestDTO)
}

@Single(binds = [ProjectsApi::class])
class ProjectsApiImpl(private val httpClient: HttpClient) : ProjectsApi {
    override suspend fun getProjectsPaging(
        query: String?,
        page: Int?,
        memberId: Long?,
        pageSize: Int?,
        orderBy: String,
        slight: Boolean
    ): HttpResponse = httpClient.get("projects") {
        url {
            if (query != null) parameters.append("q", query)
            if (page != null) parameters.append("page", page.toString())
            if (memberId != null) parameters.append("member", memberId.toString())
            if (pageSize != null) parameters.append("page_size", pageSize.toString())
            parameters.append("order_by", orderBy)
            parameters.append("slight", slight.toString())
        }
    }

    override suspend fun getProjects(memberId: Long?, orderBy: String, slight: Boolean): List<ProjectDTO> =
        httpClient.get("projects") {
            url {
                if (memberId != null) parameters.append("member", memberId.toString())
                parameters.append("order_by", orderBy)
                parameters.append("slight", slight.toString())
            }
        }.body()

    override suspend fun getProject(projectId: Long): ProjectResponseDTO = httpClient.get("projects/$projectId").body()

    override suspend fun getProjectTagsColors(projectId: Long): TagsColorsResponse =
        httpClient.get("projects/$projectId/tags_colors").body()

    override suspend fun editTag(projectId: Long, request: EditTagRequestDTO) {
        httpClient.post("projects/$projectId/edit_tag") {
            setBody(request)
        }.discardRemaining()
    }

    override suspend fun createTag(projectId: Long, request: CreateTagRequestDTO) {
        httpClient.post("projects/$projectId/create_tag") {
            setBody(request)
        }.discardRemaining()
    }

    override suspend fun mixTags(projectId: Long, request: MixTagsRequestDTO) {
        httpClient.post("projects/$projectId/mix_tags") {
            setBody(request)
        }.discardRemaining()
    }

    override suspend fun deleteTag(projectId: Long, request: DeleteTagRequestDTO) {
        httpClient.post("projects/$projectId/delete_tag") {
            setBody(request)
        }.discardRemaining()
    }
}
