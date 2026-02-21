package com.grappim.taigamobile.utils.ui.file

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.graphics.toColorInt
import com.grappim.taigamobile.core.asynckmp.IoDispatcher
import com.grappim.taigamobile.utils.ui.AttachmentInfo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File

@Factory
class FileUriManagerImpl(
    private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FileUriManager {
    override suspend fun retrieveAttachmentInfo(uri: Uri): AttachmentInfo = withContext(ioDispatcher) {
        val fileName = getFileName(uri)
        "".toColorInt()
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
