package com.grappim.taigamobile.feature.dashboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.commonVerticalPadding
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.widgets.container.HorizontalTabbedPagerWidget
import com.grappim.taigamobile.uikit.widgets.list.simpleTasksListWithTitle
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun DashboardScreen(
    showSnackbar: (NativeText) -> Unit,
    navigateToTaskScreen: (Long, CommonTaskType, Int) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        state.onLoad()
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.dashboard),
                navigationIcon = NavigationIconConfig.Menu
            )
        )
    }

    LaunchedEffect(state.error) {
        if (state.error.isNotEmpty()) {
            showSnackbar(state.error)
        }
    }

    DashboardScreenContent(
        state = state,
        navigateToTask = {
            navigateToTaskScreen(it.id, it.taskType, it.ref)
        }
    )
}

@Composable
fun DashboardScreenContent(
    state: DashboardState,
    modifier: Modifier = Modifier,
    navigateToTask: (WorkItem) -> Unit = { _ -> }
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        if (state.isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularLoaderWidget()
            }
        } else {
            HorizontalTabbedPagerWidget(
                modifier = Modifier.fillMaxSize(),
                tabs = DashboardTabs.entries.toTypedArray(),
                pagerState = rememberPagerState(pageCount = { DashboardTabs.entries.size })
            ) { page ->
                when (DashboardTabs.entries[page]) {
                    DashboardTabs.WorkingOn -> TabContent(
                        commonTasks = state.workingOn,
                        navigateToTask = navigateToTask
                    )

                    DashboardTabs.Watching -> TabContent(
                        commonTasks = state.watching,
                        navigateToTask = navigateToTask
                    )
                }
            }
        }
    }
}

@Composable
private fun TabContent(commonTasks: List<WorkItem>, navigateToTask: (WorkItem) -> Unit) =
    LazyColumn(Modifier.fillMaxSize()) {
        simpleTasksListWithTitle(
            bottomPadding = commonVerticalPadding,
            horizontalPadding = mainHorizontalScreenPadding,
            showExtendedTaskInfo = true,
            commonTasks = commonTasks,
            navigateToTask = { id, _, _ -> navigateToTask(commonTasks.find { it.id == id }!!) }
        )
    }

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() = TaigaMobileTheme {
    DashboardScreenContent(
        state = DashboardState()
    )
}
