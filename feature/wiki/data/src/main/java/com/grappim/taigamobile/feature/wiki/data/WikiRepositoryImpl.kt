package com.grappim.taigamobile.feature.wiki.data

import com.grappim.taigamobile.core.domain.AttachmentDTO
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.wiki.domain.WikiLink
import com.grappim.taigamobile.feature.wiki.domain.WikiPage
import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import javax.inject.Inject

class WikiRepositoryImpl @Inject constructor(
    private val wikiApi: WikiApi,
    private val taigaStorage: TaigaStorage
) : WikiRepository {

    override suspend fun getProjectWikiPages(): List<WikiPage> = wikiApi.getProjectWikiPages(
        projectId = taigaStorage.currentProjectIdFlow.first()
    )

    override suspend fun getProjectWikiPageBySlug(slug: String): WikiPage =
        wikiApi.getProjectWikiPageBySlug(
            projectId = taigaStorage.currentProjectIdFlow.first(),
            slug = slug
        )

    override suspend fun editWikiPage(pageId: Long, content: String, version: Int) =
        wikiApi.editWikiPage(
            pageId = pageId,
            editWikiPageRequest = EditWikiPageRequest(content, version)
        )

    override suspend fun deleteWikiPage(pageId: Long) {
        wikiApi.deleteWikiPage(
            pageId = pageId
        )
    }

    override suspend fun getPageAttachments(pageId: Long): List<AttachmentDTO> =
        wikiApi.getPageAttachments(
            pageId = pageId,
            projectId = taigaStorage.currentProjectIdFlow.first()
        )

    override suspend fun addPageAttachment(
        pageId: Long,
        fileName: String,
        inputStream: InputStream
    ) {
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

    override suspend fun getWikiLinks(): List<WikiLink> = wikiApi.getWikiLink(
        projectId = taigaStorage.currentProjectIdFlow.first()
    )

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
