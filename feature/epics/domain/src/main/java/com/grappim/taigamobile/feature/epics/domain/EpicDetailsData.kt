package com.grappim.taigamobile.feature.epics.domain

import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.CustomFields
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import kotlinx.collections.immutable.ImmutableList

data class EpicDetailsData(
    val epic: Epic,
    val attachments: ImmutableList<Attachment>,
    val customFields: CustomFields,
    val comments: ImmutableList<Comment>,
    val creator: User,
    val assignees: ImmutableList<User>,
    val watchers: ImmutableList<User>,
    val isAssignedToMe: Boolean,
    val isWatchedByMe: Boolean,
    val filtersData: FiltersData
)
