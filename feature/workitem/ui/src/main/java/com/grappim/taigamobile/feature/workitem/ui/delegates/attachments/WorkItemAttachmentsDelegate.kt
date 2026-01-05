package com.grappim.taigamobile.feature.workitem.ui.delegates.attachments

import android.net.Uri
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemAttachmentsDelegate {
    val attachmentsState: StateFlow<WorkItemAttachmentsState>

    suspend fun handleAddAttachment(
        workItemId: Long,
        uri: Uri?,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: (() -> Unit)? = null,
        doOnError: suspend (Throwable) -> Unit
    )

    suspend fun handleRemoveAttachment(
        attachment: Attachment,
        doOnPreExecute: (() -> Unit)? = null,
        doOnSuccess: (() -> Unit)? = null,
        doOnError: suspend (Throwable) -> Unit
    )

    fun setInitialAttachments(attachments: List<Attachment>)
}

data class WorkItemAttachmentsState(
    val attachments: PersistentList<Attachment> = persistentListOf(),
    val areAttachmentsLoading: Boolean = false,
    val areAttachmentsExpanded: Boolean = false,
    val setAreAttachmentsExpanded: (Boolean) -> Unit = {}
)
