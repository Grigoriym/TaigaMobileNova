package com.grappim.taigamobile.feature.issues.data

import com.grappim.taigamobile.feature.issues.dto.CreateIssueRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface IssuesApi {
    @GET("issues")
    suspend fun getIssues(
        @Query("page") page: Int? = null,
        @Query("project") project: Long? = null,
        @Query("q") query: String? = null,
        @Query("milestone") sprint: Long? = null,
        @Query("status__is_closed") isClosed: Boolean? = null,
        @Query("watchers") watcherId: Long? = null,
        @Query("page_size") pageSize: Int = 20,

        // List<Long?>?
        @Query("assigned_to", encoded = true) assignedIds: String? = null,

        // List<Long>?
        @Query("owner", encoded = true) ownerIds: String? = null,
        @Query("priority", encoded = true) priorities: String? = null,
        @Query("severity", encoded = true) severities: String? = null,
        @Query("type", encoded = true) types: String? = null,
        @Query("role", encoded = true) roles: String? = null,
        @Query("status", encoded = true) statuses: String? = null,

        // List<String>?
        @Query("tags", encoded = true) tags: String? = null,

        @Header("x-disable-pagination") disablePagination: Boolean? = (page == null).takeIf { it }
    ): List<WorkItemResponseDTO>

    @POST("issues")
    suspend fun createIssue(@Body createIssueRequest: CreateIssueRequestDTO): WorkItemResponseDTO
}
