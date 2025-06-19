package com.grappim.taigamobile.feature.sprint.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SprintApi {
    @GET("milestones")
    suspend fun getSprints(
        @Query("project") project: Long,
        @Query("page") page: Int,
        @Query("closed") isClosed: Boolean
    ): List<SprintResponse>

    @GET("milestones/{id}")
    suspend fun getSprint(@Path("id") sprintId: Long): SprintResponse

    @POST("milestones")
    suspend fun createSprint(@Body request: CreateSprintRequest)

    @PATCH("milestones/{id}")
    suspend fun editSprint(@Path("id") id: Long, @Body request: EditSprintRequest)

    @DELETE("milestones/{id}")
    suspend fun deleteSprint(@Path("id") id: Long): Response<Void>
}
