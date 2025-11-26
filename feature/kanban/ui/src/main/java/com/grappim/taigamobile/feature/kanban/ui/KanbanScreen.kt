@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.kanban.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage

@Composable
fun KanbanScreen(
    showSnackbar: (NativeText) -> Unit,
    goToTask: (Long, CommonTaskType, Int) -> Unit,
    goToCreateTask: (CommonTaskType, Long, Long?) -> Unit,
    viewModel: KanbanViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.kanban)
            )
        )
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            showSnackbar(getErrorMessage(state.error!!))
        }
    }

    KanbanScreenContent(
        state = state,
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
    state: KanbanState,
    modifier: Modifier = Modifier,
    navigateToStory: (id: Long, ref: Int) -> Unit = { _, _ -> },
    navigateToCreateTask: (statusId: Long, swinlanaeId: Long?) -> Unit = { _, _ -> }
) {
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = state.isLoading,
        onRefresh = state.onRefresh
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            if (state.error != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // todo create a common error use case solution
                    Button(onClick = {
                        state.onRefresh()
                    }, content = {
                        Text("Refresh")
                    })
                }
            } else {
                KanbanBoardWidget(
                    statusOlds = state.statusOlds,
                    stories = state.stories,
                    team = state.team,
                    swimlaneDTOS = state.swimlaneDTOS,
                    selectSwimlane = state.onSelectSwimlane,
                    selectedSwimlaneDTO = state.selectedSwimlaneDTO,
                    navigateToStory = navigateToStory,
                    navigateToCreateTask = navigateToCreateTask
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KanbanScreenPreview() = TaigaMobileTheme {
    KanbanScreenContent(
        state = KanbanState(
            onRefresh = {},
            onSelectSwimlane = {}
        )
    )
}
