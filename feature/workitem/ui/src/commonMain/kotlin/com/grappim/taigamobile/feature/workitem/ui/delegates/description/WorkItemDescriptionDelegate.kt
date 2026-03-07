package com.grappim.taigamobile.feature.workitem.ui.delegates.description

import kotlinx.coroutines.flow.StateFlow

interface WorkItemDescriptionDelegate {
    val descriptionState: StateFlow<WorkItemDescriptionState>

    suspend fun updateDescription(
        newDescription: String,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: ((newVersion: Long) -> Unit)? = null,
        doOnError: suspend (Throwable) -> Unit
    )

    fun setInitialDescription(description: String)
}

data class WorkItemDescriptionState(val isDescriptionLoading: Boolean = false, val currentDescription: String = "")
