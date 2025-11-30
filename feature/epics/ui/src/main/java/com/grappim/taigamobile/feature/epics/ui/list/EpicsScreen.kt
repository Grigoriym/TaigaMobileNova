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
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import com.grappim.taigamobile.utils.ui.getPagingPreviewItems

@Composable
fun EpicsScreen(
    showSnackbar: (message: NativeText, actionLabel: String?) -> Unit,
    goToCreateEpic: () -> Unit,
    goToEpic: (Long, CommonTaskType, Int) -> Unit,
    updateData: Boolean,
    viewModel: EpicsViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val epics = viewModel.epics.collectAsLazyPagingItems()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.epics),
                actions = listOf(
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
            epics.refresh()
        }
    }

    EpicsScreenContent(
        state = state,
        epics = epics,
        filters = filters,
        goToEpic = goToEpic
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpicsScreenContent(
    state: EpicsState,
    filters: FiltersDataDTO,
    epics: LazyPagingItems<CommonTask>,
    goToEpic: (id: Long, type: CommonTaskType, ref: Int) -> Unit,
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
            epics = getPagingPreviewItems(),
            filters = FiltersDataDTO()
        )
    }
}
