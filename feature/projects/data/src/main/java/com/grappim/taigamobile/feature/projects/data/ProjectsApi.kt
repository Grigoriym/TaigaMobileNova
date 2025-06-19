package com.grappim.taigamobile.feature.projects.data

import com.grappim.taigamobile.core.domain.Project
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
