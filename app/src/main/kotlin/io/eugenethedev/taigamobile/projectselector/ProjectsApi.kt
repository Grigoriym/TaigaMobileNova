package io.eugenethedev.taigamobile.projectselector

import io.eugenethedev.taigamobile.data.api.ProjectResponse
import io.eugenethedev.taigamobile.domain.entities.Project
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProjectsApi {
    @GET("projects?order_by=user_order&slight=true")
    suspend fun getProjects(
        @Query("q") query: String? = null,
        @Query("page") page: Int? = null,
        @Query("member") memberId: Long? = null,
        @Query("page_size") pageSize: Int? = null
    ): List<Project>

    @GET("projects/{id}")
    suspend fun getProject(@Path("id") projectId: Long): ProjectResponse
}
