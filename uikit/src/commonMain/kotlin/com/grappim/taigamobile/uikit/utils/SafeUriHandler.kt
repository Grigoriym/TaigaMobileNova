package com.grappim.taigamobile.uikit.utils

import androidx.compose.ui.platform.UriHandler
import com.grappim.taigamobile.core.logger.LogPriority
import com.grappim.taigamobile.core.logger.logcat

/**
 * Refuses to open any URI whose scheme isn't http(s) before delegating. Server- and
 * collaborator-supplied text (custom fields, markdown links, attachment URLs) reaches
 * [UriHandler.openUri] as free text; on Android an implicit `ACTION_VIEW` intent is launched
 * as-is, so an unchecked scheme (e.g. `intent://`) is a deep-link-into-another-app vector.
 */
internal class SafeUriHandler(private val delegate: UriHandler) : UriHandler {
    override fun openUri(uri: String) {
        if (uri.startsWith("http://", ignoreCase = true) || uri.startsWith("https://", ignoreCase = true)) {
            delegate.openUri(uri)
        } else {
            logcat(LogPriority.WARN) { "Refused to open a URI with a non-http(s) scheme" }
        }
    }
}
