package com.grappim.taigamobile.main.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
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
import com.grappim.taigamobile.main.UPDATE_DATA_ON_BACK
import com.grappim.taigamobile.main.setUpdateDataOnBack
import com.grappim.taigamobile.utils.ui.NativeText

fun NavGraphBuilder.epicNavGraph(
    showSnackbar: (NativeText) -> Unit,
    showSnackbarAction: (message: NativeText, actionLabel: String?) -> Unit,
    navController: NavHostController
) {
    composable<EpicsNavDestination> { navBackStackEntry ->
        val updateData: Boolean =
            navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
        EpicsScreen(
            showSnackbar = showSnackbarAction,
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

    composable<EpicDetailsNavDestination> { _ ->
        EpicDetailsScreen(
            showSnackbar = showSnackbar,
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
                navController.setUpdateDataOnBack()
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
