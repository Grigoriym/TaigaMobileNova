package com.grappim.taigamobile.projectselector

import com.grappim.taigamobile.data.api.ProjectResponse
import com.grappim.taigamobile.domain.entities.Project
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
