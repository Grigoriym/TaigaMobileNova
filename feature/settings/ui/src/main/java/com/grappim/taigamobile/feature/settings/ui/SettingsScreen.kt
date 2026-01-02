@file:OptIn(ExperimentalMaterialApi::class)

package com.grappim.taigamobile.feature.settings.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.grappim.taigamobile.core.storage.ThemeSettings
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun SettingsScreen(showSnackbar: (message: NativeText) -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.settings),
                navigationIcon = NavigationIconConfig.Menu
            )
        )
    }

    LaunchedEffect(state.error) {
        if (state.error.isNotEmpty()) {
            showSnackbar(state.error)
        }
    }

    if (state.isLoading) {
        CircularLoaderWidget(modifier = Modifier.fillMaxSize())
    }

    if (state.user != null) {
        SettingsScreenContent(state = state)
    }
}

@Composable
fun SettingsScreenContent(state: SettingsState, modifier: Modifier = Modifier) {
    requireNotNull(state.user)
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        AsyncImage(
            modifier = Modifier
                .size(120.dp)
                .clip(MaterialTheme.shapes.large),
            placeholder = painterResource(RDrawable.default_avatar),
            error = painterResource(RDrawable.default_avatar),
            model = state.user.avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.user.displayName,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = stringResource(RString.username_template).format(state.user.displayName),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = state.serverUrl,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(horizontal = mainHorizontalScreenPadding)
        ) {
            Text(
                text = stringResource(RString.appearance),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ThemeSelector(state = state)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = state.appInfo,
                style = MaterialTheme.typography.bodyLarge.merge(TextStyle(fontSize = 18.sp)),
                color = MaterialTheme.colorScheme.outline
            )

            val uriHandler = LocalUriHandler.current

            val githubUrl = stringResource(RString.github_url)
            TextButton(
                onClick = {
                    uriHandler.openUri(githubUrl)
                }
            ) {
                Text(
                    text = stringResource(RString.source_code),
                    style = MaterialTheme.typography.bodyLarge.merge(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                )
            }
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun SettingsScreenPreview() = TaigaMobileTheme {
    SettingsScreenContent(
        state = SettingsState(
            user = User(
                id = 8606,
                fullName = "Elnora Knight",
                photo = "ex",
                bigPhoto = "massa",
                username = "Elliott Dean",
                name = "Cody Terrell",
                pk = 8021
            ),
            appInfo = "debug",
            serverUrl = "https://sample.server/",
            themeSettings = ThemeSettings.System
        )
    )
}
