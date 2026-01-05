package com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.multiple

import com.grappim.taigamobile.feature.users.domain.User
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemMultipleAssigneesDelegate {
    val multipleAssigneesState: StateFlow<WorkItemMultipleAssigneesState>

    fun setInitialAssignees(assignees: List<User>, isAssignedToMe: Boolean)

    suspend fun handleUpdateAssignees(
        newAssignees: ImmutableList<Long>,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    suspend fun handleAssignToMe(
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    suspend fun handleRemoveAssignee(
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

//    fun onGoingToEditAssignees()

//    fun onRemoveAssigneeClick(user: User)
}

data class WorkItemMultipleAssigneesState(
    val assignees: PersistentList<User> = persistentListOf(),
    val isAssigneesLoading: Boolean = false,
    val isAssignedToMe: Boolean = false,
    val isRemoveAssigneeDialogVisible: Boolean = false,
    val assigneeToRemove: User? = null,
    val onGoingToEditAssignees: () -> Unit = {},
    val setIsRemoveAssigneeDialogVisible: (Boolean) -> Unit = { _ -> },
    val onRemoveAssigneeClick: (User) -> Unit = {}
)
