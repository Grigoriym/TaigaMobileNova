package com.grappim.taigamobile.createtask

import com.grappim.taigamobile.utils.ui.NativeText

data class CreateTaskState(
    val toolbarTitle: NativeText = NativeText.Empty,

    val title: String = "",
    val setTitle: (String) -> Unit,

    val description: String = "",
    val setDescription: (String) -> Unit,

    val onCreateTask: () -> Unit,
    val error: NativeText = NativeText.Empty,
    val isLoading: Boolean = false
)
