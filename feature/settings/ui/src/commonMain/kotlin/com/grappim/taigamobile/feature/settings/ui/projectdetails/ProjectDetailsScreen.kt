package com.grappim.taigamobile.feature.settings.ui.projectdetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.project_contact_activated
import com.grappim.taigamobile.strings.generated.resources.project_description
import com.grappim.taigamobile.strings.generated.resources.project_is_private
import com.grappim.taigamobile.strings.generated.resources.project_looking_for_people
import com.grappim.taigamobile.strings.generated.resources.project_looking_for_people_note
import com.grappim.taigamobile.strings.generated.resources.project_name
import com.grappim.taigamobile.strings.generated.resources.receive_feedback_from_users
import com.grappim.taigamobile.strings.generated.resources.save
import com.grappim.taigamobile.strings.generated.resources.settings_project_details
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
fun ProjectDetailsScreen(
    onNavigateBack: () -> Unit,
    showSnackbar: (NativeText) -> Unit,
    viewModel: ProjectDetailsViewModel = koinViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoading, state.isSaving) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.settings_project_details),
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

    ProjectDetailsScreenContent(state = state)
}

@Composable
private fun ProjectDetailsScreenContent(state: ProjectDetailsState) {
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
                TaigaHeightSpacer(16.dp)

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.name,
                    onValueChange = state.onNameChange,
                    label = { Text(stringResource(RString.project_name)) },
                    singleLine = true
                )

                TaigaHeightSpacer(12.dp)

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = state.description,
                    onValueChange = state.onDescriptionChange,
                    label = { Text(stringResource(RString.project_description)) },
                    minLines = 3
                )

                TaigaHeightSpacer(8.dp)

                ListItem(
                    headlineContent = { Text(stringResource(RString.project_is_private)) },
                    trailingContent = {
                        Switch(
                            checked = state.isPrivate,
                            onCheckedChange = state.onIsPrivateChange
                        )
                    }
                )

                ListItem(
                    headlineContent = { Text(stringResource(RString.project_looking_for_people)) },
                    trailingContent = {
                        Switch(
                            checked = state.isLookingForPeople,
                            onCheckedChange = state.onIsLookingForPeopleChange
                        )
                    }
                )

                if (state.isLookingForPeople) {
                    TaigaHeightSpacer(4.dp)
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.lookingForPeopleNote,
                        onValueChange = state.onLookingForPeopleNoteChange,
                        label = { Text(stringResource(RString.project_looking_for_people_note)) },
                        minLines = 2
                    )
                    TaigaHeightSpacer(4.dp)
                }

                ListItem(
                    headlineContent = { Text(stringResource(RString.project_contact_activated)) },
                    trailingContent = {
                        Switch(
                            checked = state.isContactActivated,
                            onCheckedChange = state.onIsContactActivatedChange
                        )
                    },
                    supportingContent = {
                        Text(stringResource(RString.receive_feedback_from_users))
                    }
                )

                TaigaHeightSpacer(32.dp)
            }
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun ProjectDetailsScreenPreview() = TaigaMobilePreviewTheme {
    ProjectDetailsScreenContent(
        state = ProjectDetailsState(
            name = "My Project",
            description = "A great project",
            isPrivate = true,
            isLookingForPeople = true,
            lookingForPeopleNote = "Looking for Kotlin devs",
            isContactActivated = false
        )
    )
}
