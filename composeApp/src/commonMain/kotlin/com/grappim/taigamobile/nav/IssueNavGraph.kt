package com.grappim.taigamobile.nav

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.navigation.LocalResultBus
import com.grappim.taigamobile.core.navigation.Navigator
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

// Distinct from UpdateDataOnBack to avoid colliding with IssueDetailsNavDestination's own self-refresh listener below.
private data object IssueListUpdateDataOnBack

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
fun EntryProviderScope<NavKey>.issueNavGraph(showSnackbar: (NativeText) -> Unit, navigator: Navigator) {
    entry<IssuesNavDestination>(metadata = ListDetailSceneStrategy.listPane()) {
        var updateData by remember { mutableStateOf(false) }
        ResultEffect<IssueListUpdateDataOnBack> { updateData = true }
        IssuesScreen(
            showSnackbar = showSnackbar,
            goToCreateIssue = {
                navigator.navigateToCreateIssue()
            },
            updateData = updateData,
            goToIssue = { id, ref ->
                navigator.navigateToIssueDetails(
                    issueId = id,
                    ref = ref
                )
            }
        )
    }

    entry<IssueDetailsNavDestination>(metadata = ListDetailSceneStrategy.detailPane()) { route ->
        var updateData by remember { mutableStateOf(false) }
        ResultEffect<UpdateDataOnBack> { updateData = true }
        val resultBus = LocalResultBus.current
        IssueDetailsScreen(
            route = route,
            showSnackbar = showSnackbar,
            updateData = updateData,
            goToProfile = { creatorId ->
                navigator.navigateToProfileScreen(creatorId)
            },
            goToEditDescription = { description: String, id: Long ->
                navigator.navigateToWorkItemEditDescription(
                    description = description,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue),
                    workItemId = id
                )
            },
            goToEditTags = { issueId: Long ->
                navigator.navigateToWorkItemEditTags(
                    workItemId = issueId,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue)
                )
            },
            goBack = {
                resultBus.sendResult(IssueListUpdateDataOnBack)
                navigator.goBack()
            },
            goToEditAssignee = { issueId: Long ->
                navigator.navigateToWorkItemEditTeamMember(
                    workItemId = issueId,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue)
                )
            },
            goToEditWatchers = { issueId: Long ->
                navigator.navigateToWorkItemEditTeamMember(
                    workItemId = issueId,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue)
                )
            },
            goToSprints = { issueId: Long ->
                navigator.navigateToWorkItemEditSprint(
                    workItemId = issueId,
                    taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue)
                )
            },
            goToUserStory = { userStoryId, ref ->
                navigator.navigateToUserStory(userStoryId = userStoryId, ref = ref)
            }
        )
    }
}
