package com.grappim.taigamobile.feature.settings.ui.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.grappim.taigamobile.feature.users.domain.User
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun SettingsUserScreen(viewModel: SettingsUserScreenViewModel = hiltViewModel()) {
    val topBarController: TopBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.settings_user),
                navigationIcon = NavigationIconConfig.Back()
            )
        )
    }

    SettingsUserScreenContent(state = state)
}

@Composable
private fun SettingsUserScreenContent(state: SettingsUserScreenState) {
    Surface {
        if (state.user != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
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
                        text = stringResource(RString.username_template).format(state.user.username),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = state.serverUrl,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@[Composable PreviewTaigaDarkLight]
private fun SettingsUserScreenContentPreview() {
    TaigaMobileTheme {
        SettingsUserScreenContent(
            state = SettingsUserScreenState(
                user = User(
                    id = 8606,
                    fullName = "Elnora Knight",
                    photo = "ex",
                    bigPhoto = "massa",
                    username = "elliott_dean",
                    name = "Cody Terrell",
                    pk = 8021
                ),
                serverUrl = "https://sample.server/"
            )
        )
    }
}
