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
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.issues.domain.IssueDetailsDataUseCase
import com.grappim.taigamobile.feature.issues.domain.IssueTask
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.badge.WorkItemBadgeDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.badge.WorkItemBadgeDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.comments.WorkItemCommentsDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.comments.WorkItemCommentsDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.customfields.WorkItemCustomFieldsDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.customfields.WorkItemCustomFieldsDelegateImpl
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
import com.grappim.taigamobile.utils.ui.toHex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
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
    private val usersRepository: UsersRepository,
) : ViewModel(),
    WorkItemTitleDelegate by WorkItemTitleDelegateImpl(),
    WorkItemBadgeDelegate by WorkItemBadgeDelegateImpl(patchDataGenerator),
    WorkItemTagsDelegate by WorkItemTagsDelegateImpl(workItemEditShared),
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
            setIsRemoveAssigneeDialogVisible = ::setIsRemoveAssigneeDialogVisible,
            onCustomFieldSave = ::onCustomFieldSave,
            onTagRemove = ::onTagRemove,
            setDueDate = ::setDueDate,
            onBlockToggle = ::onBlockToggle,
            setIsBlockDialogVisible = ::setIsBlockDialogVisible,
            setIsDeleteDialogVisible = ::setIsDeleteDialogVisible,
            onDelete = ::doOnDelete,
            onAttachmentRemove = ::onAttachmentRemove,
            onAttachmentAdd = ::onAttachmentAdd,
            onCommentRemove = ::deleteComment,
            onCreateCommentClick = ::createComment,
            onAssignToMe = ::onAssignToMe,
            onUnassign = ::onUnassign,
            onGoingToEditAssignee = ::onGoingToEditAssignee,
            onRemoveMeFromWatchersClick = ::onRemoveMeFromWatchersClick,
            onAddMeToWatchersClick = ::onAddMeToWatchersClick,
            removeWatcher = ::removeWatcher,
            removeAssignee = ::removeAssignee,
            onRemoveAssigneeClick = ::onRemoveAssigneeClick,
            retryLoadIssue = ::retryLoadIssue,
            onTitleSave = ::handleTitleSave,
            onBadgeSave = ::handleBadgeSave,
        )
    )
    val state = _state.asStateFlow()

    private val currentIssue: IssueTask
        get() = requireNotNull(_state.value.currentIssue)

    private val _deleteTrigger = MutableSharedFlow<Boolean>()
    val deleteTrigger = _deleteTrigger.asSharedFlow()

    init {
        workItemEditShared.teamMemberUpdateState
            .onEach(::handleTeamMemberUpdate)
            .launchIn(viewModelScope)

        workItemEditShared.tagsState
            .onEach(::onNewTagsUpdate)
            .launchIn(viewModelScope)

        workItemEditShared.descriptionState
            .onEach(::onNewDescriptionUpdate)
            .launchIn(viewModelScope)

        loadIssue()
    }

    private fun handleTitleSave() {
        onTitleSave(onSaveTitleToBackend = ::saveTitleToBackend)
    }

    private fun saveTitleToBackend() {
        val currentTitle = titleState.value.currentTitle

        viewModelScope.launch {
            val patchableData = patchDataGenerator.getTitle(currentTitle)

            patchData(
                payload = patchableData,
                doOnSuccess = { _: PatchedData, _: IssueTask ->
                    onTitleSaveSuccess()
                },
                doOnError = { error ->
                    Timber.e(error)
                    onTitleError(getErrorMessage(error))
                }
            )
        }
    }

    private fun handleBadgeSave(type: SelectableWorkItemBadgeState, item: StatusUI) {
        onBadgeSave(
            type = type,
            onSaveBadgeToBackend = {
                onBadgeSheetItemClick(type, item)
            }
        )
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
                        result.issueTask.tags.map { tag ->
                            tagUIMapper.toUI(tag)
                        }.toPersistentList()
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
                            assignees = result.assignees.toPersistentList(),
                            isAssignedToMe = result.isAssignedToMe,
                            filtersData = result.filtersData,
                            initialLoadError = NativeText.Empty
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
                        customFieldStateItems = customFieldsStateItems.await(),
                    )
                    setInitialDueDate(dueDateText = dueDateText)
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
                error = getErrorMessage(error),
            )
        }
    }

    private fun removeAssignee() {
        patchAssignedToMe(true)
    }

    private fun onRemoveAssigneeClick() {
        _state.update {
            it.copy(
                isRemoveAssigneeDialogVisible = true
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
                originalIssue = updatedIssue,
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
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            error = getErrorMessage(error),
                        )
                    }
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
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            error = getErrorMessage(error),
                        )
                    }
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

    private fun handleTeamMemberUpdate(updateState: TeamMemberUpdate) {
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

    private fun onAssigneeUpdated(newAssignee: Long?) {
        val currentIssue = _state.value.currentIssue ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(error = NativeText.Empty, isAssigneesLoading = true)
            }

            issueDetailsDataUseCase.updateAssigneesData(
                version = currentIssue.version,
                issueId = currentIssue.id,
                userId = newAssignee
            ).onSuccess { result ->
                val updatedIssue = currentIssue.copy(
                    version = result.newVersion
                )
                _state.update {
                    it.copy(
                        isAssigneesLoading = false,
                        currentIssue = updatedIssue,
                        originalIssue = updatedIssue,
                        isAssignedToMe = result.isAssignedToMe,
                        assignees = result.assignees.toPersistentList()
                    )
                }
            }.onFailure { error ->
                Timber.e(error)
                _state.update {
                    it.copy(
                        error = getErrorMessage(error),
                        isAssigneesLoading = false
                    )
                }
            }
        }
    }

    private fun onGoingToEditAssignee() {
        val assigneeId = _state.value.assignees.firstOrNull()?.id
        workItemEditShared.setCurrentAssignee(assigneeId)
    }

    private fun onAssignToMe() {
        patchAssignedToMe(false)
    }

    private fun onUnassign() {
        patchAssignedToMe(true)
    }

    private fun patchAssignedToMe(onUnassign: Boolean) {
        val currentIssue = _state.value.currentIssue ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(error = NativeText.Empty, isAssigneesLoading = true)
            }

            issueDetailsDataUseCase.updateAssigneesData(
                version = currentIssue.version,
                issueId = currentIssue.id,
                userId = if (onUnassign) null else session.userId
            ).onSuccess { result ->
                val updatedIssue = currentIssue.copy(
                    version = result.newVersion
                )
                _state.update {
                    it.copy(
                        isAssigneesLoading = false,
                        currentIssue = updatedIssue,
                        originalIssue = updatedIssue,
                        isAssignedToMe = result.isAssignedToMe,
                        assignees = result.assignees.toPersistentList()
                    )
                }
            }.onFailure { error ->
                Timber.e(error)
                _state.update {
                    it.copy(
                        error = getErrorMessage(error),
                        isAssigneesLoading = false
                    )
                }
            }
        }
    }

    private fun createComment(newComment: String) {
        viewModelScope.launch {
            handleCreateComment(
                version = currentIssue.version,
                id = currentIssue.id,
                comment = newComment,
                doOnPreExecute = {
                    _state.update {
                        it.copy(error = NativeText.Empty)
                    }
                },
                doOnSuccess = { result ->
                    val updatedUserStory = currentIssue.copy(
                        version = result.newVersion
                    )

                    _state.update {
                        it.copy(
                            currentIssue = updatedUserStory,
                            originalIssue = updatedUserStory
                        )
                    }
                },
                doOnError = { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(error = getErrorMessage(error))
                    }
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
                    _state.update {
                        it.copy(error = NativeText.Empty)
                    }
                },
                doOnError = { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(error = getErrorMessage(error))
                    }
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

            issueDetailsDataUseCase.deleteIssue(
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
            patchData(
                payload = patchDataGenerator.getBlockedPatchPayload(isBlocked, blockNote),
                doOnPreExecute = {
                    _state.update {
                        it.copy(
                            isLoading = true
                        )
                    }
                },
                doOnSuccess = { _: PatchedData, task: IssueTask ->
                    val updatedIssue = task.copy(blockedNote = blockNote)
                    _state.update {
                        it.copy(
                            currentIssue = updatedIssue,
                            originalIssue = updatedIssue,
                            isLoading = false
                        )
                    }
                },
                doOnError = { error ->
                    _state.update {
                        it.copy(
                            error = getErrorMessage(error),
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    private fun setIsBlockDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isBlockDialogVisible = isVisible)
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
                            originalIssue = updatedIssue,
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
        val currentTags = tagsState.value.tags
        val newTagsToUse = currentTags.removeAll { it.name == tag.name }

        handleTagRemove(
            tag = tag,
            onRemoveTagFromBackend = {
                patchTagsToBackend(newTagsToUse)
            }
        )
    }

    private fun patchTagsToBackend(newTags: PersistentList<TagUI>) {
        viewModelScope.launch {
            val preparedTags = newTags.map { tag ->
                listOf(tag.name, tag.color.toHex())
            }

            patchData(
                payload = patchDataGenerator.getTagsPatchPayload(preparedTags),
                doOnSuccess = { _, _ ->
                    onTagsUpdateSuccess(newTags)
                },
                doOnError = { error ->
                    Timber.e(error)
                    onTagsUpdateError()
                    _state.update {
                        it.copy(error = getErrorMessage(error))
                    }
                }
            )
        }
    }

    private fun onNewTagsUpdate(newTagsToUse: PersistentList<TagUI>) {
        handleNewTagsUpdate(
            newTags = newTagsToUse,
            onUpdateTagsToBackend = {
                patchTagsToBackend(newTagsToUse)
            }
        )
    }

    private suspend fun patchData(
        payload: ImmutableMap<String, Any?>,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: (PatchedData, IssueTask) -> Unit,
        doOnError: (Throwable) -> Unit
    ) {
        val currentIssue = _state.value.currentIssue ?: return

        doOnPreExecute?.invoke()

        issueDetailsDataUseCase.patchData(
            issueId = currentIssue.id,
            payload = payload,
            version = currentIssue.version
        ).onSuccess { result ->
            val updatedIssue = currentIssue.copy(
                version = result.newVersion
            )
            _state.update {
                it.copy(
                    currentIssue = updatedIssue,
                    originalIssue = updatedIssue
                )
            }

            doOnSuccess(result, updatedIssue)
        }.onFailure { error ->
            Timber.e(error)
            doOnError(error)
        }
    }

    private fun onNewDescriptionUpdate(newDescription: String) {
        viewModelScope.launch {
            patchData(
                payload = patchDataGenerator.getDescriptionPatchPayload(newDescription),
                doOnPreExecute = {
                    _state.update {
                        it.copy(error = NativeText.Empty, isLoading = true)
                    }
                },
                doOnSuccess = { _, issue ->
                    val updatedIssue = issue.copy(description = newDescription)

                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            currentIssue = updatedIssue,
                            originalIssue = updatedIssue
                        )
                    }
                },
                doOnError = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = getErrorMessage(error)
                        )
                    }
                }
            )
        }
    }

    private fun onBadgeSheetItemClick(type: SelectableWorkItemBadgeState, item: StatusUI) {
        viewModelScope.launch {
            patchData(
                payload = getBadgePatchPayload(type, item),
                doOnPreExecute = {
                    _state.update { currentState ->
                        currentState.copy(
                            error = NativeText.Empty
                        )
                    }
                },
                doOnSuccess = { _: PatchedData, _: IssueTask ->
                    onBadgeSaveSuccess(type, item)
                },
                doOnError = { error ->
                    Timber.e(error)
                    onBadgeSaveError()
                    _state.update {
                        it.copy(
                            error = getErrorMessage(error)
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
                version = currentIssue.version,
                workItemId = currentIssue.id,
                doOnPreExecute = {
                    clearError()
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

    private fun onAttachmentRemove(attachment: Attachment) {
        viewModelScope.launch {
            handleRemoveAttachment(
                attachment = attachment,
                doOnPreExecute = {
                    clearError()
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

    private fun setIsRemoveAssigneeDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isRemoveAssigneeDialogVisible = isVisible)
        }
    }

    private fun setDropdownMenuExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isDropdownMenuExpanded = isExpanded)
        }
    }
}
