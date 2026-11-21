package com.grappim.taigamobile.createtask

import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.kit.uikit.NativeText

data class CreateTaskState(
    val toolbarTitle: NativeText = NativeText.Empty,

    val title: String = "",
    val setTitle: (String) -> Unit,

    val description: TextFieldValue = TextFieldValue(),
    val setDescription: (TextFieldValue) -> Unit,

    val isPreview: Boolean = false,
    val onPreviewToggle: () -> Unit = {},

    val onCreateTask: () -> Unit,
    val error: NativeText = NativeText.Empty,
    val isLoading: Boolean = false
)
