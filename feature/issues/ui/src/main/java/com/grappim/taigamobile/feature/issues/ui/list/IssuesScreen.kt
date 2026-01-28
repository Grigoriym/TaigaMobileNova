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
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.grappim.taigamobile.uikit.widgets.emptystate.EmptyStateWidget
import com.grappim.taigamobile.uikit.widgets.list.simpleTasksListWithTitle
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import com.grappim.taigamobile.utils.ui.getErrorMessage
import com.grappim.taigamobile.utils.ui.getPagingPreviewItems
import com.grappim.taigamobile.utils.ui.hasError
import com.grappim.taigamobile.utils.ui.isEmpty
import com.grappim.taigamobile.utils.ui.isLoading
import com.grappim.taigamobile.utils.ui.isNotEmpty
import kotlinx.collections.immutable.toImmutableList

@Composable
fun IssuesScreen(
    showSnackbar: (NativeText) -> Unit,
    goToCreateIssue: () -> Unit,
    goToIssue: (Long, Long) -> Unit,
    updateData: Boolean,
    viewModel: IssuesViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val issues = viewModel.issues.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

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

    ObserveAsEvents(viewModel.snackBarMessage) { snackbarMessage ->
        if (snackbarMessage.isNotEmpty() && issues.isNotEmpty()) {
            showSnackbar(snackbarMessage)
        }
    }

    LaunchedEffect(updateData) {
        if (updateData) {
            issues.refresh()
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
        modifier = modifier.fillMaxSize()
    ) {
        TaskFiltersWidget(
            selected = state.activeFilters,
            onSelect = state.selectFilters,
            data = state.filters,
            filtersError = state.filtersError,
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
            isRefreshing = issues.isLoading() || state.isFiltersLoading
        ) {
            when {
                issues.hasError() && issues.isEmpty() -> {
                    ErrorStateWidget(
                        message = issues.loadState.getErrorMessage(
                            fallback = NativeText.Resource(RString.error_loading_issues)
                        ),
                        onRetry = {
                            issues.refresh()
                            state.retryLoadFilters()
                        }
                    )
                }

                issues.isEmpty() -> {
                    EmptyStateWidget(
                        message = NativeText.Resource(RString.no_issues_found)
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
