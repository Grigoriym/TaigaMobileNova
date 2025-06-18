package io.eugenethedev.taigamobile.commontask

import io.eugenethedev.taigamobile.core.ui.NativeText
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType

data class CommonTaskState(
    val commonTaskType: CommonTaskType,
    val url: String = "",
    val isBlocked: Boolean = false,
    val editActions: EditActions,
    val toolbarTitle: NativeText,
    val projectName: String,

    val isDropdownMenuExpanded: Boolean = false,
    val setDropdownMenuExpanded: (Boolean) -> Unit,

    val isTaskEditorVisible: Boolean = false,
    val setTaskEditorVisible: (Boolean) -> Unit,

    val isDeleteAlertVisible: Boolean = false,
    val setDeleteAlertVisible: (Boolean) -> Unit,

    val isPromoteAlertVisible: Boolean = false,
    val setPromoteAlertVisible: (Boolean) -> Unit,

    val isBlockDialogVisible: Boolean = false,
    val setBlockDialogVisible: (Boolean) -> Unit,
)
