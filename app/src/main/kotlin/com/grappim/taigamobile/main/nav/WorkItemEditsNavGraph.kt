package com.grappim.taigamobile.main.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.WorkItemEditDescriptionNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.WorkItemEditDescriptionScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.edittags.WorkItemEditTagsNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.edittags.WorkItemEditTagsScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.epic.WorkItemEditEpicNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.epic.WorkItemEditEpicScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.sprint.WorkItemEditSprintNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.sprint.WorkItemEditSprintScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.WorkItemEditTeamMemberNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.WorkItemEditTeamMemberScreen
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.typeMapOf
import kotlin.reflect.typeOf

fun NavGraphBuilder.workItemEditsNavGraph(showSnackbar: (NativeText) -> Unit, navController: NavHostController) {
    composable<WorkItemEditDescriptionNavDestination>(
        typeMap = typeMapOf(
            listOf(
                typeOf<TaskIdentifier>()
            )
        )
    ) {
        WorkItemEditDescriptionScreen(
            goBack = {
                navController.popBackStack()
            }
        )
    }
    composable<WorkItemEditSprintNavDestination>(
        typeMap = typeMapOf(
            listOf(typeOf<TaskIdentifier>())
        )
    ) {
        WorkItemEditSprintScreen(
            goBack = {
                navController.popBackStack()
            }
        )
    }

    composable<WorkItemEditTagsNavDestination>(
        typeMap = typeMapOf(
            listOf(typeOf<TaskIdentifier>())
        )
    ) {
        WorkItemEditTagsScreen(
            showSnackbar = showSnackbar,
            goBack = {
                navController.popBackStack()
            }
        )
    }

    composable<WorkItemEditTeamMemberNavDestination>(
        typeMap = typeMapOf(
            listOf(typeOf<TaskIdentifier>())
        )
    ) {
        WorkItemEditTeamMemberScreen(
            goBack = {
                navController.popBackStack()
            }
        )
    }

    composable<WorkItemEditEpicNavDestination>(
        typeMap = typeMapOf(
            listOf(typeOf<TaskIdentifier>())
        )
    ) {
        WorkItemEditEpicScreen(
            goBack = {
                navController.popBackStack()
            }
        )
    }
}
