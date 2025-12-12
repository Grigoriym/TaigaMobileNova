package com.grappim.taigamobile.feature.wiki.ui.page

import android.net.Uri
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
import com.grappim.taigamobile.utils.ui.NativeText

data class WikiPageState(
    val toolbarTitle: NativeText = NativeText.Empty,

    val user: User? = null,
    val currentPage: WikiPage? = null,
    val originalPage: WikiPage? = null,
    val pageSlug: String,
    val link: WikiLink? = null,

    val isDeleteAlertVisible: Boolean = false,
    val setDeleteAlertVisible: (Boolean) -> Unit = {},

    val isDropdownMenuExpanded: Boolean = false,
    val setDropdownMenuExpanded: (Boolean) -> Unit = {},

    val onDeleteConfirm: () -> Unit = {},
    val isLoading: Boolean = false,

    val onAttachmentAdd: (uri: Uri?) -> Unit = { _ -> },
    val onAttachmentRemove: (Attachment) -> Unit = {}
)
