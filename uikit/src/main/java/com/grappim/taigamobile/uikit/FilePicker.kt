package com.grappim.taigamobile.uikit

import android.net.Uri
import androidx.compose.runtime.staticCompositionLocalOf

abstract class FilePicker {
    private var onFilePicked: (Uri?) -> Unit = { _ -> }

    open fun requestFile(onFilePicked: (Uri?) -> Unit) {
        this.onFilePicked = onFilePicked
    }

    fun filePicked(uri: Uri?) {
        onFilePicked(uri)
    }
}

val LocalFilePicker = staticCompositionLocalOf<FilePicker> { error("No FilePicker provided") }
