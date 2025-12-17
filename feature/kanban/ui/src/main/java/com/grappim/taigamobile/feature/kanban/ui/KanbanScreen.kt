@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.kanban.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun KanbanScreen(
    showSnackbar: (NativeText) -> Unit,
    goToTask: (Long, CommonTaskType, Long) -> Unit,
    goToCreateTask: (CommonTaskType, Long, Long?) -> Unit,
    viewModel: KanbanViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.kanban),
                navigationIcon = NavigationIconConfig.Menu
            )
        )
    }

    LaunchedEffect(state.error) {
        if (state.error.isNotEmpty()) {
            showSnackbar(state.error)
        }
    }

    if (state.error.isEmpty()) {
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
    } else {
        ErrorStateWidget(
            modifier = Modifier.fillMaxSize(),
            message = state.error,
            onRetry = {
                state.onRefresh()
            }
        )
    }
}

@Composable
fun KanbanScreenContent(
    state: KanbanState,
    modifier: Modifier = Modifier,
    navigateToStory: (id: Long, ref: Long) -> Unit = { _, _ -> },
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
            KanbanBoardWidget(
                statuses = state.statuses,
                stories = state.stories,
                teamMembers = state.teamMembers,
                swimlanes = state.swimlanes,
                selectSwimlane = state.onSelectSwimlane,
                selectedSwimlane = state.selectedSwimlane,
                navigateToStory = navigateToStory,
                navigateToCreateTask = navigateToCreateTask
            )
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
