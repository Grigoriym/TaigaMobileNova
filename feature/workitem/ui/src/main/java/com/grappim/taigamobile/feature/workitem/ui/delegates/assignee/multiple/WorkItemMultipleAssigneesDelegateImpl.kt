package com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.multiple

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.AssigneesData
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkItemMultipleAssigneesDelegateImpl(
    private val commonTaskType: CommonTaskType,
    private val workItemRepository: WorkItemRepository,
    private val usersRepository: UsersRepository,
    private val patchDataGenerator: PatchDataGenerator,
    private val workItemEditShared: WorkItemEditShared,
    private val session: Session
) : WorkItemMultipleAssigneesDelegate {

    private val _multipleAssigneesState = MutableStateFlow(
        WorkItemMultipleAssigneesState(
            onGoingToEditAssignees = ::onGoingToEditAssignees,
            setIsRemoveAssigneeDialogVisible = ::setIsRemoveAssigneeDialogVisible,
            onRemoveAssigneeClick = ::onRemoveAssigneeClick
        )
    )
    override val multipleAssigneesState: StateFlow<WorkItemMultipleAssigneesState> =
        _multipleAssigneesState.asStateFlow()

    override fun setInitialAssignees(assignees: List<User>, isAssignedToMe: Boolean) {
        _multipleAssigneesState.update {
            it.copy(
                assignees = assignees.toPersistentList(),
                isAssignedToMe = isAssignedToMe
            )
        }
    }

    override suspend fun handleUpdateAssignees(
        newAssignees: ImmutableList<Long>,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((Long) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _multipleAssigneesState.update {
            it.copy(isAssigneesLoading = true)
        }

        resultOf {
            coroutineScope {
                val payload = patchDataGenerator.getAssignedUsersPatchPayload(
                    assignees = newAssignees
                )

                val patchedData = async {
                    workItemRepository.patchData(
                        version = version,
                        workItemId = workItemId,
                        payload = payload,
                        commonTaskType = commonTaskType
                    )
                }

                val assignees = async {
                    usersRepository.getUsersList(newAssignees.toList())
                }

                val isAssignedToMe = async {
                    usersRepository.isAnyAssignedToMe(assignees.await())
                }

                AssigneesData(
                    newVersion = patchedData.await().newVersion,
                    isAssignedToMe = isAssignedToMe.await(),
                    assignees = assignees.await().toPersistentList()
                )
            }
        }.onSuccess { data ->
            doOnSuccess?.invoke(data.newVersion)

            _multipleAssigneesState.update {
                it.copy(
                    isAssigneesLoading = false,
                    isAssignedToMe = data.isAssignedToMe,
                    assignees = data.assignees.toPersistentList()
                )
            }
        }.onFailure { error ->
            _multipleAssigneesState.update {
                it.copy(isAssigneesLoading = false)
            }
            doOnError(error)
        }
    }

    override suspend fun handleAssignToMe(
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((Long) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        val currentAssignees = _multipleAssigneesState.value.assignees
            .mapNotNull { it.id }
            .toPersistentList()
            .add(session.userId)

        handleUpdateAssignees(
            newAssignees = currentAssignees.toImmutableList(),
            version = version,
            workItemId = workItemId,
            doOnPreExecute = doOnPreExecute,
            doOnSuccess = doOnSuccess,
            doOnError = doOnError
        )
    }

    override suspend fun handleRemoveAssignee(
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((Long) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        val assigneeToRemove = _multipleAssigneesState.value.assigneeToRemove ?: return

        val newAssignees = _multipleAssigneesState.value.assignees.removeAll {
            it.actualId == assigneeToRemove.actualId
        }.map { it.actualId }.toImmutableList()

        _multipleAssigneesState.update {
            it.copy(isRemoveAssigneeDialogVisible = false)
        }

        handleUpdateAssignees(
            newAssignees = newAssignees,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = doOnPreExecute,
            doOnSuccess = doOnSuccess,
            doOnError = doOnError
        )
    }

    override fun onGoingToEditAssignees() {
        val assigneesIds = _multipleAssigneesState.value.assignees
            .mapNotNull { it.id }
            .toImmutableList()
        workItemEditShared.setCurrentAssignees(assigneesIds)
    }

    private fun setIsRemoveAssigneeDialogVisible(isVisible: Boolean) {
        _multipleAssigneesState.update {
            it.copy(
                isRemoveAssigneeDialogVisible = isVisible,
                assigneeToRemove = null
            )
        }
    }

    private fun onRemoveAssigneeClick(user: User) {
        _multipleAssigneesState.update {
            it.copy(
                isRemoveAssigneeDialogVisible = true,
                assigneeToRemove = user
            )
        }
    }
}
