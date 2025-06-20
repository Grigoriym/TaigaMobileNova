package com.grappim.taigamobile.feature.issues.ui

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
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.navigation.NavigateToTask
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.commonVerticalPadding
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
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
fun IssuesScreen(
    showMessage: (message: Int) -> Unit,
    goToCreateTask: (CommonTaskType) -> Unit,
    goToTask: (Long, CommonTaskType, Int) -> Unit,
    viewModel: IssuesViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.issues),
                actions = listOf(
                    TopBarActionResource(
                        drawable = RDrawable.ic_add,
                        contentDescription = "Add",
                        onClick = {
                            goToCreateTask(CommonTaskType.Issue)
                        }
                    )
                )
            )
        )
    }

    val issues = viewModel.issues.collectAsLazyPagingItems()
    issues.SubscribeOnError(showMessage)

    val filters by viewModel.filters.collectAsState()
    filters.SubscribeOnError(showMessage)

    val activeFilters by viewModel.activeFilters.collectAsState()

    IssuesScreenContent(
        issues = issues,
        filters = filters.data ?: FiltersData(),
        activeFilters = activeFilters,
        selectFilters = viewModel::selectFilters,
        navigateToTask = goToTask
    )
}

@Composable
fun IssuesScreenContent(
    navigateToTask: NavigateToTask,
    modifier: Modifier = Modifier,
    issues: LazyPagingItems<CommonTask>? = null,
    filters: FiltersData = FiltersData(),
    activeFilters: FiltersData = FiltersData(),
    selectFilters: (FiltersData) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        TasksFiltersWithLazyList(
            filters = filters,
            activeFilters = activeFilters,
            selectFilters = selectFilters
        ) {
            simpleTasksListWithTitle(
                commonTasksLazy = issues,
                keysHash = activeFilters.hashCode(),
                navigateToTask = navigateToTask,
                horizontalPadding = mainHorizontalScreenPadding,
                bottomPadding = commonVerticalPadding
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun IssuesScreenPreview() = TaigaMobileTheme {
    IssuesScreenContent(navigateToTask = { _, _, _ -> })
}
