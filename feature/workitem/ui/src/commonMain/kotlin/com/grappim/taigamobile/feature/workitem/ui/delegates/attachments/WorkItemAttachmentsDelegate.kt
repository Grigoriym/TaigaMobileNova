package com.grappim.taigamobile.feature.workitem.ui.delegates.attachments

import com.grappim.taigamobile.feature.workitem.domain.Attachment
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.StateFlow

interface WorkItemAttachmentsDelegate {
    val attachmentsState:
        StateFlow<WorkItemAttachmentsState>

    suspend fun handleAddAttachment(
        workItemId: Long,
        file: PlatformFile?,
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
