package com.grappim.taigamobile.data.api

import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.Stats
import com.grappim.taigamobile.core.domain.Swimlane
import com.grappim.taigamobile.core.domain.User
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * All API endpoints
 */
interface TaigaApi {

    /**
     * Users
     */

    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Long): User

    @GET("users/me")
    suspend fun getMyProfile(): User

    @GET("users/{id}/stats")
    suspend fun getUserStats(@Path("id") userId: Long): Stats

    @GET("projects/{id}/member_stats")
    suspend fun getMemberStats(@Path("id") projectId: Long): MemberStatsResponse

    /**
     * Everything related to common tasks (epics, user stories, etc.)
     */

    @GET("{taskPath}/filters_data")
    suspend fun getCommonTaskFiltersData(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Query("project") project: Long,
        @Query("milestone") milestone: Any? = null
    ): FiltersDataResponse

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
    ): List<CommonTaskResponse>

    @GET("userstories/by_ref")
    suspend fun getUserStoryByRef(
        @Query("project") projectId: Long,
        @Query("ref") ref: Int
    ): CommonTaskResponse

    @GET("{taskPath}/{id}")
    suspend fun getCommonTask(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Path("id") id: Long
    ): CommonTaskResponse

    @PATCH("{taskPath}/{id}")
    suspend fun editCommonTask(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Path("id") id: Long,
        @Body editCommonTaskRequest: EditCommonTaskRequest
    )

    @POST("{taskPath}")
    suspend fun createCommonTask(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Body createRequest: CreateCommonTaskRequest
    ): CommonTaskResponse

    @POST("tasks")
    suspend fun createTask(@Body createTaskRequest: CreateTaskRequest): CommonTaskResponse

    @POST("issues")
    suspend fun createIssue(@Body createIssueRequest: CreateIssueRequest): CommonTaskResponse

    @POST("userstories")
    suspend fun createUserstory(
        @Body createUserStoryRequest: CreateUserStoryRequest
    ): CommonTaskResponse

    @DELETE("{taskPath}/{id}")
    suspend fun deleteCommonTask(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Path("id") id: Long
    ): Response<Void>

    @POST("epics/{id}/related_userstories")
    suspend fun linkToEpic(@Path("id") epicId: Long, @Body linkToEpicRequest: LinkToEpicRequest)

    @DELETE("epics/{epicId}/related_userstories/{userStoryId}")
    suspend fun unlinkFromEpic(
        @Path("epicId") epicId: Long,
        @Path("userStoryId") userStoryId: Long
    ): Response<Void>

    @POST("{taskPath}/{id}/promote_to_user_story")
    suspend fun promoteCommonTaskToUserStory(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Path("id") taskId: Long,
        @Body promoteToUserStoryRequest: PromoteToUserStoryRequest
    ): List<Int>

    // Tasks comments

    @PATCH("{taskPath}/{id}")
    suspend fun createCommonTaskComment(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Path("id") id: Long,
        @Body createCommentRequest: CreateCommentRequest
    )

    @GET("history/{taskPath}/{id}?type=comment")
    suspend fun getCommonTaskComments(
        @Path("taskPath") taskPath: CommonTaskPathSingular,
        @Path("id") id: Long
    ): List<Comment>

    @POST("history/{taskPath}/{id}/delete_comment")
    suspend fun deleteCommonTaskComment(
        @Path("taskPath") taskPath: CommonTaskPathSingular,
        @Path("id") id: Long,
        @Query("id") commentId: String
    )

    // Tasks attachments

    @GET("{taskPath}/attachments")
    suspend fun getCommonTaskAttachments(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Query("object_id") storyId: Long,
        @Query("project") projectId: Long
    ): List<Attachment>

    @DELETE("{taskPath}/attachments/{id}")
    suspend fun deleteCommonTaskAttachment(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Path("id") attachmentId: Long
    ): Response<Void>

    @POST("{taskPath}/attachments")
    @Multipart
    suspend fun uploadCommonTaskAttachment(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Part file: MultipartBody.Part,
        @Part project: MultipartBody.Part,
        @Part objectId: MultipartBody.Part
    )

    // Custom attributes

    @GET("{taskPath}-custom-attributes")
    suspend fun getCustomAttributes(
        @Path("taskPath") taskPath: CommonTaskPathSingular,
        @Query("project") projectId: Long
    ): List<CustomAttributeResponse>

    @GET("{taskPath}/custom-attributes-values/{id}")
    suspend fun getCustomAttributesValues(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Path("id") taskId: Long
    ): CustomAttributesValuesResponse

    @PATCH("{taskPath}/custom-attributes-values/{id}")
    suspend fun editCustomAttributesValues(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Path("id") taskId: Long,
        @Body editRequest: EditCustomAttributesValuesRequest
    )

    // Swimlanes

    @GET("swimlanes")
    suspend fun getSwimlanes(@Query("project") project: Long): List<Swimlane>
}
