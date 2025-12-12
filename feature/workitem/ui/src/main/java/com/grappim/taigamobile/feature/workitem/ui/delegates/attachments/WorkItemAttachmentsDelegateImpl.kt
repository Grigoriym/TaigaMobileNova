package com.grappim.taigamobile.feature.workitem.ui.delegates.attachments

import android.net.Uri
import com.grappim.taigamobile.core.domain.Attachment
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.utils.ui.file.FileUriManager
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import timber.log.Timber

class WorkItemAttachmentsDelegateImpl(
    private val commonTaskType: CommonTaskType,
    private val workItemRepository: WorkItemRepository,
    private val taigaStorage: TaigaStorage,
    private val fileUriManager: FileUriManager
) : WorkItemAttachmentsDelegate {

    private val _attachmentsState = MutableStateFlow(
        WorkItemAttachmentsState(
            setAreAttachmentsExpanded = ::setAreAttachmentsExpanded
        )
    )
    override val attachmentsState: StateFlow<WorkItemAttachmentsState> =
        _attachmentsState.asStateFlow()

    override suspend fun handleAddAttachment(
        workItemId: Long,
        uri: Uri?,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: (() -> Unit)?,
        doOnError: suspend (Throwable) -> Unit
    ) {
        _attachmentsState.update {
            it.copy(areAttachmentsLoading = true)
        }

        if (uri == null) {
            _attachmentsState.update {
                it.copy(areAttachmentsLoading = false)
            }
            return
        }

        doOnPreExecute?.invoke()

        resultOf {
            val attachmentInfo = fileUriManager.retrieveAttachmentInfo(uri)

            workItemRepository.addAttachment(
                workItemId = workItemId,
                fileName = attachmentInfo.name,
                fileByteArray = attachmentInfo.fileBytes.toByteArray(),
                commonTaskType = commonTaskType,
                projectId = taigaStorage.currentProjectIdFlow.first()
            )
        }.onSuccess { attachment ->
            doOnSuccess?.invoke()

            _attachmentsState.update { currentState ->
                currentState.copy(
                    areAttachmentsLoading = false,
                    attachments = currentState.attachments.add(attachment)
                )
            }
        }.onFailure { error ->
            Timber.e(error)
            _attachmentsState.update {
                it.copy(areAttachmentsLoading = false)
            }
            doOnError(error)
        }
    }

    override suspend fun handleRemoveAttachment(
        attachment: Attachment,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: (() -> Unit)?,
        doOnError: suspend (Throwable) -> Unit
    ) {
        doOnPreExecute?.invoke()
        _attachmentsState.update {
            it.copy(areAttachmentsLoading = true)
        }

        resultOf {
            workItemRepository.deleteAttachment(
                attachment = attachment,
                commonTaskType = commonTaskType
            )
        }.onSuccess {
            doOnSuccess?.invoke()

            _attachmentsState.update { currentState ->
                currentState.copy(
                    areAttachmentsLoading = false,
                    attachments = currentState.attachments.remove(attachment)
                )
            }
        }.onFailure { error ->
            Timber.e(error)
            _attachmentsState.update {
                it.copy(areAttachmentsLoading = false)
            }
            doOnError(error)
        }
    }

    override fun setInitialAttachments(attachments: List<Attachment>) {
        _attachmentsState.update {
            it.copy(attachments = attachments.toPersistentList())
        }
    }

    private fun setAreAttachmentsExpanded(isExpanded: Boolean) {
        _attachmentsState.update {
            it.copy(areAttachmentsExpanded = isExpanded)
        }
    }
}
