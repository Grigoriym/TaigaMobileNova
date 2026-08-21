package com.grappim.taigamobile.nav

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.navigation.Navigator
import com.grappim.taigamobile.core.navigation.ResultEffect
import com.grappim.taigamobile.createtask.navigateToCreateTask
import com.grappim.taigamobile.feature.scrum.ui.ScrumBacklogDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumClosedSprintsDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumOpenSprintsDestination
import com.grappim.taigamobile.feature.scrum.ui.backlog.ScrumBacklogScreen
import com.grappim.taigamobile.feature.scrum.ui.closed.ScrumClosedSprintsScreen
import com.grappim.taigamobile.feature.scrum.ui.open.ScrumOpenSprintsScreen
import com.grappim.taigamobile.feature.sprint.ui.navigateToSprintScreen
import com.grappim.taigamobile.feature.userstories.ui.navigateToUserStory
import com.grappim.taigamobile.main.UpdateDataOnBack

fun EntryProviderScope<NavKey>.scrumNavGraph(navigator: Navigator) {
    entry<ScrumBacklogDestination> {
        var updateData by remember { mutableStateOf(false) }
        ResultEffect<UpdateDataOnBack> { updateData = true }
        ScrumBacklogScreen(
            updateData = updateData,
            goToCreateUserStory = {
                navigator.navigateToCreateTask(type = CommonTaskType.UserStory)
            },
            navigateToTask = { id, _, ref ->
                navigator.navigateToUserStory(
                    userStoryId = id,
                    ref = ref
                )
            }
        )
    }

    entry<ScrumOpenSprintsDestination> {
        var updateData by remember { mutableStateOf(false) }
        ResultEffect<UpdateDataOnBack> { updateData = true }
        ScrumOpenSprintsScreen(
            updateData = updateData,
            goToSprint = { sprint ->
                navigator.navigateToSprintScreen(sprint.id)
            }
        )
    }

    entry<ScrumClosedSprintsDestination> {
        var updateData by remember { mutableStateOf(false) }
        ResultEffect<UpdateDataOnBack> { updateData = true }
        ScrumClosedSprintsScreen(
            updateData = updateData,
            goToSprint = { sprint ->
                navigator.navigateToSprintScreen(sprint.id)
            }
        )
    }
}
