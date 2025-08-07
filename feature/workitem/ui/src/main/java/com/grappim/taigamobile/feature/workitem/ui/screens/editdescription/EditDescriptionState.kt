package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

data class EditDescriptionState(
    val originalDescription: String,
    val currentDescription: String,
    val onDescriptionChange: (String) -> Unit,
    val isDialogVisible: Boolean = false,
    val setIsDialogVisible: (Boolean) -> Unit,
    val shouldGoBackWithCurrentValue: (shouldReturnCurrentValue: Boolean) -> Unit
)
