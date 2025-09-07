package com.grappim.taigamobile.data.api

import com.grappim.taigamobile.core.domain.AttachmentDTO
import com.grappim.taigamobile.core.domain.CommonTaskPathPlural
import com.grappim.taigamobile.core.domain.CommonTaskPathSingular
import com.grappim.taigamobile.core.domain.CommonTaskResponse
import com.grappim.taigamobile.core.domain.CustomAttributeResponse
import com.grappim.taigamobile.core.domain.CustomAttributesValuesResponse
import com.grappim.taigamobile.data.model.CreateCommentRequest
import com.grappim.taigamobile.data.model.CreateCommonTaskRequest
import com.grappim.taigamobile.data.model.EditCommonTaskRequest
import com.grappim.taigamobile.data.model.EditCustomAttributesValuesRequest
import com.grappim.taigamobile.data.model.PromoteToUserStoryRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface TaigaApi {

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

    @DELETE("{taskPath}/{id}")
    suspend fun deleteCommonTask(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Path("id") id: Long
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

    // Tasks attachments

    @GET("{taskPath}/attachments")
    suspend fun getCommonTaskAttachments(
        @Path("taskPath") taskPath: CommonTaskPathPlural,
        @Query("object_id") storyId: Long,
        @Query("project") projectId: Long
    ): List<AttachmentDTO>

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
}
