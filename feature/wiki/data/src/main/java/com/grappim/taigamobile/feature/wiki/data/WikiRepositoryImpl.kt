package com.grappim.taigamobile.feature.wiki.data

import com.grappim.taigamobile.core.api.AttachmentMapper
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import com.grappim.taigamobile.feature.workitem.data.wiki.WikiLinkMapper
import com.grappim.taigamobile.feature.workitem.data.wiki.WikiPageMapper
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class WikiRepositoryImpl @Inject constructor(
    private val wikiApi: WikiApi,
    private val taigaStorage: TaigaStorage,
    private val attachmentMapper: AttachmentMapper,
    private val wikiPageMapper: WikiPageMapper,
    private val wikiLinkMapper: WikiLinkMapper
) : WikiRepository {

    override suspend fun getProjectWikiPages(): ImmutableList<WikiPage> {
        val result = wikiApi.getProjectWikiPages(
            projectId = taigaStorage.currentProjectIdFlow.first()
        )
        return wikiPageMapper.toDomainList(result)
    }

    override suspend fun getProjectWikiPageBySlug(slug: String): WikiPage {
        val result = wikiApi.getProjectWikiPageBySlug(
            projectId = taigaStorage.currentProjectIdFlow.first(),
            slug = slug
        )
        return wikiPageMapper.toDomain(result)
    }

    override suspend fun deleteWikiPage(pageId: Long) {
        wikiApi.deleteWikiPage(
            pageId = pageId
        )
    }

    override suspend fun getWikiLinks(): ImmutableList<WikiLink> {
        val result = wikiApi.getWikiLink(
            projectId = taigaStorage.currentProjectIdFlow.first()
        ).toImmutableList()
        return wikiLinkMapper.toDomainList(result)
    }

    override suspend fun createWikiLink(href: String, title: String) {
        val request = NewWikiLinkRequest(
            href = href,
            project = taigaStorage.currentProjectIdFlow.first(),
            title = title
        )
        wikiApi.createWikiLink(newWikiLinkRequest = request)
    }

    override suspend fun deleteWikiLink(linkId: Long) {
        wikiApi.deleteWikiLink(
            linkId = linkId
        )
    }
}
