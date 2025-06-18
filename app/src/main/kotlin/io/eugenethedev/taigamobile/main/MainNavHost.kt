package io.eugenethedev.taigamobile.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.eugenethedev.taigamobile.commontask.CommonTaskNavDestination
import io.eugenethedev.taigamobile.commontask.CommonTaskScreen
import io.eugenethedev.taigamobile.commontask.navigateToCommonTask
import io.eugenethedev.taigamobile.createtask.CreateTaskNavDestination
import io.eugenethedev.taigamobile.createtask.CreateTaskScreen
import io.eugenethedev.taigamobile.createtask.navigateToCreateTask
import io.eugenethedev.taigamobile.dashboard.DashboardNavDestination
import io.eugenethedev.taigamobile.dashboard.DashboardScreen
import io.eugenethedev.taigamobile.dashboard.navigateToDashboardAsTopDestination
import io.eugenethedev.taigamobile.epics.EpicsNavDestination
import io.eugenethedev.taigamobile.epics.EpicsScreen
import io.eugenethedev.taigamobile.issues.IssuesNavDestination
import io.eugenethedev.taigamobile.issues.IssuesScreen
import io.eugenethedev.taigamobile.kanban.KanbanNavDestination
import io.eugenethedev.taigamobile.kanban.KanbanScreen
import io.eugenethedev.taigamobile.login.LoginNavDestination
import io.eugenethedev.taigamobile.login.navigateToLoginAsTopDestination
import io.eugenethedev.taigamobile.login.ui.LoginScreen
import io.eugenethedev.taigamobile.profile.ProfileNavDestination
import io.eugenethedev.taigamobile.profile.ProfileScreen
import io.eugenethedev.taigamobile.profile.navigateToProfileScreen
import io.eugenethedev.taigamobile.projectselector.ProjectSelectorNavDestination
import io.eugenethedev.taigamobile.projectselector.ProjectSelectorScreen
import io.eugenethedev.taigamobile.projectselector.navigateToProjectSelector
import io.eugenethedev.taigamobile.scrum.ScrumNavDestination
import io.eugenethedev.taigamobile.scrum.ScrumScreen
import io.eugenethedev.taigamobile.settings.SettingsNavDestination
import io.eugenethedev.taigamobile.settings.SettingsScreen
import io.eugenethedev.taigamobile.sprint.SprintNavDestination
import io.eugenethedev.taigamobile.sprint.SprintScreen
import io.eugenethedev.taigamobile.sprint.navigateToSprintScreen
import io.eugenethedev.taigamobile.team.TeamNavDestination
import io.eugenethedev.taigamobile.team.TeamScreen
import io.eugenethedev.taigamobile.wiki.WikiNavDestination
import io.eugenethedev.taigamobile.wiki.createpage.WikiCreatePageNavDestination
import io.eugenethedev.taigamobile.wiki.createpage.WikiCreatePageScreen
import io.eugenethedev.taigamobile.wiki.list.WikiListScreen
import io.eugenethedev.taigamobile.wiki.page.WikiPageNavDestination
import io.eugenethedev.taigamobile.wiki.page.WikiPageScreen
import io.eugenethedev.taigamobile.wiki.page.navigateToWikiPage

@Composable
fun MainNavHost(
    modifier: Modifier,
    isLogged: Boolean,
    navController: NavHostController,
    showMessage: (message: Int) -> Unit,
    onShowSnackbar: (message: String) -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = remember { if (isLogged) DashboardNavDestination else LoginNavDestination }
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
                onProjectSelected = { isFromLogin: Boolean ->
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
                onLogout = navController::navigateToLoginAsTopDestination,
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
