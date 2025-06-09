package io.eugenethedev.taigamobile.ui.screens.scrum

import androidx.annotation.StringRes
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import io.eugenethedev.taigamobile.R
import io.eugenethedev.taigamobile.core.nav.Routes
import io.eugenethedev.taigamobile.domain.entities.CommonTask
import io.eugenethedev.taigamobile.domain.entities.CommonTaskType
import io.eugenethedev.taigamobile.domain.entities.FiltersData
import io.eugenethedev.taigamobile.domain.entities.Sprint
import io.eugenethedev.taigamobile.ui.components.TasksFiltersWithLazyList
import io.eugenethedev.taigamobile.ui.components.appbars.ClickableAppBar
import io.eugenethedev.taigamobile.ui.components.buttons.PlusButton
import io.eugenethedev.taigamobile.ui.components.containers.ContainerBox
import io.eugenethedev.taigamobile.ui.components.containers.HorizontalTabbedPager
import io.eugenethedev.taigamobile.ui.components.containers.Tab
import io.eugenethedev.taigamobile.ui.components.dialogs.EditSprintDialog
import io.eugenethedev.taigamobile.ui.components.dialogs.LoadingDialog
import io.eugenethedev.taigamobile.ui.components.lists.SimpleTasksListWithTitle
import io.eugenethedev.taigamobile.ui.components.loaders.DotsLoader
import io.eugenethedev.taigamobile.ui.components.texts.NothingToSeeHereText
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.ui.theme.commonVerticalPadding
import io.eugenethedev.taigamobile.ui.theme.mainHorizontalScreenPadding
import io.eugenethedev.taigamobile.ui.utils.LoadingResult
import io.eugenethedev.taigamobile.ui.utils.NavigateToTask
import io.eugenethedev.taigamobile.ui.utils.SubscribeOnError
import io.eugenethedev.taigamobile.ui.utils.navigateToCreateTaskScreen
import io.eugenethedev.taigamobile.ui.utils.navigateToSprint
import io.eugenethedev.taigamobile.ui.utils.navigateToTaskScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


@Composable
fun ScrumScreen(
    navController: NavController,
    showMessage: (message: Int) -> Unit = {},
) {
    val viewModel: ScrumViewModel = viewModel()
    LaunchedEffect(Unit) {
        viewModel.onOpen()
    }

    val projectName by viewModel.projectName.collectAsState()

    val stories = viewModel.stories
    stories.SubscribeOnError {
        showMessage(R.string.common_error_message)
    }

    val openSprints = viewModel.openSprints
    openSprints.SubscribeOnError(showMessage)

    val closedSprints = viewModel.closedSprints
    closedSprints.SubscribeOnError(showMessage)

    val createSprintResult by viewModel.createSprintResult.collectAsState()
    createSprintResult.SubscribeOnError(showMessage)

    val filters by viewModel.filters.collectAsState()
    filters.SubscribeOnError(showMessage)

    val activeFilters by viewModel.activeFilters.collectAsState()

    ScrumScreenContent(
        projectName = projectName,
        onTitleClick = { navController.navigate(Routes.projectsSelector) },
        stories = stories,
        filters = filters.data ?: FiltersData(),
        activeFilters = activeFilters,
        selectFilters = viewModel::selectFilters,
        openSprints = openSprints,
        closedSprints = closedSprints,
        isCreateSprintLoading = createSprintResult is LoadingResult,
        navigateToBoard = {
            navController.navigateToSprint(it.id)
        },
        navigateToTask = navController::navigateToTaskScreen,
        navigateToCreateTask = { navController.navigateToCreateTaskScreen(CommonTaskType.UserStory) },
        createSprint = viewModel::createSprint
    )
}

