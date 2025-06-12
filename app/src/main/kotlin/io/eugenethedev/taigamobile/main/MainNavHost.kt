package io.eugenethedev.taigamobile.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.eugenethedev.taigamobile.core.nav.Routes
import io.eugenethedev.taigamobile.dashboard.DashboardNavDestination
import io.eugenethedev.taigamobile.dashboard.DashboardScreen
import io.eugenethedev.taigamobile.dashboard.navigateToDashboardAsTopDestination
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.login.LoginNavDestination
import io.eugenethedev.taigamobile.login.navigateToLoginAsTopDestination
import io.eugenethedev.taigamobile.login.ui.LoginScreen
import io.eugenethedev.taigamobile.projectselector.ProjectSelectorNavDestination
import io.eugenethedev.taigamobile.projectselector.ProjectSelectorScreen
import io.eugenethedev.taigamobile.projectselector.navigateToProjectSelector
import io.eugenethedev.taigamobile.ui.screens.commontask.CommonTaskScreen
import io.eugenethedev.taigamobile.ui.screens.createtask.CreateTaskScreen
import io.eugenethedev.taigamobile.ui.screens.epics.EpicsNavDestination
import io.eugenethedev.taigamobile.ui.screens.epics.EpicsScreen
import io.eugenethedev.taigamobile.ui.screens.issues.IssuesNavDestination
import io.eugenethedev.taigamobile.ui.screens.issues.IssuesScreen
import io.eugenethedev.taigamobile.ui.screens.kanban.KanbanScreen
import io.eugenethedev.taigamobile.ui.screens.more.MoreNavDestination
import io.eugenethedev.taigamobile.ui.screens.more.MoreScreen
import io.eugenethedev.taigamobile.ui.screens.profile.ProfileScreen
import io.eugenethedev.taigamobile.ui.screens.scrum.ScrumNavDestination
import io.eugenethedev.taigamobile.ui.screens.scrum.ScrumScreen
import io.eugenethedev.taigamobile.ui.screens.settings.SettingsScreen
import io.eugenethedev.taigamobile.ui.screens.sprint.SprintScreen
import io.eugenethedev.taigamobile.ui.screens.team.TeamScreen
import io.eugenethedev.taigamobile.ui.screens.wiki.createpage.WikiCreatePageScreen
import io.eugenethedev.taigamobile.ui.screens.wiki.list.WikiListScreen
import io.eugenethedev.taigamobile.ui.screens.wiki.page.WikiPageScreen

