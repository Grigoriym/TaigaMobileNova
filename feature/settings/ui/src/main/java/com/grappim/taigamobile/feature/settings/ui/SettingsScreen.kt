package com.grappim.taigamobile.feature.settings.ui

import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.grappim.taigamobile.core.storage.ThemeSetting
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.utils.clickableUnindicated
import com.grappim.taigamobile.uikit.widgets.DropdownSelector
import com.grappim.taigamobile.uikit.widgets.container.ContainerBox
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SubscribeOnError
import com.grappim.taigamobile.utils.ui.activity
import timber.log.Timber

@Composable
fun SettingsScreen(
    showMessage: (message: Int) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.settings)
            )
        )
    }

    val user by viewModel.user.collectAsState()
    user.SubscribeOnError(showMessage)

    val themeSetting by viewModel.themeSetting.collectAsState()

    SettingsScreenContent(
        state = state,
        avatarUrl = user.data?.avatarUrl,
        displayName = user.data?.displayName.orEmpty(),
        username = user.data?.username.orEmpty(),
        logout = viewModel::logout,
        themeSetting = themeSetting,
        switchTheme = viewModel::switchTheme
    )
}

@Composable
fun SettingsScreenContent(
    state: SettingsState,
    avatarUrl: String?,
    displayName: String,
    username: String,
    modifier: Modifier = Modifier,
    logout: () -> Unit = {},
    themeSetting: ThemeSetting = ThemeSetting.System,
    switchTheme: (ThemeSetting) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = avatarUrl ?: RDrawable.default_avatar).apply(
                        block = fun ImageRequest.Builder.() {
                            error(RDrawable.default_avatar)
                            crossfade(true)
                        }
                    ).build()
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(MaterialTheme.shapes.large)
        )

        if (state.isAlertVisible) {
            ConfirmActionDialog(
                title = stringResource(RString.logout_title),
                text = stringResource(RString.logout_text),
                onConfirm = {
                    state.setIsAlertVisible(false)
                    logout()
                },
                onDismiss = { state.setIsAlertVisible(false) },
                iconId = RDrawable.ic_logout
            )
        }

        IconButton(
            onClick = { state.setIsAlertVisible(true) }
        ) {
            Icon(
                painter = painterResource(RDrawable.ic_logout),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = stringResource(RString.username_template).format(username),
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
            horizontalAlignment = Alignment.Start
        ) {
            SettingsBlock(
                titleId = RString.appearance,
                items = listOf {
                    SettingItem(
                        textId = RString.theme_title,
                        itemWeight = 0.4f
                    ) {
                        @Composable
                        fun titleForThemeSetting(themeSetting: ThemeSetting) = stringResource(
                            when (themeSetting) {
                                ThemeSetting.System -> RString.theme_system
                                ThemeSetting.Light -> RString.theme_light
                                ThemeSetting.Dark -> RString.theme_dark
                            }
                        )

                        DropdownSelector(
                            items = ThemeSetting.entries,
                            selectedItem = themeSetting,
                            onItemSelect = { switchTheme(it) },
                            itemContent = {
                                Text(
                                    text = titleForThemeSetting(it),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            selectedItemContent = {
                                Text(
                                    text = titleForThemeSetting(it),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                    }
                }
            )
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

            val activity = LocalContext.current.activity
            val githubUrl = stringResource(RString.github_url)
            Text(
                text = stringResource(RString.source_code),
                style = MaterialTheme.typography.bodyLarge.merge(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ),
                modifier = Modifier.clickableUnindicated {
                    activity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            githubUrl.toUri()
                        )
                    )
                }
            )

            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun SettingsBlock(@StringRes titleId: Int, items: List<@Composable () -> Unit>) {
    val verticalPadding = 2.dp

    Text(
        text = stringResource(titleId),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = mainHorizontalScreenPadding)
    )

    Spacer(Modifier.height(verticalPadding))

    items.forEach { it() }

    Spacer(Modifier.height(verticalPadding * 4))
}

@Composable
private fun SettingItem(
    @StringRes textId: Int,
    itemWeight: Float = 0.2f,
    onClick: () -> Unit = {},
    item: @Composable BoxScope.() -> Unit = {}
) = ContainerBox(
    verticalPadding = 10.dp,
    onClick = onClick
) {
    assert(itemWeight > 0 && itemWeight < 1) { Timber.e("Item weight must be between 0 and 1") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(textId),
            modifier = Modifier.weight(1 - itemWeight, fill = false)
        )

        Box(
            modifier = Modifier.weight(itemWeight),
            contentAlignment = Alignment.CenterEnd,
            content = item
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingsScreenPreview() = TaigaMobileTheme {
    SettingsScreenContent(
        avatarUrl = null,
        displayName = "Cool Name",
        username = "username",
        state = SettingsState(
            appInfo = "asdasd",
            serverUrl = "https://sample.server/",
            setIsAlertVisible = {}
        )
    )
}
