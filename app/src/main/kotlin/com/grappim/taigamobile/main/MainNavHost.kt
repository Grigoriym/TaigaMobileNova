package com.grappim.taigamobile.main

import KanbanNavDestination
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
import com.grappim.taigamobile.feature.dashboard.ui.DashboardNavDestination
import com.grappim.taigamobile.feature.dashboard.ui.DashboardScreen
import com.grappim.taigamobile.feature.dashboard.ui.navigateToDashboardAsTopDestination
import com.grappim.taigamobile.feature.epics.ui.EpicsNavDestination
import com.grappim.taigamobile.feature.epics.ui.EpicsScreen
import com.grappim.taigamobile.feature.issues.ui.IssuesNavDestination
import com.grappim.taigamobile.feature.issues.ui.IssuesScreen
import com.grappim.taigamobile.feature.kanban.ui.KanbanScreen
import com.grappim.taigamobile.feature.login.ui.LoginNavDestination
import com.grappim.taigamobile.feature.login.ui.LoginScreen
import com.grappim.taigamobile.feature.profile.ui.ProfileNavDestination
import com.grappim.taigamobile.feature.profile.ui.ProfileScreen
import com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorNavDestination
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorScreen
import com.grappim.taigamobile.feature.projectselector.ui.navigateToProjectSelector
import com.grappim.taigamobile.feature.scrum.ui.ScrumNavDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumScreen
import com.grappim.taigamobile.feature.settings.ui.SettingsNavDestination
import com.grappim.taigamobile.feature.settings.ui.SettingsScreen
import com.grappim.taigamobile.feature.sprint.ui.SprintNavDestination
import com.grappim.taigamobile.feature.sprint.ui.SprintScreen
import com.grappim.taigamobile.feature.sprint.ui.navigateToSprintScreen
import com.grappim.taigamobile.feature.teams.ui.TeamNavDestination
import com.grappim.taigamobile.feature.teams.ui.TeamScreen
import com.grappim.taigamobile.feature.wiki.ui.create.WikiCreatePageScreen
import com.grappim.taigamobile.feature.wiki.ui.list.WikiListScreen
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiCreatePageNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiPageNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.navigateToWikiPage
import com.grappim.taigamobile.feature.wiki.ui.page.WikiPageScreen
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun MainNavHost(
    isLogged: Boolean,
    navController: NavHostController,
    showMessage: (message: Int) -> Unit,
    showSnackbar: (NativeText) -> Unit,
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
                onShowSnackbar = showSnackbar,
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
                showSnackbar = showSnackbar,
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
                showSnackbar = showSnackbar
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
