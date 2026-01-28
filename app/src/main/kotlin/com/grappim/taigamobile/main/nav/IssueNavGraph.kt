package com.grappim.taigamobile.main.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.createtask.navigateToCreateIssue
import com.grappim.taigamobile.feature.issues.ui.details.IssueDetailsNavDestination
import com.grappim.taigamobile.feature.issues.ui.details.IssueDetailsScreen
import com.grappim.taigamobile.feature.issues.ui.details.navigateToIssueDetails
import com.grappim.taigamobile.feature.issues.ui.list.IssuesNavDestination
import com.grappim.taigamobile.feature.issues.ui.list.IssuesScreen
import com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen
import com.grappim.taigamobile.feature.userstories.ui.navigateToUserStory
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.navigateToWorkItemEditDescription
import com.grappim.taigamobile.feature.workitem.ui.screens.edittags.navigateToWorkItemEditTags
import com.grappim.taigamobile.feature.workitem.ui.screens.sprint.navigateToWorkItemEditSprint
import com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.navigateToWorkItemEditTeamMember
import com.grappim.taigamobile.main.UPDATE_DATA_ON_BACK
import com.grappim.taigamobile.main.setUpdateDataOnBack
import com.grappim.taigamobile.utils.ui.NativeText

fun NavGraphBuilder.issueNavGraph(showSnackbar: (NativeText) -> Unit, navController: NavHostController) {
    composable<IssuesNavDestination> { navBackStackEntry ->
        val updateData: Boolean =
            navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
        IssuesScreen(
            showSnackbar = showSnackbar,
            goToCreateIssue = {
                navController.navigateToCreateIssue()
            },
            updateData = updateData,
            goToIssue = { id, ref ->
                navController.navigateToIssueDetails(
                    issueId = id,
                    ref = ref
                )
            }
        )
    }

    composable<IssueDetailsNavDestination> { navBackStackEntry ->
        val updateData: Boolean =
            navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
        IssueDetailsScreen(
            showSnackbar = showSnackbar,
            updateData = updateData,
            goToProfile = { creatorId ->
                navController.navigateToProfileScreen(creatorId)
            },
            goToEditDescription = { description: String, id: Long ->
                navController.navigateToWorkItemEditDescription(
                    description = description,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue),
                    workItemId = id
                )
            },
            goToEditTags = { issueId: Long ->
                navController.navigateToWorkItemEditTags(
                    workItemId = issueId,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue)
                )
            },
            goBack = {
                navController.setUpdateDataOnBack()
                navController.popBackStack()
            },
            goToEditAssignee = { issueId: Long ->
                navController.navigateToWorkItemEditTeamMember(
                    workItemId = issueId,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue)
                )
            },
            goToEditWatchers = { issueId: Long ->
                navController.navigateToWorkItemEditTeamMember(
                    workItemId = issueId,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue)
                )
            },
            goToSprints = { issueId: Long ->
                navController.navigateToWorkItemEditSprint(
                    workItemId = issueId,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue)
                )
            },
            goToUserStory = { userStoryId, ref ->
                navController.navigateToUserStory(userStoryId = userStoryId, ref = ref)
            }
        )
    }
}
