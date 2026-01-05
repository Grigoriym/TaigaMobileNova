package com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.AssigneesData
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkItemSingleAssigneeDelegateImpl(
    private val commonTaskType: CommonTaskType,
    private val workItemRepository: WorkItemRepository,
    private val usersRepository: UsersRepository,
    private val patchDataGenerator: PatchDataGenerator
) : WorkItemSingleAssigneeDelegate {

    private val _singleAssigneeState = MutableStateFlow(
        WorkItemSingleAssigneeState(
            onRemoveAssigneeClick = ::onRemoveAssigneeClick,
            setIsRemoveAssigneeDialogVisible = ::setIsRemoveAssigneeDialogVisible
        )
    )
    override val singleAssigneeState: StateFlow<WorkItemSingleAssigneeState> =
        _singleAssigneeState.asStateFlow()

    override fun setInitialAssignees(assignees: List<User>, isAssignedToMe: Boolean) {
        _singleAssigneeState.update {
            it.copy(
                assignees = assignees.toPersistentList(),
                isAssignedToMe = isAssignedToMe
            )
        }
    }

    override suspend fun handleUpdateAssignee(
        newAssigneeId: Long?,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((Long) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _singleAssigneeState.update {
            it.copy(isAssigneesLoading = true)
        }

        resultOf {
            coroutineScope {
                val payload = patchDataGenerator.getAssignedToPatchPayload(assignee = newAssigneeId)

                val patchedData = async {
                    workItemRepository.patchData(
                        version = version,
                        workItemId = workItemId,
                        payload = payload,
                        commonTaskType = commonTaskType
                    )
                }

                val assignees = if (newAssigneeId != null) {
                    usersRepository.getUsersList(listOf(newAssigneeId)).toPersistentList()
                } else {
                    persistentListOf()
                }

                val isAssignedToMe = async {
                    usersRepository.isAnyAssignedToMe(assignees)
                }

                AssigneesData(
                    newVersion = patchedData.await().newVersion,
                    isAssignedToMe = isAssignedToMe.await(),
                    assignees = assignees
                )
            }
        }.onSuccess { data ->
            doOnSuccess?.invoke(data.newVersion)

            _singleAssigneeState.update {
                it.copy(
                    isAssigneesLoading = false,
                    isAssignedToMe = data.isAssignedToMe,
                    assignees = data.assignees
                )
            }
        }.onFailure { error ->
            _singleAssigneeState.update {
                it.copy(isAssigneesLoading = false)
            }
            doOnError(error)
        }
    }

    override suspend fun handleAssignToMe(
        currentUserId: Long,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((Long) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        handleUpdateAssignee(
            newAssigneeId = currentUserId,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = doOnPreExecute,
            doOnSuccess = doOnSuccess,
            doOnError = doOnError
        )
    }

    override suspend fun handleUnassign(
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((Long) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        handleUpdateAssignee(
            newAssigneeId = null,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = doOnPreExecute,
            doOnSuccess = doOnSuccess,
            doOnError = doOnError
        )
    }

    private fun onRemoveAssigneeClick() {
        _singleAssigneeState.update {
            it.copy(
                isRemoveAssigneeDialogVisible = true
            )
        }
    }

    private fun setIsRemoveAssigneeDialogVisible(isVisible: Boolean) {
        _singleAssigneeState.update {
            it.copy(isRemoveAssigneeDialogVisible = isVisible)
        }
    }
}
