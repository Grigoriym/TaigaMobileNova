package com.grappim.taigamobile.feature.settings.ui.modules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.module_backlog_activated
import com.grappim.taigamobile.strings.generated.resources.module_backlog_details
import com.grappim.taigamobile.strings.generated.resources.module_epics_activated
import com.grappim.taigamobile.strings.generated.resources.module_epics_details
import com.grappim.taigamobile.strings.generated.resources.module_issues_activated
import com.grappim.taigamobile.strings.generated.resources.module_issues_details
import com.grappim.taigamobile.strings.generated.resources.module_kanban_activated
import com.grappim.taigamobile.strings.generated.resources.module_kanban_details
import com.grappim.taigamobile.strings.generated.resources.module_total_milestones
import com.grappim.taigamobile.strings.generated.resources.module_total_story_points
import com.grappim.taigamobile.strings.generated.resources.module_wiki_activated
import com.grappim.taigamobile.strings.generated.resources.module_wiki_details
import com.grappim.taigamobile.strings.generated.resources.save
import com.grappim.taigamobile.strings.generated.resources.settings_modules
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionTextButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ModulesScreen(
    onNavigateBack: () -> Unit,
    showSnackbar: (NativeText) -> Unit,
    viewModel: ModulesViewModel = koinViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoading, state.isSaving) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.settings_modules),
                navigationIcon = NavigationIconConfig.Back(),
                actions = persistentListOf(
                    TopBarActionTextButton(
                        text = NativeText.Resource(RString.save),
                        enabled = !state.isSaving && !state.isLoading,
                        onClick = state.onSaveClick
                    )
                )
            )
        )
    }

    ObserveAsEvents(viewModel.navigateBack) { onNavigateBack() }
    ObserveAsEvents(viewModel.snackBarMessage) { showSnackbar(it) }

    ModulesScreenContent(state = state)
}

@Composable
private fun ModulesScreenContent(state: ModulesState) {
    Surface(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            CircularLoaderWidget()
        } else if (state.error.isNotEmpty()) {
            ErrorStateWidget(message = state.error)
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = mainHorizontalScreenPadding)
            ) {
                TaigaHeightSpacer(8.dp)

                ListItem(
                    headlineContent = { Text(stringResource(RString.module_epics_activated)) },
                    trailingContent = {
                        Switch(
                            checked = state.isEpicsActivated,
                            onCheckedChange = state.onEpicsActivatedChange
                        )
                    },
                    supportingContent = {
                        Text(stringResource(RString.module_epics_details))
                    }
                )

                ListItem(
                    headlineContent = { Text(stringResource(RString.module_backlog_activated)) },
                    trailingContent = {
                        Switch(
                            checked = state.isBacklogActivated,
                            onCheckedChange = state.onBacklogActivatedChange
                        )
                    },
                    supportingContent = {
                        Text(stringResource(RString.module_backlog_details))
                    }
                )

                ListItem(
                    headlineContent = { Text(stringResource(RString.module_kanban_activated)) },
                    trailingContent = {
                        Switch(
                            checked = state.isKanbanActivated,
                            onCheckedChange = state.onKanbanActivatedChange
                        )
                    },
                    supportingContent = {
                        Text(stringResource(RString.module_kanban_details))
                    }
                )

                ListItem(
                    headlineContent = { Text(stringResource(RString.module_issues_activated)) },
                    trailingContent = {
                        Switch(
                            checked = state.isIssuesActivated,
                            onCheckedChange = state.onIssuesActivatedChange
                        )
                    },
                    supportingContent = {
                        Text(stringResource(RString.module_issues_details))
                    }
                )

                ListItem(
                    headlineContent = { Text(stringResource(RString.module_wiki_activated)) },
                    trailingContent = {
                        Switch(
                            checked = state.isWikiActivated,
                            onCheckedChange = state.onWikiActivatedChange
                        )
                    },
                    supportingContent = {
                        Text(stringResource(RString.module_wiki_details))
                    }
                )

                TaigaHeightSpacer(8.dp)

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.totalMilestones,
                    onValueChange = state.onTotalMilestonesChange,
                    label = { Text(stringResource(RString.module_total_milestones)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                TaigaHeightSpacer(12.dp)

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.totalStoryPoints,
                    onValueChange = state.onTotalStoryPointsChange,
                    label = { Text(stringResource(RString.module_total_story_points)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                TaigaHeightSpacer(32.dp)
            }
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun ModulesScreenPreview() = TaigaMobilePreviewTheme {
    ModulesScreenContent(
        state = ModulesState(
            isEpicsActivated = true,
            isBacklogActivated = true,
            isKanbanActivated = false,
            isIssuesActivated = true,
            isWikiActivated = false,
            totalMilestones = "4",
            totalStoryPoints = "100.0"
        )
    )
}
