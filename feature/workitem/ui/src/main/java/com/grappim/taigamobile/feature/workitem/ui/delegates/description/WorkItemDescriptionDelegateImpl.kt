package com.grappim.taigamobile.feature.workitem.ui.delegates.description

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class WorkItemDescriptionDelegateImpl(
    private val commonTaskType: CommonTaskType,
    private val workItemRepository: WorkItemRepository,
    private val patchDataGenerator: PatchDataGenerator
) : WorkItemDescriptionDelegate {

    private val _descriptionState = MutableStateFlow(WorkItemDescriptionState())
    override val descriptionState: StateFlow<WorkItemDescriptionState> =
        _descriptionState.asStateFlow()

    /**
     * Since wiki is different from Work Items, it has its own logic
     */
    override suspend fun handleWikiContentUpdate(
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

    override suspend fun handleDescriptionUpdate(
        newDescription: String,
        version: Long,
        workItemId: Long,
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
