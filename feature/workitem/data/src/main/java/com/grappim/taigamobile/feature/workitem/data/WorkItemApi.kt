package com.grappim.taigamobile.feature.workitem.data

import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathSingular
import com.grappim.taigamobile.feature.workitem.dto.AttachmentDTO
import com.grappim.taigamobile.feature.workitem.dto.CreateWorkItemRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.CustomAttributeResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.PromoteToUserStoryRequestDTO
import com.grappim.taigamobile.feature.workitem.dto.WikiPageDTO
import com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO
import kotlinx.serialization.json.JsonObject
import okhttp3.MultipartBody
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

interface WorkItemApi {

    // Work Item
    @GET("{taskPath}/{id}")
    suspend fun getWorkItemById(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Path("id") id: Long
    ): WorkItemResponseDTO

    @GET("{taskPath}")
    suspend fun getWorkItems(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Query("project") project: Long? = null,
        @Query("assigned_to") assignedId: Long? = null,
        @Query("assigned_to", encoded = true) assignedIds: String? = null,
        @Query("watchers") watcherId: Long? = null,
        @Query("status__is_closed") isClosed: Boolean? = null,
        @Query("milestone") sprint: Long? = null,
        @Query("user_story") userStory: Any? = null,
        @Query("dashboard") isDashboard: Boolean? = null
    ): List<WorkItemResponseDTO>

    @GET("{taskPath}")
    suspend fun getWorkItemsPagination(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int = 20,
        @Query("q") query: String? = null,
        @Query("project") project: Long? = null,
        @Query("milestone") sprint: Any? = null,
        @Query("assigned_to", encoded = true) assignedIds: String? = null,
        @Query("owner", encoded = true) ownerIds: String? = null,
        @Query("role", encoded = true) roles: String? = null,
        @Query("status", encoded = true) statuses: String? = null,
        @Query("epic", encoded = true) epics: String? = null,
        @Query("tags", encoded = true) tags: String? = null,
        @Query("priority", encoded = true) priorities: String? = null,
        @Query("severity", encoded = true) severities: String? = null,
        @Query("type", encoded = true) types: String? = null,
        @Header("x-disable-pagination") disablePagination: Boolean? = (page == null).takeIf { it }
    ): retrofit2.Response<List<WorkItemResponseDTO>>

    @POST("{taskPath}")
    suspend fun createWorkItem(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Body createRequest: CreateWorkItemRequestDTO
    ): WorkItemResponseDTO

    @GET("{taskPath}/by_ref")
    suspend fun getWorkItemByRef(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Query("project") project: Long,
        @Query("ref") ref: Long
    ): WorkItemResponseDTO

    @PATCH("{taskPath}/{id}")
    suspend fun patchWorkItem(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Path("id") id: Long,
        @Body payload: JsonObject
    ): WorkItemResponseDTO

    @POST("{taskPath}/{workItemId}/unwatch")
    suspend fun unwatchWorkItem(@Path("taskPath") taskPath: WorkItemPathPlural, @Path("workItemId") workItemId: Long)

    @POST("{taskPath}/{workItemId}/watch")
    suspend fun watchWorkItem(@Path("taskPath") taskPath: WorkItemPathPlural, @Path("workItemId") workItemId: Long)

    @DELETE("{taskPath}/{workItemId}")
    suspend fun deleteWorkItem(@Path("taskPath") taskPath: WorkItemPathPlural, @Path("workItemId") workItemId: Long)

    /**
     * Though they return a list, actually they always return only one value in the list, just fyi
     */
    @POST("{taskPath}/{workItemId}/promote_to_user_story")
    suspend fun promoteToUserStory(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Path("workItemId") workItemId: Long,
        @Body body: PromoteToUserStoryRequestDTO
    ): List<Long>

    // Attachments
    @GET("{taskPath}/attachments")
    suspend fun getAttachments(
        @Path("taskPath") taskPath: String,
        @Query("object_id") objectId: Long,
        @Query("project") projectId: Long
    ): List<AttachmentDTO>

    @DELETE("{taskPath}/attachments/{id}")
    suspend fun deleteAttachment(@Path("taskPath") taskPath: String, @Path("id") attachmentId: Long)

    @POST("{taskPath}/attachments")
    @Multipart
    suspend fun uploadCommonTaskAttachment(
        @Path("taskPath") taskPath: String,
        @Part file: MultipartBody.Part,
        @Part project: MultipartBody.Part,
        @Part objectId: MultipartBody.Part
    ): AttachmentDTO

    // Custom Attributes
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
    suspend fun patchCustomAttributesValues(
        @Path("taskPath") taskPath: WorkItemPathPlural,
        @Path("id") taskId: Long,
        @Body payload: JsonObject
    ): CustomAttributesValuesResponseDTO

    // wiki
    @PATCH("wiki/{id}")
    suspend fun patchWikiPage(@Path("id") pageId: Long, @Body payload: JsonObject): WikiPageDTO
}
