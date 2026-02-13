package com.grappim.taigamobile.feature.workitem.ui.widgets.tags.editdialog

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.utils.ui.ColorSource
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class TagEditDialogState(
    val onDismiss: () -> Unit = {},
    val tagUI: TagUI? = null,
    val isVisible: Boolean = false,
    val setVisible: (Boolean) -> Unit = {},
    val presetColors: ImmutableList<Color> = persistentListOf(),
    val defaultColor: ColorSource = StaticColor(presetColors.first()),
    val dialogTitle: NativeText = NativeText.Empty,
    val isLoading: Boolean = false,
    val errorMessage: NativeText? = null
)
