package com.grappim.taigamobile.feature.issues.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.feature.issues.ui.list.IssuesState
import com.grappim.taigamobile.feature.issues.ui.list.IssuesViewModel
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.commonVerticalPadding
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.filter.TasksFiltersWithLazyList
import com.grappim.taigamobile.uikit.widgets.list.simpleTasksListWithTitle
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SubscribeOnError

@Suppress("LambdaParameterInRestartableEffect")
@Composable
fun IssuesScreen(
    showMessage: (message: Int) -> Unit,
    goToCreateTask: (CommonTaskType) -> Unit,
    goToTask: (Long, CommonTaskType, Int) -> Unit,
    updateData: Boolean,
    viewModel: IssuesViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.issues),
                actions = listOf(
                    TopBarActionIconButton(
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

    LaunchedEffect(updateData) {
        if (updateData) {
            state.onUpdateData()
        }
    }

    LaunchedEffect(state.isFiltersError) {
        if (state.isFiltersError) {
            showMessage(RString.common_error_message)
        }
    }

    IssuesScreenContent(
        state = state,
        issues = issues,
        selectFilters = viewModel::selectFilters,
        navigateToTask = goToTask
    )
}

@Composable
fun IssuesScreenContent(
    state: IssuesState,
    navigateToTask: (id: Long, type: CommonTaskType, ref: Int) -> Unit,
    modifier: Modifier = Modifier,
    issues: LazyPagingItems<CommonTask>? = null,
    selectFilters: (FiltersDataDTO) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        TasksFiltersWithLazyList(
            filters = state.filters,
            activeFilters = state.activeFilters,
            selectFilters = selectFilters
        ) {
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

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun IssuesScreenPreview() = TaigaMobileTheme {
    IssuesScreenContent(
        navigateToTask = { _, _, _ -> },
        state = IssuesState(
            onUpdateData = {}
        )
    )
}
