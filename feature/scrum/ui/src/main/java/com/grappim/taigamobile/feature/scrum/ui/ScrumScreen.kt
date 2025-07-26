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
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.grappim.taigamobile.core.domain.CommonTask
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.FiltersDataDTO
import com.grappim.taigamobile.core.domain.Sprint
import com.grappim.taigamobile.core.navigation.NavigateToTask
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.commonVerticalPadding
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.container.ContainerBox
import com.grappim.taigamobile.uikit.widgets.container.HorizontalTabbedPager
import com.grappim.taigamobile.uikit.widgets.dialog.EditSprintDialog
import com.grappim.taigamobile.uikit.widgets.dialog.LoadingDialog
import com.grappim.taigamobile.uikit.widgets.filter.TasksFiltersWithLazyList
import com.grappim.taigamobile.uikit.widgets.list.simpleTasksListWithTitle
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoader
import com.grappim.taigamobile.uikit.widgets.text.NothingToSeeHereText
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.LoadingResult
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SubscribeOnError
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ScrumScreen(
    showMessage: (message: Int) -> Unit,
    goToCreateTask: (CommonTaskType) -> Unit,
    goToSprint: (sprintId: Long) -> Unit,
    goToTask: (Long, CommonTaskType, Int) -> Unit,
    viewModel: ScrumViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { state.tabs.size })

    LaunchedEffect(Unit) {
        viewModel.onOpen()

        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.scrum),
                actions = listOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_add,
                        contentDescription = "Add",
                        onClick = {
                            when (ScrumTabs.entries[pagerState.currentPage]) {
                                ScrumTabs.Backlog -> {
                                    goToCreateTask(CommonTaskType.UserStory)
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

    val stories = viewModel.stories.collectAsLazyPagingItems()
    stories.SubscribeOnError(showMessage)

    val openSprints = viewModel.openSprints.collectAsLazyPagingItems()
    openSprints.SubscribeOnError(showMessage)

    val closedSprints = viewModel.closedSprints.collectAsLazyPagingItems()
    closedSprints.SubscribeOnError(showMessage)

    val createSprintResult by viewModel.createSprintResult.collectAsState()
    createSprintResult.SubscribeOnError(showMessage)

    val filters by viewModel.filters.collectAsState()
    filters.SubscribeOnError(showMessage)

    val activeFilters by viewModel.activeFilters.collectAsState()

    ScrumScreenContent(
        state = state,
        pagerState = pagerState,
        stories = stories,
        filters = filters.data ?: FiltersDataDTO(),
        activeFilters = activeFilters,
        selectFilters = viewModel::selectFilters,
        openSprints = openSprints,
        closedSprints = closedSprints,
        isCreateSprintLoading = createSprintResult is LoadingResult,
        navigateToBoard = {
            goToSprint(it.id)
        },
        navigateToTask = goToTask,
        createSprint = viewModel::createSprint
    )
}

@Composable
fun ScrumScreenContent(
    state: ScrumState,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    stories: LazyPagingItems<CommonTask>? = null,
    filters: FiltersDataDTO = FiltersDataDTO(),
    activeFilters: FiltersDataDTO = FiltersDataDTO(),
    selectFilters: (FiltersDataDTO) -> Unit = {},
    openSprints: LazyPagingItems<Sprint>? = null,
    closedSprints: LazyPagingItems<Sprint>? = null,
    isCreateSprintLoading: Boolean = false,
    navigateToBoard: (Sprint) -> Unit = {},
    navigateToTask: NavigateToTask = { _, _, _ -> },
    createSprint: (name: String, start: LocalDate, end: LocalDate) -> Unit = { _, _, _ -> }
) = Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.Start
) {
    if (state.isCreateSprintDialogVisible) {
        EditSprintDialog(
            onConfirm = { name, start, end ->
                createSprint(name, start, end)
                state.setIsCreateSprintDialogVisible(false)
            },
            onDismiss = { state.setIsCreateSprintDialogVisible(false) }
        )
    }

    if (isCreateSprintLoading) {
        LoadingDialog()
    }

    HorizontalTabbedPager(
        modifier = Modifier.fillMaxSize(),
        tabs = state.tabs.toTypedArray(),
        pagerState = pagerState
    ) { page ->
        when (state.tabs[page]) {
            ScrumTabs.Backlog -> BacklogTabContent(
                stories = stories,
                filters = filters,
                activeFilters = activeFilters,
                selectFilters = selectFilters,
                navigateToTask = navigateToTask
            )

            ScrumTabs.Sprints -> SprintsTabContent(
                openSprints = openSprints,
                closedSprints = closedSprints,
                navigateToBoard = navigateToBoard
            )
        }
    }
}

@Composable
private fun BacklogTabContent(
    navigateToTask: NavigateToTask,
    stories: LazyPagingItems<CommonTask>?,
    modifier: Modifier = Modifier,
    filters: FiltersDataDTO = FiltersDataDTO(),
    activeFilters: FiltersDataDTO = FiltersDataDTO(),
    selectFilters: (FiltersDataDTO) -> Unit = {}
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier.fillMaxWidth()
) {
    TasksFiltersWithLazyList(
        filters = filters,
        activeFilters = activeFilters,
        selectFilters = selectFilters
    ) {
        simpleTasksListWithTitle(
            commonTasksLazy = stories,
            keysHash = activeFilters.hashCode(),
            navigateToTask = navigateToTask,
            horizontalPadding = mainHorizontalScreenPadding,
            bottomPadding = commonVerticalPadding
        )
    }
}

@Composable
private fun SprintsTabContent(
    openSprints: LazyPagingItems<Sprint>?,
    closedSprints: LazyPagingItems<Sprint>?,
    navigateToBoard: (Sprint) -> Unit
) {
    if (openSprints == null || closedSprints == null) return

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
                DotsLoader()
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
                    DotsLoader()
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
private fun SprintItem(sprint: Sprint, navigateToBoard: (Sprint) -> Unit = {}) = ContainerBox {
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

        // TODO the only place I had to comment the code since the usage is internal and I couldn't find
        // anything to change it
        buttonColors().let {
//            val containerColor by it.containerColor(!sprint.isClosed)
//            val contentColor by it.contentColor(!sprint.isClosed)

            Button(
                onClick = { navigateToBoard(sprint) },
                modifier = Modifier.weight(0.3f)
//                colors = buttonColors(
//                    containerColor = containerColor,
//                    contentColor = contentColor
//                )
            ) {
                Text(stringResource(RString.taskboard))
            }
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
        pagerState = rememberPagerState { 2 }
    )
}
