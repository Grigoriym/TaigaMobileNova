package com.grappim.taigamobile.feature.wiki.ui.page

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.wiki.domain.WikiPageUseCase
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiPageNavDestination
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.description.WorkItemDescriptionDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.description.WorkItemDescriptionDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.file.FileUriManager
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WikiPageViewModel @Inject constructor(
    private val wikiPageUseCase: WikiPageUseCase,
    private val workItemRepository: WorkItemRepository,
    private val taigaStorage: TaigaStorage,
    private val fileUriManager: FileUriManager,
    private val patchDataGenerator: PatchDataGenerator,
    workItemEditShared: WorkItemEditShared,
    savedStateHandle: SavedStateHandle
) : ViewModel(),
    WorkItemAttachmentsDelegate by WorkItemAttachmentsDelegateImpl(
        commonTaskType = CommonTaskType.Wiki,
        workItemRepository = workItemRepository,
        taigaStorage = taigaStorage,
        fileUriManager = fileUriManager
    ),
    WorkItemDescriptionDelegate by WorkItemDescriptionDelegateImpl(
        commonTaskType = CommonTaskType.Wiki,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ) {

    private val route = savedStateHandle.toRoute<WikiPageNavDestination>()

    private val _state = MutableStateFlow(
        WikiPageState(
            pageSlug = route.slug,
            setDeleteAlertVisible = ::setDeleteAlertVisible,
            setDropdownMenuExpanded = ::setDropdownMenuExpanded,
            onDeleteConfirm = ::deleteWikiPage,
            onAttachmentRemove = ::onAttachmentRemove,
            onAttachmentAdd = ::onAttachmentAdd
        )
    )
    val state = _state.asStateFlow()

    private val _deleteWikiPageResult = Channel<Unit>()
    val deleteWikiPageResult = _deleteWikiPageResult.receiveAsFlow()

    private val currentPage: WikiPage get() = requireNotNull(_state.value.currentPage)

    init {
        loadData()

        workItemEditShared.descriptionState
            .onEach(::onNewDescriptionUpdate)
            .launchIn(viewModelScope)
    }

    private suspend fun onNewDescriptionUpdate(newDescription: String) {
        handleWikiContentUpdate(
            version = currentPage.version,
            pageId = currentPage.id,
            newDescription = newDescription,
            doOnError = { error ->
                _state.update { it.copy(error = getErrorMessage(error)) }
            },
            doOnPreExecute = {
                _state.update { it.copy(error = NativeText.Empty) }
            },
            doOnSuccess = { version ->
                updateVersion(version)

                val updatedPage = currentPage.copy(content = newDescription)

                _state.update { currentState ->
                    currentState.copy(
                        currentPage = updatedPage,
                        originalPage = updatedPage
                    )
                }
            }
        )
    }

    private fun updateVersion(newVersion: Long) {
        val updatedPage = currentPage.copy(
            version = newVersion
        )
        _state.update {
            it.copy(
                currentPage = updatedPage,
                originalPage = updatedPage
            )
        }
    }

    private fun setDeleteAlertVisible(visible: Boolean) {
        _state.update {
            it.copy(isDeleteAlertVisible = visible)
        }
    }

    private fun setDropdownMenuExpanded(expanded: Boolean) {
        _state.update {
            it.copy(isDropdownMenuExpanded = expanded)
        }
    }

    private fun onAttachmentRemove(attachment: Attachment) {
        viewModelScope.launch {
            handleRemoveAttachment(
                attachment = attachment,
                doOnPreExecute = {
                    _state.update { it.copy(error = NativeText.Empty) }
                },
                doOnError = { error ->
                    _state.update { it.copy(error = getErrorMessage(error)) }
                }
            )
        }
    }

    private fun onAttachmentAdd(uri: Uri?) {
        viewModelScope.launch {
            handleAddAttachment(
                workItemId = currentPage.id,
                uri = uri,
                doOnPreExecute = {
                    _state.update { it.copy(error = NativeText.Empty) }
                },
                doOnError = { error ->
                    _state.update { it.copy(error = getErrorMessage(error)) }
                }
            )
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = NativeText.Empty
                )
            }

            wikiPageUseCase.getWikiPageData(pageSlug = _state.value.pageSlug)
                .onSuccess { data ->
                    setInitialAttachments(data.attachments)
                    setInitialDescription(data.page.content)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentPage = data.page,
                            originalPage = data.page,
                            user = data.user,
                            link = data.wikiLink
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

    private fun deleteWikiPage() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = NativeText.Empty
                )
            }

            wikiPageUseCase.deleteWikiPage(
                wikiLinkId = _state.value.link?.id,
                pageId = currentPage.id
            ).onSuccess {
                _state.update {
                    it.copy(isLoading = false)
                }
                _deleteWikiPageResult.send(Unit)
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
