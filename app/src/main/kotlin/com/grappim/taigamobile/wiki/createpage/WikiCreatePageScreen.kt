package com.grappim.taigamobile.wiki.createpage

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.main.topbar.LocalTopBarConfig
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.ui.utils.LoadingResult
import com.grappim.taigamobile.ui.utils.SubscribeOnError
import com.grappim.taigamobile.ui.utils.SuccessResult
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.LoadingDialog
import com.grappim.taigamobile.uikit.widgets.editor.TextFieldWithHint
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionResource
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun WikiCreatePageScreen(
    viewModel: WikiCreatePageViewModel = hiltViewModel(),
    showMessage: (message: Int) -> Unit = {},
    goToWikiPage: (slug: String) -> Unit
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.create_new_page),
                showBackButton = true,
                actions = listOf(
                    TopBarActionResource(
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

    val creationResult by viewModel.creationResult.collectAsState()
    creationResult.SubscribeOnError(showMessage)

    creationResult.takeIf { it is SuccessResult }?.data?.let {
        LaunchedEffect(Unit) {
            goToWikiPage(it.slug)
        }
    }

    WikiCreatePageScreenContent(
        state = state,
        isLoading = creationResult is LoadingResult
    )
}

@Composable
fun WikiCreatePageScreenContent(
    state: WikiCreatePageState,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        if (isLoading) {
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
                onValueChange = { state.setDescription(it) }
            )
        }
    }
}

@Preview
@Composable
private fun WikiCreatePageScreenPreview() {
    WikiCreatePageScreenContent(
        state = WikiCreatePageState(
            setTitle = {},
            setDescription = {},
            onCreateWikiPage = {}
        )
    )
}
