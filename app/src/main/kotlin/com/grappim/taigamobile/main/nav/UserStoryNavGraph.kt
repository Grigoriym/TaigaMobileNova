package com.grappim.taigamobile.main.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.epics.ui.details.navigateToEpicDetails
import com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen
import com.grappim.taigamobile.feature.userstories.ui.UserStoryDetailsNavDestination
import com.grappim.taigamobile.feature.userstories.ui.UserStoryDetailsScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.navigateToWorkItemEditDescription
import com.grappim.taigamobile.feature.workitem.ui.screens.edittags.navigateToWorkItemEditTags
import com.grappim.taigamobile.feature.workitem.ui.screens.epic.navigateToWorkItemEditEpic
import com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.navigateToWorkItemEditTeamMember
import com.grappim.taigamobile.main.setUpdateDataOnBack
import com.grappim.taigamobile.utils.ui.NativeText

fun NavGraphBuilder.userStoryNavGraph(showSnackbar: (NativeText) -> Unit, navController: NavHostController) {
    composable<UserStoryDetailsNavDestination> {
        UserStoryDetailsScreen(
            showSnackbar = showSnackbar,
            goBack = {
                navController.setUpdateDataOnBack()
                navController.popBackStack()
            },
            goToEditDescription = { description: String, id: Long ->
                navController.navigateToWorkItemEditDescription(
                    description = description,
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
                )
            },
            goToEditTags = { userStoryId: Long ->
                navController.navigateToWorkItemEditTags(
                    workItemId = userStoryId,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
                )
            },
            goToProfile = { creatorId ->
                navController.navigateToProfileScreen(creatorId)
            },
            goToEditAssignee = { id: Long ->
                navController.navigateToWorkItemEditTeamMember(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
                )
            },
            goToEditWatchers = { id: Long ->
                navController.navigateToWorkItemEditTeamMember(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
                )
            },
            goToEpic = { epicId: Long, ref: Long ->
                navController.navigateToEpicDetails(epicId, ref)
            },
            goToEditEpics = { userStoryId: Long ->
                navController.navigateToWorkItemEditEpic(
                    workItemId = userStoryId,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
                )
            }
        )
    }
}
