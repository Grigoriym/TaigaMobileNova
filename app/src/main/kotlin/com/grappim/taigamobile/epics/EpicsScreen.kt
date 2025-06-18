package com.grappim.taigamobile.epics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.R
import com.grappim.taigamobile.core.nav.NavigateToTask
import com.grappim.taigamobile.core.ui.NativeText
import com.grappim.taigamobile.domain.entities.CommonTask
import com.grappim.taigamobile.domain.entities.CommonTaskType
import com.grappim.taigamobile.domain.entities.FiltersData
import com.grappim.taigamobile.main.topbar.LocalTopBarConfig
import com.grappim.taigamobile.main.topbar.TopBarActionResource
import com.grappim.taigamobile.main.topbar.TopBarConfig
import com.grappim.taigamobile.ui.components.TasksFiltersWithLazyList
import com.grappim.taigamobile.ui.components.lists.SimpleTasksListWithTitle
import com.grappim.taigamobile.ui.theme.TaigaMobileTheme
import com.grappim.taigamobile.ui.theme.commonVerticalPadding
import com.grappim.taigamobile.ui.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.ui.utils.SubscribeOnError

@Composable
fun EpicsScreen(
    viewModel: EpicsViewModel = hiltViewModel(),
    showMessage: (message: Int) -> Unit,
    goToCreateTask: (CommonTaskType) -> Unit,
    goToTask: (Long, CommonTaskType, Int) -> Unit
) {
    val topBarController = LocalTopBarConfig.current

    LaunchedEffect(Unit) {
        viewModel.onOpen()

        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(R.string.epics),
                actions = listOf(
                    TopBarActionResource(
                        drawable = R.drawable.ic_add,
                        contentDescription = "Add",
                        onClick = {
                            goToCreateTask(CommonTaskType.Epic)
                        }
                    )
                )
            )
        )
    }

    val lazyEpicItems = viewModel.epics.collectAsLazyPagingItems()

    val filters by viewModel.filters.collectAsState()
    filters.SubscribeOnError(showMessage)

    val activeFilters by viewModel.activeFilters.collectAsState()

    LaunchedEffect(lazyEpicItems.loadState.hasError) {
        if (lazyEpicItems.loadState.hasError) {
            showMessage(R.string.common_error_message)
        }
    }

    EpicsScreenContent(
        epics = lazyEpicItems,
        filters = filters.data ?: FiltersData(),
        activeFilters = activeFilters,
        selectFilters = viewModel::selectFilters,
        navigateToTask = goToTask,
    )
}

@Composable
fun EpicsScreenContent(
    epics: LazyPagingItems<CommonTask>? = null,
    filters: FiltersData = FiltersData(),
    activeFilters: FiltersData = FiltersData(),
    selectFilters: (FiltersData) -> Unit = {},
    navigateToTask: NavigateToTask = { _, _, _ -> }
) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.Start
) {
    TasksFiltersWithLazyList(
        filters = filters,
        activeFilters = activeFilters,
        selectFilters = selectFilters
    ) {
        SimpleTasksListWithTitle(
            commonTasksLazy = epics,
            keysHash = activeFilters.hashCode(),
            navigateToTask = navigateToTask,
            horizontalPadding = mainHorizontalScreenPadding,
            bottomPadding = commonVerticalPadding
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun EpicsScreenPreview() = TaigaMobileTheme {
    EpicsScreenContent()
}
