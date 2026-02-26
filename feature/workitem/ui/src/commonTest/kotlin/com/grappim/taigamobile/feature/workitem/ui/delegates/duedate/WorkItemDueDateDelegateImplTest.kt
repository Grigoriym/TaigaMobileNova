package com.grappim.taigamobile.feature.workitem.ui.delegates.duedate

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.nowLocalDate
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.uikit.theme.taigaGreenPositive
import com.grappim.taigamobile.uikit.theme.taigaOrange
import com.grappim.taigamobile.uikit.theme.taigaRed
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class WorkItemDueDateDelegateImplTest {

    private lateinit var sut: WorkItemDueDateDelegateImpl
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository = FakeWorkItemRepository()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()
    private val dateTimeUtils = FakeDateTimeUtils()

    @BeforeTest
    fun setup() {
        sut = WorkItemDueDateDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator,
            dateTimeUtils = dateTimeUtils
        )
    }

    @Test
    fun `initial state should have default values`() {
        val state = sut.dueDateState.value

        assertNull(state.dueDate)
        assertNull(state.dueDateStatus)
        assertEquals(NativeText.Empty, state.dueDateText)
        assertFalse(state.isDueDateDatePickerVisible)
        assertFalse(state.isDueDateLoading)
    }

    @Test
    fun `setDueDateDatePickerVisibility should update visibility`() {
        assertFalse(sut.dueDateState.value.isDueDateDatePickerVisible)

        sut.dueDateState.value.setDueDateDatePickerVisibility(true)

        assertTrue(sut.dueDateState.value.isDueDateDatePickerVisible)

        sut.dueDateState.value.setDueDateDatePickerVisibility(false)

        assertFalse(sut.dueDateState.value.isDueDateDatePickerVisible)
    }

    @Test
    fun `setInitialDueDate should update dueDate and dueDateStatus`() {
        val dueDate = nowLocalDate
        val dueDateStatus = DueDateStatus.Set

        sut.setInitialDueDate(dueDate, dueDateStatus)

        val state = sut.dueDateState.value
        assertEquals(dueDate, state.dueDate)
        assertEquals(dueDateStatus, state.dueDateStatus)
        assertEquals(NativeText.Simple(dueDate.toString()), state.dueDateText)
    }

    @Test
    fun `setInitialDueDate with null dueDate should set NativeText Resource`() {
        sut.setInitialDueDate(null, null)

        val state = sut.dueDateState.value
        assertNull(state.dueDate)
        assertNull(state.dueDateStatus)
        assertTrue(state.dueDateText is NativeText.Resource)
    }

    @Test
    fun `setInitialDueDate with DueDateStatus Set should set green background`() {
        sut.setInitialDueDate(nowLocalDate, DueDateStatus.Set)

        val backgroundColor = sut.dueDateState.value.backgroundColor
        assertTrue(backgroundColor is StaticColor)
        assertEquals(taigaGreenPositive, backgroundColor.color)
    }

    @Test
    fun `setInitialDueDate with DueDateStatus DueSoon should set orange background`() {
        sut.setInitialDueDate(nowLocalDate, DueDateStatus.DueSoon)

        val backgroundColor = sut.dueDateState.value.backgroundColor
        assertTrue(backgroundColor is StaticColor)
        assertEquals(taigaOrange, backgroundColor.color)
    }

    @Test
    fun `setInitialDueDate with DueDateStatus PastDue should set red background`() {
        sut.setInitialDueDate(nowLocalDate, DueDateStatus.PastDue)

        val backgroundColor = sut.dueDateState.value.backgroundColor
        assertTrue(backgroundColor is StaticColor)
        assertEquals(taigaRed, backgroundColor.color)
    }

    @Test
    fun `handleDueDateSave should call doOnPreExecute`() = runTest {
        var preExecuteCalled = false
        workItemRepository.patchDataResult = PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.handleDueDateSave(
            newDate = 1704067200000L,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleDueDateSave should update state on success`() = runTest {
        workItemRepository.patchDataResult = PatchedData(newVersion = 2L, dueDateStatus = DueDateStatus.Set)

        sut.handleDueDateSave(
            newDate = 1704067200000L,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.dueDateState.value
        assertFalse(state.isDueDateLoading)
        assertEquals(dateTimeUtils.fixedDate, state.dueDate)
        assertEquals(DueDateStatus.Set, state.dueDateStatus)
    }

    @Test
    fun `handleDueDateSave should call doOnSuccess with patchedData`() = runTest {
        var receivedPatchedData: PatchedData? = null
        val expectedPatchedData = PatchedData(newVersion = 2L, dueDateStatus = DueDateStatus.Set)
        workItemRepository.patchDataResult = expectedPatchedData

        sut.handleDueDateSave(
            newDate = 1704067200000L,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = { receivedPatchedData = it },
            doOnError = {}
        )

        assertEquals(expectedPatchedData, receivedPatchedData)
    }

    @Test
    fun `handleDueDateSave should handle null date`() = runTest {
        workItemRepository.patchDataResult = PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.handleDueDateSave(
            newDate = null,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.dueDateState.value
        assertFalse(state.isDueDateLoading)
        assertNull(state.dueDate)
    }

    @Test
    fun `handleDueDateSave should update state on error`() = runTest {
        workItemRepository.patchDataThrows = testException

        sut.handleDueDateSave(
            newDate = 1704067200000L,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertFalse(sut.dueDateState.value.isDueDateLoading)
    }

    @Test
    fun `handleDueDateSave should call doOnError on failure`() = runTest {
        var receivedError: Throwable? = null
        workItemRepository.patchDataThrows = testException

        sut.handleDueDateSave(
            newDate = 1704067200000L,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = { receivedError = it }
        )

        assertEquals(testException, receivedError)
    }

    @Test
    fun `handleDueDateSave should call repository with correct parameters`() = runTest {
        val version = getRandomLong()
        val workItemId = getRandomLong()
        workItemRepository.patchDataResult = PatchedData(newVersion = 2L, dueDateStatus = null)

        sut.handleDueDateSave(
            newDate = 1704067200000L,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val call = workItemRepository.patchDataCalls.single()
        assertEquals(version, call.version)
        assertEquals(workItemId, call.workItemId)
        assertEquals(commonTaskType, call.commonTaskType)
        assertEquals(persistentMapOf("due_date" to dateTimeUtils.fixedDate.toString()), call.payload)
    }
}
