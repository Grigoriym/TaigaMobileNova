@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.kanban.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.ui.FilterModalBottomSheetWidget
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.badge.Badge
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.launch

@Composable
fun KanbanScreen(
    showSnackbar: (NativeText) -> Unit,
    goToTask: (Long, CommonTaskType, Long) -> Unit,
    goToCreateTask: (CommonTaskType, Long, Long?) -> Unit,
    updateData: Boolean = false,
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

    LaunchedEffect(updateData) {
        if (updateData) {
            state.onRefresh()
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
    navigateToCreateTask: (statusId: Long, swimlaneId: Long?) -> Unit = { _, _ -> }
) {
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = state.isLoading,
        onRefresh = state.onRefresh
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            KanbanFilters(
                selected = state.activeFilters,
                data = state.filters,
                onSelect = state.onSelectFilters
            )

            KanbanBoardWidget(
                state = state,
                navigateToStory = navigateToStory,
                navigateToCreateTask = navigateToCreateTask,
                onMoveStory = state.onMoveStory
            )
        }
    }
}

@Composable
private fun KanbanFilters(
    selected: FiltersData,
    data: FiltersData,
    onSelect: (FiltersData) -> Unit,
    modifier: Modifier = Modifier
) {
    val unselectedFilters = data - selected
    val space = 6.dp

    val coroutineScope = rememberCoroutineScope()

    val bottomSheetState = rememberModalBottomSheetState()
    var isBottomSheetVisible by remember { mutableStateOf(false) }

    FilledTonalButton(
        onClick = {
            coroutineScope.launch {
                if (!bottomSheetState.isVisible) {
                    isBottomSheetVisible = true
                }
            }
        },
        enabled = data.assignees.isNotEmpty(),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(RDrawable.ic_filter),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(Modifier.width(space))

            Text(stringResource(RString.show_filters))

            selected.filtersNumber.takeIf { it > 0 }?.let {
                Spacer(Modifier.width(space))
                Badge(it.toString())
            }
        }
    }

    Spacer(Modifier.height(space))

    FilterModalBottomSheetWidget(
        bottomSheetState = bottomSheetState,
        unselectedFilters = unselectedFilters,
        isBottomSheetVisible = isBottomSheetVisible,
        setBottomSheetVisible = { isBottomSheetVisible = it },
        selected = selected,
        onSelect = onSelect,
        filtersError = NativeText.Empty,
        onRetryFilters = {},
        isFiltersLoading = false
    )
}
