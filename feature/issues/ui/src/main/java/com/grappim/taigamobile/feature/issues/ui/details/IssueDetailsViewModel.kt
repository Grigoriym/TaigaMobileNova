@file:OptIn(ExperimentalCoroutinesApi::class)

package com.grappim.taigamobile.feature.issues.ui.details

import android.net.Uri
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
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.issues.domain.IssueDetailsDataUseCase
import com.grappim.taigamobile.feature.issues.domain.IssueTask
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
import com.grappim.taigamobile.feature.workitem.ui.delegates.duedate.WorkItemDueDateDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.duedate.WorkItemDueDateDelegateImpl
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
import com.grappim.taigamobile.feature.workitem.ui.models.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import com.grappim.taigamobile.feature.workitem.ui.utils.getDueDateText
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.file.FileUriManager
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class IssueDetailsViewModel @Inject constructor(
    private val issueDetailsDataUseCase: IssueDetailsDataUseCase,
    savedStateHandle: SavedStateHandle,
    private val statusUIMapper: StatusUIMapper,
    private val tagUIMapper: TagUIMapper,
    private val customFieldsUIMapper: CustomFieldsUIMapper,
    private val workItemsGenerator: WorkItemsGenerator,
    private val workItemEditShared: WorkItemEditShared,
    private val dateTimeUtils: DateTimeUtils,
    private val fileUriManager: FileUriManager,
    private val session: Session,
    private val patchDataGenerator: PatchDataGenerator,
    private val historyRepository: HistoryRepository,
    private val workItemRepository: WorkItemRepository,
    private val taigaStorage: TaigaStorage,
    private val usersRepository: UsersRepository
) : ViewModel(),
    WorkItemTitleDelegate by WorkItemTitleDelegateImpl(
        commonTaskType = CommonTaskType.Issue,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ),
    WorkItemBadgeDelegate by WorkItemBadgeDelegateImpl(
        commonTaskType = CommonTaskType.Issue,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ),
    WorkItemTagsDelegate by WorkItemTagsDelegateImpl(
        commonTaskType = CommonTaskType.Issue,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator,
        workItemEditShared = workItemEditShared
    ),
    WorkItemCommentsDelegate by WorkItemCommentsDelegateImpl(
        commonTaskType = CommonTaskType.Issue,
        historyRepository = historyRepository,
        workItemRepository = workItemRepository
    ),
    WorkItemAttachmentsDelegate by WorkItemAttachmentsDelegateImpl(
        workItemRepository = workItemRepository,
        commonTaskType = CommonTaskType.Issue,
        fileUriManager = fileUriManager,
        taigaStorage = taigaStorage
    ),
    WorkItemWatchersDelegate by WorkItemWatchersDelegateImpl(
        commonTaskType = CommonTaskType.Issue,
        workItemRepository = workItemRepository,
        usersRepository = usersRepository,
        patchDataGenerator = patchDataGenerator,
        session = session,
        workItemEditShared = workItemEditShared
    ),
    WorkItemCustomFieldsDelegate by WorkItemCustomFieldsDelegateImpl(
        commonTaskType = CommonTaskType.Issue,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator,
        dateTimeUtils = dateTimeUtils
    ),
    WorkItemDueDateDelegate by WorkItemDueDateDelegateImpl(
        commonTaskType = CommonTaskType.Issue,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator,
        dateTimeUtils = dateTimeUtils
    ),
    WorkItemBlockDelegate by WorkItemBlockDelegateImpl(
        commonTaskType = CommonTaskType.Issue,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ),
    WorkItemSingleAssigneeDelegate by WorkItemSingleAssigneeDelegateImpl(
        commonTaskType = CommonTaskType.Issue,
        workItemRepository = workItemRepository,
        usersRepository = usersRepository,
        patchDataGenerator = patchDataGenerator,
        workItemEditShared = workItemEditShared
    ),
    WorkItemDescriptionDelegate by WorkItemDescriptionDelegateImpl(
        commonTaskType = CommonTaskType.Issue,
        workItemRepository = workItemRepository,
        patchDataGenerator = patchDataGenerator
    ) {

    private val route = savedStateHandle.toRoute<IssueDetailsNavDestination>()

    private val taskId: Long = route.taskId
    private val ref = route.ref

    private val _state = MutableStateFlow(
        IssueDetailsState(
            setDropdownMenuExpanded = ::setDropdownMenuExpanded,
            toolbarTitle = NativeText.Arguments(
                id = RString.issue_slug,
                args = listOf(ref)
            ),
            onCustomFieldSave = ::onCustomFieldSave,
            onTagRemove = ::onTagRemove,
            setDueDate = ::setDueDate,
            onBlockToggle = ::onBlockToggle,
            setIsDeleteDialogVisible = ::setIsDeleteDialogVisible,
            onDelete = ::doOnDelete,
            onAttachmentRemove = ::onAttachmentRemove,
            onAttachmentAdd = ::onAttachmentAdd,
            onCommentRemove = ::deleteComment,
            onCreateCommentClick = ::createComment,
            onAssignToMe = ::onAssignToMe,
            onUnassign = ::onUnassign,
            onRemoveMeFromWatchersClick = ::onRemoveMeFromWatchersClick,
            onAddMeToWatchersClick = ::onAddMeToWatchersClick,
            removeWatcher = ::removeWatcher,
            removeAssignee = ::removeAssignee,
            retryLoadIssue = ::retryLoadIssue,
            onTitleSave = ::onTitleSave,
            onBadgeSave = ::onBadgeSave
        )
    )
    val state = _state.asStateFlow()

    private val currentIssue: IssueTask
        get() = requireNotNull(_state.value.currentIssue)

    private val _deleteTrigger = MutableSharedFlow<Boolean>()
    val deleteTrigger = _deleteTrigger.asSharedFlow()

    init {
        loadIssue()

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

    private fun onTitleSave() {
        viewModelScope.launch {
            handleTitleSave(
                version = currentIssue.version,
                workItemId = currentIssue.id,
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

    private fun retryLoadIssue() {
        loadIssue()
    }

    private fun loadIssue() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    initialLoadError = NativeText.Empty
                )
            }
            issueDetailsDataUseCase.getIssueData(issueId = taskId)
                .onSuccess { result ->
                    val typeUiDeferred = result.issueTask.type?.let { type ->
                        async { statusUIMapper.toUI(type) }
                    }
                    val severityUiDeferred = result.issueTask.severity?.let { task ->
                        async { statusUIMapper.toUI(task) }
                    }

                    val priorityUiDeferred = result.issueTask.priority?.let { prio ->
                        async { statusUIMapper.toUI(prio) }
                    }
                    val statusUiDeferred = result.issueTask.status?.let { status ->
                        async { statusUIMapper.toUI(status) }
                    }

                    val tags = async {
                        tagUIMapper.toUI(result.issueTask.tags).toPersistentList()
                    }
                    val customFieldsStateItems = async {
                        customFieldsUIMapper.toUI(result.customFields)
                    }

                    val statusUi = statusUiDeferred?.await()
                    val sprint = result.sprint
                    val typeUI = typeUiDeferred?.await()
                    val severityUI = severityUiDeferred?.await()
                    val priorityUi = priorityUiDeferred?.await()
                    val workItemBadges = workItemsGenerator.getItems(
                        statusUI = statusUi,
                        typeUI = typeUI,
                        severityUI = severityUI,
                        priorityUi = priorityUi,
                        filtersData = result.filtersData
                    )
                    val dueDateText = dateTimeUtils.getDueDateText(
                        dueDate = result.issueTask.dueDate
                    )
                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentIssue = result.issueTask,
                            originalIssue = result.issueTask,
                            sprint = sprint,
                            creator = result.creator,
                            filtersData = result.filtersData,
                            initialLoadError = NativeText.Empty,
                            customFieldsVersion = result.customFields.version
                        )
                    }

                    setInitialTitle(result.issueTask.title)
                    setWorkItemBadges(workItemBadges)
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
                    setInitialDueDate(dueDateText = dueDateText)
                    setInitialAssignees(
                        assignees = result.assignees.toPersistentList(),
                        isAssignedToMe = result.isAssignedToMe
                    )
                    setInitialDescription(result.issueTask.description)
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
        _state.update {
            it.copy(
                error = getErrorMessage(error)
            )
        }
    }

    private fun updateVersion(newVersion: Long) {
        val updatedIssue = currentIssue.copy(
            version = newVersion
        )
        _state.update {
            it.copy(
                currentIssue = updatedIssue,
                originalIssue = updatedIssue
            )
        }
    }

    // Watchers segment

    private fun removeWatcher() {
        viewModelScope.launch {
            handleRemoveWatcher(
                version = currentIssue.version,
                workItemId = currentIssue.id,
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
                workItemId = currentIssue.id,
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
                workItemId = currentIssue.id,
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
                version = currentIssue.version,
                workItemId = currentIssue.id,
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

    // Assignees section

    private fun onAssigneeUpdated(newAssigneeId: Long?) {
        viewModelScope.launch {
            handleUpdateAssignee(
                newAssigneeId = newAssigneeId,
                workItemId = currentIssue.id,
                version = currentIssue.version,
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

    private fun removeAssignee() {
        onUnassign()
    }

    private fun onAssignToMe() {
        viewModelScope.launch {
            handleAssignToMe(
                workItemId = currentIssue.id,
                version = currentIssue.version,
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

    private fun onUnassign() {
        viewModelScope.launch {
            handleUnassign(
                workItemId = currentIssue.id,
                version = currentIssue.version,
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
                version = currentIssue.version,
                id = currentIssue.id,
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
                id = currentIssue.id,
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

    private fun setIsDeleteDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isDeleteDialogVisible = isVisible)
        }
    }

    private fun doOnDelete() {
        viewModelScope.launch {
            val id = _state.value.currentIssue?.id
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
                    commonTaskType = CommonTaskType.Issue
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

    private fun onBlockToggle(isBlocked: Boolean, blockNote: String?) {
        viewModelScope.launch {
            handleBlockToggle(
                isBlocked = isBlocked,
                blockNote = blockNote,
                workItemId = currentIssue.id,
                version = currentIssue.version,
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
                    val patchedIssue = currentIssue.copy(
                        blockedNote = result.blockNote,
                        version = result.patchedData.newVersion
                    )
                    _state.update {
                        it.copy(
                            currentIssue = patchedIssue,
                            originalIssue = patchedIssue,
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    private fun setDueDate(newDate: Long?) {
        viewModelScope.launch {
            handleDueDateSave(
                newDate = newDate,
                workItemId = currentIssue.id,
                version = currentIssue.version,
                doOnPreExecute = {
                    clearError()
                },
                doOnSuccess = { result ->
                    val updatedIssue = currentIssue.copy(
                        dueDate = result.dueDate,
                        dueDateStatus = result.patchedData.dueDateStatus,
                        version = result.patchedData.newVersion
                    )

                    _state.update {
                        it.copy(
                            currentIssue = updatedIssue,
                            originalIssue = updatedIssue
                        )
                    }
                },
                doOnError = { error ->
                    emitError(error)
                }
            )
        }
    }

    private fun onTagRemove(tag: TagUI) {
        viewModelScope.launch {
            handleTagRemove(
                tag = tag,
                version = currentIssue.version,
                workItemId = currentIssue.id,
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

    private suspend fun onNewTagsUpdate(newTagsToUse: PersistentList<TagUI>) {
        handleTagsUpdate(
            newTags = newTagsToUse,
            version = currentIssue.version,
            workItemId = currentIssue.id,
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
            version = currentIssue.version,
            workItemId = currentIssue.id,
            newDescription = newDescription,
            doOnPreExecute = {
                clearError()
            },
            doOnError = { error ->
                emitError(error)
            },
            doOnSuccess = { version ->
                updateVersion(version)

                val updatedIssue = currentIssue.copy(description = newDescription)

                _state.update { currentState ->
                    currentState.copy(
                        currentIssue = updatedIssue,
                        originalIssue = updatedIssue
                    )
                }
            }
        )
    }

    private fun onBadgeSave(type: SelectableWorkItemBadgeState, item: StatusUI) {
        viewModelScope.launch {
            handleBadgeSave(
                type = type,
                item = item,
                version = currentIssue.version,
                workItemId = currentIssue.id,
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

    private fun onCustomFieldSave(item: CustomFieldItemState) {
        viewModelScope.launch {
            handleCustomFieldSave(
                item = item,
                customAttributesVersion = _state.value.customFieldsVersion,
                workItemId = currentIssue.id,
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
                workItemId = currentIssue.id,
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

    private fun setDropdownMenuExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isDropdownMenuExpanded = isExpanded)
        }
    }
}
