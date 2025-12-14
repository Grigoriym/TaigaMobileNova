package com.grappim.taigamobile.feature.userstories.domain

import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.CustomFields
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.users.domain.User
import kotlinx.collections.immutable.ImmutableList

data class UserStoryDetailsData(
    val userStory: UserStory,
    val attachments: ImmutableList<Attachment>,
    val sprint: Sprint?,
    val customFields: CustomFields,
    val comments: ImmutableList<Comment>,
    val creator: User,
    val assignees: ImmutableList<User>,
    val watchers: ImmutableList<User>,
    val isAssignedToMe: Boolean,
    val isWatchedByMe: Boolean,
    val filtersData: FiltersData
)
