package com.grappim.taigamobile.core.api

import com.grappim.taigamobile.core.storage.customheaders.CustomHeadersStorage

/** Configuration for [CustomHeaders]. */
class CustomHeadersConfig {
    lateinit var storage: CustomHeadersStorage

    /** The server's URL, read per request so a change of server applies at once. */
    var serverUrl: () -> String = { "" }
}
