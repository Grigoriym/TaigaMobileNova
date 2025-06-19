package com.grappim.taigamobile.feature.wiki.data

import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.feature.wiki.domain.WikiLink
import com.grappim.taigamobile.feature.wiki.domain.WikiPage
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

interface WikiApi {
    @GET("wiki")
    suspend fun getProjectWikiPages(@Query("project") projectId: Long): List<WikiPage>

    @GET("wiki/by_slug")
    suspend fun getProjectWikiPageBySlug(
        @Query("project") projectId: Long,
        @Query("slug") slug: String
    ): WikiPage

    @PATCH("wiki/{id}")
    suspend fun editWikiPage(
        @Path("id") pageId: Long,
        @Body editWikiPageRequest: EditWikiPageRequest
    )

    @GET("wiki/attachments")
    suspend fun getPageAttachments(
        @Query("object_id") pageId: Long,
        @Query("project") projectId: Long
    ): List<Attachment>

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
    suspend fun getWikiLink(@Query("project") projectId: Long): List<WikiLink>

    @POST("wiki-links")
    suspend fun createWikiLink(@Body newWikiLinkRequest: NewWikiLinkRequest)

    @DELETE("wiki-links/{id}")
    suspend fun deleteWikiLink(@Path("id") linkId: Long): Response<Void>
}
