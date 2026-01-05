package com.grappim.taigamobile.feature.wiki.ui.bookmark.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.canAddWikiLink
import com.grappim.taigamobile.feature.projects.domain.canDeleteWikiLink
import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import com.grappim.taigamobile.feature.wiki.ui.model.WikiUIItem
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegate
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegateImpl
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WikiBookmarksViewModel @Inject constructor(
    private val wikiRepository: WikiRepository,
    private val projectsRepository: ProjectsRepository
) : ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl() {

    private val _state = MutableStateFlow(
        WikiBookmarksState(
            onOpen = ::onOpen,
            onDeleteClick = ::onDeleteClick,
            onConfirmDelete = ::onConfirmDelete,
            onDismissDeleteDialog = ::onDismissDeleteDialog
        )
    )
    val state: StateFlow<WikiBookmarksState> = _state.asStateFlow()

    private val _onDeleteSuccess = Channel<Unit>()
    val onDeleteSuccess = _onDeleteSuccess.receiveAsFlow()

    private fun getPermissions() {
        viewModelScope.launch {
            val perms = projectsRepository.getPermissions()
            _state.update {
                it.copy(
                    canAddWikiLink = perms.canAddWikiLink(),
                    canDeleteWikiLink = perms.canDeleteWikiLink()
                )
            }
        }
    }

    private fun onDeleteClick(id: Long) {
        _state.update {
            it.copy(
                isRemoveBookmarkDialogVisible = true,
                bookmarkIdToDelete = id
            )
        }
    }

    private fun onDismissDeleteDialog() {
        _state.update {
            it.copy(
                isRemoveBookmarkDialogVisible = false,
                bookmarkIdToDelete = null
            )
        }
    }

    private fun onConfirmDelete() {
        val idToDelete = _state.value.bookmarkIdToDelete ?: return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isRemoveBookmarkDialogVisible = false,
                    bookmarkIdToDelete = null,
                    isLoading = true
                )
            }

            resultOf {
                wikiRepository.deleteWikiLink(idToDelete)
            }.onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false
                    )
                }
                _onDeleteSuccess.send(Unit)
            }.onFailure { error ->
                Timber.e(error)
                _state.update {
                    it.copy(
                        isLoading = false
                    )
                }
                showSnackbarSuspend(getErrorMessage(error))
            }
        }
    }

    private fun onOpen() {
        getPermissions()

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = NativeText.Empty
                )
            }
            resultOf {
                wikiRepository.getWikiLinks()
            }.onSuccess { result ->
                val bookmarks = result.map { link ->
                    WikiUIItem(
                        id = link.id,
                        title = link.title,
                        slug = link.ref
                    )
                }.toImmutableList()
                _state.update {
                    it.copy(
                        bookmarks = bookmarks,
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                Timber.e(error)
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
