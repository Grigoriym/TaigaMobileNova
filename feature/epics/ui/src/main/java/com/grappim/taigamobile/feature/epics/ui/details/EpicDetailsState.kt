package com.grappim.taigamobile.feature.epics.ui.details

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.feature.epics.domain.Epic
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.domain.Comment
import com.grappim.taigamobile.feature.workitem.ui.models.StatusUI
import com.grappim.taigamobile.feature.workitem.ui.models.TagUI
import com.grappim.taigamobile.feature.workitem.ui.models.WorkItemUI
import com.grappim.taigamobile.feature.workitem.ui.widgets.badge.SelectableWorkItemBadgeState
import com.grappim.taigamobile.feature.workitem.ui.widgets.customfields.CustomFieldItemState
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class EpicDetailsState(
    val currentEpic: Epic? = null,
    val originalEpic: Epic? = null,

    val creator: User? = null,

    val filtersData: FiltersData? = null,

    val onTitleSave: () -> Unit = {},

    val isLoading: Boolean = false,
    val initialLoadError: NativeText = NativeText.Empty,
    val retryLoadEpic: () -> Unit = {},
    val error: NativeText = NativeText.Empty,
    val toolbarTitle: NativeText,
    val isDropdownMenuExpanded: Boolean = false,
    val setDropdownMenuExpanded: (Boolean) -> Unit,

    val onBadgeSave: (SelectableWorkItemBadgeState, StatusUI) -> Unit = { _, _ -> },

    val setIsDeleteDialogVisible: (Boolean) -> Unit = {},
    val isDeleteDialogVisible: Boolean = false,
    val onDelete: () -> Unit = {},

    val onBlockToggle: (isBlocked: Boolean, blockNote: String?) -> Unit = { _, _ -> },

    val onUnassign: () -> Unit = {},
    val onAssignToMe: () -> Unit = {},
    val removeAssignee: () -> Unit = {},

    val removeWatcher: () -> Unit = {},
    val onRemoveMeFromWatchersClick: () -> Unit = {},
    val onAddMeToWatchersClick: () -> Unit = {},
    val onTagRemove: (TagUI) -> Unit = {},

    val onAttachmentAdd: (uri: Uri?) -> Unit = { _ -> },
    val onAttachmentRemove: (Attachment) -> Unit = {},

    val onCommentRemove: (Comment) -> Unit = {},
    val onCreateCommentClick: (String) -> Unit = {},

    val onCustomFieldSave: (CustomFieldItemState) -> Unit = {},
    val customFieldsVersion: Long = 0,

    val userStories: ImmutableList<WorkItemUI> = persistentListOf(),
    val areWorkItemsExpanded: Boolean = false,
    val setAreWorkItemsExpanded: (Boolean) -> Unit,

    val onEpicColorPick: (Color) -> Unit = {},
    val isEpicColorLoading: Boolean = false,

    val onGoingToEditTags: () -> Unit = {},
    val onGoingToEditWatchers: () -> Unit = {},
    val onGoingToEditAssignee: () -> Unit = {},

    val canDeleteEpic: Boolean = false,
    val canModifyEpic: Boolean = false,
    val canComment: Boolean = false
)
