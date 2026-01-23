package com.grappim.taigamobile.feature.workitem.ui.delegates.duedate

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.nowLocalDate
import com.grappim.taigamobile.testing.testException
import com.grappim.taigamobile.uikit.theme.taigaGreenPositive
import com.grappim.taigamobile.uikit.theme.taigaOrange
import com.grappim.taigamobile.uikit.theme.taigaRed
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticColor
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkItemDueDateDelegateImplTest {

    private lateinit var sut: WorkItemDueDateDelegateImpl
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()
    private val dateTimeUtils: DateTimeUtils = mockk()

    @Before
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
        val formattedDate = getRandomString()

        every { dateTimeUtils.formatToMediumFormat(dueDate) } returns formattedDate

        sut.setInitialDueDate(dueDate, dueDateStatus)

        val state = sut.dueDateState.value
        assertEquals(dueDate, state.dueDate)
        assertEquals(dueDateStatus, state.dueDateStatus)
        assertEquals(NativeText.Simple(formattedDate), state.dueDateText)
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
        val dueDate = nowLocalDate
        every { dateTimeUtils.formatToMediumFormat(dueDate) } returns getRandomString()

        sut.setInitialDueDate(dueDate, DueDateStatus.Set)

        val backgroundColor = sut.dueDateState.value.backgroundColor
        assertTrue(backgroundColor is StaticColor)
        assertEquals(taigaGreenPositive, backgroundColor.color)
    }

    @Test
    fun `setInitialDueDate with DueDateStatus DueSoon should set orange background`() {
        val dueDate = nowLocalDate
        every { dateTimeUtils.formatToMediumFormat(dueDate) } returns getRandomString()

        sut.setInitialDueDate(dueDate, DueDateStatus.DueSoon)

        val backgroundColor = sut.dueDateState.value.backgroundColor
        assertTrue(backgroundColor is StaticColor)
        assertEquals(taigaOrange, backgroundColor.color)
    }

    @Test
    fun `setInitialDueDate with DueDateStatus PastDue should set red background`() {
        val dueDate = nowLocalDate
        every { dateTimeUtils.formatToMediumFormat(dueDate) } returns getRandomString()

        sut.setInitialDueDate(dueDate, DueDateStatus.PastDue)

        val backgroundColor = sut.dueDateState.value.backgroundColor
        assertTrue(backgroundColor is StaticColor)
        assertEquals(taigaRed, backgroundColor.color)
    }

    @Test
    fun `handleDueDateSave should call doOnPreExecute`() = runTest {
        var preExecuteCalled = false
        val newDateMillis = 1704067200000L
        val localDate = nowLocalDate
        val dateString = "2024-01-01"

        every { dateTimeUtils.fromMillisToLocalDate(newDateMillis) } returns localDate
        every { dateTimeUtils.parseLocalDateToString(localDate) } returns dateString
        every { dateTimeUtils.formatToMediumFormat(localDate) } returns getRandomString()

        val payload = persistentMapOf<String, Any?>("due_date" to dateString)
        every { patchDataGenerator.getDueDatePatchPayload(dateString) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = DueDateStatus.Set)

        sut.handleDueDateSave(
            newDate = newDateMillis,
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
        val newDateMillis = 1704067200000L
        val localDate = nowLocalDate
        val dateString = "2024-01-01"
        val formattedDate = getRandomString()

        every { dateTimeUtils.fromMillisToLocalDate(newDateMillis) } returns localDate
        every { dateTimeUtils.parseLocalDateToString(localDate) } returns dateString
        every { dateTimeUtils.formatToMediumFormat(localDate) } returns formattedDate

        val payload = persistentMapOf<String, Any?>("due_date" to dateString)
        every { patchDataGenerator.getDueDatePatchPayload(dateString) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = DueDateStatus.Set)

        sut.handleDueDateSave(
            newDate = newDateMillis,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.dueDateState.value
        assertFalse(state.isDueDateLoading)
        assertEquals(localDate, state.dueDate)
        assertEquals(DueDateStatus.Set, state.dueDateStatus)
    }

    @Test
    fun `handleDueDateSave should call doOnSuccess with patchedData`() = runTest {
        var receivedPatchedData: PatchedData? = null
        val newDateMillis = 1704067200000L
        val localDate = nowLocalDate
        val dateString = "2024-01-01"
        val expectedPatchedData = PatchedData(newVersion = 2L, dueDateStatus = DueDateStatus.Set)

        every { dateTimeUtils.fromMillisToLocalDate(newDateMillis) } returns localDate
        every { dateTimeUtils.parseLocalDateToString(localDate) } returns dateString
        every { dateTimeUtils.formatToMediumFormat(localDate) } returns getRandomString()

        val payload = persistentMapOf<String, Any?>("due_date" to dateString)
        every { patchDataGenerator.getDueDatePatchPayload(dateString) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns expectedPatchedData

        sut.handleDueDateSave(
            newDate = newDateMillis,
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
        val payload = persistentMapOf<String, Any?>("due_date" to null)
        every { patchDataGenerator.getDueDatePatchPayload(null) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = null)

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
        val newDateMillis = 1704067200000L
        val localDate = nowLocalDate
        val dateString = "2024-01-01"

        every { dateTimeUtils.fromMillisToLocalDate(newDateMillis) } returns localDate
        every { dateTimeUtils.parseLocalDateToString(localDate) } returns dateString

        val payload = persistentMapOf<String, Any?>("due_date" to dateString)
        every { patchDataGenerator.getDueDatePatchPayload(dateString) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.handleDueDateSave(
            newDate = newDateMillis,
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
        val newDateMillis = 1704067200000L
        val localDate = nowLocalDate
        val dateString = "2024-01-01"

        every { dateTimeUtils.fromMillisToLocalDate(newDateMillis) } returns localDate
        every { dateTimeUtils.parseLocalDateToString(localDate) } returns dateString

        val payload = persistentMapOf<String, Any?>("due_date" to dateString)
        every { patchDataGenerator.getDueDatePatchPayload(dateString) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.handleDueDateSave(
            newDate = newDateMillis,
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
        val newDateMillis = 1704067200000L
        val localDate = nowLocalDate
        val dateString = "2024-01-01"
        val version = getRandomLong()
        val workItemId = getRandomLong()

        every { dateTimeUtils.fromMillisToLocalDate(newDateMillis) } returns localDate
        every { dateTimeUtils.parseLocalDateToString(localDate) } returns dateString
        every { dateTimeUtils.formatToMediumFormat(localDate) } returns getRandomString()

        val payload = persistentMapOf<String, Any?>("due_date" to dateString)
        every { patchDataGenerator.getDueDatePatchPayload(dateString) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = 2L, dueDateStatus = DueDateStatus.Set)

        sut.handleDueDateSave(
            newDate = newDateMillis,
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
