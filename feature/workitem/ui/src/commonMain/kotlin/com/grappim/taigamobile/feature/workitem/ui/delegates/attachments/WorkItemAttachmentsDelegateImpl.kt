package com.grappim.taigamobile.feature.workitem.ui.delegates.attachments

import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.workitem.domain.Attachment
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.utils.ui.AttachmentInfo
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WorkItemAttachmentsDelegateImpl(
    private val taskIdentifier: TaskIdentifier,
    private val workItemRepository: WorkItemRepository,
    private val taigaSessionStorage: TaigaSessionStorage
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
        file: PlatformFile?,
        doOnPreExecute: (() -> Unit)?,
        doOnSuccess: (() -> Unit)?,
        doOnError: suspend (Throwable) -> Unit
    ) {
        _attachmentsState.update {
            it.copy(areAttachmentsLoading = true)
        }

        if (file == null) {
            _attachmentsState.update {
                it.copy(areAttachmentsLoading = false)
            }
            return
        }

        doOnPreExecute?.invoke()

        resultOf {
            val attachmentInfo = AttachmentInfo(
                name = file.name,
                fileBytes = file.readBytes().toList()
            )
            workItemRepository.addAttachment(
                workItemId = workItemId,
                fileName = attachmentInfo.name,
                fileByteArray = attachmentInfo.fileBytes.toByteArray(),
                taskIdentifier = taskIdentifier,
                projectId = taigaSessionStorage.getCurrentProjectId()
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
            logcat(throwable = error) {
                "Error adding attachment"
            }
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
                taskIdentifier = taskIdentifier
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
            logcat(throwable = error) {
                "Error removing attachment"
            }
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
