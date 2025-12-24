package com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single

import com.grappim.taigamobile.feature.users.domain.User
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemSingleAssigneeDelegate {
    val singleAssigneeState: StateFlow<WorkItemSingleAssigneeState>

    fun setInitialAssignees(assignees: List<User>, isAssignedToMe: Boolean)

    suspend fun handleUpdateAssignee(
        newAssigneeId: Long?,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    suspend fun handleAssignToMe(
        currentUserId: Long,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    suspend fun handleUnassign(
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

//    fun onGoingToEditAssignee()
}

data class WorkItemSingleAssigneeState(
    val assignees: PersistentList<User> = persistentListOf(),
    val isAssigneesLoading: Boolean = false,
    val isAssignedToMe: Boolean = false,
    val isRemoveAssigneeDialogVisible: Boolean = false,
//    val onGoingToEditAssignee: () -> Unit = {},
    val setIsRemoveAssigneeDialogVisible: (Boolean) -> Unit = { _ -> },
    val onRemoveAssigneeClick: () -> Unit = {}
)
