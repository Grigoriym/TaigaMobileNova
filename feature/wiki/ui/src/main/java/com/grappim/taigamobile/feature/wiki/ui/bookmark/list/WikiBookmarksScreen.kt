package com.grappim.taigamobile.feature.wiki.ui.bookmark.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.feature.wiki.ui.widgets.WikiListContentWidget
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.state.LocalOfflineState
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.toImmutableList

@Composable
fun WikiBookmarksScreen(
    showSnackbar: (NativeText) -> Unit,
    goToWikiCreateBookmark: () -> Unit,
    goToWikiPage: (slug: String, id: Long) -> Unit,
    viewModel: WikiBookmarksViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resources = LocalResources.current
    val isOffline = LocalOfflineState.current

    LaunchedEffect(state.canAddWikiLink, isOffline) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.bookmarks),
                navigationIcon = NavigationIconConfig.Menu,
                actions = buildList {
                    if (state.canAddWikiLink) {
                        add(
                            TopBarActionIconButton(
                                enabled = !isOffline,
                                drawable = RDrawable.ic_add,
                                contentDescription = "Add",
                                onClick = goToWikiCreateBookmark
                            )
                        )
                    }
                }.toImmutableList()
            )
        )
    }

    ObserveAsEvents(viewModel.onDeleteSuccess) {
        state.refresh()
    }

    ObserveAsEvents(viewModel.snackBarMessage) { message ->
        if (message.isNotEmpty() && state.bookmarks.isNotEmpty()) {
            showSnackbar(message)
        }
    }

    ConfirmActionDialog(
        isVisible = state.isRemoveBookmarkDialogVisible,
        onConfirm = state.onConfirmDelete,
        onDismiss = state.onDismissDeleteDialog,
        title = resources.getString(RString.delete_bookmark_title),
        description = resources.getString(RString.delete_bookmark_text)
    )

    WikiBookmarksScreenContent(
        state = state,
        navigateToCreateBookmark = goToWikiCreateBookmark,
        goToPage = goToWikiPage,
        isOffline = isOffline
    )
}

@Composable
fun WikiBookmarksScreenContent(
    state: WikiBookmarksState,
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    navigateToCreateBookmark: () -> Unit = {},
    goToPage: (slug: String, id: Long) -> Unit = { _, _ -> }
) {
    WikiListContentWidget(
        items = state.bookmarks,
        isLoading = state.isLoading,
        error = state.error,
        onRetry = state.refresh,
        navigateToCreate = navigateToCreateBookmark,
        canCreate = state.canAddWikiLink,
        onClick = goToPage,
        canDeleteItem = state.canDeleteWikiLink,
        onDeleteItemClick = state.onDeleteClick,
        modifier = modifier,
        isOffline = isOffline
    )
}

@Preview
@Composable
private fun WikiBookmarksScreenPreview() {
    WikiBookmarksScreenContent(state = WikiBookmarksState(), isOffline = false)
}
