package com.grappim.taigamobile.uikit

import android.net.Uri
import androidx.compose.runtime.staticCompositionLocalOf
import java.io.InputStream

@Deprecated("I don't find it good to pass InputStream")
abstract class FilePickerOld {
    private var onFilePicked: (String, InputStream) -> Unit = { _, _ -> }

    open fun requestFile(onFilePicked: (String, InputStream) -> Unit) {
        this.onFilePicked = onFilePicked
    }

    fun filePicked(name: String, inputStream: InputStream) = onFilePicked(name, inputStream)
}

@Deprecated("remove it")
val LocalFilePickerOld = staticCompositionLocalOf<FilePickerOld> { error("No FilePicker provided") }

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
