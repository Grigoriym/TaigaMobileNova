package com.grappim.taigamobile.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.grappim.taigamobile.commontask.CommonTaskNavDestination
import com.grappim.taigamobile.commontask.CommonTaskScreen
import com.grappim.taigamobile.commontask.navigateToCommonTask
import com.grappim.taigamobile.createtask.CreateTaskNavDestination
import com.grappim.taigamobile.createtask.CreateTaskScreen
import com.grappim.taigamobile.createtask.navigateToCreateTask
import com.grappim.taigamobile.dashboard.DashboardNavDestination
import com.grappim.taigamobile.dashboard.DashboardScreen
import com.grappim.taigamobile.dashboard.navigateToDashboardAsTopDestination
import com.grappim.taigamobile.epics.EpicsNavDestination
import com.grappim.taigamobile.epics.EpicsScreen
import com.grappim.taigamobile.issues.IssuesNavDestination
import com.grappim.taigamobile.issues.IssuesScreen
import com.grappim.taigamobile.kanban.KanbanNavDestination
import com.grappim.taigamobile.kanban.KanbanScreen
import com.grappim.taigamobile.login.LoginNavDestination
import com.grappim.taigamobile.login.navigateToLoginAsTopDestination
import com.grappim.taigamobile.login.ui.LoginScreen
import com.grappim.taigamobile.profile.ProfileNavDestination
import com.grappim.taigamobile.profile.ProfileScreen
import com.grappim.taigamobile.profile.navigateToProfileScreen
import com.grappim.taigamobile.projectselector.ProjectSelectorNavDestination
import com.grappim.taigamobile.projectselector.ProjectSelectorScreen
import com.grappim.taigamobile.projectselector.navigateToProjectSelector
import com.grappim.taigamobile.scrum.ScrumNavDestination
import com.grappim.taigamobile.scrum.ScrumScreen
import com.grappim.taigamobile.settings.SettingsNavDestination
import com.grappim.taigamobile.settings.SettingsScreen
import com.grappim.taigamobile.sprint.SprintNavDestination
import com.grappim.taigamobile.sprint.SprintScreen
import com.grappim.taigamobile.sprint.navigateToSprintScreen
import com.grappim.taigamobile.team.TeamNavDestination
import com.grappim.taigamobile.team.TeamScreen
import com.grappim.taigamobile.wiki.WikiNavDestination
import com.grappim.taigamobile.wiki.createpage.WikiCreatePageNavDestination
import com.grappim.taigamobile.wiki.createpage.WikiCreatePageScreen
import com.grappim.taigamobile.wiki.list.WikiListScreen
import com.grappim.taigamobile.wiki.page.WikiPageNavDestination
import com.grappim.taigamobile.wiki.page.WikiPageScreen
import com.grappim.taigamobile.wiki.page.navigateToWikiPage

@Composable
fun MainNavHost(
    isLogged: Boolean,
    navController: NavHostController,
    showMessage: (message: Int) -> Unit,
    onShowSnackbar: (message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = remember {
            if (isLogged) DashboardNavDestination else LoginNavDestination
        }
    ) {
        composable<LoginNavDestination> {
            LoginScreen(
                onShowSnackbar = onShowSnackbar,
                onLoginSuccess = {
                    navController.navigateToProjectSelector(isFromLogin = true)
                }
            )
        }

        composable<ProjectSelectorNavDestination> {
            ProjectSelectorScreen(
                showMessage = showMessage,
                onProjectSelect = { isFromLogin: Boolean ->
                    /**
                     * After the login and the project is selected, dashboard will become the top destination
                     */
                    if (isFromLogin) {
                        navController.navigateToDashboardAsTopDestination()
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable<DashboardNavDestination> {
            DashboardScreen(
                showMessage = showMessage,
                navigateToTaskScreen = { id, type, ref ->
                    navController.navigateToCommonTask(id, type, ref)
                }
            )
        }

        composable<ScrumNavDestination> {
            ScrumScreen(
                showMessage = showMessage,
                goToCreateTask = { type ->
                    navController.navigateToCreateTask(type = type)
                },
                goToSprint = { id ->
                    navController.navigateToSprintScreen(id)
                },
                goToTask = { id, type, ref ->
                    navController.navigateToCommonTask(id, type, ref)
                }
            )
        }

        composable<EpicsNavDestination> {
            EpicsScreen(
                showMessage = showMessage,
                goToCreateTask = { type ->
                    navController.navigateToCreateTask(type = type)
                },
                goToTask = { id, type, ref ->
                    navController.navigateToCommonTask(id, type, ref)
                }
            )
        }

        composable<IssuesNavDestination> {
            IssuesScreen(
                showMessage = showMessage,
                goToCreateTask = { type ->
                    navController.navigateToCreateTask(type = type)
                },
                goToTask = { id, type, ref ->
                    navController.navigateToCommonTask(id, type, ref)
                }
            )
        }

        composable<TeamNavDestination> {
            TeamScreen(
                showMessage = showMessage,
                goToProfile = { userId ->
                    navController.navigateToProfileScreen(userId)
                }
            )
        }

        composable<KanbanNavDestination> {
            KanbanScreen(
                showMessage = showMessage,
                goToTask = { id, type, ref ->
                    navController.navigateToCommonTask(id, type, ref)
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

        composable<WikiNavDestination> {
            WikiListScreen(
                showMessage = showMessage,
                goToWikiCreatePage = {
                    navController.navigate(route = WikiCreatePageNavDestination)
                },
                goToWikiPage = {
                    navController.navigateToWikiPage(it)
                }
            )
        }

        composable<WikiCreatePageNavDestination> {
            WikiCreatePageScreen(
                showMessage = showMessage,
                goToWikiPage = {
                    navController.popBackStack()
                    navController.navigateToWikiPage(it)
                }
            )
        }

        composable<WikiPageNavDestination> {
            WikiPageScreen(
                showMessage = showMessage,
                goToProfile = { userId ->
                    navController.navigateToProfileScreen(userId)
                },
                goBack = navController::popBackStack
            )
        }

        composable<SettingsNavDestination> {
            SettingsScreen(
                showMessage = showMessage,
                onLogout = navController::navigateToLoginAsTopDestination
            )
        }

        composable<SprintNavDestination> {
            SprintScreen(
                showMessage = showMessage,
                goBack = navController::popBackStack,
                goToTaskScreen = { id, taskType, ref ->
                    navController.navigateToCommonTask(id, taskType, ref)
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
                showMessage = showMessage
            )
        }

        composable<CommonTaskNavDestination> {
            CommonTaskScreen(
                showMessage = showMessage,
                goToProfile = { userId ->
                    navController.navigateToProfileScreen(userId)
                },
                goToUserStory = { id, taskType, ref ->
                    navController.popBackStack()
                    navController.navigateToCommonTask(id, taskType, ref)
                },
                goBack = {
                    navController.popBackStack()
                },
                navigateToCreateTask = { type, id ->
                    navController.navigateToCreateTask(
                        type = type,
                        parentId = id
                    )
                },
                navigateToTask = { id, type, ref ->
                    navController.navigateToCommonTask(id, type, ref)
                }
            )
        }

        composable<CreateTaskNavDestination> {
            CreateTaskScreen(
                showMessage = showMessage,
                navigateOnTaskCreated = { id, type, ref ->
                    navController.popBackStack()
                    navController.navigateToCommonTask(id, type, ref)
                }
            )
        }
    }
}