@Composable
fun ScrumScreenContent(
    projectName: String,
    onTitleClick: () -> Unit = {},
    stories: LazyPagingItems<CommonTask>? = null,
    filters: FiltersData = FiltersData(),
    activeFilters: FiltersData = FiltersData(),
    selectFilters: (FiltersData) -> Unit = {},
    openSprints: LazyPagingItems<Sprint>? = null,
    closedSprints: LazyPagingItems<Sprint>? = null,
    isCreateSprintLoading: Boolean = false,
    navigateToBoard: (Sprint) -> Unit = {},
    navigateToTask: NavigateToTask = { _, _, _ -> },
    navigateToCreateTask: () -> Unit = {},
    createSprint: (name: String, start: LocalDate, end: LocalDate) -> Unit = { _, _, _ -> }
) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.Start
) {
    val tabs = Tabs.entries
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    var isCreateSprintDialogVisible by remember { mutableStateOf(false) }

    ClickableAppBar(
        projectName = projectName,
        actions = {
            PlusButton(
                onClick = when (Tabs.values()[pagerState.currentPage]) {
                    Tabs.Backlog -> navigateToCreateTask
                    Tabs.Sprints -> {
                        { isCreateSprintDialogVisible = true }
                    }
                }
            )
        },
        onTitleClick = onTitleClick
    )

    if (isCreateSprintDialogVisible) {
        EditSprintDialog(
            onConfirm = { name, start, end ->
                createSprint(name, start, end)
                isCreateSprintDialogVisible = false
            },
            onDismiss = { isCreateSprintDialogVisible = false }
        )
    }

    if (isCreateSprintLoading) {
        LoadingDialog()
    }

    HorizontalTabbedPager(
        modifier = Modifier.fillMaxSize(),
        tabs = tabs.toTypedArray(),
        pagerState = pagerState
    ) { page ->
        when (tabs[page]) {
            Tabs.Backlog -> BacklogTabContent(
                stories = stories,
                filters = filters,
                activeFilters = activeFilters,
                selectFilters = selectFilters,
                navigateToTask = navigateToTask
            )

            Tabs.Sprints -> SprintsTabContent(
                openSprints = openSprints,
                closedSprints = closedSprints,
                navigateToBoard = navigateToBoard
            )
        }
    }

}

private enum class Tabs(@StringRes override val titleId: Int) : Tab {
    Backlog(R.string.backlog),
    Sprints(R.string.sprints_title)
}

@Composable
private fun BacklogTabContent(
    stories: LazyPagingItems<CommonTask>?,
    filters: FiltersData = FiltersData(),
    activeFilters: FiltersData = FiltersData(),
    selectFilters: (FiltersData) -> Unit = {},
    navigateToTask: NavigateToTask
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth()
) {
    TasksFiltersWithLazyList(
        filters = filters,
        activeFilters = activeFilters,
        selectFilters = selectFilters
    ) {
        SimpleTasksListWithTitle(
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
    navigateToBoard: (Sprint) -> Unit,
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
            if (openSprints.loadState.refresh is LoadState.Loading || openSprints.loadState.append is LoadState.Loading) {
                DotsLoader()
            }
        }

        item {
            FilledTonalButton(onClick = { isClosedSprintsVisible = !isClosedSprintsVisible }) {
                Text(stringResource(if (isClosedSprintsVisible) R.string.hide_closed_sprints else R.string.show_closed_sprints))
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
                if (closedSprints.loadState.refresh is LoadState.Loading || closedSprints.loadState.append is LoadState.Loading) {
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
private fun SprintItem(
    sprint: Sprint,
    navigateToBoard: (Sprint) -> Unit = {}
) = ContainerBox {
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
                stringResource(R.string.sprint_dates_template).format(
                    sprint.start.format(dateFormatter),
                    sprint.end.format(dateFormatter)
                )
            )

            Row {
                Text(
                    text = stringResource(R.string.stories_count_template).format(sprint.storiesCount),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.width(6.dp))

                if (sprint.isClosed) {
                    Text(
                        text = stringResource(R.string.closed),
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        //TODO the only place I had to comment the code since the usage is internal and I couldn't find
        // anything to change it
        buttonColors().let {
//            val containerColor by it.containerColor(!sprint.isClosed)
//            val contentColor by it.contentColor(!sprint.isClosed)

            Button(
                onClick = { navigateToBoard(sprint) },
                modifier = Modifier.weight(0.3f),
//                colors = buttonColors(
//                    containerColor = containerColor,
//                    contentColor = contentColor
//                )
            ) {
                Text(stringResource(R.string.taskboard))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SprintPreview() = TaigaMobileTheme {
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
fun ScrumScreenPreview() = TaigaMobileTheme {
    ScrumScreenContent("Lol")
}
