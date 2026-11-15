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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.kit.uikit.NativeText
import com.grappim.kit.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.kit.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.kit.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.kit.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.description_hint
import com.grappim.taigamobile.strings.generated.resources.title_hint
import com.grappim.taigamobile.uikit.generated.resources.ic_save
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.LoadingDialog
import com.grappim.taigamobile.uikit.widgets.editor.HintTextField
import com.grappim.taigamobile.uikit.widgets.editor.MarkdownToolbar
import com.grappim.taigamobile.uikit.widgets.editor.continueMarkdownList
import com.grappim.taigamobile.uikit.widgets.text.MarkdownTextWidget
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

const val CREATE_TASK_DESCRIPTION_TEST_TAG = "create_task_description"
const val CREATE_TASK_PREVIEW_TEST_TAG = "create_task_preview"

@Composable
fun CreateTaskScreen(
    route: CreateTaskNavDestination,
    showSnackbar: (NativeText) -> Unit,
    navigateOnTaskCreated: (Long, CommonTaskType, Long) -> Unit,
    viewModel: CreateTaskViewModel = koinViewModel { parametersOf(route) }
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

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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

                if (state.isPreview) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .testTag(CREATE_TASK_PREVIEW_TEST_TAG)
                    ) {
                        MarkdownTextWidget(text = state.description.text)
                    }
                } else {
                    HintTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(CREATE_TASK_DESCRIPTION_TEST_TAG),
                        hint = NativeText.Resource(RString.description_hint),
                        value = state.description,
                        onValueChange = { state.setDescription(continueMarkdownList(state.description, it)) }
                    )
                }
            }

            MarkdownToolbar(
                value = state.description,
                onValueChange = state.setDescription,
                isPreview = state.isPreview,
                onPreviewToggle = state.onPreviewToggle
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun CreateTaskScreenPreview() = TaigaMobilePreviewTheme {
    CreateTaskScreenContent(
        state = CreateTaskState(
            setTitle = {},
            setDescription = {},
            onCreateTask = {}
        )
    )
}
