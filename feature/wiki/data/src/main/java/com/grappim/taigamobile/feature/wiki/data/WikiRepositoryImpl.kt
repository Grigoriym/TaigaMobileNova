package com.grappim.taigamobile.feature.wiki.data

import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import com.grappim.taigamobile.feature.workitem.mapper.WikiLinkMapper
import com.grappim.taigamobile.feature.workitem.mapper.WikiPageMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject

class WikiRepositoryImpl @Inject constructor(
    private val wikiApi: WikiApi,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val wikiPageMapper: WikiPageMapper,
    private val wikiLinkMapper: WikiLinkMapper
) : WikiRepository {

    override suspend fun getProjectWikiPages(): ImmutableList<WikiPage> {
        val result = wikiApi.getProjectWikiPages(
            projectId = taigaSessionStorage.getCurrentProjectId()
        )
        return wikiPageMapper.toDomainList(result)
    }

    override suspend fun getProjectWikiPageBySlug(slug: String): WikiPage {
        val result = wikiApi.getProjectWikiPageBySlug(
            projectId = taigaSessionStorage.getCurrentProjectId(),
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
            projectId = taigaSessionStorage.getCurrentProjectId()
        ).toImmutableList()
        return wikiLinkMapper.toDomainList(result)
    }

    override suspend fun createWikiLink(title: String): WikiLink {
        val request = NewWikiLinkRequest(
            project = taigaSessionStorage.getCurrentProjectId(),
            title = title
        )
        val response = wikiApi.createWikiLink(body = request)
        return wikiLinkMapper.toDomain(response)
    }

    override suspend fun deleteWikiLink(linkId: Long) {
        wikiApi.deleteWikiLink(
            linkId = linkId
        )
    }

    override suspend fun createWikiPage(slug: String, content: String): WikiPage {
        val request = CreateWikiPageRequestDTO(
            projectId = taigaSessionStorage.getCurrentProjectId(),
            slug = slug,
            content = content
        )
        val dto = wikiApi.createWikiPage(request)
        return wikiPageMapper.toDomain(dto)
    }
}
