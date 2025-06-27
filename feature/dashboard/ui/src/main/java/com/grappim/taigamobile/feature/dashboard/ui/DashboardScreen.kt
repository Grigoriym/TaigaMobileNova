package com.grappim.taigamobile.feature.dashboard.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.Project
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.commonVerticalPadding
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.widgets.container.HorizontalTabbedPager
import com.grappim.taigamobile.uikit.widgets.list.ProjectCard
import com.grappim.taigamobile.uikit.widgets.list.simpleTasksListWithTitle
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoader
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun DashboardScreen(
    showMessage: (message: Int) -> Unit,
    navigateToTaskScreen: (Long, CommonTaskType, Int) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.dashboard)
            )
        )
    }

    LaunchedEffect(state.isError) {
        if (state.isError) {
            showMessage(RString.common_error_message)
        }
    }

    DashboardScreenContent(
        state = state,
        navigateToTask = {
            viewModel.changeCurrentProject(it.projectInfo)
            navigateToTaskScreen(it.id, it.taskType, it.ref)
        },
        changeCurrentProject = viewModel::changeCurrentProject
    )
}

@Composable
fun DashboardScreenContent(
    state: DashboardState,
    modifier: Modifier = Modifier,
    navigateToTask: (CommonTask) -> Unit = { _ -> },
    changeCurrentProject: (Project) -> Unit = { _ -> }
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
                CircularLoader()
            }
        } else {
            HorizontalTabbedPager(
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

                    DashboardTabs.MyProjects -> MyProjects(
                        myProjects = state.myProjects,
                        currentProjectId = state.currentProjectId,
                        changeCurrentProject = changeCurrentProject
                    )
                }
            }
        }
    }
}

@Composable
private fun TabContent(commonTasks: List<CommonTask>, navigateToTask: (CommonTask) -> Unit) =
    LazyColumn(Modifier.fillMaxSize()) {
        simpleTasksListWithTitle(
            bottomPadding = commonVerticalPadding,
            horizontalPadding = mainHorizontalScreenPadding,
            showExtendedTaskInfo = true,
            commonTasks = commonTasks,
            navigateToTask = { id, _, _ -> navigateToTask(commonTasks.find { it.id == id }!!) }
        )
    }

@Composable
private fun MyProjects(
    myProjects: List<Project>,
    currentProjectId: Long,
    changeCurrentProject: (Project) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(myProjects) {
            ProjectCard(
                project = it,
                isCurrent = it.id == currentProjectId,
                onClick = { changeCurrentProject(it) }
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectCardPreview() {
    TaigaMobileTheme {
        ProjectCard(
            project = Project(
                id = 0,
                name = "Name",
                slug = "slug",
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, " +
                    "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                isPrivate = true
            ),
            isCurrent = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() = TaigaMobileTheme {
    DashboardScreenContent(
        state = DashboardState()
    )
}
