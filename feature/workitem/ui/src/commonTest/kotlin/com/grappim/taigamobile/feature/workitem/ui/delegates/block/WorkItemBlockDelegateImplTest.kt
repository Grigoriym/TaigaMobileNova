package com.grappim.taigamobile.feature.workitem.ui.delegates.block

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkItemBlockDelegateImplTest {

    private lateinit var sut: WorkItemBlockDelegate
    private val commonTaskType = CommonTaskType.Issue
    private lateinit var workItemRepository: FakeWorkItemRepository
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()

    @BeforeTest
    fun setup() {
        workItemRepository = FakeWorkItemRepository()
        sut = WorkItemBlockDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator
        )
    }

    @Test
    fun `on setIsBlockDialogVisible should update field`() {
        assertFalse(sut.blockState.value.isBlockDialogVisible)

        sut.blockState.value.setIsBlockDialogVisible(true)

        assertTrue(sut.blockState.value.isBlockDialogVisible)
    }

    @Test
    fun `setIsBlockDialogVisible should toggle back to false`() {
        sut.blockState.value.setIsBlockDialogVisible(true)
        assertTrue(sut.blockState.value.isBlockDialogVisible)

        sut.blockState.value.setIsBlockDialogVisible(false)

        assertFalse(sut.blockState.value.isBlockDialogVisible)
    }

    @Test
    fun `handleBlockToggle should call doOnPreExecute`() = runTest {
        var preExecuteCalled = false
        workItemRepository.patchDataResult = PatchedData(getRandomLong(), null)

        sut.handleBlockToggle(
            isBlocked = true,
            blockNote = getRandomString(),
            version = getRandomLong(),
            workItemId = getRandomLong(),
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleBlockToggle should call doOnSuccess with correct BlockToggleResult on success`() = runTest {
        val blockNote = getRandomString()
        val newVersion = getRandomLong()
        val patchedData = PatchedData(newVersion, null)
        workItemRepository.patchDataResult = patchedData
        var result: BlockToggleResult? = null

        sut.handleBlockToggle(
            isBlocked = true,
            blockNote = blockNote,
            version = getRandomLong(),
            workItemId = getRandomLong(),
            doOnPreExecute = null,
            doOnSuccess = { result = it },
            doOnError = {}
        )

        assertEquals(blockNote, result?.blockNote)
        assertEquals(patchedData, result?.patchedData)
    }

    @Test
    fun `handleBlockToggle should call repository with correct parameters`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        workItemRepository.patchDataResult = PatchedData(getRandomLong(), null)

        sut.handleBlockToggle(
            isBlocked = false,
            blockNote = null,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertEquals(1, workItemRepository.patchDataCalls.size)
        val call = workItemRepository.patchDataCalls.first()
        assertEquals(version, call.version)
        assertEquals(workItemId, call.workItemId)
        assertEquals(commonTaskType, call.commonTaskType)
    }

    @Test
    fun `handleBlockToggle should call doOnError on failure`() = runTest {
        workItemRepository.patchDataThrows = testException
        var receivedError: Throwable? = null

        sut.handleBlockToggle(
            isBlocked = true,
            blockNote = null,
            version = getRandomLong(),
            workItemId = getRandomLong(),
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = { receivedError = it }
        )

        assertEquals(testException, receivedError)
    }

    @Test
    fun `handleBlockToggle on failure should not call doOnSuccess`() = runTest {
        workItemRepository.patchDataThrows = testException
        var successCalled = false

        sut.handleBlockToggle(
            isBlocked = true,
            blockNote = null,
            version = getRandomLong(),
            workItemId = getRandomLong(),
            doOnPreExecute = null,
            doOnSuccess = { successCalled = true },
            doOnError = {}
        )

        assertFalse(successCalled)
    }

    @Test
    fun `handleBlockToggle with null blockNote should pass empty string as blocked_note`() = runTest {
        workItemRepository.patchDataResult = PatchedData(getRandomLong(), null)
        var result: BlockToggleResult? = null

        sut.handleBlockToggle(
            isBlocked = true,
            blockNote = null,
            version = getRandomLong(),
            workItemId = getRandomLong(),
            doOnPreExecute = null,
            doOnSuccess = { result = it },
            doOnError = {}
        )

        assertNull(result?.blockNote)
        assertEquals(1, workItemRepository.patchDataCalls.size)
    }
}
