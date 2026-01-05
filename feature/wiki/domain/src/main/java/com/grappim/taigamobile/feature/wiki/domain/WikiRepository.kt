package com.grappim.taigamobile.feature.wiki.domain

import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import kotlinx.collections.immutable.ImmutableList

interface WikiRepository {
    suspend fun getProjectWikiPages(): ImmutableList<WikiPage>
    suspend fun getProjectWikiPageBySlug(slug: String): WikiPage
    suspend fun deleteWikiPage(pageId: Long)

    suspend fun getWikiLinks(): ImmutableList<WikiLink>
    suspend fun createWikiLink(title: String): WikiLink
    suspend fun deleteWikiLink(linkId: Long)

    suspend fun createWikiPage(slug: String, content: String): WikiPage
}
