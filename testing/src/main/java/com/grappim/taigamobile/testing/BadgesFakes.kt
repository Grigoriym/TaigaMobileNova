package com.grappim.taigamobile.testing

import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeStatus
import kotlinx.collections.immutable.persistentListOf

fun getSelectableWorkItemBadgeState(): SelectableWorkItemBadgeState =
    SelectableWorkItemBadgeStatus(
        options = persistentListOf(),
        currentValue = getStatusUI()
    )
