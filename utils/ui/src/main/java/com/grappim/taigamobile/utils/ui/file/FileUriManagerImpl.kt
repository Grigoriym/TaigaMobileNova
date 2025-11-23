package com.grappim.taigamobile.utils.ui.file

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.grappim.taigamobile.core.async.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class FileUriManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FileUriManager {
    override suspend fun retrieveAttachmentInfo(uri: Uri): AttachmentInfo = withContext(ioDispatcher) {
        val fileName = getFileName(uri)
        val bytes = getByteList(uri)
        AttachmentInfo(
            name = fileName,
            fileBytes = bytes
        )
    }

    private suspend fun getByteList(uri: Uri): List<Byte> = withContext(ioDispatcher) {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()?.toList() ?: emptyList()
        inputStream?.close()
        bytes
    }

    private suspend fun getFileName(uri: Uri): String = withContext(ioDispatcher) {
        val returnCursor = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            var name: String
            returnCursor.use {
                returnCursor.moveToFirst()
                name = returnCursor.getString(nameIndex)
            }
            name
        } else {
            val uriPath = requireNotNull(uri.path)
            val file = File(uriPath)
            file.name
        }
    }
}
