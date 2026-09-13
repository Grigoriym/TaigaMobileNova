package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.compose.ui.text.input.TextFieldValue

data class EditDescriptionState(
    val originalDescription: String,
    val currentDescription: TextFieldValue,
    val onDescriptionChange: (TextFieldValue) -> Unit,
    val isDialogVisible: Boolean = false,
    val setIsDialogVisible: (Boolean) -> Unit,
    val shouldGoBackWithCurrentValue: (shouldReturnCurrentValue: Boolean) -> Unit
)
