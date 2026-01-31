package com.grappim.taigamobile.feature.userstories.data

import com.grappim.taigamobile.feature.userstories.dto.BulkUpdateKanbanOrderRequest
import com.grappim.taigamobile.feature.userstories.dto.BulkUpdateKanbanOrderResponseItem
import com.grappim.taigamobile.feature.userstories.dto.CreateUserStoryRequest
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface UserStoriesApi {

    @POST("userstories")
    suspend fun createUserStory(@Body createUserStoryRequest: CreateUserStoryRequest): WorkItemResponseDTO

    @POST("userstories/bulk_update_kanban_order")
    suspend fun bulkUpdateKanbanOrder(
        @Body request: BulkUpdateKanbanOrderRequest
    ): List<BulkUpdateKanbanOrderResponseItem>

    @GET("userstories")
    suspend fun getUserStories(
        @Query("project") project: Long? = null,
        @Query("milestone") sprint: Any? = null,
        @Query("status") status: Long? = null,
        @Query("epic") epic: Long? = null,
        @Query("page") page: Int? = null,
        @Query("assigned_users") assignedId: Long? = null,
        @Query("status__is_closed") isClosed: Boolean? = null,
        @Query("watchers") watcherId: Long? = null,
        @Query("dashboard") isDashboard: Boolean? = null,
        @Query("q") query: String? = null,
        @Query("page_size") pageSize: Int = 20,

        // List<Long?>?
        @Query("assigned_to", encoded = true) assignedIds: String? = null,
        @Query("epic", encoded = true) epics: String? = null,

        // List<Long>?
        @Query("owner", encoded = true) ownerIds: String? = null,
        @Query("role", encoded = true) roles: String? = null,
        @Query("status", encoded = true) statuses: String? = null,

        // List<String>?
        @Query("tags", encoded = true) tags: String? = null,

        // here and below instead of setting header to "false" remove it,
        // because api always returns unpaginated result if header persists, regardless of its value
        @Header("x-disable-pagination") disablePagination: Boolean? = (page == null).takeIf { it }
    ): List<WorkItemResponseDTO>
}
