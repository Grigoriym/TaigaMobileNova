package com.grappim.taigamobile.main

import KanbanNavDestination
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.grappim.taigamobile.feature.settings.ui.SettingsNavDestination
import com.grappim.taigamobile.feature.settings.ui.SettingsScreen
import com.grappim.taigamobile.feature.settings.ui.about.SettingsAboutScreen
import com.grappim.taigamobile.feature.settings.ui.about.SettingsAboutScreenRouteNavDestination
import com.grappim.taigamobile.feature.settings.ui.about.goToSettingsAboutScreen
import com.grappim.taigamobile.feature.settings.ui.interfacescreen.SettingsInterfaceScreen
import com.grappim.taigamobile.feature.settings.ui.interfacescreen.SettingsInterfaceScreenNavDestination
import com.grappim.taigamobile.feature.settings.ui.interfacescreen.goToSettingsInterfaceScreen
import com.grappim.taigamobile.feature.settings.ui.user.SettingsUserScreen
import com.grappim.taigamobile.feature.settings.ui.user.SettingsUserScreenNavDestination
import com.grappim.taigamobile.feature.settings.ui.user.goToSettingsUserScreen
import com.grappim.taigamobile.feature.sprint.ui.SprintNavDestination
import com.grappim.taigamobile.feature.sprint.ui.SprintScreen
import com.grappim.taigamobile.feature.tasks.ui.navigateToTask
import com.grappim.taigamobile.feature.teams.ui.TeamNavDestination
import com.grappim.taigamobile.feature.teams.ui.TeamScreen
import com.grappim.taigamobile.feature.userstories.ui.navigateToUserStory
import com.grappim.taigamobile.main.nav.epicNavGraph
import com.grappim.taigamobile.main.nav.issueNavGraph
import com.grappim.taigamobile.main.nav.scrumNavGraph
import com.grappim.taigamobile.main.nav.taskNavGraph
import com.grappim.taigamobile.main.nav.userStoryNavGraph
import com.grappim.taigamobile.main.nav.wikiNavGraph
import com.grappim.taigamobile.main.nav.workItemEditsNavGraph
import com.grappim.taigamobile.uikit.utils.LocalScreenReadySignal
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun MainNavHost(
    initialNavState: InitialNavState,
    navController: NavHostController,
    showSnackbar: (NativeText) -> Unit,
    showSnackbarAction: (message: NativeText, actionLabel: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(initialNavState.isReady) {
        if (initialNavState.isReady) {
            when (val dest = initialNavState.startDestination) {
                is ProjectSelectorNavDestination ->
                    navController.navigateToProjectSelector(isFromLogin = dest.isFromLogin)

                is DashboardNavDestination ->
                    navController.navigateToDashboardAsTopDestination()
            }
        }
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = LoginNavDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(150))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(150))
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

        scrumNavGraph(
            navController = navController
        )

        composable<LoginNavDestination> {
            val screenReadySignal = LocalScreenReadySignal.current
            LaunchedEffect(initialNavState.isReady) {
                if (initialNavState.isReady && initialNavState.startDestination is LoginNavDestination) {
                    screenReadySignal.signalReady()
                }
            }
            LoginScreen(
                onShowSnackbar = showSnackbar,
                onLoginSuccess = {
                    navController.navigateToProjectSelector(isFromLogin = true)
                }
            )
        }

        composable<ProjectSelectorNavDestination> {
            val screenReadySignal = LocalScreenReadySignal.current
            LaunchedEffect(initialNavState.isReady) {
                if (initialNavState.isReady && initialNavState.startDestination is ProjectSelectorNavDestination) {
                    screenReadySignal.signalReady()
                }
            }
            ProjectSelectorScreen(
                goBack = {
                    navController.popBackStack()
                },
                onProjectSelect = {
                    navController.navigateToDashboardAsTopDestination()
                }
            )
        }

        composable<DashboardNavDestination> {
            val screenReadySignal = LocalScreenReadySignal.current
            LaunchedEffect(initialNavState.isReady) {
                if (initialNavState.isReady && initialNavState.startDestination is DashboardNavDestination) {
                    screenReadySignal.signalReady()
                }
            }
            DashboardScreen(
                navigateToTaskScreen = { id, type, ref ->
                    navController.navigate(id, type, ref)
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
            SettingsScreen(
                goToAboutScreen = {
                    navController.goToSettingsAboutScreen()
                },
                goToInterfaceScreen = {
                    navController.goToSettingsInterfaceScreen()
                },
                goToUserScreen = {
                    navController.goToSettingsUserScreen()
                }
            )
        }

        composable<SettingsAboutScreenRouteNavDestination> {
            SettingsAboutScreen()
        }

        composable<SettingsInterfaceScreenNavDestination> {
            SettingsInterfaceScreen()
        }

        composable<SettingsUserScreenNavDestination> {
            SettingsUserScreen()
        }

        composable<SprintNavDestination> { navBackStackEntry ->
            val updateData: Boolean =
                navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
            SprintScreen(
                updateData = updateData,
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
