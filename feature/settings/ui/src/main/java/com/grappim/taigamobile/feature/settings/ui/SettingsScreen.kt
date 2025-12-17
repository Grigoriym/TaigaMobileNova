@file:OptIn(ExperimentalMaterialApi::class)

package com.grappim.taigamobile.feature.settings.ui

import androidx.annotation.StringRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
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
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.DropdownSelector
import com.grappim.taigamobile.uikit.widgets.container.ContainerBoxWidget
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.asString
import timber.log.Timber

private const val ANIMATION_DURATION = 500

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
            horizontalAlignment = Alignment.Start
        ) {
            SettingsBlock(
                titleId = RString.appearance,
                items = listOf {
                    SettingItem(
                        textId = RString.theme_title,
                        itemWeight = 0.4f
                    ) {
                        DropdownSelector(
                            items = ThemeSettings.entries,
                            selectedItem = state.themeSettings,
                            onItemSelect = { state.onThemeChanged(it) },
                            itemContent = {
                                Text(
                                    text = state.getThemeTitle(it).asString(context),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            selectedItemContent = {
                                Text(
                                    text = state.themeDropDownTitle.asString(context),
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
) {
    ContainerBoxWidget(
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
}

@Composable
fun FeatureEnabledIcon(state: Boolean) {
    Crossfade(
        targetState = state,
        label = "custom_switch_label",
        animationSpec = tween(ANIMATION_DURATION)
    ) { enabled ->
        val imageVector = if (enabled) {
            Icons.Filled.CheckCircleOutline
        } else {
            Icons.Filled.HighlightOff
        }
        Icon(
            modifier = Modifier
                .testTag(imageVector.name),
            imageVector = imageVector,
            contentDescription = null,
            tint = if (enabled) {
                Color(0xFF_A8E272)
            } else {
                Color(0xFF_FF8C69)
            }
        )
    }
}

// @Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
// @Composable
// private fun SettingsScreenPreview() = TaigaMobileTheme {
//    SettingsScreenContent(
//        state = SettingsState(
//            appInfo = "asdasd",
//            serverUrl = "https://sample.server/",
//            onThemeChanged = {},
//            themeSettings = ThemeSettings.System,
//            showSnackbar = {}
//        )
//    )
// }
