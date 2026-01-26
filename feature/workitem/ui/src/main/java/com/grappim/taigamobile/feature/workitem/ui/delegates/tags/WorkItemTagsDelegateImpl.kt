package com.grappim.taigamobile.feature.workitem.ui.delegates.tags

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import com.grappim.taigamobile.utils.ui.toHex
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkItemTagsDelegateImpl(
    private val commonTaskType: CommonTaskType,
    private val workItemRepository: WorkItemRepository,
    private val patchDataGenerator: PatchDataGenerator
) : WorkItemTagsDelegate {

    private val _tagsState = MutableStateFlow(
        WorkItemTagsState()
    )
    override val tagsState: StateFlow<WorkItemTagsState> = _tagsState.asStateFlow()

    override fun setInitialTags(tags: PersistentList<SelectableTagUI>) {
        _tagsState.update {
            it.copy(tags = tags)
        }
    }

    override suspend fun handleTagRemove(
        tag: SelectableTagUI,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((Long) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        val newTags = _tagsState.value.tags.filter { it.name != tag.name }.toPersistentList()
        handleTagsUpdate(
            newTags = newTags,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = doOnPreExecute,
            doOnSuccess = doOnSuccess,
            doOnError = doOnError
        )
    }

    override suspend fun handleTagsUpdate(
        newTags: PersistentList<SelectableTagUI>,
        version: Long,
        workItemId: Long,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: ((Long) -> Unit)?,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _tagsState.update {
            it.copy(areTagsLoading = true)
        }

        resultOf {
            val preparedTags = newTags.map { tag ->
                listOf(tag.name, tag.color.toHex())
            }

            workItemRepository.patchData(
                version = version,
                workItemId = workItemId,
                payload = patchDataGenerator.getTagsPatchPayload(preparedTags),
                commonTaskType = commonTaskType
            )
        }.onSuccess { patchedData ->
            doOnSuccess?.invoke(patchedData.newVersion)

            _tagsState.update {
                it.copy(
                    tags = newTags,
                    areTagsLoading = false
                )
            }
        }.onFailure { error ->
            _tagsState.update {
                it.copy(areTagsLoading = false)
            }
            doOnError(error)
        }
    }
}
