package com.grappim.taigamobile.feature.wiki.data

import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiLinkDTO
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiPageDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.discardRemaining
import org.koin.core.annotation.Single

@Single
class WikiApi(private val httpClient: HttpClient) {

    suspend fun getProjectWikiPages(projectId: Long): List<WikiPageDTO> = httpClient.get("wiki") {
        url { parameters.append("project", projectId.toString()) }
    }.body()

    suspend fun getProjectWikiPageBySlug(projectId: Long, slug: String): WikiPageDTO = httpClient.get("wiki/by_slug") {
        url {
            parameters.append("project", projectId.toString())
            parameters.append("slug", slug)
        }
    }.body()

    suspend fun deleteWikiPage(pageId: Long) {
        httpClient.delete("wiki/$pageId").discardRemaining()
    }

    suspend fun getWikiLink(projectId: Long): List<WikiLinkDTO> = httpClient.get("wiki-links") {
        url { parameters.append("project", projectId.toString()) }
    }.body()

    suspend fun createWikiLink(body: NewWikiLinkRequestDTO): WikiLinkDTO = httpClient.post("wiki-links") {
        setBody(body)
    }.body()

    suspend fun deleteWikiLink(linkId: Long) {
        httpClient.delete("wiki-links/$linkId").discardRemaining()
    }

    suspend fun createWikiPage(body: CreateWikiPageRequestDTO): WikiPageDTO = httpClient.post("wiki") {
        setBody(body)
    }.body()
}
