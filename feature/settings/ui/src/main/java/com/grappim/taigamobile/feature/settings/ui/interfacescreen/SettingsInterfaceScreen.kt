@file:OptIn(ExperimentalMaterial3Api::class)

package com.grappim.taigamobile.feature.settings.ui.interfacescreen

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.feature.settings.ui.ThemeSelector
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun SettingsInterfaceScreen(viewModel: SettingsInterfaceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val topBarController: TopBarController = LocalTopBarConfig.current

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.settings_interface),
                navigationIcon = NavigationIconConfig.Back()
            )
        )
    }
    SettingsInterfaceScreenContent(state = state)
}

@Composable
private fun SettingsInterfaceScreenContent(state: SettingsInterfaceViewState) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .scrollable(rememberScrollState(), orientation = Orientation.Vertical)
                .padding(horizontal = 16.dp)
        ) {
            TaigaHeightSpacer(16.dp)

            AppearanceSection(state = state)

            TaigaHeightSpacer(16.dp)
        }
    }
}

@Composable
private fun AppearanceSection(state: SettingsInterfaceViewState) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.padding(horizontal = mainHorizontalScreenPadding)
    ) {
        androidx.compose.material3.Text(
            text = stringResource(RString.appearance),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ThemeSelector(state = state)
    }
}

@[Composable PreviewTaigaDarkLight]
private fun SettingsInterfaceScreenPreview() {
    TaigaMobileTheme {
        SettingsInterfaceScreenContent(
            state = SettingsInterfaceViewState()
        )
    }
}
