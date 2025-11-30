package com.grappim.taigamobile.feature.epics.ui.details

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.epics.domain.Epic
import com.grappim.taigamobile.feature.epics.domain.EpicDetailsDataUseCase
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single.WorkItemSingleAssigneeDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single.WorkItemSingleAssigneeDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.badge.WorkItemBadgeDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.badge.WorkItemBadgeDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.block.WorkItemBlockDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.block.WorkItemBlockDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.comments.WorkItemCommentsDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.comments.WorkItemCommentsDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.customfields.WorkItemCustomFieldsDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.customfields.WorkItemCustomFieldsDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.description.WorkItemDescriptionDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.description.WorkItemDescriptionDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.tags.WorkItemTagsDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.tags.WorkItemTagsDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.title.WorkItemTitleDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.title.WorkItemTitleDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.watchers.WorkItemWatchersDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.watchers.WorkItemWatchersDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.models.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.models.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.WorkItemUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.file.FileUriManager
import com.grappim.taigamobile.utils.ui.getErrorMessage
import com.grappim.taigamobile.utils.ui.toHex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EpicDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workItemRepository: WorkItemRepository,
    private val patchDataGenerator: PatchDataGenerator,
    private val workItemEditShared: WorkItemEditShared,
    private val historyRepository: HistoryRepository,
    private val fileUriManager: FileUriManager,
    private val usersRepository: UsersRepository,
    private val taigaStorage: TaigaStorage,
    private val session: Session,
    private val dateTimeUtils: DateTimeUtils,
    private val epicDetailsDataUseCase: EpicDetailsDataUseCase,
    private val statusUIMapper: StatusUIMapper,
    private val workItemsGenerator: WorkItemsGenerator,
    private val tagUIMapper: TagUIMapper,
    private val customFieldsUIMapper: CustomFieldsUIMapper,
    private val workItemUIMapper: WorkItemUIMapper
) : ViewModel(),
    WorkItemTitleDelegate by WorkItemTitleDelegateImpl(
        commonTaskType = epicTaskType,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ),
    WorkItemDescriptionDelegate by WorkItemDescriptionDelegateImpl(
        commonTaskType = epicTaskType,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ),
    WorkItemBadgeDelegate by WorkItemBadgeDelegateImpl(
        commonTaskType = epicTaskType,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ),
    WorkItemTagsDelegate by WorkItemTagsDelegateImpl(
        commonTaskType = epicTaskType,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator,
        workItemEditShared = workItemEditShared
    ),
    WorkItemCommentsDelegate by WorkItemCommentsDelegateImpl(
        commonTaskType = epicTaskType,
        historyRepository = historyRepository,
        workItemRepository = workItemRepository
    ),
    WorkItemAttachmentsDelegate by WorkItemAttachmentsDelegateImpl(
        commonTaskType = epicTaskType,
        workItemRepository = workItemRepository,
        fileUriManager = fileUriManager,
        taigaStorage = taigaStorage
    ),
    WorkItemSingleAssigneeDelegate by WorkItemSingleAssigneeDelegateImpl(
        commonTaskType = epicTaskType,
        workItemRepository = workItemRepository,
        usersRepository = usersRepository,
        patchDataGenerator = patchDataGenerator,
        workItemEditShared = workItemEditShared
    ),
    WorkItemBlockDelegate by WorkItemBlockDelegateImpl(
        commonTaskType = epicTaskType,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ),
    WorkItemWatchersDelegate by WorkItemWatchersDelegateImpl(
        commonTaskType = epicTaskType,
        workItemRepository = workItemRepository,
        usersRepository = usersRepository,
        patchDataGenerator = patchDataGenerator,
        session = session,
        workItemEditShared = workItemEditShared
    ),
    WorkItemCustomFieldsDelegate by WorkItemCustomFieldsDelegateImpl(
        commonTaskType = epicTaskType,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator,
        dateTimeUtils = dateTimeUtils
    ) {

    companion object {
        private val epicTaskType = CommonTaskType.Epic
    }

    private val route = savedStateHandle.toRoute<EpicDetailsNavDestination>()
    private val ref = route.ref

    private val epicId: Long = route.epicId

    private val _deleteTrigger = MutableSharedFlow<Boolean>()
    val deleteTrigger = _deleteTrigger.asSharedFlow()

    private val currentEpic: Epic
        get() = requireNotNull(_state.value.currentEpic)

    private val _state = MutableStateFlow(
        EpicDetailsState(
            setDropdownMenuExpanded = ::setDropdownMenuExpanded,
            toolbarTitle = NativeText.Arguments(
                id = RString.epic_slug,
                args = listOf(ref)
            ),
            retryLoadEpic = ::retryLoadEpic,
            onBlockToggle = ::onBlockToggle,
            onTagRemove = ::onTagRemove,
            onAssignToMe = ::onAssignToMe,
            onRemoveMeFromWatchersClick = ::onRemoveMeFromWatchersClick,
            onAddMeToWatchersClick = ::onAddMeToWatchersClick,
            removeWatcher = ::removeWatcher,
            removeAssignee = ::removeAssignee,
            onCustomFieldSave = ::onCustomFieldSave,
            setIsDeleteDialogVisible = ::setIsDeleteDialogVisible,
            onDelete = ::doOnDelete,
            onAttachmentRemove = ::onAttachmentRemove,
            onAttachmentAdd = ::onAttachmentAdd,
            onCommentRemove = ::deleteComment,
            onCreateCommentClick = ::createComment,
            onTitleSave = ::onTitleSave,
            onBadgeSave = ::onBadgeSave,
            setAreWorkItemsExpanded = ::setAreWorkItemsExpanded,
            onEpicColorPick = ::onEpicColorPick
        )
    )
    val state = _state.asStateFlow()

    init {
        loadEpic()

        workItemEditShared.teamMemberUpdateState
            .onEach(::handleTeamMemberUpdate)
            .launchIn(viewModelScope)

        workItemEditShared.tagsState
            .onEach(::onNewTagsUpdate)
            .launchIn(viewModelScope)

        workItemEditShared.descriptionState
            .onEach(::onNewDescriptionUpdate)
            .launchIn(viewModelScope)
    }

    private fun clearError() {
        _state.update {
            it.copy(
                error = NativeText.Empty
            )
        }
    }

    private fun emitError(error: Throwable) {
        Timber.e(error)
        _state.update {
            it.copy(
                error = getErrorMessage(error)
            )
        }
    }

    private fun updateVersion(newVersion: Long) {
        val updatedEpic = currentEpic.copy(
            version = newVersion
        )
        _state.update {
            it.copy(
                currentEpic = updatedEpic,
                originalEpic = updatedEpic
            )
        }
    }

    private suspend fun handleTeamMemberUpdate(updateState: TeamMemberUpdate) {
        when (updateState) {
            TeamMemberUpdate.Clear -> {}
            is TeamMemberUpdate.Assignees -> {}
            is TeamMemberUpdate.Assignee -> {
                onAssigneeUpdated(updateState.id)
            }

            is TeamMemberUpdate.Watchers -> {
                onWatchersUpdated(updateState.ids)
            }
        }
    }

    private suspend fun onNewTagsUpdate(newTagsToUse: PersistentList<TagUI>) {
        handleTagsUpdate(
            newTags = newTagsToUse,
            version = currentEpic.version,
            workItemId = currentEpic.id,
            doOnPreExecute = {
                clearError()
            },
            doOnError = { error ->
                emitError(error)
            },
            doOnSuccess = { version ->
                updateVersion(version)
            }
        )
    }

    private suspend fun onNewDescriptionUpdate(newDescription: String) {
        handleDescriptionUpdate(
            version = currentEpic.version,
            workItemId = currentEpic.id,
            newDescription = newDescription,
            doOnPreExecute = {
                clearError()
            },
            doOnError = { error ->
                emitError(error)
            },
            doOnSuccess = { version ->
                updateVersion(version)

                val updatedEpic = currentEpic.copy(description = newDescription)

                _state.update { currentState ->
                    currentState.copy(
                        currentEpic = updatedEpic,
                        originalEpic = updatedEpic
                    )
                }
            }
        )
    }

    private fun onAssigneeUpdated(newAssigneeId: Long?) {
        viewModelScope.launch {
            handleUpdateAssignee(
                newAssigneeId = newAssigneeId,
                workItemId = currentEpic.id,
                version = currentEpic.version,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                },
                doOnSuccess = { version ->
                    updateVersion(version)
                }
            )
        }
    }

    private fun retryLoadEpic() {
        loadEpic()
    }

    private fun loadEpic() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    initialLoadError = NativeText.Empty
                )
            }

            epicDetailsDataUseCase.getEpicData(epicId = epicId)
                .onSuccess { result ->
                    val statusUi = result.epic.status?.let {
                        statusUIMapper.toUI(statuses = it)
                    }
                    val workItemBadges = async {
                        workItemsGenerator.getItems(
                            statusUI = statusUi,
                            filtersData = result.filtersData
                        )
                    }
                    val tags = async {
                        tagUIMapper.toUI(result.epic.tags).toPersistentList()
                    }
                    val customFieldsStateItems = async {
                        customFieldsUIMapper.toUI(result.customFields)
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentEpic = result.epic,
                            originalEpic = result.epic,
                            creator = result.creator,
                            filtersData = result.filtersData,
                            initialLoadError = NativeText.Empty,
                            customFieldsVersion = result.customFields.version,
                            userStories = workItemUIMapper.toUI(result.userStories)
                        )
                    }
                    setInitialTitle(result.epic.title)
                    setWorkItemBadges(workItemBadges.await())
                    setInitialTags(tags.await())
                    setInitialComments(result.comments)
                    setInitialAttachments(result.attachments.toPersistentList())
                    setInitialWatchers(
                        watchers = result.watchers.toPersistentList(),
                        isWatchedByMe = result.isWatchedByMe
                    )
                    setInitialCustomFields(
                        version = result.customFields.version,
                        customFieldStateItems = customFieldsStateItems.await()
                    )
                    setInitialAssignees(
                        assignees = result.assignees.toPersistentList(),
                        isAssignedToMe = result.isAssignedToMe
                    )
                    setInitialDescription(result.epic.description)
                }.onFailure { error ->
                    Timber.e(error)
                    val errorToShow = getErrorMessage(error)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            initialLoadError = errorToShow
                        )
                    }
                }
        }
    }

    private fun setDropdownMenuExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isDropdownMenuExpanded = isExpanded)
        }
    }

    private fun setAreWorkItemsExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(areWorkItemsExpanded = isExpanded)
        }
    }

    private fun onBlockToggle(isBlocked: Boolean, blockNote: String?) {
        viewModelScope.launch {
            handleBlockToggle(
                isBlocked = isBlocked,
                blockNote = blockNote,
                workItemId = currentEpic.id,
                version = currentEpic.version,
                doOnPreExecute = {
                    clearError()
                    _state.update {
                        it.copy(
                            isLoading = true
                        )
                    }
                },
                doOnError = { error ->
                    emitError(error)
                    _state.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                },
                doOnSuccess = { result ->
                    val patchedEpic = currentEpic.copy(
                        blockedNote = result.blockNote,
                        version = result.patchedData.newVersion
                    )
                    _state.update {
                        it.copy(
                            currentEpic = patchedEpic,
                            originalEpic = patchedEpic,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    private fun onTagRemove(tag: TagUI) {
        viewModelScope.launch {
            handleTagRemove(
                tag = tag,
                version = currentEpic.version,
                workItemId = currentEpic.id,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                },
                doOnSuccess = { version ->
                    updateVersion(version)
                }
            )
        }
    }

    private fun onAssignToMe() {
        viewModelScope.launch {
            handleAssignToMe(
                workItemId = currentEpic.id,
                version = currentEpic.version,
                currentUserId = session.userId,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                },
                doOnSuccess = { version ->
                    updateVersion(version)
                }
            )
        }
    }

    // Watchers segment

    private fun removeWatcher() {
        viewModelScope.launch {
            handleRemoveWatcher(
                version = currentEpic.version,
                workItemId = currentEpic.id,
                doOnPreExecute = {
                    clearError()
                },
                doOnSuccess = { version ->
                    updateVersion(version)
                },
                doOnError = { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            error = getErrorMessage(error)
                        )
                    }
                }
            )
        }
    }

    private fun onRemoveMeFromWatchersClick() {
        viewModelScope.launch {
            handleRemoveMeFromWatchers(
                workItemId = currentEpic.id,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                }
            )
        }
    }

    private fun onAddMeToWatchersClick() {
        viewModelScope.launch {
            handleAddMeToWatchers(
                workItemId = currentEpic.id,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                }
            )
        }
    }

    private fun onWatchersUpdated(newWatchers: ImmutableList<Long>) {
        viewModelScope.launch {
            handleUpdateWatchers(
                version = currentEpic.version,
                workItemId = currentEpic.id,
                newWatchers = newWatchers,
                doOnPreExecute = {
                    clearError()
                },
                doOnSuccess = { version ->
                    updateVersion(version)
                },
                doOnError = { error ->
                    emitError(error)
                }
            )
        }
    }

    private fun removeAssignee() {
        onUnassign()
    }

    private fun onUnassign() {
        viewModelScope.launch {
            handleUnassign(
                workItemId = currentEpic.id,
                version = currentEpic.version,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                },
                doOnSuccess = { version ->
                    updateVersion(version)
                }
            )
        }
    }

    private fun createComment(newComment: String) {
        viewModelScope.launch {
            handleCreateComment(
                version = currentEpic.version,
                id = currentEpic.id,
                comment = newComment,
                doOnPreExecute = {
                    clearError()
                },
                doOnSuccess = { result ->
                    updateVersion(result.newVersion)
                },
                doOnError = { error ->
                    emitError(error)
                }
            )
        }
    }

    private fun deleteComment(comment: Comment) {
        viewModelScope.launch {
            handleDeleteComment(
                id = currentEpic.id,
                commentId = comment.id,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                }
            )
        }
    }

    private fun onCustomFieldSave(item: CustomFieldItemState) {
        viewModelScope.launch {
            handleCustomFieldSave(
                item = item,
                customAttributesVersion = _state.value.customFieldsVersion,
                workItemId = currentEpic.id,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                }
            )
        }
    }

    private fun setIsDeleteDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isDeleteDialogVisible = isVisible)
        }
    }

    private fun doOnDelete() {
        viewModelScope.launch {
            val id = _state.value.currentEpic?.id
            if (id == null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = NativeText.Resource(RString.common_error_message)
                    )
                }
                return@launch
            }

            _state.update {
                it.copy(isLoading = true)
            }

            resultOf {
                workItemRepository.deleteWorkItem(
                    workItemId = id,
                    commonTaskType = CommonTaskType.Epic
                )
            }.onSuccess {
                _deleteTrigger.emit(true)
            }.onFailure { error ->
                Timber.e(error)
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    private fun onAttachmentRemove(attachment: Attachment) {
        viewModelScope.launch {
            handleRemoveAttachment(
                attachment = attachment,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                }
            )
        }
    }

    private fun onAttachmentAdd(uri: Uri?) {
        if (uri == null) {
            _state.update {
                it.copy(
                    error = NativeText.Resource(RString.common_error_message)
                )
            }
            return
        }

        viewModelScope.launch {
            handleAddAttachment(
                workItemId = currentEpic.id,
                uri = uri,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                }
            )
        }
    }

    private fun onTitleSave() {
        viewModelScope.launch {
            handleTitleSave(
                version = currentEpic.version,
                workItemId = currentEpic.id,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    Timber.e(error)
                },
                doOnSuccess = { version ->
                    updateVersion(version)
                }
            )
        }
    }

    private fun onBadgeSave(type: SelectableWorkItemBadgeState, item: StatusUI) {
        viewModelScope.launch {
            handleBadgeSave(
                type = type,
                item = item,
                version = currentEpic.version,
                workItemId = currentEpic.id,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                },
                doOnSuccess = { version ->
                    updateVersion(version)
                }
            )
        }
    }

    private fun onEpicColorPick(color: Color) {
        viewModelScope.launch {
            clearError()
            _state.update {
                it.copy(
                    isEpicColorLoading = true
                )
            }

            val hexColor = color.toHex()
            epicDetailsDataUseCase.changeEpicColor(
                color = hexColor,
                version = currentEpic.version,
                epicId = currentEpic.id
            ).onSuccess { result ->
                val updatedEpic = currentEpic.copy(
                    epicColor = hexColor,
                    version = result.patchedData.newVersion
                )

                _state.update {
                    it.copy(
                        isEpicColorLoading = false,
                        currentEpic = updatedEpic,
                        originalEpic = updatedEpic,
                        userStories = workItemUIMapper.toUI(result.userStories)
                    )
                }
            }.onFailure { error ->
                emitError(error)

                _state.update {
                    it.copy(
                        isEpicColorLoading = false
                    )
                }
            }
        }
    }
}
