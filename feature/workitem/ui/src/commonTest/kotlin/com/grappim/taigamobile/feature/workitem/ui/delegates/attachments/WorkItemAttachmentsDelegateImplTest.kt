package com.grappim.taigamobile.feature.workitem.ui.delegates.attachments

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.testing.models.getAttachment
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.createTestPlatformFile
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class WorkItemAttachmentsDelegateImplTest {

    private val workItemRepository = FakeWorkItemRepository()
    private val taigaSessionStorage = FakeTaigaSessionStorage()

    private fun createSut(
        taskIdentifier: TaskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.Task)
    ): WorkItemAttachmentsDelegateImpl = WorkItemAttachmentsDelegateImpl(
        taskIdentifier = taskIdentifier,
        workItemRepository = workItemRepository,
        taigaSessionStorage = taigaSessionStorage
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
    fun `handleAddAttachment with null file should clear loading and return early`() = runTest {
        val sut = createSut()

        sut.handleAddAttachment(
            workItemId = getRandomLong(),
            file = null,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertFalse(sut.attachmentsState.value.areAttachmentsLoading)
        assertTrue(workItemRepository.addAttachmentCalls.isEmpty())
    }

    @Test
    fun `handleAddAttachment should call doOnPreExecute`() = runTest {
        val sut = createSut()
        var preExecuteCalled = false
        val projectId = getRandomLong()
        val attachment = getAttachment()

        taigaSessionStorage.currentProjectId = projectId
        workItemRepository.addAttachmentResult = attachment

        sut.handleAddAttachment(
            workItemId = getRandomLong(),
            file = createTestPlatformFile(getRandomString(), byteArrayOf(1, 2, 3)),
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleAddAttachment should add attachment on success`() = runTest {
        val sut = createSut()
        val projectId = getRandomLong()
        val attachment = getAttachment()

        taigaSessionStorage.currentProjectId = projectId
        workItemRepository.addAttachmentResult = attachment

        sut.handleAddAttachment(
            workItemId = getRandomLong(),
            file = createTestPlatformFile(getRandomString(), byteArrayOf(1, 2, 3)),
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
        val projectId = getRandomLong()
        val attachment = getAttachment()

        taigaSessionStorage.currentProjectId = projectId
        workItemRepository.addAttachmentResult = attachment

        sut.handleAddAttachment(
            workItemId = getRandomLong(),
            file = createTestPlatformFile(getRandomString(), byteArrayOf(1, 2, 3)),
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
        val workItemId = getRandomLong()
        val projectId = getRandomLong()
        val attachment = getAttachment()
        val fileName = getRandomString()
        val fileBytes = byteArrayOf(1, 2, 3)

        taigaSessionStorage.currentProjectId = projectId
        workItemRepository.addAttachmentResult = attachment

        sut.handleAddAttachment(
            workItemId = workItemId,
            file = createTestPlatformFile(fileName, fileBytes),
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val call = workItemRepository.addAttachmentCalls.last()
        assertEquals(workItemId, call.workItemId)
        assertEquals(fileName, call.fileName)
        assertTrue(fileBytes.contentEquals(call.fileByteArray))
        assertEquals(taskIdentifier, call.taskIdentifier)
        assertEquals(projectId, call.projectId)
    }

    @Test
    fun `handleAddAttachment should clear loading on error`() = runTest {
        val sut = createSut()

        taigaSessionStorage.currentProjectId = getRandomLong()
        workItemRepository.addAttachmentThrows = testException

        sut.handleAddAttachment(
            workItemId = getRandomLong(),
            file = createTestPlatformFile(getRandomString(), byteArrayOf(1, 2, 3)),
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

        taigaSessionStorage.currentProjectId = getRandomLong()
        workItemRepository.addAttachmentThrows = testException

        sut.handleAddAttachment(
            workItemId = getRandomLong(),
            file = createTestPlatformFile(getRandomString(), byteArrayOf(1, 2, 3)),
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

        sut.handleRemoveAttachment(
            attachment = attachment,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val call = workItemRepository.deleteAttachmentCalls.last()
        assertEquals(attachment, call.attachment)
        assertEquals(taskIdentifier, call.taskIdentifier)
    }

    @Test
    fun `handleRemoveAttachment should clear loading on error`() = runTest {
        val sut = createSut()
        val attachment = getAttachment()

        workItemRepository.deleteAttachmentThrows = testException

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

        workItemRepository.deleteAttachmentThrows = testException

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
