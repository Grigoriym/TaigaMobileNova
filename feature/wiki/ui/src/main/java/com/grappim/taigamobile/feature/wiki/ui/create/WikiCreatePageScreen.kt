package com.grappim.taigamobile.feature.wiki.ui.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.LoadingDialog
import com.grappim.taigamobile.uikit.widgets.editor.TextFieldWithHint
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WikiCreatePageScreen(
    showSnackbar: (NativeText) -> Unit,
    goToWikiPage: (slug: String) -> Unit,
    viewModel: WikiCreatePageViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.create_new_page),
                navigationIcon = NavigationIconConfig.Back(),
                actions = persistentListOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_save,
                        contentDescription = "Save",
                        onClick = {
                            state.onCreateWikiPage()
                        }
                    )
                )
            )
        )
    }

    ObserveAsEvents(viewModel.uiError) { error ->
        if (error !is NativeText.Empty) {
            showSnackbar(error)
        }
    }

    ObserveAsEvents(viewModel.creationResult) { result ->
        goToWikiPage(result.slug)
    }

    WikiCreatePageScreenContent(state = state)
}

@Composable
fun WikiCreatePageScreenContent(state: WikiCreatePageState, modifier: Modifier = Modifier) {
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

            TextFieldWithHint(
                hintId = RString.title_hint,
                value = state.title,
                onValueChange = { state.setTitle(it) },
                style = MaterialTheme.typography.headlineSmall,
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            TextFieldWithHint(
                hintId = RString.description_hint,
                value = state.description,
                onValueChange = { state.setDescription(it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Preview
@Composable
private fun WikiCreatePageScreenPreview() {
    WikiCreatePageScreenContent(
        state = WikiCreatePageState()
    )
}
