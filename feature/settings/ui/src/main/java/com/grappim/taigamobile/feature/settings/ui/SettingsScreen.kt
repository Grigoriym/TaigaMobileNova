@file:OptIn(ExperimentalMaterialApi::class)

package com.grappim.taigamobile.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun SettingsScreen(goToAboutScreen: () -> Unit, goToInterfaceScreen: () -> Unit, goToUserScreen: () -> Unit) {
    val topBarController = LocalTopBarConfig.current

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.settings),
                navigationIcon = NavigationIconConfig.Menu
            )
        )
    }

    SettingsScreenContent(
        goToAboutScreen = goToAboutScreen,
        goToInterfaceScreen = goToInterfaceScreen,
        goToUserScreen = goToUserScreen
    )
}

@Composable
fun SettingsScreenContent(
    goToAboutScreen: () -> Unit = {},
    goToInterfaceScreen: () -> Unit = {},
    goToUserScreen: () -> Unit = {}
) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .scrollable(rememberScrollState(), orientation = Orientation.Vertical)
        ) {
            TaigaHeightSpacer(8.dp)

            ListItem(
                modifier = Modifier
                    .clickable {
                        goToUserScreen()
                    },
                headlineContent = {
                    Text(text = stringResource(RString.settings_user))
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Screen"
                    )
                }
            )

            ListItem(
                modifier = Modifier
                    .clickable {
                        goToInterfaceScreen()
                    },
                headlineContent = {
                    Text(text = stringResource(RString.settings_interface))
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Outlined.TouchApp,
                        contentDescription = "Interface Screen"
                    )
                }
            )

            ListItem(
                modifier = Modifier
                    .clickable {
                        goToAboutScreen()
                    },
                headlineContent = {
                    Text(text = stringResource(RString.settings_about))
                },
                leadingContent = {
                    Icon(imageVector = Icons.Filled.Info, contentDescription = "About Screen")
                }
            )

            TaigaHeightSpacer(32.dp)
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun SettingsScreenPreviewOld() = TaigaMobileTheme {
    SettingsScreenContent()
}
