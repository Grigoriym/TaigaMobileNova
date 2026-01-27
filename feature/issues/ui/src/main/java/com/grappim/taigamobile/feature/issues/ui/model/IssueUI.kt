package com.grappim.taigamobile.feature.issues.ui.model

import com.grappim.taigamobile.feature.workitem.domain.PromotedUserStoryInfo
import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDateTime

data class IssueUI(
    val id: Long,
    val version: Long,
    val createdDateTime: LocalDateTime,
    val title: String,
    val ref: Long,
    val isClosed: Boolean,
    val blockedNote: String? = null,
    val description: String,
    val copyLinkUrl: String,
    val creatorId: Long,

    val status: StatusUI?,
    val type: StatusUI? = null,
    val priority: StatusUI? = null,
    val severity: StatusUI? = null,

    val assignedUserIds: List<Long>,
    val watcherUserIds: List<Long>,

    val tags: ImmutableList<SelectableTagUI> = persistentListOf(),
    val promotedUserStories: ImmutableList<PromotedUserStoryInfo>
)
