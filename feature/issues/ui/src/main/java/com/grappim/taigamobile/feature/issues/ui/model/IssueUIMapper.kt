package com.grappim.taigamobile.feature.issues.ui.model

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.issues.domain.Issue
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.TagUIMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class IssueUIMapper @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val statusUIMapper: StatusUIMapper,
    private val tagUIMapper: TagUIMapper
) {
    suspend fun toUI(issue: Issue): IssueUI = withContext(ioDispatcher) {
        IssueUI(
            id = issue.id,
            version = issue.version,
            createdDateTime = issue.createdDateTime,
            title = issue.title,
            ref = issue.ref,
            isClosed = issue.isClosed,
            blockedNote = issue.blockedNote,
            description = issue.description,
            copyLinkUrl = issue.copyLinkUrl,
            creatorId = issue.creatorId,
            status = issue.status?.let { statusUIMapper.toUI(it) },
            type = issue.type?.let { statusUIMapper.toUI(it) },
            priority = issue.priority?.let { statusUIMapper.toUI(it) },
            severity = issue.severity?.let { statusUIMapper.toUI(it) },
            assignedUserIds = issue.assignedUserIds,
            watcherUserIds = issue.watcherUserIds,
            tags = tagUIMapper.toUI(issue.tags),
            dueDate = issue.dueDate,
            dueDateStatus = issue.dueDateStatus,
            promotedUserStories = issue.promotedUserStories
        )
    }
}
