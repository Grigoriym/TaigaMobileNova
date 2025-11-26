package com.grappim.taigamobile.feature.issues.ui.details

import android.net.Uri
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.Comment
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.domain.User
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

data class IssueDetailsState(
    val isLoading: Boolean = false,
    /**
     * We use it on first load, so that we could reload whole issue
     */
    val initialLoadError: NativeText = NativeText.Empty,
    val retryLoadIssue: () -> Unit = {},

    val error: NativeText = NativeText.Empty,

    val currentIssue: IssueTask? = null,
    val originalIssue: IssueTask? = null,

    val onTitleSave: () -> Unit = {},
    val onBadgeSave: (SelectableWorkItemBadgeState, StatusUI) -> Unit = { _, _ -> },

    val sprint: Sprint? = null,

    val filtersData: FiltersData? = null,

    val onCommentRemove: (Comment) -> Unit = {},
    val onCreateCommentClick: (String) -> Unit = {},

    val onCustomFieldSave: (CustomFieldItemState) -> Unit = {},

    val onAttachmentAdd: (uri: Uri?) -> Unit = { _ -> },
    val onAttachmentRemove: (Attachment) -> Unit = {},

    val toolbarTitle: NativeText,
    val isDropdownMenuExpanded: Boolean = false,
    val setDropdownMenuExpanded: (Boolean) -> Unit,

    val setDueDate: (Long?) -> Unit = {},

    val creator: User? = null,

    val onUnassign: () -> Unit = {},
    val onAssignToMe: () -> Unit = {},
    val removeAssignee: () -> Unit = {},

    val removeWatcher: () -> Unit = {},
    val onRemoveMeFromWatchersClick: () -> Unit = {},
    val onAddMeToWatchersClick: () -> Unit = {},

    val onTagRemove: (TagUI) -> Unit,

    val onBlockToggle: (isBlocked: Boolean, blockNote: String?) -> Unit,

    val setIsDeleteDialogVisible: (Boolean) -> Unit = {},
    val isDeleteDialogVisible: Boolean = false,
    val onDelete: () -> Unit = {},
    val customFieldsVersion: Long = 0,
)
