@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.scrum.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.ui.TaskFilters
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.commonVerticalPadding
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.container.ContainerBoxWidget
import com.grappim.taigamobile.uikit.widgets.container.HorizontalTabbedPagerWidget
import com.grappim.taigamobile.uikit.widgets.dialog.EditSprintDialog
import com.grappim.taigamobile.uikit.widgets.dialog.LoadingDialog
import com.grappim.taigamobile.uikit.widgets.list.simpleTasksListWithTitle
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoaderWidget
import com.grappim.taigamobile.uikit.widgets.text.NothingToSeeHereText
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import com.grappim.taigamobile.utils.ui.getPagingPreviewItems
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ScrumScreen(
    showSnackbar: (NativeText) -> Unit,
    goToCreateUserStory: () -> Unit,
    goToSprint: (sprintId: Long) -> Unit,
    goToUserStory: (Long, CommonTaskType, Long) -> Unit,
    updateData: Boolean,
    viewModel: ScrumViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { state.tabs.size })
    val userStories = viewModel.userStories.collectAsLazyPagingItems()
    val openSprints = viewModel.openSprints.collectAsLazyPagingItems()
    val closedSprints = viewModel.closedSprints.collectAsLazyPagingItems()
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.scrum),
                navigationIcon = NavigationIconConfig.Menu,
                actions = persistentListOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_add,
                        contentDescription = "Add",
                        onClick = {
                            when (ScrumTabs.entries[pagerState.currentPage]) {
                                ScrumTabs.Backlog -> {
                                    goToCreateUserStory()
                                }

                                ScrumTabs.Sprints -> {
                                    state.setIsCreateSprintDialogVisible(true)
                                }
                            }
                        }
                    )
                )
            )
        )
    }

    LaunchedEffect(updateData) {
        if (updateData) {
            userStories.refresh()
        }
    }

    ObserveAsEvents(viewModel.snackBarMessage) { snackbarMessage ->
        if (snackbarMessage !is NativeText.Empty) {
            showSnackbar(snackbarMessage)
        }
    }

    LaunchedEffect(userStories.loadState.hasError) {
        if (userStories.loadState.hasError) {
            showSnackbar(
                NativeText.Resource(RString.error_loading_user_stories)
            )
        }
    }

    LaunchedEffect(openSprints.loadState.hasError) {
        if (openSprints.loadState.hasError) {
            showSnackbar(
                NativeText.Resource(RString.error_loading_open_user_stories)
            )
        }
    }

    LaunchedEffect(closedSprints.loadState.hasError) {
        if (closedSprints.loadState.hasError) {
            showSnackbar(
                NativeText.Resource(RString.error_loading_closed_stories)
            )
        }
    }

    if (state.isCreateSprintDialogVisible) {
        EditSprintDialog(
            onConfirm = { name, start, end ->
                state.onCreateSprint(name, start, end)
            },
            onDismiss = { state.setIsCreateSprintDialogVisible(false) }
        )
    }

    if (state.loading) {
        LoadingDialog()
    }

    ScrumScreenContent(
        state = state,
        pagerState = pagerState,
        stories = userStories,
        searchQuery = searchQuery,
        filters = filters,
        openSprints = openSprints,
        closedSprints = closedSprints,
        navigateToBoard = {
            goToSprint(it.id)
        },
        navigateToTask = goToUserStory
    )
}

@Composable
fun ScrumScreenContent(
    state: ScrumState,
    pagerState: PagerState,
    stories: LazyPagingItems<WorkItem>,
    openSprints: LazyPagingItems<Sprint>,
    closedSprints: LazyPagingItems<Sprint>,
    navigateToTask: (id: Long, type: CommonTaskType, ref: Long) -> Unit,
    modifier: Modifier = Modifier,
    filters: FiltersData = FiltersData(),
    searchQuery: String = "",
    navigateToBoard: (Sprint) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        HorizontalTabbedPagerWidget(
            modifier = Modifier.fillMaxSize(),
            tabs = state.tabs.toTypedArray(),
            pagerState = pagerState
        ) { page ->
            when (state.tabs[page]) {
                ScrumTabs.Backlog -> BacklogTabContent(
                    state = state,
                    stories = stories,
                    filters = filters,
                    navigateToTask = navigateToTask,
                    searchQuery = searchQuery
                )

                ScrumTabs.Sprints -> SprintsTabContent(
                    openSprints = openSprints,
                    closedSprints = closedSprints,
                    navigateToBoard = navigateToBoard
                )
            }
        }
    }
}

