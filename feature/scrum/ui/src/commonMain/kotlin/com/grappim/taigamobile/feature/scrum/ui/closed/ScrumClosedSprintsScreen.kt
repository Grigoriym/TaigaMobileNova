package com.grappim.taigamobile.feature.scrum.ui.closed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.feature.scrum.ui.widgets.SprintsListContentWidget
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.closed_sprints
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.buildDesktopRefreshTopBarAction
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScrumClosedSprintsScreen(
    updateData: Boolean,
    goToSprint: (Sprint) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScrumClosedSprintsViewModel = koinViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val closedSprints = viewModel.closedSprints.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.closed_sprints),
                navigationIcon = NavigationIconConfig.Menu,
                actions = listOfNotNull(
                    buildDesktopRefreshTopBarAction(onClick = { closedSprints.refresh() })
                ).toImmutableList()
            )
        )
    }

    LaunchedEffect(updateData) {
        if (updateData) {
            closedSprints.refresh()
        }
    }

    SprintsListContentWidget(
        sprints = closedSprints,
        goToSprint = goToSprint,
        modifier = modifier
    )
}
