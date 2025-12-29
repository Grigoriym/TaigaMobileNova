package com.grappim.taigamobile.feature.scrum.ui.closed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.feature.scrum.ui.widgets.SprintsListContent
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun ScrumClosedSprintsScreen(
    updateData: Boolean,
    goToSprint: (Sprint) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScrumClosedSprintsViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val closedSprints = viewModel.closedSprints.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.closed_sprints),
                navigationIcon = NavigationIconConfig.Menu
            )
        )
    }

    LaunchedEffect(updateData) {
        if (updateData) {
            closedSprints.refresh()
        }
    }

    SprintsListContent(
        sprints = closedSprints,
        goToSprint = goToSprint,
        modifier = modifier
    )
}
