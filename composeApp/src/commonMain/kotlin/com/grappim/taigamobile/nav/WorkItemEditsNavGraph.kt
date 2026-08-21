package com.grappim.taigamobile.nav

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
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

fun EntryProviderScope<NavKey>.workItemEditsNavGraph(showSnackbar: (NativeText) -> Unit, navigator: Navigator) {
    entry<WorkItemEditDescriptionNavDestination> { route ->
        WorkItemEditDescriptionScreen(
            route = route,
            goBack = {
                navigator.goBack()
            }
        )
    }
    entry<WorkItemEditSprintNavDestination> { route ->
        WorkItemEditSprintScreen(
            route = route,
            goBack = {
                navigator.goBack()
            }
        )
    }

    entry<WorkItemEditTagsNavDestination> { route ->
        WorkItemEditTagsScreen(
            route = route,
            showSnackbar = showSnackbar,
            goBack = {
                navigator.goBack()
            }
        )
    }

    entry<WorkItemEditTeamMemberNavDestination> { route ->
        WorkItemEditTeamMemberScreen(
            route = route,
            goBack = {
                navigator.goBack()
            }
        )
    }

    entry<WorkItemEditEpicNavDestination> { route ->
        WorkItemEditEpicScreen(
            route = route,
            goBack = {
                navigator.goBack()
            }
        )
    }
}
