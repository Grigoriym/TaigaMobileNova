package com.grappim.taigamobile.feature.workitem.ui.delegates.tagedit

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.tags.editdialog.TagEditDialogState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor
import com.grappim.taigamobile.utils.ui.getErrorMessage
import com.grappim.taigamobile.utils.ui.toHex
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

class TagEditDialogDelegateImpl(
    private val projectsRepository: ProjectsRepository,
    private val taigaSessionStorage: TaigaSessionStorage
) : TagEditDialogDelegate {

    private var tagToEdit: TagUI? = null

    private val _tagEditDialogState = MutableStateFlow(
        TagEditDialogState(
            onDismiss = ::dismissEditDialog,
            presetColors = persistentListOf(),
            defaultColor = StaticColor(Color.Gray)
        )
    )
    override val tagEditDialogState: StateFlow<TagEditDialogState> = _tagEditDialogState.asStateFlow()

    override suspend fun initDialogTags() {
        val colors = taigaSessionStorage.getTagPresetColorsAsColor()
        _tagEditDialogState.update {
            it.copy(
                presetColors = colors,
                defaultColor = StaticColor(colors.firstOrNull() ?: Color.Gray)
            )
        }
    }

    override fun showAddDialog() {
        tagToEdit = null
        _tagEditDialogState.update {
            it.copy(
                isVisible = true,
                tagUI = null,
                dialogTitle = NativeText.Resource(RString.add_tag)
            )
        }
    }

    override fun showEditDialog(tag: TagUI) {
        tagToEdit = tag
        _tagEditDialogState.update {
            it.copy(
                isVisible = true,
                tagUI = tag,
                dialogTitle = NativeText.Resource(RString.edit_tag)
            )
        }
    }

    override fun dismissEditDialog() {
        tagToEdit = null
        _tagEditDialogState.update {
            it.copy(
                isVisible = false,
                tagUI = null,
                isLoading = false
            )
        }
    }

    override suspend fun handleSaveTag(
        name: String,
        color: Color,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: (() -> Unit)?,
        doOnError: (suspend (Throwable) -> Unit)?
    ) {
        val editingTag = tagToEdit
        if (editingTag == null) {
            createTag(
                name = name,
                color = color,
                doOnPreExecute = doOnPreExecute,
                doOnSuccess = doOnSuccess,
                doOnError = doOnError
            )
        } else {
            editTag(
                originalTag = editingTag,
                newName = name,
                newColor = color,
                doOnPreExecute = doOnPreExecute,
                doOnSuccess = doOnSuccess,
                doOnError = doOnError
            )
        }
    }

    private suspend fun createTag(
        name: String,
        color: Color,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: (() -> Unit)?,
        doOnError: (suspend (Throwable) -> Unit)?
    ) {
        doOnPreExecute?.invoke()
        setLoading(true)
        clearError()

        resultOf {
            projectsRepository.createTag(tagName = name, color = color.toHex())
        }.onSuccess {
            dismissEditDialog()
            doOnSuccess?.invoke()
        }.onFailure { error ->
            setLoading(false)
            setError(getErrorMessage(error))
            Timber.e(error)
            doOnError?.invoke(error)
        }
    }

    private suspend fun editTag(
        originalTag: TagUI,
        newName: String,
        newColor: Color,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: (() -> Unit)?,
        doOnError: (suspend (Throwable) -> Unit)?
    ) {
        doOnPreExecute?.invoke()
        setLoading(true)
        clearError()

        val toTagName = if (originalTag.name != newName) newName else null
        resultOf {
            projectsRepository.editTag(
                fromTagName = originalTag.name,
                toTagName = toTagName,
                color = newColor.toHex()
            )
        }.onSuccess {
            dismissEditDialog()
            doOnSuccess?.invoke()
        }.onFailure { error ->
            Timber.e(error)
            setLoading(false)
            setError(getErrorMessage(error))
            doOnError?.invoke(error)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        _tagEditDialogState.update { it.copy(isLoading = isLoading) }
    }

    private fun setError(error: NativeText) {
        _tagEditDialogState.update { it.copy(errorMessage = error) }
    }

    private fun clearError() {
        _tagEditDialogState.update { it.copy(errorMessage = null) }
    }
}
