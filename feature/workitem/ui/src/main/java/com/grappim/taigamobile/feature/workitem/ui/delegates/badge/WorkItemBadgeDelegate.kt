package com.grappim.taigamobile.feature.workitem.ui.delegates.badge

import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemBadgeDelegate {
    val badgeState: StateFlow<WorkItemBadgeState>

    suspend fun handleBadgeSave(
        type: SelectableWorkItemBadgeState,
        item: StatusUI,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    fun setWorkItemBadges(badges: ImmutableSet<SelectableWorkItemBadgeState>)
}

data class WorkItemBadgeState(
    val activeBadge: SelectableWorkItemBadgeState? = null,
    val workItemBadges: ImmutableSet<SelectableWorkItemBadgeState> = persistentSetOf(),
    val onBadgeClick: (SelectableWorkItemBadgeState) -> Unit = {},
    val updatingBadges: PersistentSet<SelectableWorkItemBadgeState> = persistentSetOf(),
    val onBadgeSheetDismiss: () -> Unit = {}
)
