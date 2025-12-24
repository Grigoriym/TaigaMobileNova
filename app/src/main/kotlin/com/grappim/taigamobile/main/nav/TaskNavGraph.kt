package com.grappim.taigamobile.main.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen
import com.grappim.taigamobile.feature.tasks.ui.TaskDetailsNavDestination
import com.grappim.taigamobile.feature.tasks.ui.TaskDetailsScreen
import com.grappim.taigamobile.feature.userstories.ui.navigateToUserStory
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.navigateToWorkItemEditDescription
import com.grappim.taigamobile.feature.workitem.ui.screens.edittags.navigateToWorkItemEditTags
import com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.navigateToWorkItemEditTeamMember
import com.grappim.taigamobile.main.setUpdateDataOnBack
import com.grappim.taigamobile.utils.ui.NativeText

fun NavGraphBuilder.taskNavGraph(showSnackbar: (NativeText) -> Unit, navController: NavHostController) {
    composable<TaskDetailsNavDestination> {
        TaskDetailsScreen(
            showSnackbar = showSnackbar,
            goBack = {
                navController.setUpdateDataOnBack()
                navController.popBackStack()
            },
            goToEditDescription = { description: String, id: Long ->
                navController.navigateToWorkItemEditDescription(
                    description = description,
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Task)
                )
            },
            goToEditTags = { id: Long ->
                navController.navigateToWorkItemEditTags(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Task)
                )
            },
            goToProfile = { creatorId ->
                navController.navigateToProfileScreen(creatorId)
            },
            goToEditAssignee = { id: Long ->
                navController.navigateToWorkItemEditTeamMember(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Task)
                )
            },
            goToEditWatchers = { id: Long ->
                navController.navigateToWorkItemEditTeamMember(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Task)
                )
            },
            goToUserStory = { id, ref ->
                navController.navigateToUserStory(
                    userStoryId = id,
                    ref = ref,
                    popUpToRoute = TaskDetailsNavDestination::class
                )
            }
        )
    }
}
