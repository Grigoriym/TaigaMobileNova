@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.issues.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.feature.filters.ui.TaskFiltersWidget
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.commonVerticalPadding
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.list.simpleTasksListWithTitle
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import com.grappim.taigamobile.utils.ui.getPagingPreviewItems
import kotlinx.collections.immutable.toImmutableList

@Composable
fun IssuesScreen(
    showSnackbar: (NativeText) -> Unit,
    showSnackbarAction: (message: NativeText, actionLabel: String?) -> Unit,
    goToCreateIssue: () -> Unit,
    goToIssue: (Long, Long) -> Unit,
    updateData: Boolean,
    viewModel: IssuesViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val issues = viewModel.issues.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val resources = LocalResources.current

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.issues),
                navigationIcon = NavigationIconConfig.Menu,
                actions = buildList {
                    if (state.canCreateIssue) {
                        add(
                            TopBarActionIconButton(
                                drawable = RDrawable.ic_add,
                                contentDescription = "Add",
                                onClick = {
                                    goToCreateIssue()
                                }
                            )
                        )
                    }
                }.toImmutableList()
            )
        )
    }

    LaunchedEffect(issues.loadState.hasError) {
        if (issues.loadState.hasError) {
            showSnackbarAction(
                NativeText.Resource(RString.error_loading_issues),
                resources.getString(RString.close)
            )
        }
    }

    ObserveAsEvents(viewModel.snackBarMessage) { snackbarMessage ->
        if (snackbarMessage.isNotEmpty()) {
            showSnackbarAction(
                snackbarMessage,
                resources.getString(RString.close)
            )
        }
    }

    LaunchedEffect(updateData) {
        if (updateData) {
            issues.refresh()
        }
    }

    LaunchedEffect(state.filtersError) {
        if (state.filtersError.isNotEmpty()) {
            showSnackbar(state.filtersError)
        }
    }

    IssuesScreenContent(
        state = state,
        issues = issues,
        navigateToTask = goToIssue,
        searchQuery = searchQuery
    )
}

@Composable
fun IssuesScreenContent(
    state: IssuesState,
    navigateToTask: (id: Long, ref: Long) -> Unit,
    issues: LazyPagingItems<WorkItem>,
    modifier: Modifier = Modifier,
    searchQuery: String = ""
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        TaskFiltersWidget(
            selected = state.activeFilters,
            onSelect = state.selectFilters,
            data = state.filters,
            isFiltersError = state.filtersError.isNotEmpty(),
            onRetryFilters = state.retryLoadFilters,
            isFiltersLoading = state.isFiltersLoading,
            searchQuery = searchQuery,
            setSearchQuery = state.setSearchQuery
        )
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            onRefresh = {
                issues.refresh()
                state.retryLoadFilters()
            },
            isRefreshing = issues.loadState.refresh is LoadState.Loading || state.isFiltersLoading
        ) {
            when {
                issues.loadState.hasError && issues.itemCount == 0 -> {
                    ErrorStateWidget(
                        message = NativeText.Resource(RString.error_loading_issues),
                        onRetry = { issues.refresh() }
                    )
                }

                else -> {
                    LazyColumn {
                        simpleTasksListWithTitle(
                            commonTasksLazy = issues,
                            keysHash = state.activeFilters.hashCode(),
                            navigateToTask = { id, _, ref ->
                                navigateToTask(id, ref)
                            },
                            horizontalPadding = mainHorizontalScreenPadding,
                            bottomPadding = commonVerticalPadding
                        )
                    }
                }
            }
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun IssuesScreenPreview() = TaigaMobileTheme {
    IssuesScreenContent(
        navigateToTask = { _, _ -> },
        state = IssuesState(),
        issues = getPagingPreviewItems()
    )
}
