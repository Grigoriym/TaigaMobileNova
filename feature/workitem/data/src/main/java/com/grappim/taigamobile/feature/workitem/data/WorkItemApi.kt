package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.core.domain.AttachmentDTO
import com.grappim.taigamobile.core.domain.CustomAttributeResponseDTO
import com.grappim.taigamobile.core.domain.CustomAttributesValuesResponseDTO
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkItemApi {

    //Work Item
    @GET("{taskPath}/{id}")
    suspend fun getWorkItemById(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Path("id") id: Long
    ): WorkItemResponseDTO

    @GET("{taskPath}/by_ref")
    suspend fun getWorkItemByRef(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Query("project") project: Long,
        @Query("ref") ref: Int
    ): WorkItemResponseDTO

    @PATCH("{taskPath}/{id}")
    @JvmSuppressWildcards
    suspend fun patchWorkItem(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Path("id") id: Long,
        @Body payload: Map<String, Any?>
    ): WorkItemResponseDTO

    @POST("{taskPath}/{workItemId}/unwatch")
    suspend fun unwatchWorkItem(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Path("workItemId") workItemId: Long,
    )

    @POST("{taskPath}/{workItemId}/watch")
    suspend fun watchWorkItem(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Path("workItemId") workItemId: Long,
    )

    @DELETE("{taskPath}/{workItemId}")
    suspend fun deleteWorkItem(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Path("workItemId") workItemId: Long
    )

    //Attachments
    @GET("{taskPath}/attachments")
    suspend fun getAttachments(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Query("object_id") objectId: Long,
        @Query("project") projectId: Long
    ): List<AttachmentDTO>

    @DELETE("{taskPath}/attachments/{id}")
    suspend fun deleteAttachment(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Path("id") attachmentId: Long
    )

    @POST("{taskPath}/attachments")
    @Multipart
    suspend fun uploadCommonTaskAttachment(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Part file: MultipartBody.Part,
        @Part project: MultipartBody.Part,
        @Part objectId: MultipartBody.Part
    ): AttachmentDTO

    //Custom Attributes
    @GET("{taskPath}-custom-attributes")
    suspend fun getCustomAttributes(
        @Path("taskPath") taskPath: WorkItemPathSingular,
        @Query("project") projectId: Long
    ): List<CustomAttributeResponseDTO>

    @GET("{taskPath}/custom-attributes-values/{id}")
    suspend fun getCustomAttributesValues(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Path("id") id: Long
    ): CustomAttributesValuesResponseDTO

    @PATCH("{taskPath}/custom-attributes-values/{id}")
    @JvmSuppressWildcards
    suspend fun patchCustomAttributesValues(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Path("id") taskId: Long,
        @Body payload: Map<String, Any?>
    ): CustomAttributesValuesResponseDTO
}
