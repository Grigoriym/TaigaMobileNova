package com.grappim.taigamobile.feature.issues.ui.details

import android.net.Uri
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.core.domain.patch.PatchableField
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.issues.domain.IssueTask
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

data class IssueDetailsState(
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty,

    val currentIssue: IssueTask? = null,
    val originalIssue: IssueTask? = null,

    val savingFields: PersistentSet<PatchableField> = persistentSetOf(),
    val editableFields: PersistentSet<PatchableField> = persistentSetOf(),

    val updatingBadges: PersistentSet<SelectableWorkItemBadgeState> = persistentSetOf(),
    val activeBadge: SelectableWorkItemBadgeState? = null,
    val workItemBadges: ImmutableSet<SelectableWorkItemBadgeState> = persistentSetOf(),
    val onWorkingItemBadgeClick: (SelectableWorkItemBadgeState) -> Unit = {},
    val onBadgeSheetDismiss: () -> Unit = {},
    val onBadgeSheetItemClick: (SelectableWorkItemBadgeState, StatusUI) -> Unit,

    val onFieldSetIsEditable: (PatchableField, Boolean) -> Unit,
    val onFieldChanged: (field: PatchableField, newValue: Any) -> Unit,
    val onSaveField: (field: PatchableField) -> Unit,
    val patchableFieldError: NativeText = NativeText.Empty,

    val sprint: Sprint? = null,

    val filtersData: FiltersData? = null,

    val comments: PersistentList<Comment> = persistentListOf(),
    val isCommentsLoading: Boolean = false,
    val onCommentRemove: (Comment) -> Unit = {},
    val isCommentsWidgetExpanded: Boolean = false,
    val setIsCommentsWidgetExpanded: (Boolean) -> Unit = {},
    val onCreateCommentClick: (String) -> Unit = {},

    val isCustomFieldsLoading: Boolean = false,
    val customFieldStateItems: ImmutableList<CustomFieldItemState> = persistentListOf(),
    val onCustomFieldChange: (CustomFieldItemState) -> Unit = {},
    val onCustomFieldSave: (CustomFieldItemState) -> Unit = {},
    val customFieldsVersion: Long = 0,
    val isCustomFieldsWidgetExpanded: Boolean = false,
    val setIsCustomFieldsWidgetExpanded: (Boolean) -> Unit = {},
    val onCustomFieldEditToggle: (CustomFieldItemState) -> Unit = {},
    val editingItemIds: ImmutableSet<Long> = persistentSetOf(),

    val attachments: PersistentList<Attachment> = persistentListOf(),
    val isAttachmentsLoading: Boolean = false,
    val onAttachmentAdd: (uri: Uri?) -> Unit = { _ -> },
    val onAttachmentRemove: (Attachment) -> Unit = {},
    val areAttachmentsExpanded: Boolean = false,
    val setAreAttachmentsExpanded: (Boolean) -> Unit = {},

    val tags: PersistentList<TagUI> = persistentListOf(),
    val areTagsLoading: Boolean = false,

    val toolbarTitle: NativeText,
    val isDropdownMenuExpanded: Boolean = false,
    val setDropdownMenuExpanded: (Boolean) -> Unit,

    val isDueDateDatePickerVisible: Boolean = false,
    val setIsDueDatePickerVisible: (Boolean) -> Unit = {},
    val setDueDate: (Long?) -> Unit = {},
    val isDueDateLoading: Boolean = false,
    val dueDateText: NativeText = NativeText.Empty,

    val creator: User? = null,

    val watchers: PersistentList<User> = persistentListOf(),
    val assignees: PersistentList<User> = persistentListOf(),

    val isAssigneesLoading: Boolean = false,
    val isRemoveAssigneeDialogVisible: Boolean = false,
    val setIsRemoveAssigneeDialogVisible: (Boolean) -> Unit = {},
    val onRemoveAssigneeClick: () -> Unit = {},

    val isWatchersLoading: Boolean = false,
    val isRemoveWatcherDialogVisible: Boolean = false,
    val watcherIdToRemove: Long? = null,
    val removeWatcher: () -> Unit = {},
    val setIsRemoveWatcherDialogVisible: (Boolean) -> Unit = {},
    val onRemoveWatcherClick: (Long) -> Unit = {},
    val onGoingToEditWatchers: () -> Unit = {},
    val onRemoveMeFromWatchersClick: () -> Unit = {},
    val onAddMeToWatchersClick: () -> Unit = {},

    val isAssignedToMe: Boolean = false,
    val removeAssignee: () -> Unit = {},
    val isWatchedByMe: Boolean = false,
    val onAssignToMe: () -> Unit = {},
    val onUnassign: () -> Unit = {},
    val onGoingToEditAssignee: () -> Unit = {},
    val onUsersUpdate: () -> Unit,

    val onNewDescriptionUpdate: (String) -> Unit,
    val onTagsUpdate: () -> Unit,
    val onTagRemove: (TagUI) -> Unit,
    val onGoingToEditTags: () -> Unit,

    val onBlockToggle: (isBlocked: Boolean, blockNote: String?) -> Unit,
    val isBlockDialogVisible: Boolean = false,
    val setIsBlockDialogVisible: (Boolean) -> Unit = {},

    val setIsDeleteDialogVisible: (Boolean) -> Unit = {},
    val isDeleteDialogVisible: Boolean = false,
    val onDelete: () -> Unit = {}
)
