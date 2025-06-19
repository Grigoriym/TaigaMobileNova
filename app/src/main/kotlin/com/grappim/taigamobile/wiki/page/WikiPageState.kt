package com.grappim.taigamobile.wiki.page

import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.feature.wiki.domain.WikiPage
import com.grappim.taigamobile.utils.ui.NativeText

data class WikiPageState(
    val toolbarTitle: NativeText = NativeText.Empty,

    val user: User? = null,
    val page: WikiPage? = null,

    val isDeleteAlertVisible: Boolean = false,
    val setDeleteAlertVisible: (Boolean) -> Unit,

    val isDropdownMenuExpanded: Boolean = false,
    val setDropdownMenuExpanded: (Boolean) -> Unit,

    val isEditPageVisible: Boolean = false,
    val setEditPageVisible: (Boolean) -> Unit,

    val description: TextFieldValue = TextFieldValue(""),
    val setDescription: (TextFieldValue) -> Unit
)
