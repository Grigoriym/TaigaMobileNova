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
import com.grappim.taigamobile.createtask.navigateToCreateTask
import com.grappim.taigamobile.feature.epics.ui.details.EpicDetailsNavDestination
import com.grappim.taigamobile.feature.epics.ui.details.EpicDetailsScreen
import com.grappim.taigamobile.feature.epics.ui.details.navigateToEpicDetails
import com.grappim.taigamobile.feature.epics.ui.list.EpicsNavDestination
import com.grappim.taigamobile.feature.epics.ui.list.EpicsScreen
import com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen
import com.grappim.taigamobile.feature.userstories.ui.navigateToUserStory
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.navigateToWorkItemEditDescription
import com.grappim.taigamobile.feature.workitem.ui.screens.edittags.navigateToWorkItemEditTags
import com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.navigateToWorkItemEditTeamMember
import com.grappim.taigamobile.main.UpdateDataOnBack
import com.grappim.taigamobile.utils.ui.NativeText

fun NavGraphBuilder.epicNavGraph(showSnackbar: (NativeText) -> Unit, navController: NavHostController) {
    composable<EpicsNavDestination> {
        var updateData by remember { mutableStateOf(false) }
        ResultEffect<UpdateDataOnBack> { updateData = true }
        EpicsScreen(
            showSnackbar = showSnackbar,
            goToCreateEpic = {
                navController.navigateToCreateTask(type = CommonTaskType.Epic)
            },
            updateData = updateData,
            goToEpic = { id, _, ref ->
                navController.navigateToEpicDetails(
                    epicId = id,
                    ref = ref
                )
            }
        )
    }

    composable<EpicDetailsNavDestination> { navBackStackEntry ->
        var updateData by remember { mutableStateOf(false) }
        ResultEffect<UpdateDataOnBack> { updateData = true }
        val resultBus = LocalResultBus.current
        EpicDetailsScreen(
            route = navBackStackEntry.toRoute(),
            showSnackbar = showSnackbar,
            updateData = updateData,
            goToProfile = { creatorId ->
                navController.navigateToProfileScreen(creatorId)
            },
            goToEditDescription = { description: String, id: Long ->
                navController.navigateToWorkItemEditDescription(
                    description = description,
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Epic)
                )
            },
            goToEditTags = { id ->
                navController.navigateToWorkItemEditTags(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Epic)
                )
            },
            goBack = {
                resultBus.sendResult(UpdateDataOnBack)
                navController.popBackStack()
            },
            goToEditAssignee = { id ->
                navController.navigateToWorkItemEditTeamMember(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Epic)
                )
            },
            goToEditWatchers = { id ->
                navController.navigateToWorkItemEditTeamMember(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Epic)
                )
            },
            goToUserStory = { id, _, ref ->
                navController.navigateToUserStory(
                    userStoryId = id,
                    ref = ref
                )
            }
        )
    }
}
