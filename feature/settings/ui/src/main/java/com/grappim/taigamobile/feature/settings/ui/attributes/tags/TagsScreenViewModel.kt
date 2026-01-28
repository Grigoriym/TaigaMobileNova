package com.grappim.taigamobile.feature.settings.ui.attributes.tags

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.workitem.ui.delegates.tagedit.TagEditDialogDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.tagedit.TagEditDialogDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegate
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegateImpl
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TagsScreenViewModel @Inject constructor(
    private val projectsRepository: ProjectsRepository,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val tagUIMapper: TagUIMapper
) : ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl(),
    TagEditDialogDelegate by TagEditDialogDelegateImpl(
        projectsRepository = projectsRepository,
        taigaSessionStorage = taigaSessionStorage
    ) {

    private val _state = MutableStateFlow(
        TagsScreenState(
            deleteTag = ::deleteTag,
            refresh = ::fetchTagsColors,
            onTagEditClick = ::onEditTagClick,
            onTagDeleteClick = ::onDeleteTagClick,
            closeDeleteDialog = ::closeDeleteDialog,
            setDropdownMenuExpanded = ::setDropdownMenuExpanded,
            onAddTagClick = ::onAddTagClick,
            onMergeTagsClick = ::onMergeTagsClick,
            onMainTagSelect = ::onMainTagSelect,
            onTagToMergeToggle = ::onTagToMergeToggle,
            onCancelMerge = ::onCancelMerge,
            onConfirmMerge = ::onConfirmMerge,
            onSaveClick = ::onSaveTag
        )
    )
    val state = _state.asStateFlow()

    private var tagToDelete: TagUI? = null

    init {
        fetchTagsColors()
        viewModelScope.launch { initDialogTags() }
    }

    private fun onMergeTagsClick() {
        setDropdownMenuExpanded(false)
        _state.update {
            it.copy(
                isMergeMode = true,
                mainTagName = null,
                tagsToMerge = persistentSetOf()
            )
        }
    }

    private fun onMainTagSelect(tag: TagUI) {
        _state.update {
            val newTagsToMerge = it.tagsToMerge - tag.name
            it.copy(
                mainTagName = tag.name,
                tagsToMerge = newTagsToMerge.toPersistentSet()
            )
        }
    }

    private fun onTagToMergeToggle(tag: TagUI) {
        _state.update {
            val newTagsToMerge = if (it.tagsToMerge.contains(tag.name)) {
                it.tagsToMerge - tag.name
            } else {
                it.tagsToMerge + tag.name
            }
            it.copy(tagsToMerge = newTagsToMerge.toPersistentSet())
        }
    }

    private fun onCancelMerge() {
        _state.update {
            it.copy(
                isMergeMode = false,
                mainTagName = null,
                tagsToMerge = persistentSetOf()
            )
        }
    }

    private fun onConfirmMerge() {
        val mainTag = _state.value.mainTagName ?: return
        val tagsToMerge = _state.value.tagsToMerge.toList()
        if (tagsToMerge.isEmpty()) return

        viewModelScope.launch {
            _state.update {
                it.copy(isOperationLoading = true)
            }

            resultOf {
                projectsRepository.mixTags(fromTags = tagsToMerge, toTag = mainTag)
            }.onSuccess {
                _state.update {
                    it.copy(
                        isOperationLoading = false,
                        isMergeMode = false,
                        mainTagName = null,
                        tagsToMerge = persistentSetOf()
                    )
                }
                fetchTagsColors()
            }.onFailure { error ->
                Timber.e(error)
                showSnackbarSuspend(getErrorMessage(error))
                _state.update {
                    it.copy(isOperationLoading = false)
                }
            }
        }
    }

    private fun setDropdownMenuExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isDropdownMenuExpanded = isExpanded)
        }
    }

    private fun onAddTagClick() {
        setDropdownMenuExpanded(false)
        showAddDialog()
    }

    private fun onEditTagClick(tag: TagUI) {
        showEditDialog(tag)
    }

    private fun onSaveTag(name: String, color: Color) {
        viewModelScope.launch {
            handleSaveTag(
                name = name,
                color = color,
                doOnPreExecute = {
                    _state.update {
                        it.copy(isOperationLoading = true)
                    }
                    dismissEditDialog()
                },
                doOnSuccess = {
                    _state.update { it.copy(isOperationLoading = false) }
                    fetchTagsColors()
                },
                doOnError = { error ->
                    showSnackbarSuspend(getErrorMessage(error))
                    _state.update {
                        it.copy(isOperationLoading = false)
                    }
                }
            )
        }
    }

    private fun closeDeleteDialog() {
        tagToDelete = null
        setIsDeleteTagDialogVisible(false)
    }

    private fun onDeleteTagClick(tag: TagUI) {
        tagToDelete = tag
        setIsDeleteTagDialogVisible(true)
    }

    private fun setIsDeleteTagDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isDeleteTagDialogVisible = isVisible)
        }
    }

    private fun deleteTag() {
        val tag = requireNotNull(tagToDelete)

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isOperationLoading = true,
                    isDeleteTagDialogVisible = false
                )
            }

            resultOf {
                projectsRepository.deleteTag(tagName = tag.name)
            }.onSuccess {
                val newTags = _state.value.tags.filter { it.name != tag.name }
                _state.update {
                    it.copy(
                        isOperationLoading = false,
                        tags = newTags.toImmutableList()
                    )
                }
            }.onFailure { error ->
                Timber.e(error)
                showSnackbarSuspend(getErrorMessage(error))
                _state.update {
                    it.copy(isOperationLoading = false)
                }
            }

            tagToDelete = null
        }
    }

    private fun fetchTagsColors() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = NativeText.Empty
                )
            }
            resultOf {
                projectsRepository.getTagsColors()
            }.onSuccess { result ->
                val uiTags = tagUIMapper.toTagUIList(result)

                _state.update {
                    it.copy(
                        isLoading = false,
                        tags = uiTags
                    )
                }
            }.onFailure { error ->
                Timber.e(error)

                showSnackbarSuspend(getErrorMessage(error))
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = getErrorMessage(error)
                    )
                }
            }
        }
    }
}
