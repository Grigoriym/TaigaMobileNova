@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.scrum.ui.backlog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.ui.TaskFilters
import com.grappim.taigamobile.feature.workitem.domain.WorkItem
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.commonVerticalPadding
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.list.simpleTasksListWithTitle
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ScrumBacklogScreen(
    updateData: Boolean,
    goToCreateUserStory: () -> Unit,
    navigateToTask: (id: Long, type: CommonTaskType, ref: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScrumBacklogViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stories = viewModel.userStories.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.backlog),
                navigationIcon = NavigationIconConfig.Menu,
                actions = buildList {
                    if (state.canAddUserStory) {
                        add(
                            TopBarActionIconButton(
                                drawable = RDrawable.ic_add,
                                contentDescription = "Add User Story",
                                onClick = goToCreateUserStory
                            )
                        )
                    }
                }.toImmutableList()
            )
        )
    }

    LaunchedEffect(updateData) {
        if (updateData) {
            stories.refresh()
        }
    }

    BacklogContent(
        state = state,
        stories = stories,
        filters = state.filters,
        navigateToTask = navigateToTask,
        searchQuery = searchQuery,
        modifier = modifier
    )
}

@Composable
private fun BacklogContent(
    state: ScrumBacklogState,
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
            isFiltersError = state.filtersError.isNotEmpty(),
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
