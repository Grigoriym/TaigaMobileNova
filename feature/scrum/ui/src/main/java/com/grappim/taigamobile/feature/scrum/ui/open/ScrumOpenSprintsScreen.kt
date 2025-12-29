package com.grappim.taigamobile.feature.scrum.ui.open

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.feature.scrum.ui.widgets.SprintsListContent
import com.grappim.taigamobile.feature.sprint.domain.Sprint
import com.grappim.taigamobile.feature.workitem.ui.delegates.sprint.EditSprintDialog
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf

@Composable
fun ScrumOpenSprintsScreen(
    updateData: Boolean,
    goToSprint: (Sprint) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScrumOpenSprintsViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val openSprints = viewModel.openSprints.collectAsLazyPagingItems()
    val sprintDialogState by viewModel.sprintDialogState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.open_sprints),
                navigationIcon = NavigationIconConfig.Menu,
                actions = persistentListOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_add,
                        contentDescription = "Add Sprint",
                        onClick = state.onCreateSprintClick
                    )
                )
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

    EditSprintDialog(
        state = sprintDialogState,
        onConfirm = state.onCreateSprintConfirm
    )

    SprintsListContent(
        sprints = openSprints,
        goToSprint = goToSprint,
        modifier = modifier
    )
}
