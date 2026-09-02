package com.grappim.taigamobile.feature.wiki.ui.bookmark.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.feature.wiki.ui.widgets.WikiListContentWidget
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.bookmarks
import com.grappim.taigamobile.strings.generated.resources.delete_bookmark_text
import com.grappim.taigamobile.strings.generated.resources.delete_bookmark_title
import com.grappim.taigamobile.uikit.generated.resources.ic_add
import com.grappim.taigamobile.uikit.state.LocalOfflineState
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.DesktopRefreshEffect
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.buildDesktopRefreshTopBarAction
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WikiBookmarksScreen(
    showSnackbar: (NativeText) -> Unit,
    updateData: Boolean,
    goToWikiCreateBookmark: () -> Unit,
    goToWikiPage: (slug: String, id: Long) -> Unit,
    viewModel: WikiBookmarksViewModel = koinViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
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
                    buildDesktopRefreshTopBarAction(onClick = state.refresh)?.let { add(it) }
                }.toImmutableList()
            )
        )
    }

    LaunchedEffect(updateData) {
        if (updateData) {
            state.refresh()
        }
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
        title = stringResource(RString.delete_bookmark_title),
        description = stringResource(RString.delete_bookmark_text)
    )

    WikiBookmarksScreenContent(
        state = state,
        navigateToCreateBookmark = goToWikiCreateBookmark,
        goToPage = goToWikiPage,
        isOffline = isOffline
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiBookmarksScreenContent(
    state: WikiBookmarksState,
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    navigateToCreateBookmark: () -> Unit = {},
    goToPage: (slug: String, id: Long) -> Unit = { _, _ -> }
) {
    DesktopRefreshEffect(onRefresh = state.refresh)
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        onRefresh = state.refresh,
        isRefreshing = state.isLoading
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
            isOffline = isOffline
        )
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WikiBookmarksScreenPreview() {
    TaigaMobilePreviewTheme {
        WikiBookmarksScreenContent(state = WikiBookmarksState(), isOffline = false)
    }
}
