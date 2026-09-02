package com.grappim.taigamobile.nav

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.navigation.LocalResultBus
import com.grappim.taigamobile.core.navigation.Navigator
import com.grappim.taigamobile.core.navigation.sendResult
import com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen
import com.grappim.taigamobile.feature.tasks.ui.TaskDetailsNavDestination
import com.grappim.taigamobile.feature.tasks.ui.TaskDetailsScreen
import com.grappim.taigamobile.feature.userstories.ui.navigateToUserStory
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.navigateToWorkItemEditDescription
import com.grappim.taigamobile.feature.workitem.ui.screens.edittags.navigateToWorkItemEditTags
import com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.navigateToWorkItemEditTeamMember
import com.grappim.taigamobile.main.UpdateDataOnBack
import com.grappim.taigamobile.utils.ui.NativeText

fun EntryProviderScope<NavKey>.taskNavGraph(showSnackbar: (NativeText) -> Unit, navigator: Navigator) {
    entry<TaskDetailsNavDestination> { route ->
        val resultBus = LocalResultBus.current
        TaskDetailsScreen(
            route = route,
            showSnackbar = showSnackbar,
            goBack = {
                resultBus.sendResult(UpdateDataOnBack)
                navigator.goBack()
            },
            goToEditDescription = { description: String, id: Long ->
                navigator.navigateToWorkItemEditDescription(
                    description = description,
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Task)
                )
            },
            goToEditTags = { id: Long ->
                navigator.navigateToWorkItemEditTags(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Task)
                )
            },
            goToProfile = { creatorId ->
                navigator.navigateToProfileScreen(creatorId)
            },
            goToEditAssignee = { id: Long ->
                navigator.navigateToWorkItemEditTeamMember(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Task)
                )
            },
            goToEditWatchers = { id: Long ->
                navigator.navigateToWorkItemEditTeamMember(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Task)
                )
            },
            goToUserStory = { id, ref ->
                navigator.navigateToUserStory(
                    userStoryId = id,
                    ref = ref,
                    replaceCurrent = true
                )
            }
        )
    }
}
