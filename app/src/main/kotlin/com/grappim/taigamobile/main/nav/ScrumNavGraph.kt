package com.grappim.taigamobile.main.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.createtask.navigateToCreateTask
import com.grappim.taigamobile.feature.scrum.ui.ScrumBacklogDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumClosedSprintsDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumOpenSprintsDestination
import com.grappim.taigamobile.feature.scrum.ui.backlog.ScrumBacklogScreen
import com.grappim.taigamobile.feature.scrum.ui.closed.ScrumClosedSprintsScreen
import com.grappim.taigamobile.feature.scrum.ui.open.ScrumOpenSprintsScreen
import com.grappim.taigamobile.feature.sprint.ui.navigateToSprintScreen
import com.grappim.taigamobile.feature.userstories.ui.navigateToUserStory
import com.grappim.taigamobile.main.UPDATE_DATA_ON_BACK

fun NavGraphBuilder.scrumNavGraph(navController: NavHostController) {
    composable<ScrumBacklogDestination> { navBackStackEntry ->
        val updateData: Boolean =
            navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
        ScrumBacklogScreen(
            updateData = updateData,
            goToCreateUserStory = {
                navController.navigateToCreateTask(type = CommonTaskType.UserStory)
            },
            navigateToTask = { id, _, ref ->
                navController.navigateToUserStory(
                    userStoryId = id,
                    ref = ref
                )
            }
        )
    }

    composable<ScrumOpenSprintsDestination> { navBackStackEntry ->
        val updateData: Boolean =
            navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
        ScrumOpenSprintsScreen(
            updateData = updateData,
            goToSprint = { sprint ->
                navController.navigateToSprintScreen(sprint.id)
            }
        )
    }

    composable<ScrumClosedSprintsDestination> { navBackStackEntry ->
        val updateData: Boolean =
            navBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK] ?: false
        ScrumClosedSprintsScreen(
            updateData = updateData,
            goToSprint = { sprint ->
                navController.navigateToSprintScreen(sprint.id)
            }
        )
    }
}
