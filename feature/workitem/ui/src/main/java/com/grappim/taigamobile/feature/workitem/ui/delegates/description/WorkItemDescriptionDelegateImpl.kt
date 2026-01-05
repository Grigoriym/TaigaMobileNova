package com.grappim.taigamobile.feature.workitem.ui.delegates.description

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class WorkItemDescriptionDelegateImpl(
    private val taskIdentifier: TaskIdentifier,
    private val workItemRepository: WorkItemRepository,
    private val patchDataGenerator: PatchDataGenerator
) : WorkItemDescriptionDelegate {

    private val _descriptionState = MutableStateFlow(WorkItemDescriptionState())
    override val descriptionState: StateFlow<WorkItemDescriptionState> =
        _descriptionState.asStateFlow()

    override suspend fun updateDescription(
        newDescription: String,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((newVersion: Long) -> Unit)?,
        doOnError: suspend (Throwable) -> Unit
    ) {
        when (taskIdentifier) {
            is TaskIdentifier.WorkItem -> handleDescriptionUpdate(
                newDescription = newDescription,
                version = version,
                workItemId = workItemId,
                commonTaskType = taskIdentifier.commonTaskType,
                doOnPreExecute = doOnPreExecute,
                doOnSuccess = doOnSuccess,
                doOnError = doOnError
            )

            is TaskIdentifier.Wiki -> handleWikiContentUpdate(
                newDescription = newDescription,
                version = version,
                pageId = workItemId,
                doOnPreExecute = doOnPreExecute,
                doOnSuccess = doOnSuccess,
                doOnError = doOnError
            )
        }
    }

    /**
     * Since wiki is different from Work Items, it has its own logic
     */
    private suspend fun handleWikiContentUpdate(
        newDescription: String,
        version: Long,
        pageId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((newVersion: Long) -> Unit)?,
        doOnError: suspend (Throwable) -> Unit
    ) {
        if (_descriptionState.value.currentDescription == newDescription) {
            return
        }
        doOnPreExecute?.invoke()
        _descriptionState.update {
            it.copy(isDescriptionLoading = true)
        }

        resultOf {
            workItemRepository.patchWikiPage(
                version = version,
                pageId = pageId,
                payload = patchDataGenerator.getWikiContent(newDescription)
            )
        }.onSuccess { patchedData ->
            doOnSuccess?.invoke(patchedData.newVersion)
            setInitialDescription(newDescription)

            _descriptionState.update {
                it.copy(isDescriptionLoading = false)
            }
        }.onFailure { error ->
            Timber.e(error)
            _descriptionState.update {
                it.copy(isDescriptionLoading = false)
            }
            doOnError(error)
        }
    }

    private suspend fun handleDescriptionUpdate(
        newDescription: String,
        version: Long,
        workItemId: Long,
        commonTaskType: CommonTaskType,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((newVersion: Long) -> Unit)?,
        doOnError: suspend (Throwable) -> Unit
    ) {
        if (_descriptionState.value.currentDescription == newDescription) {
            return
        }

        doOnPreExecute?.invoke()
        _descriptionState.update {
            it.copy(isDescriptionLoading = true)
        }

        resultOf {
            workItemRepository.patchData(
                version = version,
                workItemId = workItemId,
                payload = patchDataGenerator.getDescriptionPatchPayload(newDescription),
                commonTaskType = commonTaskType
            )
        }.onSuccess { patchedData ->
            doOnSuccess?.invoke(patchedData.newVersion)
            setInitialDescription(newDescription)

            _descriptionState.update {
                it.copy(isDescriptionLoading = false)
            }
        }.onFailure { error ->
            Timber.e(error)
            _descriptionState.update {
                it.copy(isDescriptionLoading = false)
            }
            doOnError(error)
        }
    }

    override fun setInitialDescription(description: String) {
        _descriptionState.update {
            it.copy(currentDescription = description)
        }
    }
}
