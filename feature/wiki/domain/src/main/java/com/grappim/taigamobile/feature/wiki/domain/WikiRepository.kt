package com.grappim.taigamobile.feature.wiki.domain

import com.grappim.taigamobile.core.domain.Attachment
import java.io.InputStream

interface WikiRepository {
    suspend fun getProjectWikiPages(): List<WikiPage>
    suspend fun getProjectWikiPageBySlug(slug: String): WikiPage
    suspend fun editWikiPage(pageId: Long, content: String, version: Int)
    suspend fun deleteWikiPage(pageId: Long)
    suspend fun getPageAttachments(pageId: Long): List<Attachment>
    suspend fun addPageAttachment(pageId: Long, fileName: String, inputStream: InputStream)
    suspend fun deletePageAttachment(attachmentId: Long)

    suspend fun getWikiLinks(): List<WikiLink>
    suspend fun createWikiLink(href: String, title: String)
    suspend fun deleteWikiLink(linkId: Long)
}
