package com.grappim.taigamobile.feature.workitem.ui.delegates.attachments

import android.net.Uri
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.testing.getAttachment
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.testException
import com.grappim.taigamobile.utils.ui.file.AttachmentInfo
import com.grappim.taigamobile.utils.ui.file.FileUriManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemAttachmentsDelegateImplTest {

    private val workItemRepository: WorkItemRepository = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val fileUriManager: FileUriManager = mockk()

    private fun createSut(
        taskIdentifier: TaskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Task)
    ): WorkItemAttachmentsDelegateImpl = WorkItemAttachmentsDelegateImpl(
        taskIdentifier = taskIdentifier,
        workItemRepository = workItemRepository,
        taigaSessionStorage = taigaSessionStorage,
        fileUriManager = fileUriManager
    )

    @Test
    fun `initial state should have empty attachments and no loading`() {
        val sut = createSut()

        val state = sut.attachmentsState.value

        assertTrue(state.attachments.isEmpty())
        assertFalse(state.areAttachmentsLoading)
        assertFalse(state.areAttachmentsExpanded)
    }

    @Test
    fun `setInitialAttachments should update attachments`() {
        val sut = createSut()
        val attachments = listOf(getAttachment(), getAttachment())

        sut.setInitialAttachments(attachments)

        assertEquals(attachments, sut.attachmentsState.value.attachments)
    }

    @Test
    fun `handleAddAttachment with null uri should clear loading and return early`() = runTest {
        val sut = createSut()

        sut.handleAddAttachment(
            workItemId = getRandomLong(),
            uri = null,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertFalse(sut.attachmentsState.value.areAttachmentsLoading)
        coVerify(exactly = 0) { workItemRepository.addAttachment(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `handleAddAttachment should call doOnPreExecute`() = runTest {
        val sut = createSut()
        var preExecuteCalled = false
        val uri: Uri = mockk()
        val projectId = getRandomLong()
        val attachment = getAttachment()
        val attachmentInfo = AttachmentInfo(
            name = getRandomString(),
            fileBytes = listOf(1, 2, 3)
        )

        coEvery { fileUriManager.retrieveAttachmentInfo(uri) } returns attachmentInfo
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coEvery {
            workItemRepository.addAttachment(any(), any(), any(), any(), any())
        } returns attachment

        sut.handleAddAttachment(
            workItemId = getRandomLong(),
            uri = uri,
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleAddAttachment should add attachment on success`() = runTest {
        val sut = createSut()
        val uri: Uri = mockk()
        val projectId = getRandomLong()
        val attachment = getAttachment()
        val attachmentInfo = AttachmentInfo(
            name = getRandomString(),
            fileBytes = listOf(1, 2, 3)
        )

        coEvery { fileUriManager.retrieveAttachmentInfo(uri) } returns attachmentInfo
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coEvery {
            workItemRepository.addAttachment(any(), any(), any(), any(), any())
        } returns attachment

        sut.handleAddAttachment(
            workItemId = getRandomLong(),
            uri = uri,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.attachmentsState.value
        assertEquals(1, state.attachments.size)
        assertEquals(attachment, state.attachments[0])
        assertFalse(state.areAttachmentsLoading)
    }

    @Test
    fun `handleAddAttachment should call doOnSuccess on success`() = runTest {
        val sut = createSut()
        var successCalled = false
        val uri: Uri = mockk()
        val projectId = getRandomLong()
        val attachment = getAttachment()
        val attachmentInfo = AttachmentInfo(
            name = getRandomString(),
            fileBytes = listOf(1, 2, 3)
        )

        coEvery { fileUriManager.retrieveAttachmentInfo(uri) } returns attachmentInfo
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coEvery {
            workItemRepository.addAttachment(any(), any(), any(), any(), any())
        } returns attachment

        sut.handleAddAttachment(
            workItemId = getRandomLong(),
            uri = uri,
            doOnPreExecute = null,
            doOnSuccess = { successCalled = true },
            doOnError = {}
        )

        assertTrue(successCalled)
    }

    @Test
    fun `handleAddAttachment should call repository with correct parameters`() = runTest {
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Issue)
        val sut = createSut(taskIdentifier)
        val uri: Uri = mockk()
        val workItemId = getRandomLong()
        val projectId = getRandomLong()
        val attachment = getAttachment()
        val fileName = getRandomString()
        val fileBytes = listOf<Byte>(1, 2, 3)
        val attachmentInfo = AttachmentInfo(
            name = fileName,
            fileBytes = fileBytes
        )

        coEvery { fileUriManager.retrieveAttachmentInfo(uri) } returns attachmentInfo
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId
        coEvery {
            workItemRepository.addAttachment(any(), any(), any(), any(), any())
        } returns attachment

        sut.handleAddAttachment(
            workItemId = workItemId,
            uri = uri,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify {
            workItemRepository.addAttachment(
                workItemId = workItemId,
                fileName = fileName,
                fileByteArray = fileBytes.toByteArray(),
                taskIdentifier = taskIdentifier,
                projectId = projectId
            )
        }
    }

    @Test
    fun `handleAddAttachment should clear loading on error`() = runTest {
        val sut = createSut()
        val uri: Uri = mockk()
        val attachmentInfo = AttachmentInfo(
            name = getRandomString(),
            fileBytes = listOf(1, 2, 3)
        )

        coEvery { fileUriManager.retrieveAttachmentInfo(uri) } returns attachmentInfo
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns getRandomLong()
        coEvery {
            workItemRepository.addAttachment(any(), any(), any(), any(), any())
        } throws testException

        sut.handleAddAttachment(
            workItemId = getRandomLong(),
            uri = uri,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertFalse(sut.attachmentsState.value.areAttachmentsLoading)
    }

    @Test
    fun `handleAddAttachment should call doOnError on failure`() = runTest {
        val sut = createSut()
        var receivedError: Throwable? = null
        val uri: Uri = mockk()
        val attachmentInfo = AttachmentInfo(
            name = getRandomString(),
            fileBytes = listOf(1, 2, 3)
        )

        coEvery { fileUriManager.retrieveAttachmentInfo(uri) } returns attachmentInfo
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns getRandomLong()
        coEvery {
            workItemRepository.addAttachment(any(), any(), any(), any(), any())
        } throws testException

        sut.handleAddAttachment(
            workItemId = getRandomLong(),
            uri = uri,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = { receivedError = it }
        )

        assertEquals(testException, receivedError)
    }

    @Test
    fun `handleRemoveAttachment should call doOnPreExecute`() = runTest {
        val sut = createSut()
        var preExecuteCalled = false
        val attachment = getAttachment()

        coEvery {
            workItemRepository.deleteAttachment(any(), any())
        } returns Unit

        sut.handleRemoveAttachment(
            attachment = attachment,
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleRemoveAttachment should remove attachment on success`() = runTest {
        val sut = createSut()
        val attachment = getAttachment()
        sut.setInitialAttachments(listOf(attachment))

        coEvery {
            workItemRepository.deleteAttachment(any(), any())
        } returns Unit

        sut.handleRemoveAttachment(
            attachment = attachment,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.attachmentsState.value
        assertTrue(state.attachments.isEmpty())
        assertFalse(state.areAttachmentsLoading)
    }

    @Test
    fun `handleRemoveAttachment should call doOnSuccess on success`() = runTest {
        val sut = createSut()
        var successCalled = false
        val attachment = getAttachment()

        coEvery {
            workItemRepository.deleteAttachment(any(), any())
        } returns Unit

        sut.handleRemoveAttachment(
            attachment = attachment,
            doOnPreExecute = null,
            doOnSuccess = { successCalled = true },
            doOnError = {}
        )

        assertTrue(successCalled)
    }

    @Test
    fun `handleRemoveAttachment should call repository with correct parameters`() = runTest {
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        val sut = createSut(taskIdentifier)
        val attachment = getAttachment()

        coEvery {
            workItemRepository.deleteAttachment(any(), any())
        } returns Unit

        sut.handleRemoveAttachment(
            attachment = attachment,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify {
            workItemRepository.deleteAttachment(
                attachment = attachment,
                taskIdentifier = taskIdentifier
            )
        }
    }

    @Test
    fun `handleRemoveAttachment should clear loading on error`() = runTest {
        val sut = createSut()
        val attachment = getAttachment()

        coEvery {
            workItemRepository.deleteAttachment(any(), any())
        } throws testException

        sut.handleRemoveAttachment(
            attachment = attachment,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertFalse(sut.attachmentsState.value.areAttachmentsLoading)
    }

    @Test
    fun `handleRemoveAttachment should call doOnError on failure`() = runTest {
        val sut = createSut()
        var receivedError: Throwable? = null
        val attachment = getAttachment()

        coEvery {
            workItemRepository.deleteAttachment(any(), any())
        } throws testException

        sut.handleRemoveAttachment(
            attachment = attachment,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = { receivedError = it }
        )

        assertEquals(testException, receivedError)
    }

    @Test
    fun `setAreAttachmentsExpanded should update expanded state`() {
        val sut = createSut()

        sut.attachmentsState.value.setAreAttachmentsExpanded(true)

        assertTrue(sut.attachmentsState.value.areAttachmentsExpanded)

        sut.attachmentsState.value.setAreAttachmentsExpanded(false)

        assertFalse(sut.attachmentsState.value.areAttachmentsExpanded)
    }
}
