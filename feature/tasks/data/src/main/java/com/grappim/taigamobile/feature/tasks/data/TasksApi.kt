package com.grappim.taigamobile.feature.tasks.data

import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface TasksApi {
    @GET("tasks?order_by=us_order")
    suspend fun getTasks(
        @Query("user_story") userStory: Any? = null,
        @Query("project") project: Long? = null,
        @Query("milestone") sprint: Long? = null,
        @Query("page") page: Int? = null,
        @Query("assigned_to") assignedId: Long? = null,
        @Query("status__is_closed") isClosed: Boolean? = null,
        @Query("watchers") watcherId: Long? = null,
        @Header("x-disable-pagination") disablePagination: Boolean? = (page == null).takeIf { it }
    ): List<WorkItemResponseDTO>

    @POST("tasks")
    suspend fun createTask(@Body createTaskRequestDTO: CreateTaskRequestDTO): WorkItemResponseDTO
}
