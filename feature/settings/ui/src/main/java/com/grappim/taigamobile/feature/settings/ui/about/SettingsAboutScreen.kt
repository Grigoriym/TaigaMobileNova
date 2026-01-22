package com.grappim.taigamobile.feature.settings.ui.about

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.button.TaigaOutlinedButton
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.asString

@Composable
fun SettingsAboutScreen(viewModel: SettingsAboutScreenViewModel = hiltViewModel()) {
    val topBarController: TopBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.settings_about),
                navigationIcon = NavigationIconConfig.Back()
            )
        )
    }

    SettingsAboutScreenContent(state = state)
}

@Composable
private fun SettingsAboutScreenContent(state: SettingsAboutScreenState) {
    Surface {
        Column {
            GithubRepoContent(state = state)

            PrivacyPolicyContent(state = state)

            TaigaHeightSpacer(16.dp)
            VersionContent(state = state)
        }
    }
}

@Composable
private fun GithubRepoContent(state: SettingsAboutScreenState) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    TaigaHeightSpacer(16.dp)
    TaigaOutlinedButton(
        painter = painterResource(id = RDrawable.github_mark),
        text = stringResource(id = RString.github_repo_link),
        onClick = {
            uriHandler.openUri(state.githubRepoLink.asString(context))
        }
    )
}

@Composable
private fun PrivacyPolicyContent(state: SettingsAboutScreenState) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    TaigaHeightSpacer(16.dp)
    TaigaOutlinedButton(
        imageVector = Icons.Filled.Security,
        text = stringResource(id = RString.privacy_policy_title),
        onClick = { uriHandler.openUri(state.privacyPolicyLink.asString(context)) }
    )
}

@Composable
private fun VersionContent(state: SettingsAboutScreenState) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = state.appInfo)
    }
}

@[Composable PreviewTaigaDarkLight]
private fun SettingsAboutScreenContentPreview() {
    TaigaMobileTheme {
        SettingsAboutScreenContent(
            state = SettingsAboutScreenState(
                appInfo = "info"
            )
        )
    }
}
