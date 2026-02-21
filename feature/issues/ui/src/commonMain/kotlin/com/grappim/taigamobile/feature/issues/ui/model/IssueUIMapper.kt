package com.grappim.taigamobile.feature.issues.ui.model

import com.grappim.taigamobile.feature.issues.domain.Issue
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import org.koin.core.annotation.Factory

@Factory
class IssueUIMapper(private val statusUIMapper: StatusUIMapper, private val tagUIMapper: TagUIMapper) {
    fun toUI(issue: Issue): IssueUI = IssueUI(
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
        tags = tagUIMapper.toSelectableUI(issue.tags),
        promotedUserStories = issue.promotedUserStories
    )
}
