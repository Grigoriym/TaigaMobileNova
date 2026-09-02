package com.grappim.taigamobile.nav

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.navigation.LocalResultBus
import com.grappim.taigamobile.core.navigation.Navigator
import com.grappim.taigamobile.core.navigation.sendResult
import com.grappim.taigamobile.feature.epics.ui.details.navigateToEpicDetails
import com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen
import com.grappim.taigamobile.feature.userstories.ui.UserStoryDetailsNavDestination
import com.grappim.taigamobile.feature.userstories.ui.UserStoryDetailsScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.navigateToWorkItemEditDescription
import com.grappim.taigamobile.feature.workitem.ui.screens.edittags.navigateToWorkItemEditTags
import com.grappim.taigamobile.feature.workitem.ui.screens.epic.navigateToWorkItemEditEpic
import com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.navigateToWorkItemEditTeamMember
import com.grappim.taigamobile.main.UpdateDataOnBack
import com.grappim.taigamobile.utils.ui.NativeText

fun EntryProviderScope<NavKey>.userStoryNavGraph(showSnackbar: (NativeText) -> Unit, navigator: Navigator) {
    entry<UserStoryDetailsNavDestination> { route ->
        val resultBus = LocalResultBus.current
        UserStoryDetailsScreen(
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
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
                )
            },
            goToEditTags = { userStoryId: Long ->
                navigator.navigateToWorkItemEditTags(
                    workItemId = userStoryId,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
                )
            },
            goToProfile = { creatorId ->
                navigator.navigateToProfileScreen(creatorId)
            },
            goToEditAssignee = { id: Long ->
                navigator.navigateToWorkItemEditTeamMember(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
                )
            },
            goToEditWatchers = { id: Long ->
                navigator.navigateToWorkItemEditTeamMember(
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
                )
            },
            goToEpic = { epicId: Long, ref: Long ->
                navigator.navigateToEpicDetails(epicId, ref)
            },
            goToEditEpics = { userStoryId: Long ->
                navigator.navigateToWorkItemEditEpic(
                    workItemId = userStoryId,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
                )
            }
        )
    }
}
