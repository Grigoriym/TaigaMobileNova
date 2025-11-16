package com.grappim.taigamobile.feature.issues.data

import com.grappim.taigamobile.core.domain.AttachmentDTO
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
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
    ): List<CommonTaskResponse>

    @POST("issues")
    suspend fun createIssue(@Body createIssueRequest: CreateIssueRequest): CommonTaskResponse

    @GET("issues/attachments")
    suspend fun getIssueAttachments(
        @Query("object_id") storyId: Long,
        @Query("project") projectId: Long
    ): List<AttachmentDTO>

    @DELETE("issues/attachments/{id}")
    suspend fun deleteAttachment(@Path("id") attachmentId: Long)

    @DELETE("issues/{id}")
    suspend fun deleteCommonTask(@Path("id") id: Long)

    @POST("issues/attachments")
    @Multipart
    suspend fun uploadCommonTaskAttachment(
        @Part file: MultipartBody.Part,
        @Part project: MultipartBody.Part,
        @Part objectId: MultipartBody.Part
    ): AttachmentDTO
}
