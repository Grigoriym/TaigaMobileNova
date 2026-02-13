package com.grappim.taigamobile.feature.workitem.ui.delegates.title

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.testException
import com.grappim.taigamobile.utils.ui.NativeText
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemTitleDelegateImplTest {

    private lateinit var sut: WorkItemTitleDelegateImpl
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()

    @Before
    fun setup() {
        sut = WorkItemTitleDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator
        )
    }

    @Test
    fun `initial state should have empty values`() {
        val state = sut.titleState.value

        assertEquals("", state.currentTitle)
        assertEquals("", state.originalTitle)
        assertFalse(state.isTitleEditable)
        assertFalse(state.isTitleLoading)
        assertEquals(NativeText.Empty, state.titleError)
    }

    @Test
    fun `setInitialTitle should update both currentTitle and originalTitle`() {
        val title = getRandomString()

        sut.setInitialTitle(title)

        val state = sut.titleState.value
        assertEquals(title, state.currentTitle)
        assertEquals(title, state.originalTitle)
    }

    @Test
    fun `onTitleChange should update currentTitle`() {
        val initialTitle = getRandomString()
        val newTitle = getRandomString()
        sut.setInitialTitle(initialTitle)

        sut.titleState.value.onTitleChange(newTitle)

        assertEquals(newTitle, sut.titleState.value.currentTitle)
        assertEquals(initialTitle, sut.titleState.value.originalTitle)
    }

    @Test
    fun `setIsTitleEditable should update isTitleEditable`() {
        assertFalse(sut.titleState.value.isTitleEditable)

        sut.titleState.value.setIsTitleEditable(true)

        assertTrue(sut.titleState.value.isTitleEditable)
    }

    @Test
    fun `onCancelClick should reset title to original and clear editable state`() {
        val originalTitle = getRandomString()
        val modifiedTitle = getRandomString()
        sut.setInitialTitle(originalTitle)
        sut.titleState.value.onTitleChange(modifiedTitle)
        sut.titleState.value.setIsTitleEditable(true)

        sut.titleState.value.onCancelClick()

        val state = sut.titleState.value
        assertEquals(originalTitle, state.currentTitle)
        assertEquals(originalTitle, state.originalTitle)
        assertFalse(state.isTitleEditable)
        assertFalse(state.isTitleLoading)
        assertEquals(NativeText.Empty, state.titleError)
    }

    @Test
    fun `handleTitleSave should not call repository when title unchanged`() = runTest {
        val title = getRandomString()
        sut.setInitialTitle(title)

        sut.handleTitleSave(
            version = getRandomLong(),
            workItemId = getRandomLong(),
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify(exactly = 0) { workItemRepository.patchData(any(), any(), any(), any()) }
        assertFalse(sut.titleState.value.isTitleEditable)
    }

    @Test
    fun `handleTitleSave should call doOnPreExecute`() = runTest {
        val originalTitle = getRandomString()
        val newTitle = getRandomString()
        var preExecuteCalled = false
        sut.setInitialTitle(originalTitle)
        sut.titleState.value.onTitleChange(newTitle)

        val payload = persistentMapOf<String, Any?>("subject" to newTitle)
        coEvery { patchDataGenerator.getTitle(newTitle) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.handleTitleSave(
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleTitleSave should update state on success`() = runTest {
        val originalTitle = getRandomString()
        val newTitle = getRandomString()
        val newVersion = getRandomLong()
        sut.setInitialTitle(originalTitle)
        sut.titleState.value.onTitleChange(newTitle)

        val payload = persistentMapOf<String, Any?>("subject" to newTitle)
        coEvery { patchDataGenerator.getTitle(newTitle) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)

        sut.handleTitleSave(
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.titleState.value
        assertEquals(newTitle, state.currentTitle)
        assertEquals(newTitle, state.originalTitle)
        assertFalse(state.isTitleEditable)
        assertFalse(state.isTitleLoading)
        assertEquals(NativeText.Empty, state.titleError)
    }

    @Test
    fun `handleTitleSave should call doOnSuccess with new version`() = runTest {
        val originalTitle = getRandomString()
        val newTitle = getRandomString()
        val newVersion = getRandomLong()
        var receivedVersion: Long? = null
        sut.setInitialTitle(originalTitle)
        sut.titleState.value.onTitleChange(newTitle)

        val payload = persistentMapOf<String, Any?>("subject" to newTitle)
        coEvery { patchDataGenerator.getTitle(newTitle) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)

        sut.handleTitleSave(
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = { receivedVersion = it },
            doOnError = {}
        )

        assertEquals(newVersion, receivedVersion)
    }

    @Test
    fun `handleTitleSave should update state on error`() = runTest {
        val originalTitle = getRandomString()
        val newTitle = getRandomString()
        sut.setInitialTitle(originalTitle)
        sut.titleState.value.onTitleChange(newTitle)

        val payload = persistentMapOf<String, Any?>("subject" to newTitle)
        coEvery { patchDataGenerator.getTitle(newTitle) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.handleTitleSave(
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.titleState.value
        assertFalse(state.isTitleLoading)
        assertTrue(state.titleError !is NativeText.Empty)
    }

    @Test
    fun `handleTitleSave should call doOnError on failure`() = runTest {
        val originalTitle = getRandomString()
        val newTitle = getRandomString()
        var receivedError: Throwable? = null
        sut.setInitialTitle(originalTitle)
        sut.titleState.value.onTitleChange(newTitle)

        val payload = persistentMapOf<String, Any?>("subject" to newTitle)
        coEvery { patchDataGenerator.getTitle(newTitle) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.handleTitleSave(
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = { receivedError = it }
        )

        assertEquals(testException, receivedError)
    }

    @Test
    fun `handleTitleSave should call repository with correct parameters`() = runTest {
        val originalTitle = getRandomString()
        val newTitle = getRandomString()
        val version = getRandomLong()
        val workItemId = getRandomLong()
        sut.setInitialTitle(originalTitle)
        sut.titleState.value.onTitleChange(newTitle)

        val payload = persistentMapOf<String, Any?>("subject" to newTitle)
        coEvery { patchDataGenerator.getTitle(newTitle) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.handleTitleSave(
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
}
