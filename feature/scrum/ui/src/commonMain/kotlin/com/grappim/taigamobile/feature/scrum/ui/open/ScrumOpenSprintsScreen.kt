package com.grappim.taigamobile.feature.scrum.ui.open

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.feature.scrum.ui.widgets.SprintsListContent
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.open_sprints
import com.grappim.taigamobile.uikit.generated.resources.ic_add
import com.grappim.taigamobile.uikit.state.LocalOfflineState
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScrumOpenSprintsScreen(
    updateData: Boolean,
    goToSprint: (Sprint) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScrumOpenSprintsViewModel = koinViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val openSprints = viewModel.openSprints.collectAsLazyPagingItems()
    val sprintDialogState by viewModel.sprintDialogState.collectAsStateWithLifecycle()
    val isOffline = LocalOfflineState.current

    LaunchedEffect(state.canAddSprint, isOffline) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.open_sprints),
                navigationIcon = NavigationIconConfig.Menu,
                actions = buildList {
                    if (state.canAddSprint) {
                        add(
                            TopBarActionIconButton(
                                enabled = !isOffline,
                                drawable = RDrawable.ic_add,
                                contentDescription = "Add Sprint",
                                onClick = state.onCreateSprintClick
                            )
                        )
                    }
                }.toImmutableList()
            )
        )
    }

    ObserveAsEvents(viewModel.reloadOpenSprints) {
        openSprints.refresh()
    }

    LaunchedEffect(updateData) {
        if (updateData) {
            openSprints.refresh()
        }
    }

    _root_ide_package_.com.grappim.taigamobile.feature.workitem.ui.delegates.sprint.EditSprintDialog(
        state = sprintDialogState,
        onConfirm = state.onCreateSprintConfirm
    )

    SprintsListContent(
        sprints = openSprints,
        goToSprint = goToSprint,
        modifier = modifier
    )
}
