package com.grappim.taigamobile.feature.workitem.ui.widgets.badge

import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import kotlinx.collections.immutable.ImmutableList

sealed interface SelectableWorkItemBadgeState {
    val options: ImmutableList<StatusUI>
    val currentValue: StatusUI
}

data class SelectableWorkItemBadgeStatus(
    override val options: ImmutableList<StatusUI>,
    override val currentValue: StatusUI
) : SelectableWorkItemBadgeState

data class SelectableWorkItemBadgeType(
    override val options: ImmutableList<StatusUI>,
    override val currentValue: StatusUI
) : SelectableWorkItemBadgeState

data class SelectableWorkItemBadgeSeverity(
    override val options: ImmutableList<StatusUI>,
    override val currentValue: StatusUI
) : SelectableWorkItemBadgeState

data class SelectableWorkItemBadgePriority(
    override val options: ImmutableList<StatusUI>,
    override val currentValue: StatusUI
) : SelectableWorkItemBadgeState
