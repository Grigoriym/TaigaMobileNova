package com.grappim.taigamobile.testing.api

import com.grappim.taigamobile.feature.wiki.data.CreateWikiPageRequestDTO
import com.grappim.taigamobile.feature.wiki.data.NewWikiLinkRequestDTO
import com.grappim.taigamobile.feature.wiki.data.WikiApi
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiLinkDTO
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiPageDTO

class FakeWikiApi : WikiApi {

    var wikiPagesResult: List<WikiPageDTO> = emptyList()
    var wikiPageBySlugResult: WikiPageDTO? = null
    var wikiLinksResult: List<WikiLinkDTO> = emptyList()
    var createWikiLinkResult: WikiLinkDTO? = null
    var createWikiPageResult: WikiPageDTO? = null

    var errorToThrow: Throwable? = null

    var lastGetProjectWikiPagesProjectId: Long? = null
    var lastGetBySlugProjectId: Long? = null
    var lastGetBySlugSlug: String? = null
    var lastGetWikiLinkProjectId: Long? = null
    var lastCreateWikiLinkRequest: NewWikiLinkRequestDTO? = null
    var lastCreateWikiPageRequest: CreateWikiPageRequestDTO? = null
    var deletedPageIds: MutableList<Long> = mutableListOf()
    var deletedLinkIds: MutableList<Long> = mutableListOf()

    override suspend fun getProjectWikiPages(projectId: Long): List<WikiPageDTO> {
        errorToThrow?.let { throw it }
        lastGetProjectWikiPagesProjectId = projectId
        return wikiPagesResult
    }

    override suspend fun getProjectWikiPageBySlug(projectId: Long, slug: String): WikiPageDTO {
        errorToThrow?.let { throw it }
        lastGetBySlugProjectId = projectId
        lastGetBySlugSlug = slug
        return wikiPageBySlugResult ?: error("wikiPageBySlugResult not set")
    }

    override suspend fun deleteWikiPage(pageId: Long) {
        errorToThrow?.let { throw it }
        deletedPageIds.add(pageId)
    }

    override suspend fun getWikiLink(projectId: Long): List<WikiLinkDTO> {
        errorToThrow?.let { throw it }
        lastGetWikiLinkProjectId = projectId
        return wikiLinksResult
    }

    override suspend fun createWikiLink(body: NewWikiLinkRequestDTO): WikiLinkDTO {
        errorToThrow?.let { throw it }
        lastCreateWikiLinkRequest = body
        return createWikiLinkResult ?: error("createWikiLinkResult not set")
    }

    override suspend fun deleteWikiLink(linkId: Long) {
        errorToThrow?.let { throw it }
        deletedLinkIds.add(linkId)
    }

    override suspend fun createWikiPage(body: CreateWikiPageRequestDTO): WikiPageDTO {
        errorToThrow?.let { throw it }
        lastCreateWikiPageRequest = body
        return createWikiPageResult ?: error("createWikiPageResult not set")
    }
}
