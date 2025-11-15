package com.grappim.taigamobile.feature.workitem.ui.delegates.badge

import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemBadgeDelegate {
    val badgeState: StateFlow<WorkItemBadgeState>

    fun onBadgeSave(type: SelectableWorkItemBadgeState, onSaveBadgeToBackend: () -> Unit)
    fun setWorkItemBadges(badges: ImmutableSet<SelectableWorkItemBadgeState>)
    fun onBadgeSaveSuccess(type: SelectableWorkItemBadgeState, item: StatusUI)
    fun onBadgeSaveError()

    fun getBadgePatchPayload(
        type: SelectableWorkItemBadgeState,
        item: StatusUI
    ): ImmutableMap<String, Any?>
}

data class WorkItemBadgeState(
    val activeBadge: SelectableWorkItemBadgeState? = null,
    val workItemBadges: ImmutableSet<SelectableWorkItemBadgeState> = persistentSetOf(),
    val onBadgeClick: (SelectableWorkItemBadgeState) -> Unit = {},
    val updatingBadges: PersistentSet<SelectableWorkItemBadgeState> = persistentSetOf(),
    val onBadgeSheetDismiss: () -> Unit = {}
)