@Composable
private fun BacklogTabContent(
    state: ScrumState,
    navigateToTask: (id: Long, type: CommonTaskType, ref: Long) -> Unit,
    stories: LazyPagingItems<WorkItem>,
    modifier: Modifier = Modifier,
    filters: FiltersData = FiltersData(),
    searchQuery: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        TaskFilters(
            selected = state.activeFilters,
            onSelect = state.onSelectFilters,
            data = filters,
            isFiltersError = state.isFiltersError,
            onRetryFilters = state.retryLoadFilters,
            isFiltersLoading = state.isFiltersLoading,
            searchQuery = searchQuery,
            setSearchQuery = state.onSetSearchQuery
        )

        PullToRefreshBox(
            modifier = Modifier.fillMaxSize(),
            onRefresh = {
                stories.refresh()
                state.retryLoadFilters()
            },
            isRefreshing = stories.loadState.refresh is LoadState.Loading || state.isFiltersLoading
        ) {
            when {
                stories.loadState.hasError && stories.itemCount == 0 -> {
                    ErrorStateWidget(
                        message = NativeText.Resource(RString.error_loading_issues),
                        onRetry = { stories.refresh() }
                    )
                }

                else -> {
                    LazyColumn {
                        simpleTasksListWithTitle(
                            commonTasksLazy = stories,
                            keysHash = state.activeFilters.hashCode(),
                            navigateToTask = navigateToTask,
                            horizontalPadding = mainHorizontalScreenPadding,
                            bottomPadding = commonVerticalPadding
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SprintsTabContent(
    openSprints: LazyPagingItems<Sprint>,
    closedSprints: LazyPagingItems<Sprint>,
    navigateToBoard: (Sprint) -> Unit
) {
    var isClosedSprintsVisible by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(
            count = openSprints.itemCount,
            key = openSprints.itemKey {
                it.id
            }
        ) { index ->
            val item = openSprints[index]
            if (item != null) {
                SprintItem(
                    sprint = item,
                    navigateToBoard = navigateToBoard
                )
            }
        }

        item {
            if (openSprints.loadState.refresh is LoadState.Loading ||
                openSprints.loadState.append is LoadState.Loading
            ) {
                DotsLoaderWidget()
            }
        }

        item {
            FilledTonalButton(onClick = { isClosedSprintsVisible = !isClosedSprintsVisible }) {
                Text(
                    stringResource(
                        if (isClosedSprintsVisible) {
                            RString.hide_closed_sprints
                        } else {
                            RString.show_closed_sprints
                        }
                    )
                )
            }
        }

        if (isClosedSprintsVisible) {
            items(
                count = closedSprints.itemCount,
                key = closedSprints.itemKey {
                    it.id
                },
                contentType = closedSprints.itemContentType()
            ) { index ->
                val item = closedSprints[index]
                if (item != null) {
                    SprintItem(
                        sprint = item,
                        navigateToBoard = navigateToBoard
                    )
                }
            }

            item {
                if (closedSprints.loadState.refresh is LoadState.Loading ||
                    closedSprints.loadState.append is LoadState.Loading
                ) {
                    DotsLoaderWidget()
                }
            }
        }

        item {
            if (openSprints.itemCount == 0 && closedSprints.itemCount == 0) {
                NothingToSeeHereText()
            }

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun SprintItem(sprint: Sprint, navigateToBoard: (Sprint) -> Unit = {}) = ContainerBoxWidget {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.weight(0.7f)) {
            Text(
                text = sprint.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                stringResource(RString.sprint_dates_template).format(
                    sprint.start.format(dateFormatter),
                    sprint.end.format(dateFormatter)
                )
            )

            Row {
                Text(
                    text = stringResource(
                        RString.stories_count_template
                    ).format(sprint.storiesCount),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.width(6.dp))

                if (sprint.isClosed) {
                    Text(
                        text = stringResource(RString.closed),
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        Button(
            onClick = { navigateToBoard(sprint) },
            modifier = Modifier.weight(0.3f)
        ) {
            Text(stringResource(RString.taskboard))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SprintPreview() = TaigaMobileTheme {
    SprintItem(
        Sprint(
            id = 0L,
            name = "1 sprint",
            order = 0,
            start = LocalDate.now(),
            end = LocalDate.now(),
            storiesCount = 4,
            isClosed = true
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ScrumScreenPreview() = TaigaMobileTheme {
    ScrumScreenContent(
        state = ScrumState(
            setIsCreateSprintDialogVisible = {}
        ),
        pagerState = rememberPagerState { 2 },
        stories = getPagingPreviewItems(),
        navigateToTask = { _, _, _ -> },
        openSprints = getPagingPreviewItems(),
        closedSprints = getPagingPreviewItems()
    )
}
