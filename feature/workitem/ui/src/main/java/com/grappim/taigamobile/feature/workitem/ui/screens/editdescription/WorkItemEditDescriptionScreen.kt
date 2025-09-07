package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionTextButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WorkItemEditDescriptionScreen(
    goBack: () -> Unit,
    viewModel: EditDescriptionViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.edit_description),
                showBackButton = true,
                overrideBackHandlerAction = {
                    state.setIsDialogVisible(!state.isDialogVisible)
                },
                actions = persistentListOf(
                    TopBarActionTextButton(
                        text = NativeText.Resource(RString.save),
                        contentDescription = "",
                        onClick = {
                            state.shouldGoBackWithCurrentValue(true)
                        }
                    )
                )
            )
        )
    }

    BackHandler {
        state.setIsDialogVisible(!state.isDialogVisible)
    }

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

    EditDescriptionContent(state = state)
}

@Composable
private fun EditDescriptionContent(state: EditDescriptionState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
    ) {
        BasicTextField(
            modifier = Modifier.fillMaxSize(),
            value = state.currentDescription,
            onValueChange = state.onDescriptionChange,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface)
        )
    }
}
