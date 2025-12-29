package com.grappim.taigamobile.feature.workitem.ui.delegates.sprint

import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

interface WorkItemSprintDelegate {
    val sprintDialogState: StateFlow<SprintDialogState>

    fun setInitialSprint(start: LocalDate? = null, end: LocalDate? = null, sprintName: String = "")

    suspend fun editSprint(
        sprintId: Long,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: (suspend () -> Unit)? = null,
        doOnError: (() -> Unit)? = null
    )

    suspend fun createSprint(
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: (suspend () -> Unit)? = null,
        doOnError: (() -> Unit)? = null
    )

    fun setSprintDialogVisibility(isVisible: Boolean)
}
