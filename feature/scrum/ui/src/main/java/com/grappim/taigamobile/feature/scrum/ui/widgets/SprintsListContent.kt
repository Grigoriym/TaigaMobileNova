@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.scrum.ui.widgets

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoaderWidget
import com.grappim.taigamobile.uikit.widgets.text.NothingToSeeHereText

@Composable
fun SprintsListContent(sprints: LazyPagingItems<Sprint>, goToSprint: (Sprint) -> Unit, modifier: Modifier = Modifier) {
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        onRefresh = { sprints.refresh() },
        isRefreshing = sprints.loadState.refresh is LoadState.Loading
    ) {
        LazyColumn(
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
                    SprintItem(
                        sprint = item,
                        goToSprint = goToSprint
                    )
                }
            }

            item {
                if (sprints.loadState.append is LoadState.Loading) {
                    DotsLoaderWidget()
                }
            }

            item {
                if (sprints.itemCount == 0) {
                    NothingToSeeHereText()
                }

                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}
