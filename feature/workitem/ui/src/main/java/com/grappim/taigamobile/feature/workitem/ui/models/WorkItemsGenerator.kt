package com.grappim.taigamobile.feature.workitem.ui.models

import com.grappim.taigamobile.core.async.IoDispatcher
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgePriority
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeSeverity
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeStatus
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeType
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WorkItemsGenerator @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val statusUIMapper: StatusUIMapper
) {
    suspend fun getItems(
        statusUI: StatusUI? = null,
        typeUI: StatusUI? = null,
        severityUI: StatusUI? = null,
        priorityUi: StatusUI? = null,
        filtersData: FiltersData
    ): ImmutableSet<SelectableWorkItemBadgeState> = withContext(dispatcher) {
        val workItemBadges = mutableSetOf<SelectableWorkItemBadgeState>()

        if (statusUI != null) {
            workItemBadges.add(
                SelectableWorkItemBadgeStatus(
                    options = filtersData.statuses.map {
                        statusUIMapper.toUI(it)
                    }.toImmutableList(),
                    currentValue = statusUI
                )
            )
        }

        if (typeUI != null) {
            workItemBadges.add(
                SelectableWorkItemBadgeType(
                    options = filtersData.types.map {
                        statusUIMapper.toUI(it)
                    }.toImmutableList(),
                    currentValue = typeUI
                )
            )
        }

        if (severityUI != null) {
            workItemBadges.add(
                SelectableWorkItemBadgeSeverity(
                    options = filtersData.severities.map {
                        statusUIMapper.toUI(it)
                    }.toImmutableList(),
                    currentValue = severityUI
                )
            )
        }

        if (priorityUi != null) {
            workItemBadges.add(
                SelectableWorkItemBadgePriority(
                    options = filtersData.priorities.map {
                        statusUIMapper.toUI(it)
                    }.toImmutableList(),
                    currentValue = priorityUi
                )
            )
        }

        workItemBadges.toImmutableSet()
    }
}
