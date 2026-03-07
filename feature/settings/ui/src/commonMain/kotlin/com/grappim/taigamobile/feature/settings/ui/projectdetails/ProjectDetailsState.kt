package com.grappim.taigamobile.feature.settings.ui.projectdetails

import com.grappim.taigamobile.utils.ui.NativeText

data class ProjectDetailsState(
    val name: String = "",
    val description: String = "",
    val isPrivate: Boolean = false,
    val isLookingForPeople: Boolean = false,
    val lookingForPeopleNote: String = "",
    val isContactActivated: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: NativeText = NativeText.Empty,
    val onNameChange: (String) -> Unit = {},
    val onDescriptionChange: (String) -> Unit = {},
    val onIsPrivateChange: (Boolean) -> Unit = {},
    val onIsLookingForPeopleChange: (Boolean) -> Unit = {},
    val onLookingForPeopleNoteChange: (String) -> Unit = {},
    val onIsContactActivatedChange: (Boolean) -> Unit = {},
    val onSaveClick: () -> Unit = {}
)