@Composable
fun MainNavHost(
    modifier: Modifier,
    viewModel: MainViewModel,
    navController: NavHostController,
    showMessage: (message: Int) -> Unit,
    onShowSnackbar: (message: String) -> Unit
) {
    val isLogged by viewModel.isLogged.collectAsState()

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
                onBack = navController::popBackStack,
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
                navController = navController,
                showMessage = showMessage
            )
        }

        composable<ScrumNavDestination> {
            ScrumScreen(
                navController = navController,
                showMessage = showMessage,
                goToProjectSelector = navController::navigateToProjectSelector,
            )
        }

        composable<EpicsNavDestination> {
            EpicsScreen(
                navController = navController,
                showMessage = showMessage,
                goToProjectSelector = navController::navigateToProjectSelector,
            )
        }

        composable<IssuesNavDestination> {
            IssuesScreen(
                navController = navController,
                showMessage = showMessage,
                goToProjectSelector = navController::navigateToProjectSelector,
            )
        }

        composable<MoreNavDestination> {
            MoreScreen(
                navController = navController
            )
        }

        composable(Routes.team) {
            TeamScreen(
                navController = navController,
                showMessage = showMessage,
                goToProjectSelector = navController::navigateToProjectSelector,
                goBck = navController::popBackStack
            )
        }

        composable(Routes.kanban) {
            KanbanScreen(
                navController = navController,
                showMessage = showMessage,
                goToProjectSelector = navController::navigateToProjectSelector,
            )
        }

        composable(Routes.wiki_selector) {
            WikiListScreen(
                navController = navController,
                showMessage = showMessage,
                goToProjectSelector = navController::navigateToProjectSelector,
                goBack = navController::popBackStack
            )
        }

        composable(Routes.wiki_create_page) {
            WikiCreatePageScreen(
                navController = navController,
                showMessage = showMessage
            )
        }

        composable(
            "${Routes.wiki_page}/{${Routes.Arguments.wikiSlug}}",
            arguments = listOf(
                navArgument(Routes.Arguments.wikiSlug) { type = NavType.StringType }
            )
        ) {
            WikiPageScreen(
                slug = it.arguments!!.getString(Routes.Arguments.wikiSlug).orEmpty(),
                navController = navController,
                showMessage = showMessage
            )
        }

        composable(Routes.settings) {
            SettingsScreen(
                navController = navController,
                showMessage = showMessage,
                onLogout = navController::navigateToLoginAsTopDestination,
            )
        }

        composable(
            "${Routes.sprint}/{${Routes.Arguments.sprintId}}",
            arguments = listOf(
                navArgument(Routes.Arguments.sprintId) { type = NavType.LongType }
            )
        ) {
            SprintScreen(
                navController = navController,
                sprintId = it.arguments!!.getLong(Routes.Arguments.sprintId),
                showMessage = showMessage
            )
        }

        composable(
            "${Routes.profile}/{${Routes.Arguments.userId}}",
            arguments = listOf(
                navArgument(Routes.Arguments.userId) { type = NavType.LongType }
            )
        ) {
            ProfileScreen(
                navController = navController,
                showMessage = showMessage,
                userId = it.arguments!!.getLong(Routes.Arguments.userId),
            )
        }

        composable(
            Routes.Arguments.run { "${Routes.commonTask}/{$commonTaskId}/{$commonTaskType}/{$ref}" },
            arguments = listOf(
                navArgument(Routes.Arguments.commonTaskType) { type = NavType.StringType },
                navArgument(Routes.Arguments.commonTaskId) { type = NavType.LongType },
                navArgument(Routes.Arguments.ref) { type = NavType.IntType },
            )
        ) {
            CommonTaskScreen(
                navController = navController,
                commonTaskId = it.arguments!!.getLong(Routes.Arguments.commonTaskId),
                commonTaskType = CommonTaskType.valueOf(
                    it.arguments!!.getString(
                        Routes.Arguments.commonTaskType,
                        ""
                    )
                ),
                ref = it.arguments!!.getInt(Routes.Arguments.ref),
                showMessage = showMessage
            )
        }

        composable(
            Routes.Arguments.run { "${Routes.createTask}/{$commonTaskType}?$parentId={$parentId}&$sprintId={$sprintId}&$statusId={$statusId}&$swimlaneId={$swimlaneId}" },
            arguments = listOf(
                navArgument(Routes.Arguments.commonTaskType) { type = NavType.StringType },
                navArgument(Routes.Arguments.parentId) {
                    type = NavType.LongType
                    defaultValue = -1L // long does not allow null values
                },
                navArgument(Routes.Arguments.sprintId) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument(Routes.Arguments.statusId) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument(Routes.Arguments.swimlaneId) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            )
        ) {
            CreateTaskScreen(
                navController = navController,
                commonTaskType = CommonTaskType.valueOf(
                    it.arguments!!.getString(
                        Routes.Arguments.commonTaskType,
                        ""
                    )
                ),
                parentId = it.arguments!!.getLong(Routes.Arguments.parentId).takeIf { it >= 0 },
                sprintId = it.arguments!!.getLong(Routes.Arguments.sprintId).takeIf { it >= 0 },
                statusId = it.arguments!!.getLong(Routes.Arguments.statusId).takeIf { it >= 0 },
                swimlaneId = it.arguments!!.getLong(Routes.Arguments.swimlaneId)
                    .takeIf { it >= 0 },
                showMessage = showMessage
            )
        }
    }
}
