package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.workitem.ui.delegates.tagedit.TagEditDialogDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.tagedit.TagEditDialogDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.SelectableTagUI
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.utils.ui.SnackbarDelegate
import com.grappim.taigamobile.utils.ui.SnackbarDelegateImpl
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
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class WorkItemEditTagsViewModel(
    private val tagUIMapper: TagUIMapper,
    private val workItemEditStateRepository: WorkItemEditStateRepository,
    private val projectsRepository: ProjectsRepository,
    private val taigaSessionStorage: TaigaSessionStorage,
    @InjectedParam private val route: WorkItemEditTagsNavDestination
) : ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl(),
    TagEditDialogDelegate by TagEditDialogDelegateImpl(
        projectsRepository = projectsRepository,
        taigaSessionStorage = taigaSessionStorage
    ) {

    private val _state = MutableStateFlow(
        EditTagsState(
            setIsDialogVisible = ::setIsDialogVisible,
            onTagClick = ::onTagClick,
            shouldGoBackWithCurrentValue = ::onGoingBack,
            onSaveTags = ::onSaveTagsClick,
            onAddTagClick = ::onAddTagClick,
            onBackClick = ::onBackClick,
            onSaveClick = ::onSaveTag
        )
    )
    val state = _state.asStateFlow()

    private val _onBackAction = Channel<Unit>()
    val onBackAction = _onBackAction.receiveAsFlow()

    init {
        fetchTags()
        viewModelScope.launch { initDialogTags() }
    }

    private fun onSaveTag(name: String, color: Color) {
        viewModelScope.launch {
            handleSaveTag(
                name = name,
                color = color,
                doOnSuccess = {
                    _state.update { currentState ->
                        val newTag = SelectableTagUI(name = name, color = color, isSelected = true)
                        currentState.copy(
                            currentSelectedTags = currentState.currentSelectedTags.add(newTag)
                        )
                    }
                    fetchTags()
                }
            )
        }
    }

    private fun onBackClick() {
        setIsDialogVisible(!state.value.isDialogVisible)
    }

    private fun onSaveTagsClick() {
        onGoingBack(true)
    }

    private fun onAddTagClick() {
        showAddDialog()
    }

    private fun getOriginalTags(): ImmutableList<SelectableTagUI> = workItemEditStateRepository.getOriginalTags(
        workItemId = route.workItemId,
        type = route.taskIdentifier
    )

    private fun getCurrentTags(): PersistentList<SelectableTagUI> = workItemEditStateRepository.getCurrentTags(
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

    private fun onTagClick(tag: SelectableTagUI) {
        _state.update { currentState ->
            var clickedTagWithUpdatedSelection: SelectableTagUI? = null
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
                logcat(priority = LogPriority.WARN) {
                    "Clicked tag not found in the list of tags: ${tag.name}"
                }
                // This case should ideally not happen if 'tag' is always present in 'currentState.tags'
                currentState.currentSelectedTags
            }
            currentState.copy(
                tags = updatedTags.toImmutableList(),
                currentSelectedTags = currentSelectedTags
            )
        }
    }

    private fun fetchTags() {
        viewModelScope.launch {
            val currentSelectedNames = _state.value.currentSelectedTags.map { it.name }.toSet()
            val isInitialLoad = _state.value.tags.isEmpty()

            resultOf {
                projectsRepository.getTagsColors()
            }.onSuccess { result ->
                val tags = tagUIMapper.toSelectableUI(result)
                val selectedNames = if (isInitialLoad) {
                    workItemEditStateRepository.getOriginalTagsNames(
                        workItemId = route.workItemId,
                        type = route.taskIdentifier
                    )
                } else {
                    currentSelectedNames
                }
                val updatedTags = tags.map { tag ->
                    if (tag.name in selectedNames) {
                        tag.copy(isSelected = true)
                    } else {
                        tag
                    }
                }.sortedByDescending { it.isSelected }
                    .toImmutableList()

                val newCurrentSelectedTags = if (isInitialLoad) {
                    getCurrentTags()
                } else {
                    updatedTags.filter { it.isSelected }.toPersistentList()
                }

                _state.update {
                    it.copy(
                        tags = updatedTags,
                        originalSelectedTags = getOriginalTags(),
                        currentSelectedTags = newCurrentSelectedTags
                    )
                }
            }.onFailure { error ->
                logcat(throwable = error) {
                    "Error while fetching tags"
                }
            }
        }
    }
}
