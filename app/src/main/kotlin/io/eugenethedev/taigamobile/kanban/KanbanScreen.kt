package io.eugenethedev.taigamobile.kanban

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
import com.grappim.taigamobile.R
import io.eugenethedev.taigamobile.core.ui.NativeText
import io.eugenethedev.taigamobile.domain.entities.CommonTaskExtended
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.domain.entities.Status
import io.eugenethedev.taigamobile.domain.entities.Swimlane
import io.eugenethedev.taigamobile.domain.entities.User
import io.eugenethedev.taigamobile.main.topbar.LocalTopBarConfig
import io.eugenethedev.taigamobile.main.topbar.TopBarConfig
import io.eugenethedev.taigamobile.ui.components.loaders.CircularLoader
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.ui.utils.LoadingResult
import io.eugenethedev.taigamobile.ui.utils.SubscribeOnError

@Composable
fun KanbanScreen(
    viewModel: KanbanViewModel = hiltViewModel(),
    showMessage: (message: Int) -> Unit,
    goToTask: (Long, CommonTaskType, Int) -> Unit,
    goToCreateTask: (CommonTaskType, Long, Long?) -> Unit
) {
    val topBarController = LocalTopBarConfig.current
    LaunchedEffect(Unit) {
        viewModel.onOpen()

        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(R.string.kanban),
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
    modifier = Modifier.fillMaxSize(),
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
        KanbanBoard(
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
fun KanbanScreenPreview() = TaigaMobileTheme {
    KanbanScreenContent()
}
