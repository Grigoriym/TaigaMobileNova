package com.grappim.taigamobile.feature.issues.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.patch.PatchableField
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.feature.issues.domain.IssueDetailsDataUseCase
import com.grappim.taigamobile.feature.issues.domain.IssueTask
import com.grappim.taigamobile.feature.workitem.ui.models.CustomFieldsUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.models.TagUIMapper
import com.grappim.taigamobile.feature.workitem.ui.models.WorkItemsGenerator
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgePriority
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeSeverity
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeStatus
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeType
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.DateItemState
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import com.grappim.taigamobile.utils.ui.toHex
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentMap
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
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
    private val dateTimeUtils: DateTimeUtils
) : ViewModel() {

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
            setIsDueDatePickerVisible = ::setDueDateDatePickerVisibility,
            setIsRemoveAssigneeDialogVisible = ::setIsRemoveAssigneeDialogVisible,
            setIsAddAssigneeDialogVisible = ::setIsAddAssigneeDialogVisible,
            setIsAddWatcherDialogVisible = ::setIsAddWatcherDialogVisible,
            setIsRemoveWatcherDialogVisible = ::setIsRemoveWatcherDialogVisible,
            onCustomFieldChange = ::onCustomFieldChange,
            onCustomFieldSave = ::onCustomFieldSave,
            onCustomFieldEditToggle = ::onCustomFieldEditToggle,
            setIsCustomFieldsWidgetExpanded = ::setIsCustomFieldsWidgetExpanded,
            setIsCommentsWidgetExpanded = ::setIsCommentsWidgetExpanded,
            onFieldChanged = ::updateLocalField,
            onSaveField = ::saveField,
            onFieldSetIsEditable = ::setFieldIsEditable,
            onWorkingItemBadgeClick = ::onWorkItemBadgeClick,
            onBadgeSheetDismiss = ::onBadgeSheetDismiss,
            onBadgeSheetItemClick = ::onBadgeSheetItemClick,
            onNewDescriptionUpdate = ::onNewDescriptionUpdate,
            onTagsUpdate = ::onNewTagsUpdate,
            onGoingToEditTags = ::onGoingToEditTags,
            onTagRemove = ::onTagRemove,
            setDueDate = ::setDueDate,
            onBlockToggle = ::onBlockToggle,
            setIsBlockDialogVisible = ::setIsBlockDialogVisible,
            setIsDeleteDialogVisible = ::setIsDeleteDialogVisible,
            onDelete = ::doOnDelete
        )
    )
    val state = _state.asStateFlow()

    private val _deleteTrigger = MutableSharedFlow<Boolean>()
    val deleteTrigger = _deleteTrigger.asSharedFlow()

    init {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true)
            }
            issueDetailsDataUseCase.getIssueData(
                taskId = taskId,
                ref = ref
            ).onSuccess { result ->
                _state.update {
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

                    it.copy(
                        workItemBadges = workItemBadges,
                        isLoading = false,
                        currentIssue = result.issueTask,
                        originalIssue = result.issueTask,
                        attachments = result.attachments,
                        comments = result.comments,
                        sprint = sprint,
                        tags = tags.await(),
                        dueDateText = getDueDateText(result.issueTask.dueDate),
                        creator = result.creator,
                        assignees = result.assignees,
                        watchers = result.watchers,
                        isAssignedToMe = result.isAssignedToMe,
                        isWatchedByMe = result.isWatchedByMe,
                        customFieldsVersion = result.customFields.version,
                        customFieldStateItems = customFieldsStateItems.await(),
                        filtersData = result.filtersData
                    )
                }
            }.onFailure { error ->
                Timber.e(error)
                _state.update {
                    it.copy(isLoading = false)
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
        val patchableData = mapOf(
            "is_blocked" to isBlocked,
            "blocked_note" to blockNote.orEmpty()
        ).toPersistentMap()
        patchData(
            payload = patchableData,
            doOnPreExecute = {
                _state.update {
                    it.copy(
                        isLoading = true
                    )
                }
            },
            doOnSuccess = { data: PatchedData, task: IssueTask ->
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

    private fun setIsBlockDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isBlockDialogVisible = isVisible)
        }
    }

    private fun setDueDate(newDate: Long?) {
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

        val patchableData = mapOf("due_date" to jsonLocalDate).toPersistentMap()

        patchData(
            payload = patchableData,
            doOnPreExecute = {
                _state.update {
                    it.copy(
                        error = NativeText.Empty,
                        isDueDateLoading = true
                    )
                }
            },
            doOnSuccess = { data: PatchedData, task: IssueTask ->
                val updatedIssue = task.copy(
                    dueDate = localDate,
                    dueDateStatus = data.dueDateStatus
                )

                _state.update { currentState ->
                    currentState.copy(
                        currentIssue = updatedIssue,
                        originalIssue = updatedIssue,
                        dueDateText = getDueDateText(localDate),
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

    private fun getDueDateText(dueDate: LocalDate?): NativeText = if (dueDate == null) {
        NativeText.Resource(id = RString.no_due_date)
    } else {
        NativeText.Simple(dateTimeUtils.formatLocalDateUiMedium(dueDate))
    }

    private fun onGoingToEditTags() {
        workItemEditShared.setTags(_state.value.tags)
    }

    private fun onTagRemove(tag: TagUI) {
        val currentTags = _state.value.tags
        val newTagsToUse = currentTags.removeAll { it.name == tag.name }

        val preparedTags = newTagsToUse.map { tag ->
            listOf(tag.name, tag.color.toHex())
        }

        val patchableData = mapOf("tags" to preparedTags).toPersistentMap()

        patchData(
            payload = patchableData,
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

    private fun patchData(
        payload: PersistentMap<String, Any?>,
        doOnPreExecute: () -> Unit,
        doOnSuccess: (PatchedData, IssueTask) -> Unit,
        doOnError: (Throwable) -> Unit
    ) {
        val currentIssue = _state.value.currentIssue ?: return
        viewModelScope.launch {
            doOnPreExecute()

            issueDetailsDataUseCase.patchData(
                issueId = currentIssue.id,
                payload = payload,
                version = currentIssue.version
            ).onSuccess { result ->
                val updatedIssue = currentIssue.copy(
                    version = result.version
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
    }

    private fun onNewTagsUpdate() {
        val newTagsToUse = workItemEditShared.currentTags.toPersistentList()
        workItemEditShared.clear()
        val preparedTags = newTagsToUse.map { tag ->
            listOf(tag.name, tag.color.toHex())
        }

        val patchableData = mapOf("tags" to preparedTags).toPersistentMap()

        patchData(
            payload = patchableData,
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

    private fun onNewDescriptionUpdate(newDescription: String) {
        val patchableData = mapOf("description" to newDescription).toPersistentMap()

        patchData(
            payload = patchableData,
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
                doOnSuccess = { data: PatchedData, updatedIssue: IssueTask ->
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

    private fun onWorkItemBadgeClick(type: SelectableWorkItemBadgeState) {
        _state.update {
            it.copy(activeBadge = type)
        }
    }

    private fun updateLocalField(field: PatchableField, newValue: Any) {
        _state.update { currentState ->
            val updatedIssue = when (field) {
                PatchableField.TITLE -> currentState.currentIssue?.copy(title = newValue as String)
                else -> currentState.currentIssue
            }
            currentState.copy(currentIssue = updatedIssue)
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

    private fun saveField(field: PatchableField) {
        val currentIssue = _state.value.currentIssue ?: return
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
                doOnSuccess = { data: PatchedData, task: IssueTask ->
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

    private fun setIsCustomFieldsWidgetExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isCustomFieldsWidgetExpanded = isExpanded)
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

    private fun setIsCommentsWidgetExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isCommentsWidgetExpanded = isExpanded)
        }
    }

    private fun onCustomFieldSave(item: CustomFieldItemState) {
        val currentIssue = _state.value.currentIssue ?: return
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
                    when (stateItem) {
                        is DateItemState -> {
                            if (stateItem.currentValue != null) {
                                dateTimeUtils.parseLocalDateToString(stateItem.currentValue!!)
                            } else {
                                null
                            }
                        }

                        else -> stateItem.getValueForPatching()
                    }
                }
            )

            val payload = mapOf("attributes_values" to patchedData).toPersistentMap()

            issueDetailsDataUseCase.patchCustomAttributes(
                issueId = currentIssue.id,
                version = currentState.customFieldsVersion,
                payload = payload
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

    private fun setIsRemoveWatcherDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isRemoveWatcherDialogVisible = isVisible)
        }
    }

    private fun setIsAddWatcherDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isAddWatcherDialogVisible = isVisible)
        }
    }

    private fun setIsAddAssigneeDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isAddAssigneeDialogVisible = isVisible)
        }
    }

    private fun setIsRemoveAssigneeDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isRemoveAssigneeDialogVisible = isVisible)
        }
    }

    private fun setDueDateDatePickerVisibility(isVisible: Boolean) {
        _state.update {
            it.copy(isDueDateDatePickerVisible = isVisible)
        }
    }

    private fun setDropdownMenuExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isDropdownMenuExpanded = isExpanded)
        }
    }
}
