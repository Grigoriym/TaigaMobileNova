package com.grappim.taigamobile.createtask

import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.taigamobile.utils.ui.NativeText

data class CreateTaskState(
    val toolbarTitle: NativeText = NativeText.Empty,

    val title: TextFieldValue = TextFieldValue(""),
    val setTitle: (TextFieldValue) -> Unit,

    val description: TextFieldValue = TextFieldValue(""),
    val setDescription: (TextFieldValue) -> Unit,

    val onCreateTask: () -> Unit,
    val error: NativeText = NativeText.Empty,
    val isLoading: Boolean = false
)
