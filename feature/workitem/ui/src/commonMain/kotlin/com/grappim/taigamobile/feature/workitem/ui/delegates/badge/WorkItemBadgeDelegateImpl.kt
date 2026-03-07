package com.grappim.taigamobile.feature.workitem.ui.delegates.badge

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgePriority
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeSeverity
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeStatus
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeType
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkItemBadgeDelegateImpl(
    private val commonTaskType: CommonTaskType,
    private val workItemRepository: WorkItemRepository,
    private val patchDataGenerator: PatchDataGenerator
) : WorkItemBadgeDelegate {
    private val _badgeState = MutableStateFlow(
        WorkItemBadgeState(
            onBadgeClick = ::onWorkItemBadgeClick,
            onBadgeSheetDismiss = ::onBadgeSheetDismiss
        )
    )
    override val badgeState: StateFlow<WorkItemBadgeState> = _badgeState.asStateFlow()

    override suspend fun handleBadgeSave(
        type: SelectableWorkItemBadgeState,
        item: StatusUI,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((Long) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _badgeState.update {
            it.copy(
                activeBadge = null,
                updatingBadges = it.updatingBadges.add(type)
            )
        }

        resultOf {
            val payload = when (type) {
                is SelectableWorkItemBadgeStatus -> {
                    patchDataGenerator.getStatus(item.id)
                }

                is SelectableWorkItemBadgeType -> {
                    patchDataGenerator.getType(item.id)
                }

                is SelectableWorkItemBadgeSeverity -> {
                    patchDataGenerator.getSeverity(item.id)
                }

                is SelectableWorkItemBadgePriority -> {
                    patchDataGenerator.getPriority(item.id)
                }
            }

            workItemRepository.patchData(
                version = version,
                workItemId = workItemId,
                payload = payload,
                commonTaskType = commonTaskType
            )
        }.onSuccess { patchedData ->
            doOnSuccess?.invoke(patchedData.newVersion)

            _badgeState.update { currentState ->
                val updatedWorkItemBadges = currentState.workItemBadges.map { badge ->
                    if (badge == type) {
                        when (badge) {
                            is SelectableWorkItemBadgeStatus -> {
                                badge.copy(currentValue = item)
                            }

                            is SelectableWorkItemBadgeType -> {
                                badge.copy(currentValue = item)
                            }

                            is SelectableWorkItemBadgeSeverity -> {
                                badge.copy(currentValue = item)
                            }

                            is SelectableWorkItemBadgePriority -> {
                                badge.copy(currentValue = item)
                            }
                        }
                    } else {
                        badge
                    }
                }

                currentState.copy(
                    activeBadge = null,
                    workItemBadges = updatedWorkItemBadges.toPersistentSet(),
                    updatingBadges = currentState.updatingBadges.remove(type)
                )
            }
        }.onFailure { error ->
            _badgeState.update {
                it.copy(
                    activeBadge = null,
                    updatingBadges = it.updatingBadges.remove(type)
                )
            }
            doOnError(error)
        }
    }

    override fun setWorkItemBadges(badges: ImmutableSet<SelectableWorkItemBadgeState>) {
        _badgeState.update {
            it.copy(workItemBadges = badges)
        }
    }

    private fun onWorkItemBadgeClick(type: SelectableWorkItemBadgeState) {
        _badgeState.update {
            it.copy(activeBadge = type)
        }
    }

    private fun onBadgeSheetDismiss() {
        _badgeState.update {
            it.copy(activeBadge = null)
        }
    }
}
