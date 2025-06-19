package com.grappim.taigamobile.dashboard

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.Project
import com.grappim.taigamobile.feature.dashboard.ui.DashboardTabs
import com.grappim.taigamobile.main.topbar.LocalTopBarConfig
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.uikit.widgets.container.HorizontalTabbedPager
import com.grappim.taigamobile.ui.components.lists.ProjectCard
import com.grappim.taigamobile.ui.components.lists.SimpleTasksListWithTitle
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoader
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.commonVerticalPadding
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.ui.utils.LoadingResult
import com.grappim.taigamobile.ui.utils.SubscribeOnError

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    showMessage: (message: Int) -> Unit,
    navigateToTaskScreen: (Long, CommonTaskType, Int) -> Unit,
) {
    val topBarController = LocalTopBarConfig.current
    LaunchedEffect(Unit) {
        viewModel.onOpen()

        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.dashboard)
            )
        )
    }

    val workingOn by viewModel.workingOn.collectAsState()
    workingOn.SubscribeOnError(showMessage)

    val watching by viewModel.watching.collectAsState()
    watching.SubscribeOnError(showMessage)

    val myProjects by viewModel.myProjects.collectAsState()
    myProjects.SubscribeOnError(showMessage)

    val currentProjectId by viewModel.currentProjectId.collectAsState()

    DashboardScreenContent(
        isLoading = listOf(workingOn, watching, myProjects).any { it is LoadingResult<*> },
        workingOn = workingOn.data.orEmpty(),
        watching = watching.data.orEmpty(),
        myProjects = myProjects.data.orEmpty(),
        currentProjectId = currentProjectId,
        navigateToTask = {
            viewModel.changeCurrentProject(it.projectInfo)
            navigateToTaskScreen(it.id, it.taskType, it.ref)
        },
        changeCurrentProject = viewModel::changeCurrentProject
    )
}

@Composable
fun DashboardScreenContent(
    isLoading: Boolean = false,
    workingOn: List<CommonTask> = emptyList(),
    watching: List<CommonTask> = emptyList(),
    myProjects: List<Project> = emptyList(),
    currentProjectId: Long = 0,
    navigateToTask: (CommonTask) -> Unit = { _ -> },
    changeCurrentProject: (Project) -> Unit = { _ -> }
) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.Start
) {
    if (isLoading) {
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
            pagerState = rememberPagerState(pageCount = { DashboardTabs.entries.size }),
        ) { page ->
            when (DashboardTabs.entries[page]) {
                DashboardTabs.WorkingOn -> TabContent(
                    commonTasks = workingOn,
                    navigateToTask = navigateToTask
                )

                DashboardTabs.Watching -> TabContent(
                    commonTasks = watching,
                    navigateToTask = navigateToTask
                )

                DashboardTabs.MyProjects -> MyProjects(
                    myProjects = myProjects,
                    currentProjectId = currentProjectId,
                    changeCurrentProject = changeCurrentProject
                )
            }
        }
    }
}

@Composable
private fun TabContent(
    commonTasks: List<CommonTask>,
    navigateToTask: (CommonTask) -> Unit,
) = LazyColumn(Modifier.fillMaxSize()) {
    SimpleTasksListWithTitle(
        bottomPadding = commonVerticalPadding,
        horizontalPadding = mainHorizontalScreenPadding,
        showExtendedTaskInfo = true,
        commonTasks = commonTasks,
        navigateToTask = { id, _, _ -> navigateToTask(commonTasks.find { it.id == id }!!) },
    )
}

@Composable
private fun MyProjects(
    myProjects: List<Project>,
    currentProjectId: Long,
    changeCurrentProject: (Project) -> Unit
) = LazyColumn {
    items(myProjects) {
        ProjectCard(
            project = it,
            isCurrent = it.id == currentProjectId,
            onClick = { changeCurrentProject(it) }
        )

        Spacer(Modifier.height(12.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectCardPreview() = TaigaMobileTheme {
    ProjectCard(
        project = Project(
            id = 0,
            name = "Name",
            slug = "slug",
            description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            isPrivate = true
        ),
        isCurrent = true,
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun DashboardPreview() = TaigaMobileTheme {
    DashboardScreenContent(
        myProjects = List(3) {
            Project(
                id = it.toLong(),
                name = "Name",
                slug = "slug",
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
                isPrivate = true
            )
        }
    )
}
