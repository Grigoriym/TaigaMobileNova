package com.grappim.taigamobile.utils.ui.file

import android.net.Uri
import com.grappim.taigamobile.utils.ui.AttachmentInfo

interface FileUriManager {
    suspend fun retrieveAttachmentInfo(uri: Uri): AttachmentInfo
}
