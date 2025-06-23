package com.grappim.taigamobile.feature.kanban.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.grappim.taigamobile.core.domain.CommonTaskExtended
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.Status
import com.grappim.taigamobile.core.domain.Swimlane
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoader
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.LoadingResult
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SubscribeOnError

@Composable
fun KanbanScreen(
    showMessage: (message: Int) -> Unit,
    goToTask: (Long, CommonTaskType, Int) -> Unit,
    goToCreateTask: (CommonTaskType, Long, Long?) -> Unit,
    viewModel: KanbanViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    LaunchedEffect(Unit) {
        viewModel.onOpen()

        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.kanban)
            )
        )
    }

    val swimlanes by viewModel.swimlanes.collectAsState()
    swimlanes.SubscribeOnError(showMessage)

    val statuses by viewModel.statuses.collectAsState()
    statuses.SubscribeOnError(showMessage)

    val team by viewModel.team.collectAsState()
    team.SubscribeOnError(showMessage)

    val stories by viewModel.stories.collectAsState()
    stories.SubscribeOnError(showMessage)

    val selectedSwimlane by viewModel.selectedSwimlane.collectAsState()

    KanbanScreenContent(
        isLoading = listOf(swimlanes, team, stories).any { it is LoadingResult },
        statuses = statuses.data.orEmpty(),
        stories = stories.data.orEmpty(),
        team = team.data.orEmpty(),
        swimlanes = swimlanes.data.orEmpty(),
        selectSwimlane = viewModel::selectSwimlane,
        selectedSwimlane = selectedSwimlane,
        navigateToStory = { id, ref ->
            goToTask(
                id,
                CommonTaskType.UserStory,
                ref
            )
        },
        navigateToCreateTask = { statusId, swimlaneId ->
            goToCreateTask(
                CommonTaskType.UserStory,
                statusId,
                swimlaneId
            )
        }
    )
}

@Composable
fun KanbanScreenContent(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    statuses: List<Status> = emptyList(),
    stories: List<CommonTaskExtended> = emptyList(),
    team: List<User> = emptyList(),
    swimlanes: List<Swimlane?> = emptyList(),
    selectSwimlane: (Swimlane?) -> Unit = {},
    selectedSwimlane: Swimlane? = null,
    navigateToStory: (id: Long, ref: Int) -> Unit = { _, _ -> },
    navigateToCreateTask: (statusId: Long, swinlanaeId: Long?) -> Unit = { _, _ -> }
) = Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.Start
) {
    if (isLoading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularLoader()
        }
    } else {
        KanbanBoardWidget(
            statuses = statuses,
            stories = stories,
            team = team,
            swimlanes = swimlanes,
            selectSwimlane = selectSwimlane,
            selectedSwimlane = selectedSwimlane,
            navigateToStory = navigateToStory,
            navigateToCreateTask = navigateToCreateTask
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KanbanScreenPreview() = TaigaMobileTheme {
    KanbanScreenContent()
}
