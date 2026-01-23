package com.grappim.taigamobile.feature.wiki.ui.page.create

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
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
fun WikiCreatePageScreen(
    goToWikiPage: (slug: String, id: Long) -> Unit,
    viewModel: WikiCreatePageViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                navigationIcon = NavigationIconConfig.Back(),
                title = NativeText.Resource(RString.create_new_page),
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

    ObserveAsEvents(viewModel.creationResult) { result ->
        goToWikiPage(result.slug, result.id)
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
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(RString.slug_helper_text),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )

            Spacer(Modifier.height(8.dp))

            HintTextField(
                error = state.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                hint = NativeText.Resource(RString.slug_hint),
                value = state.slug,
                onValueChange = { state.setSlug(it) }
            )

            Spacer(Modifier.height(8.dp))

            HintTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                hint = NativeText.Resource(RString.content_hint),
                value = state.content,
                onValueChange = { state.setContent(it) }
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WikiCreatePageScreenPreview() {
    WikiCreatePageScreenContent(
        state = WikiCreatePageState(
            slug = "getting-started-guide",
            content = "This is the content..."
        )
    )
}
