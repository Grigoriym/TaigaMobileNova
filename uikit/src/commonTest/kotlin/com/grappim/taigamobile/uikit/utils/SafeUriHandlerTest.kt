package com.grappim.taigamobile.uikit.utils

import androidx.compose.ui.platform.UriHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

private class FakeUriHandler : UriHandler {
    var openedUri: String? = null

    override fun openUri(uri: String) {
        openedUri = uri
    }
}

class SafeUriHandlerTest {

    @Test
    fun opensHttpUri() {
        val fake = FakeUriHandler()
        val safeUriHandler = SafeUriHandler(fake)

        safeUriHandler.openUri("http://example.com")

        assertEquals("http://example.com", fake.openedUri)
    }

    @Test
    fun opensHttpsUriCaseInsensitively() {
        val fake = FakeUriHandler()
        val safeUriHandler = SafeUriHandler(fake)

        safeUriHandler.openUri("HTTPS://example.com")

        assertEquals("HTTPS://example.com", fake.openedUri)
    }

    @Test
    fun refusesIntentScheme() {
        val fake = FakeUriHandler()
        val safeUriHandler = SafeUriHandler(fake)

        safeUriHandler.openUri("intent://example.com#Intent;end")

        assertNull(fake.openedUri)
    }

    @Test
    fun refusesJavascriptScheme() {
        val fake = FakeUriHandler()
        val safeUriHandler = SafeUriHandler(fake)

        safeUriHandler.openUri("javascript:alert(1)")

        assertNull(fake.openedUri)
    }
}
