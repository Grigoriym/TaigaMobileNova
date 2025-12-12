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
import com.grappim.taigamobile.commontask.CommonTaskNavDestination
import com.grappim.taigamobile.commontask.CommonTaskScreen
import com.grappim.taigamobile.commontask.navigateToCommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.createtask.CreateTaskNavDestination
import com.grappim.taigamobile.createtask.CreateTaskScreen
import com.grappim.taigamobile.createtask.navigateToCreateIssue
import com.grappim.taigamobile.createtask.navigateToCreateTask
import com.grappim.taigamobile.feature.dashboard.ui.DashboardNavDestination
import com.grappim.taigamobile.feature.dashboard.ui.DashboardScreen
import com.grappim.taigamobile.feature.dashboard.ui.navigateToDashboardAsTopDestination
import com.grappim.taigamobile.feature.epics.ui.details.EpicDetailsNavDestination
import com.grappim.taigamobile.feature.epics.ui.details.EpicDetailsScreen
import com.grappim.taigamobile.feature.epics.ui.details.navigateToEpicDetails
import com.grappim.taigamobile.feature.epics.ui.list.EpicsNavDestination
import com.grappim.taigamobile.feature.epics.ui.list.EpicsScreen
import com.grappim.taigamobile.feature.issues.ui.details.IssueDetailsNavDestination
import com.grappim.taigamobile.feature.issues.ui.details.IssueDetailsScreen
import com.grappim.taigamobile.feature.issues.ui.details.navigateToIssueDetails
import com.grappim.taigamobile.feature.issues.ui.list.IssuesNavDestination
import com.grappim.taigamobile.feature.issues.ui.list.IssuesScreen
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
import com.grappim.taigamobile.feature.userstories.ui.UserStoryDetailsNavDestination
import com.grappim.taigamobile.feature.userstories.ui.UserStoryDetailsScreen
import com.grappim.taigamobile.feature.userstories.ui.navigateToUserStory
import com.grappim.taigamobile.feature.wiki.ui.create.WikiCreatePageScreen
import com.grappim.taigamobile.feature.wiki.ui.list.WikiListScreen
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiCreatePageNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiPageNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.navigateToWikiPage
import com.grappim.taigamobile.feature.wiki.ui.page.WikiPageScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.WorkItemEditDescriptionNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.WorkItemEditDescriptionScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.navigateToWorkItemEditDescription
import com.grappim.taigamobile.feature.workitem.ui.screens.edittags.WorkItemEditTagsNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.edittags.WorkItemEditTagsScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.edittags.navigateToWorkItemEditTags
import com.grappim.taigamobile.feature.workitem.ui.screens.epic.WorkItemEditEpicNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.epic.WorkItemEditEpicScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.epic.navigateToWorkItemEditEpic
import com.grappim.taigamobile.feature.workitem.ui.screens.sprint.WorkItemEditSprintNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.sprint.WorkItemEditSprintScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.sprint.navigateToWorkItemEditSprint
import com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.WorkItemEditTeamMemberNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.WorkItemEditTeamMemberScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.navigateToWorkItemEditTeamMember
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun MainNavHost(
    isLogged: Boolean,
    isNewUiUsed: Boolean,
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
            if (isLogged) DashboardNavDestination else LoginNavDestination
        },
        enterTransition = {
            fadeIn(animationSpec = tween(100))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(100))
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
                    if (isNewUiUsed) {
                        when (type) {
                            CommonTaskType.UserStory -> navController.navigateToUserStory(
                                taskId = id,
                                ref = ref
                            )

                            CommonTaskType.Epic -> navController.navigateToEpicDetails(
                                epicId = id,
                                ref = ref
                            )

                            CommonTaskType.Issue -> navController.navigateToIssueDetails(
                                taskId = id,
                                ref = ref
                            )

                            else -> navController.navigateToCommonTask(id, type, ref)
                        }
                    } else {
                        navController.navigateToCommonTask(id, type, ref)
                    }
                }
            )
        }

        composable<ScrumNavDestination> { navBackStackEntry ->
            val updateData: Boolean =
                navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
            ScrumScreen(
                showSnackbar = showSnackbar,
                goToCreateUserStory = {
                    navController.navigateToCreateTask(type = CommonTaskType.UserStory)
                },
                goToSprint = { id ->
                    navController.navigateToSprintScreen(id)
                },
                updateData = updateData,
                goToUserStory = { id, type, ref ->
                    if (isNewUiUsed) {
                        navController.navigateToUserStory(
                            taskId = id,
                            ref = ref
                        )
                    } else {
                        navController.navigateToCommonTask(id, type, ref)
                    }
                }
            )
        }

        composable<UserStoryDetailsNavDestination> {
            UserStoryDetailsScreen(
                showSnackbar = showSnackbar,
                goBack = {
                    navController.setUpdateDataOnBack()
                    navController.popBackStack()
                },
                goToEditDescription = { description: String ->
                    navController.navigateToWorkItemEditDescription(
                        description = description
                    )
                },
                goToEditTags = {
                    navController.navigateToWorkItemEditTags()
                },
                goToProfile = { creatorId ->
                    navController.navigateToProfileScreen(creatorId)
                },
                goToEditAssignee = {
                    navController.navigateToWorkItemEditTeamMember()
                },
                goToEditWatchers = {
                    navController.navigateToWorkItemEditTeamMember()
                },
                goToEpic = { epicId: Long, ref: Int ->
                    navController.navigateToEpicDetails(epicId, ref)
                },
                goToEditEpics = {
                    navController.navigateToWorkItemEditEpic()
                }
            )
        }

        composable<EpicsNavDestination> { navBackStackEntry ->
            val updateData: Boolean =
                navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
            EpicsScreen(
                showSnackbar = showSnackbarAction,
                goToCreateEpic = {
                    navController.navigateToCreateTask(type = CommonTaskType.Epic)
                },
                updateData = updateData,
                goToEpic = { id, type, ref ->
                    if (isNewUiUsed) {
                        navController.navigateToEpicDetails(
                            epicId = id,
                            ref = ref
                        )
                    } else {
                        navController.navigateToCommonTask(id, type, ref)
                    }
                }
            )
        }

        composable<EpicDetailsNavDestination> { _ ->
            EpicDetailsScreen(
                showSnackbar = showSnackbar,
                goToProfile = { creatorId ->
                    navController.navigateToProfileScreen(creatorId)
                },
                goToEditDescription = { description: String ->
                    navController.navigateToWorkItemEditDescription(
                        description = description
                    )
                },
                goToEditTags = {
                    navController.navigateToWorkItemEditTags()
                },
                goBack = {
                    navController.setUpdateDataOnBack()
                    navController.popBackStack()
                },
                goToEditAssignee = {
                    navController.navigateToWorkItemEditTeamMember()
                },
                goToEditWatchers = {
                    navController.navigateToWorkItemEditTeamMember()
                },
                goToUserStory = { id, _, ref ->
                    navController.navigateToUserStory(
                        taskId = id,
                        ref = ref
                    )
                }
            )
        }

        composable<IssuesNavDestination> { navBackStackEntry ->
            val updateData: Boolean =
                navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
            IssuesScreen(
                showSnackbar = showSnackbarAction,
                goToCreateTask = {
                    navController.navigateToCreateIssue()
                },
                updateData = updateData,
                goToTask = { id, type, ref ->
                    if (isNewUiUsed) {
                        navController.navigateToIssueDetails(
                            taskId = id,
                            ref = ref
                        )
                    } else {
                        navController.navigateToCommonTask(id, type, ref)
                    }
                }
            )
        }

        composable<IssueDetailsNavDestination> { _ ->
            IssueDetailsScreen(
                showSnackbar = showSnackbar,
                goToProfile = { creatorId ->
                    navController.navigateToProfileScreen(creatorId)
                },
                goToEditDescription = { description: String ->
                    navController.navigateToWorkItemEditDescription(
                        description = description
                    )
                },
                goToEditTags = {
                    navController.navigateToWorkItemEditTags()
                },
                goBack = {
                    navController.setUpdateDataOnBack()
                    navController.popBackStack()
                },
                goToEditAssignee = {
                    navController.navigateToWorkItemEditTeamMember()
                },
                goToEditWatchers = {
                    navController.navigateToWorkItemEditTeamMember()
                },
                goToSprints = {
                    navController.navigateToWorkItemEditSprint()
                }
            )
        }

        composable<WorkItemEditSprintNavDestination> {
            WorkItemEditSprintScreen(
                goBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<WorkItemEditDescriptionNavDestination> {
            WorkItemEditDescriptionScreen(
                goBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<WorkItemEditTagsNavDestination> {
            WorkItemEditTagsScreen(
                goBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<WorkItemEditTeamMemberNavDestination> {
            WorkItemEditTeamMemberScreen(
                goBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<WorkItemEditEpicNavDestination> {
            WorkItemEditEpicScreen(
                goBack = {
                    navController.popBackStack()
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
                showSnackbar = showSnackbar,
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
                showSnackbar = showSnackbar,
                goToWikiPage = {
                    navController.navigateToWikiPage(
                        slug = it,
                        popUpToRoute = WikiCreatePageNavDestination
                    )
                }
            )
        }

        composable<WikiPageNavDestination> {
            WikiPageScreen(
                showSnackbar = showSnackbar,
                goToProfile = { userId ->
                    navController.navigateToProfileScreen(userId)
                },
                goBack = navController::popBackStack,
                goToEditDescription = { description: String ->
                    navController.navigateToWorkItemEditDescription(
                        description = description
                    )
                }
            )
        }

        composable<SettingsNavDestination> {
            SettingsScreen(showSnackbar = showSnackbar)
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
                    navController.setUpdateDataOnBack()
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

const val UPDATE_DATA_ON_BACK = "UpdateDataOnBack"

fun NavController.setUpdateDataOnBack() {
    previousBackStackEntry
        ?.savedStateHandle
        ?.set(UPDATE_DATA_ON_BACK, true)
}
