package com.grappim.taigamobile.feature.wiki.data

import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream
import javax.inject.Inject

class WikiRepositoryImpl @Inject constructor(
    private val session: Session,
    private val wikiApi: WikiApi
) : WikiRepository {

    private val currentProjectId get() = session.currentProject

    override suspend fun getProjectWikiPages() = wikiApi.getProjectWikiPages(
        projectId = currentProjectId
    )

    override suspend fun getProjectWikiPageBySlug(slug: String) = wikiApi.getProjectWikiPageBySlug(
        projectId = currentProjectId,
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

    override suspend fun getPageAttachments(pageId: Long): List<Attachment> =
        wikiApi.getPageAttachments(
            pageId = pageId,
            projectId = currentProjectId
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
        val project = MultipartBody.Part.createFormData("project", currentProjectId.toString())
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

    override suspend fun getWikiLinks() = wikiApi.getWikiLink(
        projectId = currentProjectId
    )

    override suspend fun createWikiLink(href: String, title: String) = wikiApi.createWikiLink(
        newWikiLinkRequest = NewWikiLinkRequest(
            href = href,
            project = currentProjectId,
            title = title
        )
    )

    override suspend fun deleteWikiLink(linkId: Long) {
        wikiApi.deleteWikiLink(
            linkId = linkId
        )
    }
}
