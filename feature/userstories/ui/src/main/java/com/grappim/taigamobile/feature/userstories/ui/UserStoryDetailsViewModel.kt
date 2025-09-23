package com.grappim.taigamobile.feature.userstories.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.patch.PatchableField
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.domain.UserStoryDetailsDataUseCase
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.ui.models.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.models.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.screens.TeamMemberUpdate
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import com.grappim.taigamobile.feature.workitem.ui.utils.getDueDateText
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgePriority
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeSeverity
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeStatus
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeType
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
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet
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
    private val customFieldsUIMapper: CustomFieldsUIMapper
) : ViewModel() {

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
            onFieldSetIsEditable = ::setFieldIsEditable,
            onFieldChanged = ::updateLocalField,
            onSaveField = ::saveField,
            onWorkingItemBadgeClick = ::onWorkItemBadgeClick,
            setIsDueDatePickerVisible = ::setDueDateDatePickerVisibility,
            setDueDate = ::setDueDate,
            onTagRemove = ::onTagRemove,
            onGoingToEditTags = ::onGoingToEditTags,
            onBadgeSheetDismiss = ::onBadgeSheetDismiss,
            onBadgeSheetItemClick = ::onBadgeSheetItemClick,
            onGoingToEditAssignees = ::onGoingToEditAssignees,
            onGoingToEditWatchers = ::onGoingToEditWatchers,
            onAssignToMe = ::onAssignToMe,
            onRemoveMeFromWatchersClick = ::onRemoveMeFromWatchersClick,
            onAddMeToWatchersClick = ::onAddMeToWatchersClick,
            onRemoveWatcherClick = ::onRemoveWatcherClick,
            onRemoveAssigneeClick = ::onRemoveAssigneeClick,
            removeWatcher = ::removeWatcher,
            removeAssignee = ::removeAssignee,
            onCustomFieldChange = ::onCustomFieldChange,
            onCustomFieldSave = ::onCustomFieldSave,
            onCustomFieldEditToggle = ::onCustomFieldEditToggle,
            setIsCustomFieldsWidgetExpanded = ::setIsCustomFieldsWidgetExpanded,
            setIsCommentsWidgetExpanded = ::setIsCommentsWidgetExpanded,
            setIsRemoveAssigneeDialogVisible = ::setIsRemoveAssigneeDialogVisible,
            setIsRemoveWatcherDialogVisible = ::setIsRemoveWatcherDialogVisible,
            onBlockToggle = ::onBlockToggle,
            setIsBlockDialogVisible = ::setIsBlockDialogVisible,
            setIsDeleteDialogVisible = ::setIsDeleteDialogVisible,
            onDelete = ::doOnDelete,
            onAttachmentRemove = ::onAttachmentRemove,
            onAttachmentAdd = ::onAttachmentAdd,
            setAreAttachmentsExpanded = ::setAreAttachmentsExpanded,
            onCommentRemove = ::deleteComment,
            onCreateCommentClick = ::createComment
        )
    )
    val state = _state.asStateFlow()

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

    private fun createComment(newComment: String) {
        val currentUserStory = _state.value.currentUserStory ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isCommentsLoading = true,
                    error = NativeText.Empty
                )
            }

            userStoryDetailsDataUseCase.createComment(
                version = currentUserStory.version,
                id = currentUserStory.id,
                comment = newComment
            ).onSuccess { result ->
                val updatedUserStory = currentUserStory.copy(
                    version = result.newVersion
                )

                _state.update {
                    it.copy(
                        isCommentsLoading = false,
                        currentUserStory = updatedUserStory,
                        originalUserStory = updatedUserStory,
                        comments = result.comments.toPersistentList()
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        error = getErrorMessage(error),
                        isCommentsLoading = false
                    )
                }
            }
        }
    }

    private fun deleteComment(comment: Comment) {
        val currentIssue = _state.value.currentUserStory ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isCommentsLoading = true,
                    error = NativeText.Empty
                )
            }

            userStoryDetailsDataUseCase.deleteComment(
                id = currentIssue.id,
                commentId = comment.id
            ).onSuccess { result ->
                _state.update { currentState ->
                    currentState.copy(
                        isCommentsLoading = false,
                        comments = _state.value.comments.removeAll { it.id == comment.id }
                    )
                }
            }.onFailure { error ->
                Timber.e(error)
                _state.update {
                    it.copy(
                        isCommentsLoading = false,
                        error = getErrorMessage(error)
                    )
                }
            }
        }
    }

    private fun setAreAttachmentsExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(areAttachmentsExpanded = isExpanded)
        }
    }

    private fun onAttachmentAdd(uri: Uri?) {
        val currentUserStory = _state.value.currentUserStory ?: return
        viewModelScope.launch {
            if (uri == null) {
                _state.update {
                    it.copy(
                        isAttachmentsLoading = true,
                        error = NativeText.Resource(RString.common_error_message)
                    )
                }
                return@launch
            }

            val attachmentInfoToSend = fileUriManager.retrieveAttachmentInfo(uri)

            userStoryDetailsDataUseCase.addAttachment(
                id = currentUserStory.id,
                fileName = attachmentInfoToSend.name,
                fileByteArray = attachmentInfoToSend.fileBytes.toByteArray()
            ).onSuccess { result ->
                val currentAttachments = _state.value.attachments
                _state.update {
                    it.copy(
                        isAttachmentsLoading = false,
                        attachments = currentAttachments.add(result)
                    )
                }
            }.onFailure { error ->
                Timber.e(error)
                _state.update {
                    it.copy(
                        isAttachmentsLoading = false,
                        error = getErrorMessage(error)
                    )
                }
            }
        }
    }

    private fun onAttachmentRemove(attachment: Attachment) {
        _state.update {
            it.copy(isAttachmentsLoading = true)
        }
        viewModelScope.launch {
            userStoryDetailsDataUseCase.deleteAttachment(attachment)
                .onSuccess { result ->
                    val currentAttachments = _state.value.attachments
                    _state.update {
                        it.copy(
                            isAttachmentsLoading = false,
                            attachments = currentAttachments.remove(attachment)
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            isAttachmentsLoading = false,
                            error = getErrorMessage(error)
                        )
                    }
                }
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
                doOnSuccess = { data: PatchedData, userStory: UserStory ->
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

    private fun setIsRemoveWatcherDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isRemoveWatcherDialogVisible = isVisible)
        }
    }

    private fun setIsCommentsWidgetExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isCommentsWidgetExpanded = isExpanded)
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

    private fun removeWatcher() {
        viewModelScope.launch {
            val newWatchers = _state.value.watchers.map { it.actualId }
                .filterNot { it == _state.value.watcherIdToRemove }

            patchData(
                payload = patchDataGenerator.getWatchersPatchPayload(newWatchers),
                doOnPreExecute = {
                    _state.update {
                        it.copy(
                            isWatchersLoading = true,
                            error = NativeText.Empty
                        )
                    }
                },
                doOnSuccess = { data: PatchedData, userStory: UserStory ->
                    val isWatchedByMe = session.userId in newWatchers
                    val watchersToSave = _state.value.watchers.removeAll {
                        it.actualId == _state.value.watcherIdToRemove
                    }

                    _state.update {
                        it.copy(
                            isWatchersLoading = false,
                            error = NativeText.Empty,
                            watcherIdToRemove = null,
                            isWatchedByMe = isWatchedByMe,
                            watchers = watchersToSave
                        )
                    }
                },
                doOnError = { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            isWatchersLoading = false,
                            error = getErrorMessage(error)
                        )
                    }
                }
            )
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

    private fun onRemoveMeFromWatchersClick() {
        val currentUserStory = _state.value.currentUserStory ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            userStoryDetailsDataUseCase.removeMeFromWatchers(userStoryId = currentUserStory.id)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isWatchedByMe = result.isWatchedByMe,
                            watchers = result.watchers.toPersistentList(),
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            error = getErrorMessage(error),
                            isLoading = false
                        )
                    }
                }
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

    private fun onRemoveWatcherClick(watcherId: Long) {
        _state.update {
            it.copy(
                watcherIdToRemove = watcherId,
                isRemoveWatcherDialogVisible = true
            )
        }
    }

    private fun onAddMeToWatchersClick() {
        val currentUserStory = _state.value.currentUserStory ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            userStoryDetailsDataUseCase.addMeToWatchers(userStoryId = currentUserStory.id)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isWatchedByMe = result.isWatchedByMe,
                            watchers = result.watchers.toPersistentList(),
                            isLoading = false
                        )
                    }
                }.onFailure { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            error = getErrorMessage(error),
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun onGoingToEditAssignees() {
        val assigneesIds = _state.value.assignees.mapNotNull { it.id }.toImmutableList()
        workItemEditShared.setCurrentAssignees(assigneesIds)
    }

    private fun onGoingToEditWatchers() {
        val watchersIds = _state.value.watchers.mapNotNull { it.id }
            .toPersistentList()
        workItemEditShared.setCurrentWatchers(watchersIds)
    }

    private fun onBadgeSheetDismiss() {
        _state.update {
            it.copy(activeBadge = null)
        }
    }

    private fun onBadgeSheetItemClick(type: SelectableWorkItemBadgeState, item: StatusUI) {
        viewModelScope.launch {
            val patchableData = when (type) {
                is SelectableWorkItemBadgeStatus -> {
                    mapOf("status" to item.id)
                }

                is SelectableWorkItemBadgeType -> {
                    mapOf("type" to item.id)
                }

                is SelectableWorkItemBadgeSeverity -> {
                    mapOf("severity" to item.id)
                }

                is SelectableWorkItemBadgePriority -> {
                    mapOf("priority" to item.id)
                }
            }.toPersistentMap()

            patchData(
                payload = patchableData,
                doOnPreExecute = {
                    _state.update { currentState ->
                        currentState.copy(
                            updatingBadges = currentState.updatingBadges.add(type),
                            error = NativeText.Empty
                        )
                    }
                },
                doOnSuccess = { data: PatchedData, updatedUserStory: UserStory ->
                    _state.update { currentState ->
                        val updatedWorkItemBadges = currentState.workItemBadges.map { badge ->
                            if (badge == type) {
                                when (badge) {
                                    is SelectableWorkItemBadgeStatus -> {
                                        badge.copy(currentValue = item)
                                    }

                                    is SelectableWorkItemBadgeType -> {
                                        badge.copy(currentValue = item)
                                    }

                                    is SelectableWorkItemBadgeSeverity -> {
                                        badge.copy(currentValue = item)
                                    }

                                    is SelectableWorkItemBadgePriority -> {
                                        badge.copy(currentValue = item)
                                    }
                                }
                            } else {
                                badge
                            }
                        }

                        currentState.copy(
                            activeBadge = null,
                            workItemBadges = updatedWorkItemBadges.toPersistentSet(),
                            updatingBadges = currentState.updatingBadges.remove(type)
                        )
                    }
                },
                doOnError = { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            activeBadge = null,
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

    private fun onWatchersUpdated(newWatchers: ImmutableList<Long>) {
        val currentUserStory = _state.value.currentUserStory ?: return
        viewModelScope.launch {
            _state.update {
                it.copy(error = NativeText.Empty, isWatchersLoading = true)
            }

            userStoryDetailsDataUseCase.updateWatchersData(
                version = currentUserStory.version,
                userStoryId = currentUserStory.id,
                newList = newWatchers
            ).onSuccess { result ->
                val updatedUserStory = currentUserStory.copy(
                    version = result.version
                )
                _state.update {
                    it.copy(
                        currentUserStory = updatedUserStory,
                        originalUserStory = updatedUserStory,
                        isWatchersLoading = false,
                        isWatchedByMe = result.isWatchedByMe,
                        watchers = result.watchers.toPersistentList()
                    )
                }
            }.onFailure { error ->
                Timber.e(error)
                _state.update {
                    it.copy(
                        error = getErrorMessage(error),
                        isWatchersLoading = false
                    )
                }
            }
        }
    }

    private fun onNewTagsUpdate(newTagsToUse: PersistentList<TagUI>) {
        viewModelScope.launch {
            val preparedTags = newTagsToUse.map { tag ->
                listOf(tag.name, tag.color.toHex())
            }

            patchData(
                payload = patchDataGenerator.getTagsPatchPayload(preparedTags),
                doOnPreExecute = {
                    _state.update {
                        it.copy(
                            error = NativeText.Empty,
                            areTagsLoading = true
                        )
                    }
                },
                doOnSuccess = { _, _ ->
                    _state.update { currentState ->
                        currentState.copy(
                            tags = newTagsToUse,
                            areTagsLoading = false
                        )
                    }
                },
                doOnError = { error ->
                    _state.update {
                        it.copy(
                            error = getErrorMessage(error),
                            areTagsLoading = false
                        )
                    }
                }
            )
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
                            filtersData = result.filtersData,
                            sprint = result.sprint
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
                            workItemBadges = workItemBadges.await(),
                            isLoading = false,
                            currentUserStory = result.userStory,
                            originalUserStory = result.userStory,
                            attachments = result.attachments.toPersistentList(),
                            comments = result.comments.toPersistentList(),
                            sprint = result.sprint,
                            tags = tags.await(),
                            dueDateText = dueDateText,
                            creator = result.creator,
                            assignees = result.assignees.toPersistentList(),
                            watchers = result.watchers.toPersistentList(),
                            isAssignedToMe = result.isAssignedToMe,
                            isWatchedByMe = result.isWatchedByMe,
                            customFieldsVersion = result.customFields.version,
                            customFieldStateItems = customFieldsStateItems.await(),
                            filtersData = result.filtersData,
                            initialLoadError = NativeText.Empty
                        )
                    }
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

    private fun setFieldIsEditable(field: PatchableField, isEditable: Boolean) {
        _state.update { currentState ->
            if (isEditable) {
                currentState.copy(editableFields = currentState.editableFields.add(field))
            } else {
                currentState.copy(editableFields = currentState.editableFields.remove(field))
            }
        }
    }

    private fun updateLocalField(field: PatchableField, newValue: Any) {
        _state.update { currentState ->
            val updatedIssue = when (field) {
                PatchableField.TITLE -> currentState.currentUserStory?.copy(
                    title = newValue as String
                )

                else -> currentState.currentUserStory
            }
            currentState.copy(currentUserStory = updatedIssue)
        }
    }

    private fun saveField(field: PatchableField) {
        val currentIssue = _state.value.currentUserStory ?: return
        viewModelScope.launch {
            val patchableData = when (field) {
                PatchableField.TITLE -> mapOf("subject" to currentIssue.title)
            }.toPersistentMap()

            patchData(
                payload = patchableData,
                doOnPreExecute = {
                    _state.update {
                        it.copy(
                            savingFields = it.savingFields.add(field),
                            patchableFieldError = NativeText.Empty
                        )
                    }
                },
                doOnSuccess = { data: PatchedData, userStory: UserStory ->
                    setFieldIsEditable(field, false)
                    _state.update {
                        it.copy(
                            savingFields = it.savingFields.remove(field)
                        )
                    }
                },
                doOnError = { error ->
                    Timber.e(error)
                    _state.update {
                        it.copy(
                            patchableFieldError = getErrorMessage(error),
                            savingFields = it.savingFields.remove(field)
                        )
                    }
                }
            )
        }
    }

    private suspend fun patchData(
        payload: ImmutableMap<String, Any?>,
        doOnPreExecute: () -> Unit,
        doOnSuccess: (PatchedData, UserStory) -> Unit,
        doOnError: (Throwable) -> Unit
    ) {
        val currentUserStory = _state.value.currentUserStory ?: return

        doOnPreExecute()

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

    private fun onWorkItemBadgeClick(type: SelectableWorkItemBadgeState) {
        _state.update {
            it.copy(activeBadge = type)
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

    private fun onGoingToEditTags() {
        workItemEditShared.setTags(_state.value.tags)
    }

    private fun onTagRemove(tag: TagUI) {
        viewModelScope.launch {
            val currentTags = _state.value.tags
            val newTagsToUse = currentTags.removeAll { it.name == tag.name }

            val preparedTags = newTagsToUse.map { tag ->
                listOf(tag.name, tag.color.toHex())
            }

            patchData(
                payload = patchDataGenerator.getTagsPatchPayload(preparedTags),
                doOnPreExecute = {
                    _state.update {
                        it.copy(
                            error = NativeText.Empty,
                            areTagsLoading = true
                        )
                    }
                },
                doOnSuccess = { _, _ ->
                    _state.update { currentState ->
                        currentState.copy(
                            tags = newTagsToUse,
                            areTagsLoading = false
                        )
                    }
                },
                doOnError = { error ->
                    _state.update {
                        it.copy(
                            error = getErrorMessage(error),
                            areTagsLoading = false
                        )
                    }
                }
            )
        }
    }
}
