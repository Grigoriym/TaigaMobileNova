package com.grappim.taigamobile.utils.ui.file

import android.net.Uri

interface FileUriManager {
    suspend fun retrieveAttachmentInfo(uri: Uri): AttachmentInfo
}
