package com.grappim.taigamobile.feature.settings.ui.customheaders

import com.grappim.taigamobile.core.storage.customheaders.CustomHeader
import com.grappim.taigamobile.core.storage.customheaders.CustomHeaderError
import com.grappim.taigamobile.core.storage.customheaders.isBlank
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * [rows] is what the user is editing; [saved] is what storage last emitted. Save is offered only
 * while the two differ, so a screen with nothing changed has nothing to press.
 */
data class CustomHeadersState(
    val rows: ImmutableList<CustomHeaderRow> = persistentListOf(),
    val saved: ImmutableList<CustomHeader> = persistentListOf(),
    val isSaving: Boolean = false,
    val onAdd: () -> Unit = {},
    val onRemove: (Int) -> Unit = {},
    val onNameChange: (Int, String) -> Unit = { _, _ -> },
    val onValueChange: (Int, String) -> Unit = { _, _ -> },
    val onSaveClick: () -> Unit = {}
) {
    /** The headers a save would store: trimmed, unfilled rows dropped. */
    val headers: List<CustomHeader>
        get() = rows.map { CustomHeader(it.name.trim(), it.value.trim()) }.filterNot { it.isBlank() }

    val isSaveEnabled: Boolean
        get() = !isSaving && rows.none { it.error != null } && headers != saved
}

/** One editable header; [error] is the validator's verdict on the row as it stands. */
data class CustomHeaderRow(val name: String = "", val value: String = "", val error: CustomHeaderError? = null)
