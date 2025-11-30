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
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.feature.filters.ui.TaskFilters
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.commonVerticalPadding
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewDarkLight
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
import kotlinx.collections.immutable.persistentListOf

@Composable
fun IssuesScreen(
    showSnackbar: (message: NativeText, actionLabel: String?) -> Unit,
    goToCreateTask: () -> Unit,
    goToTask: (Long, CommonTaskType, Int) -> Unit,
    updateData: Boolean,
    viewModel: IssuesViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val issues = viewModel.issues.collectAsLazyPagingItems()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.issues),
                navigationIcon = NavigationIconConfig.Menu,
                actions = persistentListOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_add,
                        contentDescription = "Add",
                        onClick = {
                            goToCreateTask()
                        }
                    )
                )
            )
        )
    }

    LaunchedEffect(issues.loadState.hasError) {
        if (issues.loadState.hasError) {
            showSnackbar(
                NativeText.Resource(RString.error_loading_issues),
                context.getString(RString.close)
            )
        }
    }

    ObserveAsEvents(viewModel.snackBarMessage) { snackbarMessage ->
        if (snackbarMessage !is NativeText.Empty) {
            showSnackbar(snackbarMessage, context.getString(RString.close))
        }
    }

    LaunchedEffect(updateData) {
        if (updateData) {
            issues.refresh()
        }
    }

    IssuesScreenContent(
        state = state,
        filters = filters,
        issues = issues,
        navigateToTask = goToTask
    )
}

@Composable
fun IssuesScreenContent(
    state: IssuesState,
    filters: FiltersDataDTO,
    navigateToTask: (id: Long, type: CommonTaskType, ref: Int) -> Unit,
    issues: LazyPagingItems<CommonTask>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        TaskFilters(
            selected = state.activeFilters,
            onSelect = state.selectFilters,
            data = filters,
            isFiltersError = state.isFiltersError,
            onRetryFilters = state.retryLoadFilters,
            isFiltersLoading = state.isFiltersLoading
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
                            navigateToTask = navigateToTask,
                            horizontalPadding = mainHorizontalScreenPadding,
                            bottomPadding = commonVerticalPadding
                        )
                    }
                }
            }
        }
    }
}

@PreviewDarkLight
@Composable
private fun IssuesScreenPreview() = TaigaMobileTheme {
    IssuesScreenContent(
        navigateToTask = { _, _, _ -> },
        state = IssuesState(),
        issues = getPagingPreviewItems(),
        filters = FiltersDataDTO()
    )
}
