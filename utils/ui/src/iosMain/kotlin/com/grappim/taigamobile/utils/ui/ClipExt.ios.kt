package com.grappim.taigamobile.utils.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

@OptIn(ExperimentalComposeUiApi::class)
actual fun String.toClipEntry(): ClipEntry = ClipEntry.withPlainText(this)
