package com.grappim.taigamobile.feature.wiki.data

import com.grappim.taigamobile.feature.workitem.dto.WikiLinkDTO
import com.grappim.taigamobile.feature.workitem.dto.WikiPageDTO
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface WikiApi {
    @GET("wiki")
    suspend fun getProjectWikiPages(@Query("project") projectId: Long): List<WikiPageDTO>

    @GET("wiki/by_slug")
    suspend fun getProjectWikiPageBySlug(@Query("project") projectId: Long, @Query("slug") slug: String): WikiPageDTO

    @DELETE("wiki/{id}")
    suspend fun deleteWikiPage(@Path("id") pageId: Long)

    @GET("wiki-links")
    suspend fun getWikiLink(@Query("project") projectId: Long): List<WikiLinkDTO>

    @POST("wiki-links")
    suspend fun createWikiLink(@Body newWikiLinkRequest: NewWikiLinkRequest)

    @DELETE("wiki-links/{id}")
    suspend fun deleteWikiLink(@Path("id") linkId: Long)
}
