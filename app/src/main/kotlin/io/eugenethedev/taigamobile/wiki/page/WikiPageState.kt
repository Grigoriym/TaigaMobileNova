package io.eugenethedev.taigamobile.wiki.page

import androidx.compose.ui.text.input.TextFieldValue
import io.eugenethedev.taigamobile.core.ui.NativeText
import io.eugenethedev.taigamobile.domain.entities.User
import io.eugenethedev.taigamobile.domain.entities.WikiPage

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
    val setDescription: (TextFieldValue) -> Unit,
)
