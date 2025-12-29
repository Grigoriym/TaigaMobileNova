package com.grappim.taigamobile.main

import KanbanNavDestination
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.createtask.CreateTaskNavDestination
import com.grappim.taigamobile.createtask.CreateTaskScreen
import com.grappim.taigamobile.createtask.navigateToCreateTask
import com.grappim.taigamobile.feature.dashboard.ui.DashboardNavDestination
import com.grappim.taigamobile.feature.dashboard.ui.DashboardScreen
import com.grappim.taigamobile.feature.dashboard.ui.navigateToDashboardAsTopDestination
import com.grappim.taigamobile.feature.epics.ui.details.navigateToEpicDetails
import com.grappim.taigamobile.feature.issues.ui.details.navigateToIssueDetails
import com.grappim.taigamobile.feature.kanban.ui.KanbanScreen
import com.grappim.taigamobile.feature.login.ui.LoginNavDestination
import com.grappim.taigamobile.feature.login.ui.LoginScreen
import com.grappim.taigamobile.feature.profile.ui.ProfileNavDestination
import com.grappim.taigamobile.feature.profile.ui.ProfileScreen
import com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorNavDestination
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorScreen
import com.grappim.taigamobile.feature.projectselector.ui.navigateToProjectSelector
import com.grappim.taigamobile.feature.scrum.ui.ScrumBacklogDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumClosedSprintsDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumOpenSprintsDestination
import com.grappim.taigamobile.feature.scrum.ui.backlog.ScrumBacklogScreen
import com.grappim.taigamobile.feature.scrum.ui.closed.ScrumClosedSprintsScreen
import com.grappim.taigamobile.feature.scrum.ui.open.ScrumOpenSprintsScreen
import com.grappim.taigamobile.feature.settings.ui.SettingsNavDestination
import com.grappim.taigamobile.feature.settings.ui.SettingsScreen
import com.grappim.taigamobile.feature.sprint.ui.SprintNavDestination
import com.grappim.taigamobile.feature.sprint.ui.SprintScreen
import com.grappim.taigamobile.feature.sprint.ui.navigateToSprintScreen
import com.grappim.taigamobile.feature.tasks.ui.navigateToTask
import com.grappim.taigamobile.feature.teams.ui.TeamNavDestination
import com.grappim.taigamobile.feature.teams.ui.TeamScreen
import com.grappim.taigamobile.feature.userstories.ui.navigateToUserStory
import com.grappim.taigamobile.main.nav.epicNavGraph
import com.grappim.taigamobile.main.nav.issueNavGraph
import com.grappim.taigamobile.main.nav.taskNavGraph
import com.grappim.taigamobile.main.nav.userStoryNavGraph
import com.grappim.taigamobile.main.nav.wikiNavGraph
import com.grappim.taigamobile.main.nav.workItemEditsNavGraph
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun MainNavHost(
    isLoggedIn: Boolean,
    navController: NavHostController,
    showMessage: (message: Int) -> Unit,
    showSnackbar: (NativeText) -> Unit,
    showSnackbarAction: (message: NativeText, actionLabel: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = remember {
            if (isLoggedIn) DashboardNavDestination else LoginNavDestination
        },
        enterTransition = {
            fadeIn(animationSpec = tween(100))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(100))
        }
    ) {
        issueNavGraph(
            showSnackbar = showSnackbar,
            navController = navController,
            showSnackbarAction = showSnackbarAction
        )

        userStoryNavGraph(
            showSnackbar = showSnackbar,
            navController = navController
        )

        taskNavGraph(
            showSnackbar = showSnackbar,
            navController = navController
        )

        workItemEditsNavGraph(
            navController = navController
        )

        epicNavGraph(
            showSnackbar = showSnackbar,
            navController = navController,
            showSnackbarAction = showSnackbarAction
        )

        wikiNavGraph(
            showSnackbar = showSnackbar,
            navController = navController
        )

        composable<LoginNavDestination> {
            LoginScreen(
                onShowSnackbar = showSnackbar,
                onLoginSuccess = {
                    navController.navigateToProjectSelector(isFromLogin = true)
                }
            )
        }

        composable<ProjectSelectorNavDestination> {
            ProjectSelectorScreen(
                showMessage = showMessage,
                onProjectSelect = {
                    navController.navigateToDashboardAsTopDestination()
                }
            )
        }

        composable<DashboardNavDestination> {
            DashboardScreen(
                showSnackbar = showSnackbar,
                navigateToTaskScreen = { id, type, ref ->
                    navController.navigate(id, type, ref)
                }
            )
        }

//        composable<ScrumNavDestination> { navBackStackEntry ->
//            val updateData: Boolean =
//                navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
//            ScrumScreen(
//                showSnackbar = showSnackbar,
//                goToCreateUserStory = {
//                    navController.navigateToCreateTask(type = CommonTaskType.UserStory)
//                },
//                goToSprint = { id ->
//                    navController.navigateToSprintScreen(id)
//                },
//                updateData = updateData,
//                goToUserStory = { id, _, ref ->
//                    navController.navigateToUserStory(
//                        userStoryId = id,
//                        ref = ref
//                    )
//                }
//            )
//        }

        composable<ScrumBacklogDestination> { navBackStackEntry ->
            val updateData: Boolean =
                navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
            ScrumBacklogScreen(
                updateData = updateData,
                goToCreateUserStory = {
                    navController.navigateToCreateTask(type = CommonTaskType.UserStory)
                },
                navigateToTask = { id, _, ref ->
                    navController.navigateToUserStory(
                        userStoryId = id,
                        ref = ref
                    )
                }
            )
        }

        composable<ScrumOpenSprintsDestination> { navBackStackEntry ->
            val updateData: Boolean =
                navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
            ScrumOpenSprintsScreen(
                updateData = updateData,
                navigateToBoard = { sprint ->
                    navController.navigateToSprintScreen(sprint.id)
                }
            )
        }

        composable<ScrumClosedSprintsDestination> { navBackStackEntry ->
            val updateData: Boolean =
                navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
            ScrumClosedSprintsScreen(
                updateData = updateData,
                navigateToBoard = { sprint ->
                    navController.navigateToSprintScreen(sprint.id)
                }
            )
        }

        composable<TeamNavDestination> {
            TeamScreen(
                showSnackbar = showSnackbar,
                goToProfile = { userId ->
                    navController.navigateToProfileScreen(userId)
                }
            )
        }

        composable<KanbanNavDestination> { navBackStackEntry ->
            val updateData: Boolean =
                navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
            KanbanScreen(
                updateData = updateData,
                showSnackbar = showSnackbar,
                goToTask = { id, type, ref ->
                    navController.navigate(id, type, ref)
                },
                goToCreateTask = { task, statusId, swimlaneId ->
                    navController.navigateToCreateTask(
                        type = task,
                        statusId = statusId,
                        swimlaneId = swimlaneId
                    )
                }
            )
        }

        composable<SettingsNavDestination> {
            SettingsScreen(showSnackbar = showSnackbar)
        }

        composable<SprintNavDestination> {
            SprintScreen(
                showSnackbar = showSnackbar,
                goBack = {
                    navController.setUpdateDataOnBack()
                    navController.popBackStack()
                },
                goToTaskScreen = { id, type, ref ->
                    navController.navigate(id, type, ref)
                },
                goToCreateTask = { type, parentId, sprintId ->
                    navController.navigateToCreateTask(
                        type = type,
                        parentId = parentId,
                        sprintId = sprintId
                    )
                }
            )
        }

        composable<ProfileNavDestination> {
            ProfileScreen(
                showSnackbar = showSnackbar
            )
        }

        composable<CreateTaskNavDestination> {
            CreateTaskScreen(
                showSnackbar = showSnackbar,
                navigateOnTaskCreated = { id, type, ref ->
                    navController.popBackStack()
                    navController.navigate(id, type, ref)
                }
            )
        }
    }
}

private fun NavController.navigate(id: Long, type: CommonTaskType, ref: Long) {
    when (type) {
        CommonTaskType.UserStory -> this.navigateToUserStory(
            userStoryId = id,
            ref = ref
        )

        CommonTaskType.Epic -> this.navigateToEpicDetails(
            epicId = id,
            ref = ref
        )

        CommonTaskType.Issue -> this.navigateToIssueDetails(
            issueId = id,
            ref = ref
        )

        CommonTaskType.Task -> this.navigateToTask(
            taskId = id,
            ref = ref
        )
    }
}

const val UPDATE_DATA_ON_BACK = "UpdateDataOnBack"

fun NavController.setUpdateDataOnBack() {
    previousBackStackEntry
        ?.savedStateHandle
        ?.set(UPDATE_DATA_ON_BACK, true)
}
