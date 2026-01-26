package com.grappim.taigamobile.feature.settings.ui.attributes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Sell
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
fun AttributesScreen(goToTagsScreen: () -> Unit) {
    val topBarController = LocalTopBarConfig.current

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.settings_attributes),
                navigationIcon = NavigationIconConfig.Back()
            )
        )
    }

    AttributesScreenContent(goToTagsScreen = goToTagsScreen)
}

@Composable
fun AttributesScreenContent(goToTagsScreen: () -> Unit = {}) {
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
                        goToTagsScreen()
                    },
                headlineContent = {
                    Text(text = stringResource(RString.tags_title))
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Sell,
                        contentDescription = "Tags Screen"
                    )
                }
            )

            TaigaHeightSpacer(32.dp)
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun AttributesScreenPreview() = TaigaMobileTheme {
    AttributesScreenContent()
}
