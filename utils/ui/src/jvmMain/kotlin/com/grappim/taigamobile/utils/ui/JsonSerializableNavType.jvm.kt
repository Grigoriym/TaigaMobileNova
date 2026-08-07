package com.grappim.taigamobile.utils.ui

import io.ktor.http.encodeURLParameter

internal actual fun urlEncode(value: String): String = value.encodeURLParameter()
