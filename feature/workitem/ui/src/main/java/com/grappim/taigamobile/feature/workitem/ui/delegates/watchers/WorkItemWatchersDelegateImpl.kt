package com.grappim.taigamobile.feature.workitem.ui.delegates.watchers

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WatchersData
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkItemWatchersDelegateImpl(
    private val commonTaskType: CommonTaskType,
    private val workItemRepository: WorkItemRepository,
    private val usersRepository: UsersRepository,
    private val patchDataGenerator: PatchDataGenerator,
    private val session: Session,
    private val workItemEditShared: WorkItemEditShared
) : WorkItemWatchersDelegate {

    private val _watchersState = MutableStateFlow(
        WorkItemWatchersState(
            setIsRemoveWatcherDialogVisible = ::setIsRemoveWatcherDialogVisible,
            onGoingToEditWatchers = ::onGoingToEditWatchers,
            onRemoveWatcherClick = ::onRemoveWatcherClick
        )
    )
    override val watchersState: StateFlow<WorkItemWatchersState> = _watchersState.asStateFlow()

    private fun onRemoveWatcherClick(watcherId: Long) {
        _watchersState.update {
            it.copy(
                watcherIdToRemove = watcherId,
                isRemoveWatcherDialogVisible = true
            )
        }
    }

    private fun onGoingToEditWatchers() {
        val watchersIds = _watchersState.value.watchers.mapNotNull { it.id }
            .toPersistentList()
        workItemEditShared.setCurrentWatchers(watchersIds)
    }

    private fun setIsRemoveWatcherDialogVisible(isVisible: Boolean) {
        _watchersState.update {
            it.copy(isRemoveWatcherDialogVisible = isVisible)
        }
    }

    override suspend fun handleAddMeToWatchers(
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((WatchersData) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _watchersState.update {
            it.copy(areWatchersLoading = true)
        }

        resultOf {
            workItemRepository.watchWorkItem(
                workItemId = workItemId,
                commonTaskType = commonTaskType
            )

            val workItem = workItemRepository.getWorkItem(
                workItemId = workItemId,
                commonTaskType = commonTaskType
            )

            val watchers = usersRepository.getUsersList(workItem.watcherUserIds)

            val isWatchedByMe = usersRepository.isAnyAssignedToMe(watchers)

            WatchersData(
                watchers = watchers,
                isWatchedByMe = isWatchedByMe
            )
        }.onSuccess { result ->
            doOnSuccess?.invoke(result)

            _watchersState.update {
                it.copy(
                    areWatchersLoading = false,
                    watchers = result.watchers.toPersistentList(),
                    isWatchedByMe = result.isWatchedByMe
                )
            }
        }.onFailure { error ->
            _watchersState.update {
                it.copy(areWatchersLoading = false)
            }
            doOnError(error)
        }
    }

    override suspend fun handleRemoveMeFromWatchers(
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((WatchersData) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _watchersState.update {
            it.copy(areWatchersLoading = true)
        }

        resultOf {
            workItemRepository.unwatchWorkItem(
                workItemId = workItemId,
                commonTaskType = commonTaskType
            )

            val workItem = workItemRepository.getWorkItem(
                workItemId = workItemId,
                commonTaskType = commonTaskType
            )

            val watchers = usersRepository.getUsersList(workItem.watcherUserIds)
            val isWatchedByMe = usersRepository.isAnyAssignedToMe(watchers)

            WatchersData(
                watchers = watchers,
                isWatchedByMe = isWatchedByMe
            )
        }.onSuccess { result ->
            doOnSuccess?.invoke(result)

            _watchersState.update {
                it.copy(
                    areWatchersLoading = false,
                    watchers = result.watchers.toPersistentList(),
                    isWatchedByMe = result.isWatchedByMe
                )
            }
        }.onFailure { error ->
            _watchersState.update {
                it.copy(areWatchersLoading = false)
            }
            doOnError(error)
        }
    }

    override suspend fun handleRemoveWatcher(
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((version: Long) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _watchersState.update {
            it.copy(areWatchersLoading = true)
        }

        val newWatchers = _watchersState.value.watchers
            .map { it.actualId }
            .filterNot { it == _watchersState.value.watcherIdToRemove }

        resultOf {
            val payload = patchDataGenerator.getWatchersPatchPayload(newWatchers)
            workItemRepository.patchData(
                version = version,
                workItemId = workItemId,
                payload = payload,
                commonTaskType = commonTaskType
            )
        }.onSuccess { result ->
            doOnSuccess?.invoke(result.newVersion)

            val isWatchedByMe = session.userId in newWatchers
            val watchersToSave = _watchersState.value.watchers.removeAll {
                it.actualId == _watchersState.value.watcherIdToRemove
            }

            _watchersState.update {
                it.copy(
                    areWatchersLoading = false,
                    watcherIdToRemove = null,
                    isWatchedByMe = isWatchedByMe,
                    watchers = watchersToSave
                )
            }
        }.onFailure { error ->
            _watchersState.update {
                it.copy(areWatchersLoading = false)
            }
            doOnError(error)
        }
    }

    override suspend fun handleUpdateWatchers(
        version: Long,
        workItemId: Long,
        newWatchers: ImmutableList<Long>,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((version: Long) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _watchersState.update {
            it.copy(areWatchersLoading = true)
        }

        resultOf {
            workItemRepository.updateWatchersData(
                version = version,
                workItemId = workItemId,
                newWatchers = newWatchers,
                commonTaskType = commonTaskType
            )
        }.onSuccess { result ->
            doOnSuccess?.invoke(result.version)

            _watchersState.update {
                it.copy(
                    areWatchersLoading = false,
                    watchers = result.watchers.toPersistentList(),
                    isWatchedByMe = result.isWatchedByMe
                )
            }
        }.onFailure { error ->
            _watchersState.update {
                it.copy(areWatchersLoading = false)
            }
            doOnError(error)
        }
    }

    override fun setInitialWatchers(watchers: List<User>, isWatchedByMe: Boolean) {
        _watchersState.update {
            it.copy(
                watchers = watchers.toPersistentList(),
                isWatchedByMe = isWatchedByMe
            )
        }
    }
}
