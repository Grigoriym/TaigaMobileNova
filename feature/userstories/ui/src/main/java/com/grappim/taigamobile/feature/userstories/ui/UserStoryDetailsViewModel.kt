package com.grappim.taigamobile.feature.userstories.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoryDetailsDataUseCase
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.domain.Comment
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.multiple.WorkItemMultipleAssigneesDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.multiple.WorkItemMultipleAssigneesDelegateImpl
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
import com.grappim.taigamobile.feature.workitem.ui.delegates.duedate.WorkItemDueDateDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.duedate.WorkItemDueDateDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.tags.WorkItemTagsDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.tags.WorkItemTagsDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.title.WorkItemTitleDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.title.WorkItemTitleDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.watchers.WorkItemWatchersDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.watchers.WorkItemWatchersDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.mappers.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.mappers.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegate
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegateImpl
import com.grappim.taigamobile.utils.ui.file.FileUriManager
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
class UserStoryDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userStoryDetailsDataUseCase: UserStoryDetailsDataUseCase,
    private val workItemsGenerator: WorkItemsGenerator,
    private val workItemEditStateRepository: WorkItemEditStateRepository,
    private val patchDataGenerator: PatchDataGenerator,
    private val statusUIMapper: StatusUIMapper,
    private val tagUIMapper: TagUIMapper,
    private val dateTimeUtils: DateTimeUtils,
    private val fileUriManager: FileUriManager,
    private val customFieldsUIMapper: CustomFieldsUIMapper,
    private val historyRepository: HistoryRepository,
    private val workItemRepository: WorkItemRepository,
    private val taigaSessionStorage: TaigaSessionStorage,
    private val usersRepository: UsersRepository,
    private val epicsRepository: EpicsRepository
) : ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl(),
    WorkItemTitleDelegate by WorkItemTitleDelegateImpl(
        commonTaskType = CommonTaskType.UserStory,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ),
    WorkItemBadgeDelegate by WorkItemBadgeDelegateImpl(
        commonTaskType = CommonTaskType.UserStory,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ),
    WorkItemTagsDelegate by WorkItemTagsDelegateImpl(
        commonTaskType = CommonTaskType.UserStory,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ),
    WorkItemCommentsDelegate by WorkItemCommentsDelegateImpl(
        historyRepository = historyRepository,
        commonTaskType = CommonTaskType.UserStory,
        workItemRepository = workItemRepository
    ),
    WorkItemAttachmentsDelegate by WorkItemAttachmentsDelegateImpl(
        taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory),
        workItemRepository = workItemRepository,
        fileUriManager = fileUriManager,
        taigaSessionStorage = taigaSessionStorage
    ),
    WorkItemWatchersDelegate by WorkItemWatchersDelegateImpl(
        commonTaskType = CommonTaskType.UserStory,
        workItemRepository = workItemRepository,
        usersRepository = usersRepository,
        patchDataGenerator = patchDataGenerator,
        taigaSessionStorage = taigaSessionStorage
    ),
    WorkItemCustomFieldsDelegate by WorkItemCustomFieldsDelegateImpl(
        commonTaskType = CommonTaskType.UserStory,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator,
        dateTimeUtils = dateTimeUtils
    ),
    WorkItemDueDateDelegate by WorkItemDueDateDelegateImpl(
        commonTaskType = CommonTaskType.UserStory,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator,
        dateTimeUtils = dateTimeUtils
    ),
    WorkItemBlockDelegate by WorkItemBlockDelegateImpl(
        commonTaskType = CommonTaskType.UserStory,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ),
    WorkItemMultipleAssigneesDelegate by WorkItemMultipleAssigneesDelegateImpl(
        commonTaskType = CommonTaskType.UserStory,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator,
        usersRepository = usersRepository,
        taigaSessionStorage = taigaSessionStorage
    ),
    WorkItemDescriptionDelegate by WorkItemDescriptionDelegateImpl(
        taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory),
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ) {

    private val route = savedStateHandle.toRoute<UserStoryDetailsNavDestination>()
    private val ref = route.ref
    private val userStoryId: Long = route.userStoryId

    private val _state = MutableStateFlow(
        UserStoryDetailsState(
            toolbarTitle = NativeText.Arguments(
                id = RString.userstory_slug,
                args = listOf(ref)
            ),
            setDropdownMenuExpanded = ::setDropdownMenuExpanded,
            retryLoadUserStory = ::retryLoadUserStory,
            setDueDate = ::setDueDate,
            onTagRemove = ::onTagRemove,
            onAssignToMe = ::onAssignToMe,
            onRemoveMeFromWatchersClick = ::onRemoveMeFromWatchersClick,
            onAddMeToWatchersClick = ::onAddMeToWatchersClick,
            removeWatcher = ::removeWatcher,
            removeAssignee = ::removeAssignee,
            onCustomFieldSave = ::onCustomFieldSave,
            onBlockToggle = ::onBlockToggle,
            setIsDeleteDialogVisible = ::setIsDeleteDialogVisible,
            onDelete = ::doOnDelete,
            onAttachmentRemove = ::onAttachmentRemove,
            onAttachmentAdd = ::onAttachmentAdd,
            onCommentRemove = ::deleteComment,
            onCreateCommentClick = ::createComment,
            onTitleSave = ::onTitleSave,
            onBadgeSave = ::onBadgeSave,
            onGoingToEditEpics = ::onGoingToEditEpics,
            onEpicRemoveClick = ::onEpicRemoveClick,
            onEditTags = ::onEditTags,
            onGoingToEditWatchers = ::onGoingToEditWatchers,
            onGoingToEditAssignees = ::onGoingToEditAssignees
        )
    )
    val state = _state.asStateFlow()

    private val currentUserStory: UserStory
        get() = requireNotNull(_state.value.currentUserStory)

    private val _deleteTrigger = MutableSharedFlow<Boolean>()
    val deleteTrigger = _deleteTrigger.asSharedFlow()

    init {
        loadUserStory()

        workItemEditStateRepository
            .getTeamMemberUpdateFlow(userStoryId, TaskIdentifier.WorkItem(CommonTaskType.UserStory))
            .onEach(::handleTeamMemberUpdate)
            .launchIn(viewModelScope)

        workItemEditStateRepository
            .getTagsFlow(userStoryId, TaskIdentifier.WorkItem(CommonTaskType.UserStory))
            .onEach(::onNewTagsUpdate)
            .launchIn(viewModelScope)

        workItemEditStateRepository
            .getDescriptionFlow(userStoryId, TaskIdentifier.WorkItem(CommonTaskType.UserStory))
            .onEach(::onNewDescriptionUpdate)
            .launchIn(viewModelScope)

        workItemEditStateRepository
            .getEpicsFlow(userStoryId, TaskIdentifier.WorkItem(CommonTaskType.UserStory))
            .onEach(::onEpicsUpdate)
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        workItemEditStateRepository.clearSession(
            workItemId = userStoryId,
            type = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        )
        Timber.d("UserStoryDetailsViewModel cleared - session cleaned up for taskId: $userStoryId")
    }

    private fun onEditTags() {
        workItemEditStateRepository.setTags(
            workItemId = userStoryId,
            type = TaskIdentifier.WorkItem(CommonTaskType.UserStory),
            tags = tagsState.value.tags
        )
    }

    private fun onGoingToEditWatchers() {
        val watchersIds = watchersState.value.watchers.mapNotNull { it.id }
            .toPersistentList()
        workItemEditStateRepository.setCurrentWatchers(
            ids = watchersIds,
            workItemId = userStoryId,
            type = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        )
    }

    fun onGoingToEditAssignees() {
        val assigneesIds = multipleAssigneesState.value.assignees
            .mapNotNull { it.id }
            .toImmutableList()
        workItemEditStateRepository.setCurrentAssignees(
            ids = assigneesIds,
            workItemId = userStoryId,
            type = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        )
    }

    private fun loadUserStory() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    initialLoadError = NativeText.Empty
                )
            }

            userStoryDetailsDataUseCase.getUserStoryData(id = userStoryId)
                .onSuccess { result ->
                    val statusUi = result.userStory.status?.let {
                        statusUIMapper.toUI(statuses = it)
                    }
                    val workItemBadges = async {
                        workItemsGenerator.getItems(
                            statusUI = statusUi,
                            filtersData = result.filtersData
                        )
                    }
                    val tags = async {
                        tagUIMapper.toUI(result.userStory.tags).toPersistentList()
                    }
                    val customFieldsStateItems = async {
                        customFieldsUIMapper.toUI(result.customFields)
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentUserStory = result.userStory,
                            originalUserStory = result.userStory,
                            sprint = result.sprint,
                            creator = result.creator,
                            filtersData = result.filtersData,
                            initialLoadError = NativeText.Empty,
                            customFieldsVersion = result.customFields.version,
                            canEditUserStory = result.canModifyUserStory,
                            canDeleteUserStory = result.canDeleteUserStory,
                            canComment = result.canComment,
                            canModifyRelatedEpic = result.canModifyRelatedEpic
                        )
                    }
                    setInitialTitle(result.userStory.title)
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
                    setInitialDueDate(
                        dueDate = result.userStory.dueDate,
                        dueDateStatus = result.userStory.dueDateStatus
                    )
                    setInitialAssignees(
                        assignees = result.assignees.toPersistentList(),
                        isAssignedToMe = result.isAssignedToMe
                    )
                    setInitialDescription(result.userStory.description)
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

    private fun clearError() {
        _state.update {
            it.copy(
                error = NativeText.Empty
            )
        }
    }

    private fun emitError(error: Throwable) {
        Timber.e(error)
        viewModelScope.launch {
            showSnackbarSuspend(getErrorMessage(error))
        }
    }

    private fun updateVersion(newVersion: Long) {
        val updatedUserStory = currentUserStory.copy(
            version = newVersion
        )
        _state.update {
            it.copy(
                currentUserStory = updatedUserStory,
                originalUserStory = updatedUserStory
            )
        }
    }

    private fun onTitleSave() {
        viewModelScope.launch {
            handleTitleSave(
                version = currentUserStory.version,
                workItemId = currentUserStory.id,
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

    private fun createComment(newComment: String) {
        viewModelScope.launch {
            handleCreateComment(
                version = currentUserStory.version,
                id = currentUserStory.id,
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
                id = currentUserStory.id,
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
                workItemId = currentUserStory.id,
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

    private fun setIsDeleteDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isDeleteDialogVisible = isVisible)
        }
    }

    private fun doOnDelete() {
        viewModelScope.launch {
            val id = _state.value.currentUserStory?.id
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

            userStoryDetailsDataUseCase.deleteUserStory(
                id = id
            ).onSuccess {
                _deleteTrigger.emit(true)
            }.onFailure { error ->
                Timber.e(error)
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    private fun onBlockToggle(isBlocked: Boolean, blockNote: String?) {
        viewModelScope.launch {
            handleBlockToggle(
                isBlocked = isBlocked,
                blockNote = blockNote,
                workItemId = currentUserStory.id,
                version = currentUserStory.version,
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
                    val updatedUserStory = currentUserStory.copy(
                        blockedNote = result.blockNote,
                        version = result.patchedData.newVersion
                    )
                    _state.update {
                        it.copy(
                            currentUserStory = updatedUserStory,
                            originalUserStory = updatedUserStory,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    private fun onCustomFieldSave(item: CustomFieldItemState) {
        viewModelScope.launch {
            handleCustomFieldSave(
                item = item,
                customAttributesVersion = _state.value.customFieldsVersion,
                workItemId = currentUserStory.id,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                }
            )
        }
    }

    // Assignees section
    private fun removeAssignee() {
        viewModelScope.launch {
            handleRemoveAssignee(
                version = currentUserStory.version,
                workItemId = currentUserStory.id,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                },
                doOnSuccess = { newVersion ->
                    updateVersion(newVersion)
                }
            )
        }
    }

    private fun onAssignToMe() {
        viewModelScope.launch {
            handleAssignToMe(
                workItemId = currentUserStory.id,
                version = currentUserStory.version,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                },
                doOnSuccess = { newVersion ->
                    updateVersion(newVersion)
                }
            )
        }
    }

    private fun onAssigneeUpdated(newAssignees: ImmutableList<Long>) {
        viewModelScope.launch {
            handleUpdateAssignees(
                newAssignees = newAssignees,
                workItemId = currentUserStory.id,
                version = currentUserStory.version,
                doOnPreExecute = {
                    clearError()
                },
                doOnError = { error ->
                    emitError(error)
                },
                doOnSuccess = { newVersion ->
                    updateVersion(newVersion)
                }
            )
        }
    }

    // Watchers segment

    private fun removeWatcher() {
        viewModelScope.launch {
            handleRemoveWatcher(
                version = currentUserStory.version,
                workItemId = currentUserStory.id,
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

    private fun onRemoveMeFromWatchersClick() {
        viewModelScope.launch {
            handleRemoveMeFromWatchers(
                workItemId = currentUserStory.id,
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
                workItemId = currentUserStory.id,
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
                version = currentUserStory.version,
                workItemId = currentUserStory.id,
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

    private fun onBadgeSave(type: SelectableWorkItemBadgeState, item: StatusUI) {
        viewModelScope.launch {
            handleBadgeSave(
                type = type,
                item = item,
                version = currentUserStory.version,
                workItemId = currentUserStory.id,
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

    private suspend fun handleTeamMemberUpdate(updateState: TeamMemberUpdate) {
        when (updateState) {
            TeamMemberUpdate.Clear -> {}
            is TeamMemberUpdate.Assignee -> {}
            is TeamMemberUpdate.Assignees -> {
                onAssigneeUpdated(updateState.ids)
            }

            is TeamMemberUpdate.Watchers -> {
                onWatchersUpdated(updateState.ids)
            }
        }
    }

    private suspend fun onNewDescriptionUpdate(newDescription: String) {
        updateDescription(
            version = currentUserStory.version,
            workItemId = currentUserStory.id,
            newDescription = newDescription,
            doOnPreExecute = {
                clearError()
            },
            doOnError = { error ->
                emitError(error)
            },
            doOnSuccess = { version ->
                updateVersion(version)

                val updatedUserStory = currentUserStory.copy(description = newDescription)

                _state.update { currentState ->
                    currentState.copy(
                        currentUserStory = updatedUserStory,
                        originalUserStory = updatedUserStory
                    )
                }
            }
        )
    }

    private fun retryLoadUserStory() {
        loadUserStory()
    }

    private fun setDropdownMenuExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isDropdownMenuExpanded = isExpanded)
        }
    }

    private fun setDueDate(newDate: Long?) {
        viewModelScope.launch {
            handleDueDateSave(
                newDate = newDate,
                workItemId = currentUserStory.id,
                version = currentUserStory.version,
                doOnPreExecute = {
                    clearError()
                },
                doOnSuccess = { patchedData ->
                    val updatedUserStory = currentUserStory.copy(
                        version = patchedData.newVersion
                    )

                    _state.update {
                        it.copy(
                            currentUserStory = updatedUserStory,
                            originalUserStory = updatedUserStory
                        )
                    }
                },
                doOnError = { error ->
                    emitError(error)
                }
            )
        }
    }

    private suspend fun onNewTagsUpdate(newTagsToUse: PersistentList<TagUI>) {
        handleTagsUpdate(
            newTags = newTagsToUse,
            version = currentUserStory.version,
            workItemId = currentUserStory.id,
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

    private fun onTagRemove(tag: TagUI) {
        viewModelScope.launch {
            handleTagRemove(
                tag = tag,
                version = currentUserStory.version,
                workItemId = currentUserStory.id,
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

    private fun onGoingToEditEpics() {
        val currentEpicsIds = _state.value.currentUserStory?.userStoryEpics
            ?.map { it.id }?.toImmutableList()
        workItemEditStateRepository.setCurrentEpics(
            ids = currentEpicsIds,
            workItemId = userStoryId,
            type = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        )
    }

    private fun onEpicRemoveClick(id: Long) {
        viewModelScope.launch {
            _state.update {
                it.copy(areUserStoryEpicsLoading = true)
            }

            resultOf {
                epicsRepository.unlinkFromEpic(
                    epicId = id,
                    userStoryId = userStoryId
                )
                userStoryDetailsDataUseCase.getUserStory(userStoryId)
            }.onSuccess { result ->
                _state.update {
                    it.copy(
                        currentUserStory = result,
                        originalUserStory = result,
                        areUserStoryEpicsLoading = false
                    )
                }
            }.onFailure { error ->
                emitError(error)
                _state.update {
                    it.copy(areUserStoryEpicsLoading = false)
                }
            }
        }
    }

    private fun onEpicsUpdate(ids: PersistentList<Long>) {
        viewModelScope.launch {
            val previousIds = currentUserStory.userStoryEpics.map { it.id }.toSet()
            val newIds = ids.toSet()

            val idsToAdd = newIds - previousIds
            val idsToRemove = previousIds - newIds

            if (idsToAdd.isEmpty() && idsToRemove.isEmpty()) {
                return@launch
            }

            _state.update {
                it.copy(areUserStoryEpicsLoading = true)
            }

            resultOf {
                coroutineScope {
                    val operations = buildList {
                        idsToRemove.forEach { epicId ->
                            add(
                                async {
                                    epicsRepository.unlinkFromEpic(
                                        epicId = epicId,
                                        userStoryId = userStoryId
                                    )
                                }
                            )
                        }
                        idsToAdd.forEach { epicId ->
                            add(
                                async {
                                    epicsRepository.linkToEpic(
                                        epicId = epicId,
                                        userStoryId = userStoryId
                                    )
                                }
                            )
                        }
                    }

                    operations.awaitAll()
                }

                userStoryDetailsDataUseCase.getUserStory(userStoryId)
            }.onSuccess { result ->
                _state.update {
                    it.copy(
                        currentUserStory = result,
                        originalUserStory = result,
                        areUserStoryEpicsLoading = false
                    )
                }
            }.onFailure { error ->
                emitError(error)
                _state.update {
                    it.copy(areUserStoryEpicsLoading = false)
                }
            }
        }
    }
}
