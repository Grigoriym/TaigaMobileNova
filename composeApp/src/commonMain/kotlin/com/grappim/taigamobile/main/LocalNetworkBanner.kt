package com.grappim.taigamobile.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.local_network_banner_message
import com.grappim.taigamobile.strings.generated.resources.local_network_banner_open_settings
import com.grappim.taigamobile.uikit.permission.rememberIsLocalNetworkBlocked
import com.grappim.taigamobile.uikit.permission.rememberLocalNetworkPermissionRequest
import com.grappim.taigamobile.uikit.permission.rememberOpenAppSettings
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import org.jetbrains.compose.resources.stringResource

/**
 * Sits above every screen, because a blocked local server fails each of them the same way and each
 * would only report it as unreachable. Someone already signed in never passes through Login, so this
 * also asks once per run. [serverUrl] is empty while signed out, which keeps it hidden.
 */
@Composable
internal fun LocalNetworkBanner(serverUrl: String, modifier: Modifier = Modifier) {
    val localNetworkPermission = rememberLocalNetworkPermissionRequest()
    val openAppSettings = rememberOpenAppSettings()
    var hasAsked by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(serverUrl) {
        if (serverUrl.isNotEmpty() && !hasAsked) {
            hasAsked = true
            localNetworkPermission.request(serverUrl = serverUrl, onAllowed = {}, onDenied = {})
        }
    }

    if (serverUrl.isNotEmpty() && rememberIsLocalNetworkBlocked(serverUrl)) {
        LocalNetworkBannerContent(onOpenSettingsClick = openAppSettings, modifier = modifier)
    }
}

@Composable
private fun LocalNetworkBannerContent(onOpenSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.padding(start = BANNER_PADDING),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BANNER_PADDING)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(RString.local_network_banner_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onOpenSettingsClick) {
                Text(stringResource(RString.local_network_banner_open_settings))
            }
        }
    }
}

private val BANNER_PADDING = 16.dp

@PreviewTaigaDarkLight
@Composable
private fun LocalNetworkBannerContentPreview() = TaigaMobilePreviewTheme {
    LocalNetworkBannerContent(onOpenSettingsClick = {})
}
