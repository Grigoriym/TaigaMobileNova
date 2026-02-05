package com.grappim.taigamobile.feature.wiki.ui.page.list

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
fun WikiPagesScreen(
    showSnackbar: (NativeText) -> Unit,
    goToWikiCreatePage: () -> Unit,
    goToWikiPage: (slug: String, id: Long) -> Unit,
    viewModel: WikiPagesViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val resources = LocalResources.current
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
                }.toImmutableList()
            )
        )
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
        title = resources.getString(RString.delete_wiki_title),
        description = resources.getString(RString.delete_wiki_text)
    )

    WikiPagesScreenContent(
        state = state,
        navigateToCreatePage = goToWikiCreatePage,
        goToPage = goToWikiPage,
        isOffline = isOffline
    )
}

@Composable
fun WikiPagesScreenContent(
    state: WikiPagesState,
    isOffline: Boolean,
    modifier: Modifier = Modifier,
    navigateToCreatePage: () -> Unit = {},
    goToPage: (slug: String, id: Long) -> Unit = { _, _ -> }
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
        modifier = modifier,
        isOffline = isOffline
    )
}

@Preview
@Composable
private fun WikiPagesScreenPreview() {
    WikiPagesScreenContent(state = WikiPagesState(), isOffline = false)
}
