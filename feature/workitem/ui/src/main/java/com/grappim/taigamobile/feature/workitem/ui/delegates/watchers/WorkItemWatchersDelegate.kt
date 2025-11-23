package com.grappim.taigamobile.feature.workitem.ui.delegates.watchers

import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.feature.workitem.domain.WatchersData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemWatchersDelegate {
    val watchersState: StateFlow<WorkItemWatchersState>

    suspend fun handleAddMeToWatchers(
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((WatchersData) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    suspend fun handleRemoveMeFromWatchers(
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((WatchersData) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    suspend fun handleRemoveWatcher(
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((version: Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    suspend fun handleUpdateWatchers(
        version: Long,
        workItemId: Long,
        newWatchers: ImmutableList<Long>,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((version: Long) -> Unit)? = null,
        doOnError: (Throwable) -> Unit
    )

    fun setInitialWatchers(watchers: List<User>, isWatchedByMe: Boolean)
}

data class WorkItemWatchersState(
    val watchers: PersistentList<User> = persistentListOf(),
    val areWatchersLoading: Boolean = false,
    val isWatchedByMe: Boolean = false,
    val isRemoveWatcherDialogVisible: Boolean = false,
    val setIsRemoveWatcherDialogVisible: (Boolean) -> Unit = {},
    val onGoingToEditWatchers: () -> Unit = {},
    val watcherIdToRemove: Long? = null,
    val onRemoveWatcherClick: (Long) -> Unit = {}
)
