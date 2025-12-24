package com.grappim.taigamobile.feature.epics.ui.list

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
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.ui.TaskFilters
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
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
fun EpicsScreen(
    showSnackbar: (NativeText) -> Unit,
    showSnackbarAction: (message: NativeText, actionLabel: String?) -> Unit,
    goToCreateEpic: () -> Unit,
    goToEpic: (Long, CommonTaskType, Long) -> Unit,
    updateData: Boolean,
    viewModel: EpicsViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val epics = viewModel.epics.collectAsLazyPagingItems()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val resources = LocalResources.current

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.epics),
                navigationIcon = NavigationIconConfig.Menu,
                actions = persistentListOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_add,
                        contentDescription = "Add",
                        onClick = {
                            goToCreateEpic()
                        }
                    )
                )
            )
        )
    }

    LaunchedEffect(epics.loadState.hasError) {
        if (epics.loadState.hasError) {
            showSnackbarAction(
                NativeText.Resource(RString.error_loading_issues),
                resources.getString(RString.close)
            )
        }
    }

    LaunchedEffect(state.filtersError) {
        if (state.filtersError.isNotEmpty()) {
            showSnackbar(state.filtersError)
        }
    }

    ObserveAsEvents(viewModel.snackBarMessage) { snackbarMessage ->
        if (snackbarMessage !is NativeText.Empty) {
            showSnackbarAction(snackbarMessage, resources.getString(RString.close))
        }
    }

    LaunchedEffect(updateData) {
        if (updateData) {
            epics.refresh()
        }
    }

    EpicsScreenContent(
        state = state,
        epics = epics,
        query = query,
        goToEpic = goToEpic
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpicsScreenContent(
    state: EpicsState,
    epics: LazyPagingItems<WorkItem>,
    goToEpic: (id: Long, type: CommonTaskType, ref: Long) -> Unit,
    modifier: Modifier = Modifier,
    query: String = ""
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        TaskFilters(
            selected = state.activeFilters,
            onSelect = state.selectFilters,
            data = state.filters,
            isFiltersError = state.filtersError.isNotEmpty(),
            onRetryFilters = state.retryLoadFilters,
            isFiltersLoading = state.isFiltersLoading,
            searchQuery = query,
            setSearchQuery = state.onSetQuery
        )
        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            onRefresh = {
                epics.refresh()
                state.retryLoadFilters()
            },
            isRefreshing = epics.loadState.refresh is LoadState.Loading || state.isFiltersLoading
        ) {
            when {
                epics.loadState.hasError && epics.itemCount == 0 -> {
                    ErrorStateWidget(
                        message = NativeText.Resource(RString.error_loading_issues),
                        onRetry = { epics.refresh() }
                    )
                }

                else -> {
                    LazyColumn {
                        simpleTasksListWithTitle(
                            commonTasksLazy = epics,
                            keysHash = state.activeFilters.hashCode(),
                            navigateToTask = goToEpic,
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
private fun EpicsScreenPreview() {
    TaigaMobileTheme {
        EpicsScreenContent(
            state = EpicsState(),
            goToEpic = { _, _, _ -> },
            epics = getPagingPreviewItems()
        )
    }
}
