@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.scrum.ui.widgets

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.no_sprints
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.emptystate.EmptyStateAction
import com.grappim.taigamobile.uikit.widgets.emptystate.EmptyStateWidget
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import com.grappim.taigamobile.utils.ui.hasCompletedLoad
import com.grappim.taigamobile.utils.ui.hasError
import com.grappim.taigamobile.utils.ui.isEmpty
import com.grappim.taigamobile.utils.ui.isLoading
import com.grappim.taigamobile.utils.ui.isNotLoading

@Composable
fun SprintsListContentWidget(
    sprints: LazyPagingItems<Sprint>,
    goToSprint: (Sprint) -> Unit,
    modifier: Modifier = Modifier
) {
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        onRefresh = { sprints.refresh() },
        isRefreshing = sprints.isLoading()
    ) {
        when {
            sprints.hasError() && sprints.isEmpty() ->
                ErrorStateWidget(
                    message = sprints.loadState.getErrorMessage(),
                    onRetry = { sprints.refresh() }
                )

            sprints.isEmpty() && sprints.isNotLoading() && sprints.hasCompletedLoad() -> EmptyStateWidget(
                message = NativeText.Resource(RString.no_sprints),
                action = EmptyStateAction(
                    onClick = { sprints.refresh() }
                )
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(
                    count = sprints.itemCount,
                    key = sprints.itemKey { it.id },
                    contentType = sprints.itemContentType()
                ) { index ->
                    val item = sprints[index]
                    if (item != null) {
                        SprintItemWidget(
                            sprint = item,
                            goToSprint = goToSprint
                        )
                    }
                }
            }
        }
    }
}
