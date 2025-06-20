package com.grappim.taigamobile.feature.epics.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.navigation.NavigateToTask
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.commonVerticalPadding
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewMulti
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TasksFiltersWithLazyList
import com.grappim.taigamobile.uikit.widgets.list.simpleTasksListWithTitle
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionResource
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SubscribeOnError

@Suppress("LambdaParameterInRestartableEffect")
@Composable
fun EpicsScreen(
    showMessage: (message: Int) -> Unit,
    goToCreateTask: (CommonTaskType) -> Unit,
    goToTask: (Long, CommonTaskType, Int) -> Unit,
    viewModel: EpicsViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current

    LaunchedEffect(Unit) {
        viewModel.onOpen()

        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.epics),
                actions = listOf(
                    TopBarActionResource(
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

    val epics = viewModel.epics.collectAsLazyPagingItems()
    epics.SubscribeOnError(showMessage)

    val filters by viewModel.filters.collectAsState()
    filters.SubscribeOnError(showMessage)

    val activeFilters by viewModel.activeFilters.collectAsState()

    EpicsScreenContent(
        epics = epics,
        filters = filters.data ?: FiltersData(),
        activeFilters = activeFilters,
        selectFilters = viewModel::selectFilters,
        navigateToTask = goToTask
    )
}

@Composable
fun EpicsScreenContent(
    modifier: Modifier = Modifier,
    epics: LazyPagingItems<CommonTask>? = null,
    filters: FiltersData = FiltersData(),
    activeFilters: FiltersData = FiltersData(),
    selectFilters: (FiltersData) -> Unit = {},
    navigateToTask: NavigateToTask = { _, _, _ -> }
) = Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.Start
) {
    TasksFiltersWithLazyList(
        filters = filters,
        activeFilters = activeFilters,
        selectFilters = selectFilters
    ) {
        simpleTasksListWithTitle(
            commonTasksLazy = epics,
            keysHash = activeFilters.hashCode(),
            navigateToTask = navigateToTask,
            horizontalPadding = mainHorizontalScreenPadding,
            bottomPadding = commonVerticalPadding
        )
    }
}

@PreviewMulti
@Composable
private fun EpicsScreenPreview() = TaigaMobileTheme {
    EpicsScreenContent()
}
