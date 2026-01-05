package com.grappim.taigamobile.feature.wiki.ui.page.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.canAddWikiPage
import com.grappim.taigamobile.feature.projects.domain.canDeleteWikiPage
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
class WikiPagesViewModel @Inject constructor(
    private val wikiRepository: WikiRepository,
    private val projectsRepository: ProjectsRepository
) : ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl() {

    private val _state = MutableStateFlow(
        WikiPagesState(
            onOpen = ::onOpen,
            onDeleteClick = ::onDeleteClick,
            onConfirmDelete = ::onConfirmDelete,
            onDismissDeleteDialog = ::onDismissDeleteDialog
        )
    )
    val state: StateFlow<WikiPagesState> = _state.asStateFlow()

    private val _onDeleteSuccess = Channel<Unit>()
    val onDeleteSuccess = _onDeleteSuccess.receiveAsFlow()

    private fun getPermissions() {
        viewModelScope.launch {
            val perms = projectsRepository.getPermissions()
            _state.update {
                it.copy(
                    canAddWikiPage = perms.canAddWikiPage(),
                    canDeleteWikiPage = perms.canDeleteWikiPage()
                )
            }
        }
    }

    private fun onDeleteClick(id: Long) {
        _state.update {
            it.copy(
                isRemovePageDialogVisible = true,
                pageIdToDelete = id
            )
        }
    }

    private fun onDismissDeleteDialog() {
        _state.update {
            it.copy(
                isRemovePageDialogVisible = false,
                pageIdToDelete = null
            )
        }
    }

    private fun onConfirmDelete() {
        val idToDelete = _state.value.pageIdToDelete ?: return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isRemovePageDialogVisible = false,
                    pageIdToDelete = null,
                    isLoading = true
                )
            }

            resultOf {
                wikiRepository.deleteWikiPage(idToDelete)
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
                wikiRepository.getProjectWikiPages()
            }.onSuccess { pages ->
                val allPages = pages.map { page ->
                    WikiUIItem(
                        id = page.id,
                        title = page.slug,
                        slug = page.slug
                    )
                }.toImmutableList()

                _state.update {
                    it.copy(
                        allPages = allPages,
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
