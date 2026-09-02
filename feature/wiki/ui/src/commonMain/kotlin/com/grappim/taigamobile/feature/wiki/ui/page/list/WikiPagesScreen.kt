package com.grappim.taigamobile.feature.wiki.ui.page.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.feature.wiki.ui.widgets.WikiListContentWidget
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.all_wiki_pages
import com.grappim.taigamobile.strings.generated.resources.delete_wiki_text
import com.grappim.taigamobile.strings.generated.resources.delete_wiki_title
import com.grappim.taigamobile.uikit.generated.resources.ic_add
import com.grappim.taigamobile.uikit.state.LocalOfflineState
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
fun WikiPagesScreen(
    showSnackbar: (NativeText) -> Unit,
    updateData: Boolean,
    goToWikiCreatePage: () -> Unit,
    goToWikiPage: (slug: String, id: Long) -> Unit,
    viewModel: WikiPagesViewModel = koinViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isOffline = LocalOfflineState.current

    LaunchedEffect(state.canAddWikiPage, isOffline) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.all_wiki_pages),
                navigationIcon = NavigationIconConfig.Menu,
                actions = buildList {
                    if (state.canAddWikiPage) {
                        add(
                            TopBarActionIconButton(
                                enabled = !isOffline,
                                drawable = RDrawable.ic_add,
                                contentDescription = "Add",
                                onClick = goToWikiCreatePage
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
        if (message.isNotEmpty() && state.allPages.isNotEmpty()) {
            showSnackbar(message)
        }
    }

    ConfirmActionDialog(
        isVisible = state.isRemovePageDialogVisible,
        onConfirm = state.onConfirmDelete,
        onDismiss = state.onDismissDeleteDialog,
        title = stringResource(RString.delete_wiki_title),
        description = stringResource(RString.delete_wiki_text)
    )

    WikiPagesScreenContent(
        state = state,
        navigateToCreatePage = goToWikiCreatePage,
        goToPage = goToWikiPage,
        isOffline = isOffline
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiPagesScreenContent(
    state: WikiPagesState,
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    navigateToCreatePage: () -> Unit = {},
    goToPage: (slug: String, id: Long) -> Unit = { _, _ -> }
) {
    DesktopRefreshEffect(onRefresh = state.refresh)
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        onRefresh = state.refresh,
        isRefreshing = state.isLoading
    ) {
        WikiListContentWidget(
            items = state.allPages,
            isLoading = state.isLoading,
            error = state.error,
            onRetry = state.refresh,
            navigateToCreate = navigateToCreatePage,
            canCreate = state.canAddWikiPage,
            onClick = goToPage,
            canDeleteItem = state.canDeleteWikiPage,
            onDeleteItemClick = state.onDeleteClick,
            isOffline = isOffline
        )
    }
}

@Preview
@Composable
private fun WikiPagesScreenPreview() {
    WikiPagesScreenContent(state = WikiPagesState(), isOffline = false)
}
