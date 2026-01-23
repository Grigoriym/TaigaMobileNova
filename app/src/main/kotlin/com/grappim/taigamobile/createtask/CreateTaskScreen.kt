package com.grappim.taigamobile.createtask

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.LoadingDialog
import com.grappim.taigamobile.uikit.widgets.editor.HintTextField
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf

@Composable
fun CreateTaskScreen(
    showSnackbar: (NativeText) -> Unit,
    navigateOnTaskCreated: (Long, CommonTaskType, Long) -> Unit,
    viewModel: CreateTaskViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = state.toolbarTitle,
                navigationIcon = NavigationIconConfig.Back(),
                actions = persistentListOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_save,
                        contentDescription = "Save",
                        onClick = {
                            state.onCreateTask()
                        }
                    )
                )
            )
        )
    }

    LaunchedEffect(state.error) {
        if (state.error.isNotEmpty()) {
            showSnackbar(state.error)
        }
    }

    ObserveAsEvents(viewModel.creationResult) { result ->
        navigateOnTaskCreated(result.id, result.type, result.ref)
    }

    CreateTaskScreenContent(
        state = state
    )
}

@Composable
fun CreateTaskScreenContent(state: CreateTaskState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        if (state.isLoading) {
            LoadingDialog()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = mainHorizontalScreenPadding)
        ) {
            Spacer(Modifier.height(8.dp))

            HintTextField(
                modifier = Modifier.fillMaxWidth(),
                hint = NativeText.Resource(RString.title_hint),
                value = state.title,
                onValueChange = { state.setTitle(it) },
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            HintTextField(
                modifier = Modifier.fillMaxSize(),
                hint = NativeText.Resource(RString.description_hint),
                value = state.description,
                onValueChange = { state.setDescription(it) }
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CreateTaskScreenPreview() = TaigaMobileTheme {
    CreateTaskScreenContent(
        state = CreateTaskState(
            setTitle = {},
            setDescription = {},
            onCreateTask = {}
        )
    )
}
