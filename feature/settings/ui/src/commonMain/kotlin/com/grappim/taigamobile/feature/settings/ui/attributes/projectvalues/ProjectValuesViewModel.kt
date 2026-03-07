package com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.projects.domain.ProjectValueItem
import com.grappim.taigamobile.feature.projects.domain.ProjectValueType
import com.grappim.taigamobile.feature.projects.domain.ProjectValuesRepository
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SnackbarDelegate
import com.grappim.taigamobile.utils.ui.SnackbarDelegateImpl
import com.grappim.taigamobile.utils.ui.getErrorMessage
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ProjectValuesViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: ProjectValuesRepository,
    private val sessionStorage: TaigaSessionStorage
) : ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl() {

    private val route = savedStateHandle.toRoute<ProjectValuesNavDestination>()
    private val type = ProjectValueType.valueOf(route.typeName)

    private val _state = MutableStateFlow(
        ProjectValuesState(
            type = type,
            refresh = ::loadItems,
            onAddClick = ::onAddClick,
            onEditClick = ::onEditClick,
            onSaveItem = ::onSaveItem,
            onDismissEditDialog = ::onDismissEditDialog,
            onDeleteClick = ::onDeleteClick,
            onConfirmDelete = ::onConfirmDelete,
            onDismissDeleteDialog = ::onDismissDeleteDialog
        )
    )
    val state = _state.asStateFlow()

    init {
        loadItems()
        loadPresetColors()
    }

    private fun loadPresetColors() {
        viewModelScope.launch {
            val colors = sessionStorage.getPresetColors()
            _state.update { it.copy(presetColors = colors) }
        }
    }

    private fun loadItems() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = NativeText.Empty) }
            resultOf {
                repository.getProjectValues(type)
            }.onSuccess { items ->
                _state.update { it.copy(isLoading = false, items = items) }
            }.onFailure { error ->
                logcat(throwable = error) { "Error loading project values for $type" }
                _state.update {
                    it.copy(isLoading = false, error = getErrorMessage(error))
                }
            }
        }
    }

    private fun onAddClick() {
        _state.update { it.copy(isEditDialogVisible = true, editingItem = null) }
    }

    private fun onEditClick(item: ProjectValueItem) {
        _state.update { it.copy(isEditDialogVisible = true, editingItem = item) }
    }

    private fun onDismissEditDialog() {
        _state.update { it.copy(isEditDialogVisible = false, editingItem = null) }
    }

    private fun onSaveItem(
        name: String,
        color: String,
        isClosed: Boolean,
        isArchived: Boolean,
        value: String,
        daysToDue: String
    ) {
        val editingItem = _state.value.editingItem
        viewModelScope.launch {
            _state.update { it.copy(isOperationLoading = true, isEditDialogVisible = false) }
            resultOf {
                if (editingItem == null) {
                    repository.createProjectValue(
                        type = type,
                        name = name,
                        color = color.ifBlank { null },
                        isClosed = isClosed,
                        isArchived = isArchived,
                        value = value.toDoubleOrNull(),
                        daysToDue = daysToDue.toIntOrNull()
                    )
                } else {
                    repository.updateProjectValue(
                        type = type,
                        id = editingItem.id,
                        name = name,
                        color = color.ifBlank { null },
                        isClosed = isClosed,
                        isArchived = isArchived,
                        value = value.toDoubleOrNull(),
                        daysToDue = daysToDue.toIntOrNull()
                    )
                }
            }.onSuccess { savedItem ->
                val currentItems = _state.value.items.toMutableList()
                val index = currentItems.indexOfFirst { it.id == savedItem.id }
                if (index >= 0) {
                    currentItems[index] = savedItem
                } else {
                    currentItems.add(savedItem)
                }
                _state.update {
                    it.copy(
                        isOperationLoading = false,
                        editingItem = null,
                        items = currentItems.toImmutableList()
                    )
                }
            }.onFailure { error ->
                logcat(throwable = error) { "Error saving project value" }
                showSnackbarSuspend(getErrorMessage(error))
                _state.update { it.copy(isOperationLoading = false) }
            }
        }
    }

    private fun onDeleteClick(item: ProjectValueItem) {
        _state.update { it.copy(isDeleteDialogVisible = true, deletingItem = item) }
    }

    private fun onDismissDeleteDialog() {
        _state.update { it.copy(isDeleteDialogVisible = false, deletingItem = null) }
    }

    private fun onConfirmDelete(moveTo: Long?) {
        val item = _state.value.deletingItem ?: return
        viewModelScope.launch {
            _state.update { it.copy(isOperationLoading = true, isDeleteDialogVisible = false) }
            resultOf {
                repository.deleteProjectValue(type = type, id = item.id, moveTo = moveTo)
            }.onSuccess {
                val updatedItems = _state.value.items.filter { it.id != item.id }.toImmutableList()
                _state.update {
                    it.copy(
                        isOperationLoading = false,
                        deletingItem = null,
                        items = updatedItems
                    )
                }
            }.onFailure { error ->
                logcat(throwable = error) { "Error deleting project value" }
                showSnackbarSuspend(getErrorMessage(error))
                _state.update { it.copy(isOperationLoading = false, deletingItem = null) }
            }
        }
    }
}
