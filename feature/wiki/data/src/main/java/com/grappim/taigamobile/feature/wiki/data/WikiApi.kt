package com.grappim.taigamobile.feature.wiki.data

import com.grappim.taigamobile.core.domain.AttachmentDTO
import com.grappim.taigamobile.feature.workitem.data.wiki.WikiLinkDTO
import com.grappim.taigamobile.feature.workitem.data.wiki.WikiPageDTO
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface WikiApi {
    @GET("wiki")
    suspend fun getProjectWikiPages(@Query("project") projectId: Long): List<WikiPageDTO>

    @GET("wiki/by_slug")
    suspend fun getProjectWikiPageBySlug(@Query("project") projectId: Long, @Query("slug") slug: String): WikiPageDTO

    @GET("wiki/attachments")
    suspend fun getPageAttachments(
        @Query("object_id") pageId: Long,
        @Query("project") projectId: Long
    ): List<AttachmentDTO>

    @POST("wiki/attachments")
    @Multipart
    suspend fun uploadPageAttachment(
        @Part file: MultipartBody.Part,
        @Part project: MultipartBody.Part,
        @Part objectId: MultipartBody.Part
    )

    @DELETE("wiki/attachments/{id}")
    suspend fun deletePageAttachment(@Path("id") attachmentId: Long): Response<Void>

    @DELETE("wiki/{id}")
    suspend fun deleteWikiPage(@Path("id") pageId: Long): Response<Void>

    @GET("wiki-links")
    suspend fun getWikiLink(@Query("project") projectId: Long): List<WikiLinkDTO>

    @POST("wiki-links")
    suspend fun createWikiLink(@Body newWikiLinkRequest: NewWikiLinkRequest)

    @DELETE("wiki-links/{id}")
    suspend fun deleteWikiLink(@Path("id") linkId: Long): Response<Void>
}
