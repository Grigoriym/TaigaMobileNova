package com.grappim.taigamobile.utils.ui

import android.net.Uri

internal actual fun urlEncode(value: String): String = Uri.encode(value)

internal actual fun urlDecode(value: String): String = Uri.decode(value)
