package com.grappim.taigamobile.testing.repo

import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class FakeWikiRepository : WikiRepository {

    var getProjectWikiPagesResult: Result<ImmutableList<WikiPage>> =
        Result.success(persistentListOf())

    var deleteWikiPageThrows: Throwable? = null
    var deleteWikiPageCalled = false
    var deleteWikiPageId: Long? = null

    override suspend fun getProjectWikiPages(): ImmutableList<WikiPage> =
        getProjectWikiPagesResult.getOrThrow()

    var getProjectWikiPageBySlugResult: WikiPage? = null
    var getProjectWikiPageBySlugThrows: Throwable? = null

    override suspend fun getProjectWikiPageBySlug(slug: String): WikiPage {
        getProjectWikiPageBySlugThrows?.let { throw it }
        return getProjectWikiPageBySlugResult ?: error("getProjectWikiPageBySlugResult not set")
    }

    override suspend fun deleteWikiPage(pageId: Long) {
        deleteWikiPageCalled = true
        deleteWikiPageId = pageId
        deleteWikiPageThrows?.let { throw it }
    }

    var getWikiLinksResult: ImmutableList<WikiLink> = persistentListOf()
    var getWikiLinksThrows: Throwable? = null

    override suspend fun getWikiLinks(): ImmutableList<WikiLink> {
        getWikiLinksThrows?.let { throw it }
        return getWikiLinksResult
    }

    var createWikiLinkResult: WikiLink? = null
    var createWikiLinkThrows: Throwable? = null
    var createWikiLinkCalled = false
    var createWikiLinkTitle: String? = null

    override suspend fun createWikiLink(title: String): WikiLink {
        createWikiLinkCalled = true
        createWikiLinkTitle = title
        createWikiLinkThrows?.let { throw it }
        return createWikiLinkResult ?: error("createWikiLinkResult not set")
    }

    var deleteWikiLinkCalled = false
    var deleteWikiLinkId: Long? = null

    override suspend fun deleteWikiLink(linkId: Long) {
        deleteWikiLinkCalled = true
        deleteWikiLinkId = linkId
    }

    override suspend fun createWikiPage(slug: String, content: String): WikiPage {
        TODO("Not yet implemented")
    }
}
