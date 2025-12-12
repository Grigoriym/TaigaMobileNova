package com.grappim.taigamobile.feature.wiki.domain

import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import kotlinx.collections.immutable.ImmutableList
import java.io.InputStream

interface WikiRepository {
    suspend fun getProjectWikiPages(): ImmutableList<WikiPage>
    suspend fun getProjectWikiPageBySlug(slug: String): WikiPage
    suspend fun deleteWikiPage(pageId: Long)
    suspend fun getPageAttachments(pageId: Long): ImmutableList<Attachment>
    suspend fun addPageAttachment(pageId: Long, fileName: String, inputStream: InputStream)
    suspend fun deletePageAttachment(attachmentId: Long)

    suspend fun getWikiLinks(): ImmutableList<WikiLink>
    suspend fun createWikiLink(href: String, title: String)
    suspend fun deleteWikiLink(linkId: Long)
}
