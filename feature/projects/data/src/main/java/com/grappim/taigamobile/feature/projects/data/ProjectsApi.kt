package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.feature.projects.dto.ProjectDTO
import com.grappim.taigamobile.feature.projects.dto.ProjectResponseDTO
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProjectsApi {
    @GET("projects")
    suspend fun getProjects(
        @Query("q") query: String? = null,
        @Query("page") page: Int? = null,
        @Query("member") memberId: Long? = null,
        @Query("page_size") pageSize: Int? = null,
        @Query("order_by") orderBy: String = "user_order",
        @Query("slight") slight: Boolean = true
    ): List<ProjectDTO>

    @GET("projects/{id}")
    suspend fun getProject(@Path("id") projectId: Long): ProjectResponseDTO
}
