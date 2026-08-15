package com.grappim.taigamobile.nav

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.navigation.LocalResultBus
import com.grappim.taigamobile.core.navigation.ResultEffect
import com.grappim.taigamobile.core.navigation.sendResult
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
import com.grappim.taigamobile.main.UpdateDataOnBack
import com.grappim.taigamobile.utils.ui.NativeText

fun NavGraphBuilder.issueNavGraph(showSnackbar: (NativeText) -> Unit, navController: NavHostController) {
    composable<IssuesNavDestination> {
        var updateData by remember { mutableStateOf(false) }
        ResultEffect<UpdateDataOnBack> { updateData = true }
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
        var updateData by remember { mutableStateOf(false) }
        ResultEffect<UpdateDataOnBack> { updateData = true }
        val resultBus = LocalResultBus.current
        IssueDetailsScreen(
            route = navBackStackEntry.toRoute(),
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
                resultBus.sendResult(UpdateDataOnBack)
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
