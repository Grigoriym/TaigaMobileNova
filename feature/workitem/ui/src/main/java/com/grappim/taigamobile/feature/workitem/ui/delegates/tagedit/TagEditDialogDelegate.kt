package com.grappim.taigamobile.feature.workitem.ui.delegates.tagedit

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.tags.editdialog.TagEditDialogState
import kotlinx.coroutines.flow.StateFlow

interface TagEditDialogDelegate {
    val tagEditDialogState: StateFlow<TagEditDialogState>

    /**
     * Callers need to call this function
     */
    suspend fun initDialogTags()

    fun showAddDialog()
    fun showEditDialog(tag: TagUI)
    fun dismissEditDialog()

    suspend fun handleSaveTag(
        name: String,
        color: Color,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: (() -> Unit)? = null,
        doOnError: (suspend (Throwable) -> Unit)? = null
    )
}
