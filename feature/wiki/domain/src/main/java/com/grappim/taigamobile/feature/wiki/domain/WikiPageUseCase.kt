package com.grappim.taigamobile.feature.wiki.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class WikiPageUseCase @Inject constructor(
    private val wikiRepository: WikiRepository,
    private val usersRepository: UsersRepository,
    private val workItemRepository: WorkItemRepository
) {

    suspend fun getWikiPageData(pageSlug: String): Result<WikiPageData> = resultOf {
        coroutineScope {
            val page = wikiRepository.getProjectWikiPageBySlug(pageSlug)
            val user = async { usersRepository.getUser(page.lastModifier) }

            val wikiLink = async { wikiRepository.getWikiLinks().find { it.ref == pageSlug } }
            val attachments = async {
                workItemRepository.getWorkItemAttachments(
                    workItemId = page.id,
                    commonTaskType = CommonTaskType.Wiki
                )
            }

            WikiPageData(
                page = page,
                user = user.await(),
                wikiLink = wikiLink.await(),
                attachments = attachments.await()
            )
        }
    }

    suspend fun deleteWikiPage(wikiLinkId: Long?, pageId: Long) = resultOf {
        wikiRepository.deleteWikiPage(pageId)
        wikiLinkId?.let { wikiRepository.deleteWikiLink(it) }
    }
}
