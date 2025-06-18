package io.eugenethedev.taigamobile.epics

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
import io.eugenethedev.taigamobile.core.nav.NavigateToTask
import io.eugenethedev.taigamobile.core.ui.NativeText
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.domain.entities.FiltersData
import io.eugenethedev.taigamobile.main.topbar.LocalTopBarConfig
import io.eugenethedev.taigamobile.main.topbar.TopBarActionResource
import io.eugenethedev.taigamobile.main.topbar.TopBarConfig
import io.eugenethedev.taigamobile.ui.components.TasksFiltersWithLazyList
import io.eugenethedev.taigamobile.ui.components.lists.SimpleTasksListWithTitle
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.ui.theme.commonVerticalPadding
import io.eugenethedev.taigamobile.ui.theme.mainHorizontalScreenPadding
import io.eugenethedev.taigamobile.ui.utils.SubscribeOnError

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
