package com.grappim.taigamobile.utils.ui

import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry

actual fun String.toClipEntry(): ClipEntry = android.content.ClipData.newPlainText(this, this).toClipEntry()
