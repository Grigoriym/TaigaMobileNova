package com.grappim.taigamobile.uikit.permission

import androidx.compose.runtime.Composable

/** No runtime permission gates local network access on this platform. */
@Composable
actual fun rememberLocalNetworkPermissionRequest(): LocalNetworkPermissionRequest =
    LocalNetworkPermissionRequest { _, onAllowed, _ -> onAllowed() }

@Composable
actual fun rememberIsLocalNetworkBlocked(serverUrl: String): Boolean = false

@Composable
actual fun rememberOpenAppSettings(): () -> Unit = {}
