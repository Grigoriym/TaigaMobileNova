package com.grappim.taigamobile.feature.wiki.domain

import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.canModifyWikiPage
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class WikiPageUseCase @Inject constructor(
    private val wikiRepository: WikiRepository,
    private val usersRepository: UsersRepository,
    private val workItemRepository: WorkItemRepository,
    private val projectsRepository: ProjectsRepository
) {

    suspend fun getWikiPageData(pageSlug: String): Result<WikiPageData> = resultOf {
        coroutineScope {
            val page = wikiRepository.getProjectWikiPageBySlug(pageSlug)
            val user = page.lastModifier?.let {
                async { usersRepository.getUser(it) }
            }

            val wikiLink = async { wikiRepository.getWikiLinks().find { it.ref == pageSlug } }
            val attachments = async {
                workItemRepository.getWorkItemAttachments(
                    workItemId = page.id,
                    taskIdentifier = TaskIdentifier.Wiki
                )
            }

            WikiPageData(
                page = page,
                user = user?.await(),
                wikiLink = wikiLink.await(),
                attachments = attachments.await(),
                canModifyPage = projectsRepository.getPermissions().canModifyWikiPage()
            )
        }
    }

    suspend fun deleteWikiPage(wikiLinkId: Long?, pageId: Long) = resultOf {
        wikiRepository.deleteWikiPage(pageId)
        wikiLinkId?.let { wikiRepository.deleteWikiLink(it) }
    }
}
