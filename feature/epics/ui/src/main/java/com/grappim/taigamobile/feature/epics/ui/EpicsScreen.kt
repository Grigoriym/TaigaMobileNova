package com.grappim.taigamobile.feature.epics.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.commonVerticalPadding
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.filter.TasksFiltersWithLazyList
import com.grappim.taigamobile.uikit.widgets.list.simpleTasksListWithTitle
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SubscribeOnError
import com.grappim.taigamobile.utils.ui.getPagingPreviewItems

@Composable
fun EpicsScreen(
    showMessage: (message: Int) -> Unit,
    goToCreateTask: (CommonTaskType) -> Unit,
    goToTask: (Long, CommonTaskType, Int) -> Unit,
    viewModel: EpicsViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.epics),
                actions = listOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_add,
                        contentDescription = "Add",
                        onClick = {
                            goToCreateTask(CommonTaskType.Epic)
                        }
                    )
                )
            )
        )
    }

    LaunchedEffect(state.isError) {
        if (state.isError) {
            showMessage(RString.common_error_message)
        }
    }

    val epics = viewModel.epics.collectAsLazyPagingItems()
    epics.SubscribeOnError(showMessage)

    EpicsScreenContent(
        state = state,
        epics = epics,
        selectFilters = viewModel::selectFilters,
        navigateToTask = goToTask
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpicsScreenContent(
    state: EpicsState,
    epics: LazyPagingItems<CommonTask>,
    navigateToTask: (id: Long, type: CommonTaskType, ref: Int) -> Unit,
    modifier: Modifier = Modifier,
    selectFilters: (FiltersDataDTO) -> Unit = {}
) {
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = epics.loadState.refresh is LoadState.Loading,
        onRefresh = state.onRefresh
    ) {
        TasksFiltersWithLazyList(
            filters = state.filters,
            activeFilters = state.activeFilters,
            selectFilters = selectFilters
        ) {
            simpleTasksListWithTitle(
                commonTasksLazy = epics,
                keysHash = state.activeFilters.hashCode(),
                navigateToTask = navigateToTask,
                horizontalPadding = mainHorizontalScreenPadding,
                bottomPadding = commonVerticalPadding
            )
        }
    }
}

@PreviewDarkLight
@Composable
private fun EpicsScreenPreview() {
    TaigaMobileTheme {
        EpicsScreenContent(
            state = EpicsState(onRefresh = {}),
            navigateToTask = { _, _, _ -> },
            epics = getPagingPreviewItems()
        )
    }
}
