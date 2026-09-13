package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.grappim.kit.uikit.NativeText
import com.grappim.kit.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.kit.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.kit.uikit.widgets.topbar.TopBarActionTextButton
import com.grappim.kit.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.are_you_sure_discarding_changes
import com.grappim.taigamobile.strings.generated.resources.discard
import com.grappim.taigamobile.strings.generated.resources.edit_description
import com.grappim.taigamobile.strings.generated.resources.keep_editing
import com.grappim.taigamobile.strings.generated.resources.save
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.editor.MentionSuggestionsRow
import com.grappim.taigamobile.uikit.widgets.editor.findActiveMentionQuery
import com.grappim.taigamobile.uikit.widgets.editor.insertMention
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

const val EDIT_DESCRIPTION_TEXT_FIELD_TEST_TAG = "edit_description_text_field"

@Composable
fun WorkItemEditDescriptionScreen(
    route: WorkItemEditDescriptionNavDestination,
    goBack: () -> Unit,
    viewModel: EditDescriptionViewModel = koinViewModel { parametersOf(route) }
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val mentionsState by viewModel.mentionsState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.edit_description),
                navigationIcon = NavigationIconConfig.Back(
                    onBackClick = { state.setIsDialogVisible(!state.isDialogVisible) }
                ),
                actions = persistentListOf(
                    TopBarActionTextButton(
                        text = NativeText.Resource(RString.save),
                        onClick = {
                            state.shouldGoBackWithCurrentValue(true)
                        }
                    )
                )
            )
        )
    }

    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = true,
        onBackCompleted = {
            state.setIsDialogVisible(!state.isDialogVisible)
        }
    )

    ObserveAsEvents(viewModel.onBackAction, isImmediate = false) {
        goBack()
    }

    ConfirmActionDialog(
        isVisible = state.isDialogVisible,
        description = stringResource(RString.are_you_sure_discarding_changes),
        onConfirm = {
            state.shouldGoBackWithCurrentValue(false)
        },
        onDismiss = {
            state.setIsDialogVisible(false)
        },
        confirmButtonText = NativeText.Resource(RString.discard),
        dismissButtonText = NativeText.Resource(RString.keep_editing)
    )

    EditDescriptionContent(state = state, members = mentionsState.members)
}

@Composable
fun EditDescriptionContent(state: EditDescriptionState, members: ImmutableList<TeamMember>) {
    val activeMentionQuery = findActiveMentionQuery(state.currentDescription)
    val mentionSuggestions = remember(activeMentionQuery, members) {
        val query = activeMentionQuery?.query
        if (query == null) {
            persistentListOf()
        } else {
            members.filter { it.username.startsWith(query, ignoreCase = true) }.toPersistentList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) {
        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag(EDIT_DESCRIPTION_TEXT_FIELD_TEST_TAG),
            value = state.currentDescription,
            onValueChange = { newValue -> state.onDescriptionChange(newValue) },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
        )

        MentionSuggestionsRow(
            members = mentionSuggestions,
            onSelect = { member ->
                activeMentionQuery?.let { query ->
                    state.onDescriptionChange(insertMention(state.currentDescription, query, member.username))
                }
            }
        )
    }
}
