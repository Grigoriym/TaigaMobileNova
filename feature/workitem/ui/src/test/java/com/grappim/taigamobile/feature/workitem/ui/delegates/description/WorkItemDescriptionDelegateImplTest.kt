package com.grappim.taigamobile.feature.workitem.ui.delegates.description

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.testException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class WorkItemDescriptionDelegateImplTest {

    private val workItemRepository: WorkItemRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()

    private fun createSut(taskIdentifier: TaskIdentifier): WorkItemDescriptionDelegateImpl =
        WorkItemDescriptionDelegateImpl(
            taskIdentifier = taskIdentifier,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator
        )

    @Test
    fun `initial state should have empty description and no loading`() {
        val sut = createSut(TaskIdentifier.WorkItem(CommonTaskType.Issue))

        val state = sut.descriptionState.value

        assertEquals("", state.currentDescription)
        assertFalse(state.isDescriptionLoading)
    }

    @Test
    fun `setInitialDescription should update currentDescription`() {
        val sut = createSut(TaskIdentifier.WorkItem(CommonTaskType.Issue))
        val description = getRandomString()

        sut.setInitialDescription(description)

        assertEquals(description, sut.descriptionState.value.currentDescription)
    }

    @Test
    fun `updateDescription for WorkItem should not call repository when description unchanged`() = runTest {
        val sut = createSut(TaskIdentifier.WorkItem(CommonTaskType.Issue))
        val description = getRandomString()
        sut.setInitialDescription(description)

        sut.updateDescription(
            newDescription = description,
            version = getRandomLong(),
            workItemId = getRandomLong(),
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify(exactly = 0) { workItemRepository.patchData(any(), any(), any(), any()) }
    }

    @Test
    fun `updateDescription for WorkItem should call doOnPreExecute`() = runTest {
        val sut = createSut(TaskIdentifier.WorkItem(CommonTaskType.Issue))
        var preExecuteCalled = false
        val newDescription = getRandomString()

        val payload = persistentMapOf<String, Any?>("description" to newDescription)
        every { patchDataGenerator.getDescriptionPatchPayload(newDescription) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.updateDescription(
            newDescription = newDescription,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertEquals(true, preExecuteCalled)
    }

    @Test
    fun `updateDescription for WorkItem should update description on success`() = runTest {
        val sut = createSut(TaskIdentifier.WorkItem(CommonTaskType.Issue))
        val newDescription = getRandomString()

        val payload = persistentMapOf<String, Any?>("description" to newDescription)
        every { patchDataGenerator.getDescriptionPatchPayload(newDescription) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.updateDescription(
            newDescription = newDescription,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.descriptionState.value
        assertEquals(newDescription, state.currentDescription)
        assertFalse(state.isDescriptionLoading)
    }

    @Test
    fun `updateDescription for WorkItem should call doOnSuccess with new version`() = runTest {
        val sut = createSut(TaskIdentifier.WorkItem(CommonTaskType.Issue))
        var receivedVersion: Long? = null
        val newVersion = getRandomLong()
        val newDescription = getRandomString()

        val payload = persistentMapOf<String, Any?>("description" to newDescription)
        every { patchDataGenerator.getDescriptionPatchPayload(newDescription) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)

        sut.updateDescription(
            newDescription = newDescription,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = { receivedVersion = it },
            doOnError = {}
        )

        assertEquals(newVersion, receivedVersion)
    }

    @Test
    fun `updateDescription for WorkItem should clear loading on error`() = runTest {
        val sut = createSut(TaskIdentifier.WorkItem(CommonTaskType.Issue))
        val newDescription = getRandomString()

        val payload = persistentMapOf<String, Any?>("description" to newDescription)
        every { patchDataGenerator.getDescriptionPatchPayload(newDescription) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.updateDescription(
            newDescription = newDescription,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertFalse(sut.descriptionState.value.isDescriptionLoading)
    }

    @Test
    fun `updateDescription for WorkItem should call doOnError on failure`() = runTest {
        val sut = createSut(TaskIdentifier.WorkItem(CommonTaskType.Issue))
        var receivedError: Throwable? = null
        val newDescription = getRandomString()

        val payload = persistentMapOf<String, Any?>("description" to newDescription)
        every { patchDataGenerator.getDescriptionPatchPayload(newDescription) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.updateDescription(
            newDescription = newDescription,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = { receivedError = it }
        )

        assertEquals(testException, receivedError)
    }

    @Test
    fun `updateDescription for WorkItem should call repository with correct parameters`() = runTest {
        val commonTaskType = CommonTaskType.UserStory
        val sut = createSut(TaskIdentifier.WorkItem(commonTaskType))
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val newDescription = getRandomString()

        val payload = persistentMapOf<String, Any?>("description" to newDescription)
        every { patchDataGenerator.getDescriptionPatchPayload(newDescription) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.updateDescription(
            newDescription = newDescription,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify {
            workItemRepository.patchData(
                version = version,
                workItemId = workItemId,
                payload = payload,
                commonTaskType = commonTaskType
            )
        }
    }

    @Test
    fun `updateDescription for Wiki should not call repository when description unchanged`() = runTest {
        val sut = createSut(TaskIdentifier.Wiki)
        val description = getRandomString()
        sut.setInitialDescription(description)

        sut.updateDescription(
            newDescription = description,
            version = getRandomLong(),
            workItemId = getRandomLong(),
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify(exactly = 0) { workItemRepository.patchWikiPage(any(), any(), any()) }
    }

    @Test
    fun `updateDescription for Wiki should call doOnPreExecute`() = runTest {
        val sut = createSut(TaskIdentifier.Wiki)
        var preExecuteCalled = false
        val newDescription = getRandomString()

        val payload = persistentMapOf<String, Any?>("content" to newDescription)
        every { patchDataGenerator.getWikiContent(newDescription) } returns payload
        coEvery {
            workItemRepository.patchWikiPage(any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.updateDescription(
            newDescription = newDescription,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertEquals(true, preExecuteCalled)
    }

    @Test
    fun `updateDescription for Wiki should update description on success`() = runTest {
        val sut = createSut(TaskIdentifier.Wiki)
        val newDescription = getRandomString()

        val payload = persistentMapOf<String, Any?>("content" to newDescription)
        every { patchDataGenerator.getWikiContent(newDescription) } returns payload
        coEvery {
            workItemRepository.patchWikiPage(any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.updateDescription(
            newDescription = newDescription,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.descriptionState.value
        assertEquals(newDescription, state.currentDescription)
        assertFalse(state.isDescriptionLoading)
    }

    @Test
    fun `updateDescription for Wiki should call doOnSuccess with new version`() = runTest {
        val sut = createSut(TaskIdentifier.Wiki)
        var receivedVersion: Long? = null
        val newVersion = getRandomLong()
        val newDescription = getRandomString()

        val payload = persistentMapOf<String, Any?>("content" to newDescription)
        every { patchDataGenerator.getWikiContent(newDescription) } returns payload
        coEvery {
            workItemRepository.patchWikiPage(any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)

        sut.updateDescription(
            newDescription = newDescription,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = { receivedVersion = it },
            doOnError = {}
        )

        assertEquals(newVersion, receivedVersion)
    }

    @Test
    fun `updateDescription for Wiki should clear loading on error`() = runTest {
        val sut = createSut(TaskIdentifier.Wiki)
        val newDescription = getRandomString()

        val payload = persistentMapOf<String, Any?>("content" to newDescription)
        every { patchDataGenerator.getWikiContent(newDescription) } returns payload
        coEvery {
            workItemRepository.patchWikiPage(any(), any(), any())
        } throws testException

        sut.updateDescription(
            newDescription = newDescription,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertFalse(sut.descriptionState.value.isDescriptionLoading)
    }

    @Test
    fun `updateDescription for Wiki should call doOnError on failure`() = runTest {
        val sut = createSut(TaskIdentifier.Wiki)
        var receivedError: Throwable? = null
        val newDescription = getRandomString()

        val payload = persistentMapOf<String, Any?>("content" to newDescription)
        every { patchDataGenerator.getWikiContent(newDescription) } returns payload
        coEvery {
            workItemRepository.patchWikiPage(any(), any(), any())
        } throws testException

        sut.updateDescription(
            newDescription = newDescription,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = { receivedError = it }
        )

        assertEquals(testException, receivedError)
    }

    @Test
    fun `updateDescription for Wiki should call repository with correct parameters`() = runTest {
        val sut = createSut(TaskIdentifier.Wiki)
        val version = getRandomLong()
        val pageId = getRandomLong()
        val newDescription = getRandomString()

        val payload = persistentMapOf<String, Any?>("content" to newDescription)
        every { patchDataGenerator.getWikiContent(newDescription) } returns payload
        coEvery {
            workItemRepository.patchWikiPage(any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.updateDescription(
            newDescription = newDescription,
            version = version,
            workItemId = pageId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify {
            workItemRepository.patchWikiPage(
                version = version,
                pageId = pageId,
                payload = payload
            )
        }
    }
}
