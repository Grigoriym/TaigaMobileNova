package com.grappim.taigamobile.feature.userstories.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.history.domain.HistoryRepository
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoryDetailsDataUseCase
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.badge.WorkItemBadgeDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.badge.WorkItemBadgeDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.delegates.comments.WorkItemCommentsDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.comments.WorkItemCommentsDelegateImpl
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
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.DateItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.NumberItemState
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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
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
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class UserStoryDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userStoryDetailsDataUseCase: UserStoryDetailsDataUseCase,
    private val workItemsGenerator: WorkItemsGenerator,
    private val workItemEditShared: WorkItemEditShared,
    private val patchDataGenerator: PatchDataGenerator,
    private val statusUIMapper: StatusUIMapper,
    private val tagUIMapper: TagUIMapper,
    private val dateTimeUtils: DateTimeUtils,
    private val session: Session,
    private val fileUriManager: FileUriManager,
    private val customFieldsUIMapper: CustomFieldsUIMapper,
    private val historyRepository: HistoryRepository,
    private val workItemRepository: WorkItemRepository,
    private val taigaStorage: TaigaStorage,
    private val usersRepository: UsersRepository
) : ViewModel(),
    WorkItemTitleDelegate by WorkItemTitleDelegateImpl(),
    WorkItemBadgeDelegate by WorkItemBadgeDelegateImpl(patchDataGenerator),
    WorkItemTagsDelegate by WorkItemTagsDelegateImpl(workItemEditShared),
    WorkItemCommentsDelegate by WorkItemCommentsDelegateImpl(
        historyRepository = historyRepository,
        commonTaskType = CommonTaskType.UserStory,
        workItemRepository = workItemRepository
    ),
    WorkItemAttachmentsDelegate by WorkItemAttachmentsDelegateImpl(
        workItemRepository = workItemRepository,
        commonTaskType = CommonTaskType.UserStory,
        fileUriManager = fileUriManager,
        taigaStorage = taigaStorage
    ),
    WorkItemWatchersDelegate by WorkItemWatchersDelegateImpl(
        commonTaskType = CommonTaskType.UserStory,
        workItemRepository = workItemRepository,
        usersRepository = usersRepository,
        patchDataGenerator = patchDataGenerator,
        session = session,
        workItemEditShared = workItemEditShared
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
            setIsDueDatePickerVisible = ::setDueDateDatePickerVisibility,
            setDueDate = ::setDueDate,
            onTagRemove = ::onTagRemove,
            onGoingToEditAssignees = ::onGoingToEditAssignees,
            onAssignToMe = ::onAssignToMe,
            onRemoveMeFromWatchersClick = ::onRemoveMeFromWatchersClick,
            onAddMeToWatchersClick = ::onAddMeToWatchersClick,
            onRemoveAssigneeClick = ::onRemoveAssigneeClick,
            removeWatcher = ::removeWatcher,
            removeAssignee = ::removeAssignee,
            onCustomFieldChange = ::onCustomFieldChange,
            onCustomFieldSave = ::onCustomFieldSave,
            onCustomFieldEditToggle = ::onCustomFieldEditToggle,
            setIsCustomFieldsWidgetExpanded = ::setIsCustomFieldsWidgetExpanded,
            setIsRemoveAssigneeDialogVisible = ::setIsRemoveAssigneeDialogVisible,
            onBlockToggle = ::onBlockToggle,
            setIsBlockDialogVisible = ::setIsBlockDialogVisible,
            setIsDeleteDialogVisible = ::setIsDeleteDialogVisible,
            onDelete = ::doOnDelete,
            onAttachmentRemove = ::onAttachmentRemove,
            onAttachmentAdd = ::onAttachmentAdd,
            onCommentRemove = ::deleteComment,
            onCreateCommentClick = ::createComment,
            onTitleSave = ::handleTitleSave,
            onBadgeSave = ::handleBadgeSave
        )
    )
    val state = _state.asStateFlow()

    private val currentUserStory: UserStory
        get() = requireNotNull(_state.value.currentUserStory)

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

        loadUserStory()
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
                    val dueDateText = dateTimeUtils.getDueDateText(
                        dueDate = result.userStory.dueDate
                    )
                    val customFieldsStateItems = async {
                        customFieldsUIMapper.toUI(result.customFields)
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            currentUserStory = result.userStory,
                            originalUserStory = result.userStory,
                            sprint = result.sprint,
                            dueDateText = dueDateText,
                            creator = result.creator,
                            assignees = result.assignees.toPersistentList(),
                            isAssignedToMe = result.isAssignedToMe,
                            customFieldsVersion = result.customFields.version,
                            customFieldStateItems = customFieldsStateItems.await(),
                            filtersData = result.filtersData,
                            initialLoadError = NativeText.Empty
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

    private fun emptyError() {
        _state.update {
            it.copy(
                error = NativeText.Empty
            )
        }
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
                doOnSuccess = { _: PatchedData, _: UserStory ->
                    onTitleSaveSuccess()
                },
                doOnError = { error ->
                    Timber.e(error)
                    onTitleError(getErrorMessage(error))
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
                    _state.update {
                        it.copy(error = NativeText.Empty)
                    }
                },
                doOnSuccess = { result ->
                    val updatedUserStory = currentUserStory.copy(
                        version = result.newVersion
                    )

                    _state.update {
                        it.copy(
                            currentUserStory = updatedUserStory,
                            originalUserStory = updatedUserStory
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
                id = currentUserStory.id,
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
                    _state.update {
                        it.copy(
                            error = NativeText.Empty
                        )
                    }
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
                    _state.update {
                        it.copy(
                            error = NativeText.Empty
                        )
                    }
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

            userStoryDetailsDataUseCase.deleteIssue(
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
                doOnSuccess = { _: PatchedData, userStory: UserStory ->
                    val updatedUserStory = userStory.copy(blockedNote = blockNote)
                    _state.update {
                        it.copy(
                            currentUserStory = updatedUserStory,
                            originalUserStory = updatedUserStory,
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

    private fun setIsRemoveAssigneeDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(
                isRemoveAssigneeDialogVisible = isVisible,
                assigneeToRemove = null
            )
        }
    }

    private fun onCustomFieldEditToggle(item: CustomFieldItemState) {
        _state.update { currentState ->
            val currentIds = _state.value.editingItemIds
            val newIds = if (item.id in currentIds) {
                currentIds - item.id
            } else {
                currentIds + item.id
            }.toImmutableSet()
            currentState.copy(editingItemIds = newIds)
        }
    }

    private fun setIsCustomFieldsWidgetExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isCustomFieldsWidgetExpanded = isExpanded)
        }
    }

    private fun onCustomFieldChange(updatedItem: CustomFieldItemState) {
        _state.update { currentState ->
            val updatedList = currentState.customFieldStateItems.map { item ->
                if (item.id == updatedItem.id) {
                    updatedItem
                } else {
                    item
                }
            }.toImmutableList()
            currentState.copy(customFieldStateItems = updatedList)
        }
    }

    private fun onCustomFieldSave(item: CustomFieldItemState) {
        val currentUserStory = _state.value.currentUserStory ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    error = NativeText.Empty,
                    isCustomFieldsLoading = true
                )
            }

            val currentState = _state.value

            val patchedData = currentState.customFieldStateItems.associateBy(
                keySelector = { stateItem ->
                    stateItem.id.toString()
                },
                valueTransform = { stateItem ->
                    if (stateItem.id == item.id) {
                        getCustomFieldValue(stateItem, true)
                    } else if (stateItem.isModified) {
                        getCustomFieldValue(stateItem, false)
                    } else {
                        getCustomFieldValue(stateItem, true)
                    }
                }
            )

            userStoryDetailsDataUseCase.patchCustomAttributes(
                userStoryId = currentUserStory.id,
                version = currentState.customFieldsVersion,
                payload = patchDataGenerator.getAttributesPatchPayload(patchedData)
            ).onSuccess { result ->
                onCustomFieldEditToggle(item)
                onCustomFieldSaved(item.getSavedItem())
                _state.update {
                    it.copy(
                        error = NativeText.Empty,
                        isCustomFieldsLoading = false,
                        customFieldsVersion = result.version
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        error = getErrorMessage(error),
                        isCustomFieldsLoading = false
                    )
                }
            }
        }
    }

    private fun onCustomFieldSaved(newItem: CustomFieldItemState) {
        _state.update { currentState ->
            val updatedList = currentState.customFieldStateItems.map { item ->
                if (item.id == newItem.id) {
                    newItem
                } else {
                    item
                }
            }.toImmutableList()
            currentState.copy(customFieldStateItems = updatedList)
        }
    }

    private fun getCustomFieldValue(
        stateItem: CustomFieldItemState,
        takeCurrentValue: Boolean
    ): Any? {
        val valueToUse = if (takeCurrentValue) {
            stateItem.currentValue
        } else {
            stateItem.originalValue
        }
        return when (stateItem) {
            is DateItemState -> {
                if (valueToUse != null) {
                    dateTimeUtils.parseLocalDateToString(valueToUse as LocalDate)
                } else {
                    null
                }
            }

            is NumberItemState -> {
                valueToUse?.toString()?.toLongOrNull()
            }

            else -> valueToUse
        }
    }

    private fun removeAssignee() {
        viewModelScope.launch {
            _state.update {
                it.copy(isRemoveAssigneeDialogVisible = false)
            }

            val assigneeToRemove = _state.value.assigneeToRemove?.actualId ?: return@launch
            val newAssignees = _state.value.assignees.removeAll {
                it.actualId == assigneeToRemove
            }.map { it.actualId }.toImmutableList()

            updateAssignees(newAssignees)
        }
    }

    private fun onAssignToMe() {
        viewModelScope.launch {
            val assigneesList = _state.value.assignees.mapNotNull { it.id }
                .toPersistentList()
                .add(session.userId)

            updateAssignees(assigneesList.toImmutableList())
        }
    }

    private suspend fun updateAssignees(newAssignees: ImmutableList<Long>) {
        val currentUserStory = _state.value.currentUserStory ?: return

        _state.update {
            it.copy(isAssigneesLoading = true)
        }

        userStoryDetailsDataUseCase.updateAssigneesData(
            version = currentUserStory.version,
            userStoryId = currentUserStory.id,
            assigneesList = newAssignees.toImmutableList()
        ).onSuccess { result ->
            val updatedUserStory = currentUserStory.copy(
                version = result.newVersion
            )
            _state.update {
                it.copy(
                    isAssigneesLoading = false,
                    currentUserStory = updatedUserStory,
                    originalUserStory = updatedUserStory,
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

    private fun onAssigneeUpdated(newAssignees: ImmutableList<Long>) {
        viewModelScope.launch {
            updateAssignees(newAssignees)
        }
    }

    private fun onRemoveAssigneeClick(user: User) {
        _state.update {
            it.copy(
                isRemoveAssigneeDialogVisible = true,
                assigneeToRemove = user
            )
        }
    }

    private fun updateVersion(newVersion: Long) {
        val updatedUserStory = currentUserStory.copy(
            version = newVersion
        )
        _state.update {
            it.copy(
                currentUserStory = updatedUserStory,
                originalUserStory = updatedUserStory,
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
                    emptyError()
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
                workItemId = currentUserStory.id,
                doOnPreExecute = {
                    emptyError()
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
                workItemId = currentUserStory.id,
                doOnPreExecute = {
                    emptyError()
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
                version = currentUserStory.version,
                workItemId = currentUserStory.id,
                newWatchers = newWatchers,
                doOnPreExecute = {
                    emptyError()
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

    private fun onGoingToEditAssignees() {
        val assigneesIds = _state.value.assignees.mapNotNull { it.id }.toImmutableList()
        workItemEditShared.setCurrentAssignees(assigneesIds)
    }

    private fun handleBadgeSave(type: SelectableWorkItemBadgeState, item: StatusUI) {
        onBadgeSave(
            type = type,
            onSaveBadgeToBackend = {
                onBadgeSheetItemClick(type, item)
            }
        )
    }

    private fun onBadgeSheetItemClick(type: SelectableWorkItemBadgeState, item: StatusUI) {
        viewModelScope.launch {
            patchData(
                payload = getBadgePatchPayload(type, item),
                doOnPreExecute = {
                    _state.update {
                        it.copy(
                            error = NativeText.Empty
                        )
                    }
                },
                doOnSuccess = { _: PatchedData, _: UserStory ->
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

    private fun handleTeamMemberUpdate(updateState: TeamMemberUpdate) {
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
                    val updatedUserStory = issue.copy(description = newDescription)

                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            currentUserStory = updatedUserStory,
                            originalUserStory = updatedUserStory
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

    private fun retryLoadUserStory() {
        loadUserStory()
    }

    private fun setDropdownMenuExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isDropdownMenuExpanded = isExpanded)
        }
    }

    private suspend fun patchData(
        payload: ImmutableMap<String, Any?>,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: (PatchedData, UserStory) -> Unit,
        doOnError: (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()

        userStoryDetailsDataUseCase.patchData(
            userStoryId = currentUserStory.id,
            payload = payload,
            version = currentUserStory.version
        ).onSuccess { result ->
            val updatedUserStory = currentUserStory.copy(
                version = result.newVersion
            )
            _state.update {
                it.copy(
                    currentUserStory = updatedUserStory,
                    originalUserStory = updatedUserStory
                )
            }

            doOnSuccess(result, updatedUserStory)
        }.onFailure { error ->
            Timber.e(error)
            doOnError(error)
        }
    }

    private fun setDueDate(newDate: Long?) {
        viewModelScope.launch {
            val localDate = if (newDate != null) {
                dateTimeUtils.fromMillisToLocalDate(newDate)
            } else {
                null
            }
            val jsonLocalDate = if (localDate != null) {
                dateTimeUtils.parseLocalDateToString(localDate)
            } else {
                null
            }

            patchData(
                payload = patchDataGenerator.getDueDatePatchPayload(jsonLocalDate),
                doOnPreExecute = {
                    _state.update {
                        it.copy(
                            error = NativeText.Empty,
                            isDueDateLoading = true
                        )
                    }
                },
                doOnSuccess = { data: PatchedData, userStory: UserStory ->
                    val updatedUserStory = userStory.copy(
                        dueDate = localDate,
                        dueDateStatus = data.dueDateStatus
                    )

                    _state.update { currentState ->
                        currentState.copy(
                            currentUserStory = updatedUserStory,
                            originalUserStory = updatedUserStory,
                            dueDateText = dateTimeUtils.getDueDateText(localDate),
                            isDueDateLoading = false
                        )
                    }
                },
                doOnError = { error ->
                    _state.update {
                        it.copy(
                            error = getErrorMessage(error),
                            isDueDateLoading = false
                        )
                    }
                }
            )
        }
    }

    private fun setDueDateDatePickerVisibility(isVisible: Boolean) {
        _state.update {
            it.copy(isDueDateDatePickerVisible = isVisible)
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
}
