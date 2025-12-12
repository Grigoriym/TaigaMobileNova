package com.grappim.taigamobile.feature.wiki.data

import com.grappim.taigamobile.core.api.AttachmentMapper
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import com.grappim.taigamobile.feature.workitem.data.wiki.WikiLinkMapper
import com.grappim.taigamobile.feature.workitem.data.wiki.WikiPageMapper
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
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

    override suspend fun getPageAttachments(pageId: Long): ImmutableList<Attachment> {
        val attachments = wikiApi.getPageAttachments(
            pageId = pageId,
            projectId = taigaStorage.currentProjectIdFlow.first()
        )
        return attachmentMapper.toDomain(attachments)
    }

    override suspend fun addPageAttachment(pageId: Long, fileName: String, inputStream: InputStream) {
        val file = MultipartBody.Part.createFormData(
            name = "attached_file",
            filename = fileName,
            body = inputStream.readBytes().toRequestBody("*/*".toMediaType())
        )
        val project = MultipartBody.Part.createFormData(
            "project",
            taigaStorage.currentProjectIdFlow.first().toString()
        )
        val objectId = MultipartBody.Part.createFormData("object_id", pageId.toString())

        inputStream.use {
            wikiApi.uploadPageAttachment(
                file = file,
                project = project,
                objectId = objectId
            )
        }
    }

    override suspend fun deletePageAttachment(attachmentId: Long) {
        wikiApi.deletePageAttachment(
            attachmentId = attachmentId
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
