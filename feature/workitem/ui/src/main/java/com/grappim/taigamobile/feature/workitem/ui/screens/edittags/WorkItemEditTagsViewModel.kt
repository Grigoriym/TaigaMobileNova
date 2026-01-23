package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.utils.ui.typeMapOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.reflect.typeOf

@HiltViewModel
class WorkItemEditTagsViewModel @Inject constructor(
    private val filtersRepository: FiltersRepository,
    private val tagUIMapper: TagUIMapper,
    private val workItemEditStateRepository: WorkItemEditStateRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<WorkItemEditTagsNavDestination>(
        typeMap = typeMapOf(
            listOf(
                typeOf<TaskIdentifier>()
            )
        )
    )
    private val _state = MutableStateFlow(
        EditTagsState(
            setIsDialogVisible = ::setIsDialogVisible,
            onTagClick = ::onTagClick,
            shouldGoBackWithCurrentValue = ::onGoingBack
        )
    )
    val state = _state.asStateFlow()

    private val _onBackAction = Channel<Unit>()
    val onBackAction = _onBackAction.receiveAsFlow()

    init {
        getFiltersData()
    }

    private fun getOriginalTags(): ImmutableList<TagUI> = workItemEditStateRepository.getOriginalTags(
        workItemId = route.workItemId,
        type = route.taskIdentifier
    )

    private fun getCurrentTags(): PersistentList<TagUI> = workItemEditStateRepository.getCurrentTags(
        workItemId = route.workItemId,
        type = route.taskIdentifier
    ).toPersistentList()

    private fun onGoingBack(shouldReturnCurrentValue: Boolean) {
        viewModelScope.launch {
            setIsDialogVisible(false)
            notifyChange(shouldReturnCurrentValue)
            _onBackAction.send(Unit)
        }
    }

    private fun notifyChange(shouldReturnCurrentValue: Boolean) {
        viewModelScope.launch {
            val wereTagsChanged =
                _state.value.currentSelectedTags != _state.value.originalSelectedTags
            if (shouldReturnCurrentValue && wereTagsChanged) {
                workItemEditStateRepository.updateTags(
                    workItemId = route.workItemId,
                    type = route.taskIdentifier,
                    tags = _state.value.currentSelectedTags
                )
            }
        }
    }

    private fun setIsDialogVisible(newValue: Boolean) {
        _state.update {
            it.copy(isDialogVisible = newValue)
        }
    }

    private fun onTagClick(tag: TagUI) {
        _state.update { currentState ->
            var clickedTagWithUpdatedSelection: TagUI? = null
            val updatedTags = currentState.tags.map { item ->
                if (item.name == tag.name) {
                    clickedTagWithUpdatedSelection = item.copy(isSelected = !item.isSelected)
                    clickedTagWithUpdatedSelection
                } else {
                    item
                }
            }

            val currentSelectedTags = if (clickedTagWithUpdatedSelection != null) {
                if (clickedTagWithUpdatedSelection.isSelected) {
                    currentState.currentSelectedTags.add(clickedTagWithUpdatedSelection)
                } else {
                    currentState.currentSelectedTags.removeAll { it.name == tag.name }
                }
            } else {
                // This case should ideally not happen if 'tag' is always present in 'currentState.tags'
                Timber.w("Clicked tag not found in the list of tags: ${tag.name}")
                currentState.currentSelectedTags
            }
            currentState.copy(
                tags = updatedTags.toImmutableList(),
                currentSelectedTags = currentSelectedTags
            )
        }
    }

    private fun getFiltersData() {
        viewModelScope.launch {
            resultOf {
                require(route.taskIdentifier is TaskIdentifier.WorkItem)
                filtersRepository.getFiltersData(route.taskIdentifier.commonTaskType)
            }.onSuccess { result ->
                val tags = tagUIMapper.toUIFromFilters(list = result.tags).toPersistentList()
                val originalTagsNames = workItemEditStateRepository.getOriginalTagsNames(
                    workItemId = route.workItemId,
                    type = route.taskIdentifier
                )
                val updatedTags = tags.map { tag ->
                    if (tag.name in originalTagsNames) {
                        tag.copy(isSelected = true)
                    } else {
                        tag
                    }
                }.sortedByDescending { it.isSelected }
                    .toImmutableList()
                _state.update {
                    it.copy(
                        tags = updatedTags,
                        originalSelectedTags = getOriginalTags(),
                        currentSelectedTags = getCurrentTags()
                    )
                }
            }.onFailure { error ->
                Timber.e(error)
            }
        }
    }
}
