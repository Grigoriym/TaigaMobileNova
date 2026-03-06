package com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues

import com.grappim.taigamobile.feature.projects.domain.ProjectValueItem
import com.grappim.taigamobile.feature.projects.domain.ProjectValueType
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProjectValuesState(
    val type: ProjectValueType = ProjectValueType.EPIC_STATUSES,
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val isOperationLoading: Boolean = false,
    val items: ImmutableList<ProjectValueItem> = persistentListOf(),
    val presetColors: ImmutableList<String> = persistentListOf(),
    val refresh: () -> Unit = {},

    // Edit dialog
    val isEditDialogVisible: Boolean = false,
    val editingItem: ProjectValueItem? = null,
    val onAddClick: () -> Unit = {},
    val onEditClick: (ProjectValueItem) -> Unit = {},
    val onSaveItem: (name: String, color: String, isClosed: Boolean, isArchived: Boolean, value: String, daysToDue: String) -> Unit = { _, _, _, _, _, _ -> },
    val onDismissEditDialog: () -> Unit = {},

    // Delete dialog
    val isDeleteDialogVisible: Boolean = false,
    val deletingItem: ProjectValueItem? = null,
    val onDeleteClick: (ProjectValueItem) -> Unit = {},
    val onConfirmDelete: (moveTo: Long?) -> Unit = {},
    val onDismissDeleteDialog: () -> Unit = {}
)
