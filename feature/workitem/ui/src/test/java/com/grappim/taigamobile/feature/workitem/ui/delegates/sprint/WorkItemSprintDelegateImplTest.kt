package com.grappim.taigamobile.feature.workitem.ui.delegates.sprint

import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.nowLocalDate
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WorkItemSprintDelegateImplTest {

    private val dateTimeUtils: DateTimeUtils = mockk()
    private val sprintsRepository: SprintsRepository = mockk()

    private lateinit var sut: WorkItemSprintDelegateImpl

    @Before
    fun setup() {
        sut = WorkItemSprintDelegateImpl(
            dateTimeUtils = dateTimeUtils,
            sprintsRepository = sprintsRepository
        )
    }

    @Test
    fun `setInitialSprint should set sprint name and dates`() = runTest {
        val sprintName = getRandomString()
        val startDate = nowLocalDate
        val endDate = nowLocalDate.plusDays(14)
        val formattedStart = "Jan 1, 2024"
        val formattedEnd = "Jan 15, 2024"

        every { dateTimeUtils.formatToMediumFormat(startDate) } returns formattedStart
        every { dateTimeUtils.formatToMediumFormat(endDate) } returns formattedEnd

        sut.setInitialSprint(startDate, endDate, sprintName)

        val state = sut.sprintDialogState.value
        assertEquals(sprintName, state.sprintNameValue)
        assertEquals(startDate, state.startDate)
        assertEquals(endDate, state.endDate)
        assertEquals(formattedStart, state.startDateToDisplay)
        assertEquals(formattedEnd, state.endDateToDisplay)
    }

    @Test
    fun `setInitialSprint should use default dates when null`() = runTest {
        val sprintName = getRandomString()
        val formattedStartDate = "Jan 1, 2024"
        val formattedEndDate = "Jan 13, 2024"

        val startDate = nowLocalDate
        val endDate = nowLocalDate.plusDays(14)

        every { dateTimeUtils.getLocalDateNow() } returns startDate
        every { dateTimeUtils.formatToMediumFormat(startDate) } returns formattedStartDate
        every { dateTimeUtils.formatToMediumFormat(endDate) } returns formattedEndDate

        sut.setInitialSprint(null, null, sprintName)

        val state = sut.sprintDialogState.value
        assertEquals(sprintName, state.sprintNameValue)
        assertEquals(startDate, state.startDate)
        assertEquals(endDate, state.endDate)
        assertEquals(formattedStartDate, state.startDateToDisplay)
        assertEquals(formattedEndDate, state.endDateToDisplay)
    }

    @Test
    fun `setSprintDialogVisibility should update visibility`() = runTest {
        sut.setSprintDialogVisibility(true)
        assertTrue(sut.sprintDialogState.value.isSprintDialogVisible)

        sut.setSprintDialogVisibility(false)
        assertFalse(sut.sprintDialogState.value.isSprintDialogVisible)
    }

    @Test
    fun `createSprint should show error when name is empty`() = runTest {
        setupValidDates()

        sut.createSprint()

        val state = sut.sprintDialogState.value
        assertEquals(NativeText.Resource(RString.sprint_name_empty), state.sprintNameError)
    }

    @Test
    fun `createSprint should show error when name is blank`() = runTest {
        setupValidDates()
        sut.sprintDialogState.value.onSetSprintNameValue("   ")

        sut.createSprint()

        val state = sut.sprintDialogState.value
        assertEquals(NativeText.Resource(RString.sprint_name_empty), state.sprintNameError)
    }

    @Test
    fun `createSprint should show error when start date is null`() = runTest {
        sut.sprintDialogState.value.onSetSprintNameValue(getRandomString())

        sut.createSprint()

        val state = sut.sprintDialogState.value
        assertEquals(NativeText.Resource(RString.sprint_start_date_empty), state.dialogError)
    }

    @Test
    fun `createSprint should call repository on success`() = runTest {
        val sprintName = getRandomString()
        val startDate = nowLocalDate
        val endDate = nowLocalDate.plusDays(14)

        setupSprintState(sprintName, startDate, endDate)
        coJustRun { sprintsRepository.createSprint(sprintName, startDate, endDate) }

        var successCalled = false
        sut.createSprint(doOnSuccess = { successCalled = true })

        coVerify { sprintsRepository.createSprint(sprintName, startDate, endDate) }
        assertTrue(successCalled)
        assertFalse(sut.sprintDialogState.value.isSprintDialogVisible)
    }

    @Test
    fun `createSprint should call doOnPreExecute before repository call`() = runTest {
        val sprintName = getRandomString()
        val startDate = nowLocalDate
        val endDate = nowLocalDate.plusDays(14)

        setupSprintState(sprintName, startDate, endDate)
        coJustRun { sprintsRepository.createSprint(any(), any(), any()) }

        var preExecuteCalled = false
        sut.createSprint(doOnPreExecute = { preExecuteCalled = true })

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `editSprint should show error when name is empty`() = runTest {
        setupValidDates()

        sut.editSprint(sprintId = getRandomLong())

        val state = sut.sprintDialogState.value
        assertEquals(NativeText.Resource(RString.sprint_name_empty), state.sprintNameError)
    }

    @Test
    fun `editSprint should show error when start date is null`() = runTest {
        sut.sprintDialogState.value.onSetSprintNameValue(getRandomString())

        sut.editSprint(sprintId = getRandomLong())

        val state = sut.sprintDialogState.value
        assertEquals(NativeText.Resource(RString.sprint_start_date_empty), state.dialogError)
    }

    @Test
    fun `editSprint should call repository on success`() = runTest {
        val sprintId = getRandomLong()
        val sprintName = getRandomString()
        val startDate = nowLocalDate
        val endDate = nowLocalDate.plusDays(14)

        setupSprintState(sprintName, startDate, endDate)
        coJustRun { sprintsRepository.editSprint(sprintId, sprintName, startDate, endDate) }

        var successCalled = false
        sut.editSprint(sprintId = sprintId, doOnSuccess = { successCalled = true })

        coVerify { sprintsRepository.editSprint(sprintId, sprintName, startDate, endDate) }
        assertTrue(successCalled)
        assertFalse(sut.sprintDialogState.value.isSprintDialogVisible)
    }

    @Test
    fun `onSetSprintNameValue should update name and clear errors`() = runTest {
        val newName = getRandomString()

        sut.sprintDialogState.value.onSetSprintNameValue(newName)

        val state = sut.sprintDialogState.value
        assertEquals(newName, state.sprintNameValue)
        assertEquals(NativeText.Empty, state.sprintNameError)
        assertEquals(NativeText.Empty, state.dialogError)
    }

    @Test
    fun `onDismiss should reset state`() = runTest {
        val sprintName = getRandomString()
        val startDate = nowLocalDate
        val endDate = nowLocalDate.plusDays(14)

        setupSprintState(sprintName, startDate, endDate)
        sut.setSprintDialogVisibility(true)

        sut.sprintDialogState.value.onDismiss()

        val state = sut.sprintDialogState.value
        assertFalse(state.isSprintDialogVisible)
        assertEquals("", state.sprintNameValue)
        assertEquals(NativeText.Empty, state.sprintNameError)
        assertEquals(NativeText.Empty, state.dialogError)
    }

    @Test
    fun `setIsStartDateDialogVisible should update visibility`() = runTest {
        sut.sprintDialogState.value.setIsStartDateDialogVisible(true)
        assertTrue(sut.sprintDialogState.value.isStartDateDialogVisible)

        sut.sprintDialogState.value.setIsStartDateDialogVisible(false)
        assertFalse(sut.sprintDialogState.value.isStartDateDialogVisible)
    }

    @Test
    fun `onStartDateConfirmButtonClick should update start date`() = runTest {
        val millis = 1704067200000L
        val localDate = nowLocalDate
        val formattedDate = "Jan 1, 2024"

        every { dateTimeUtils.fromMillisToLocalDate(millis) } returns localDate
        every { dateTimeUtils.formatToMediumFormat(localDate) } returns formattedDate

        sut.sprintDialogState.value.onStartDateConfirmButtonClick(millis)

        val state = sut.sprintDialogState.value
        assertFalse(state.isStartDateDialogVisible)
        assertEquals(localDate, state.startDate)
        assertEquals(formattedDate, state.startDateToDisplay)
    }

    @Test
    fun `onStartDateConfirmButtonClick should do nothing when millis is null`() = runTest {
        sut.sprintDialogState.value.onStartDateConfirmButtonClick(null)

        verify(exactly = 0) { dateTimeUtils.fromMillisToLocalDate(any()) }
    }

    @Test
    fun `setIsEndDateDialogVisible should update visibility`() = runTest {
        sut.sprintDialogState.value.setIsEndDateDialogVisible(true)
        assertTrue(sut.sprintDialogState.value.isEndDateDialogVisible)

        sut.sprintDialogState.value.setIsEndDateDialogVisible(false)
        assertFalse(sut.sprintDialogState.value.isEndDateDialogVisible)
    }

    @Test
    fun `onEndDateConfirmButtonClick should update end date`() = runTest {
        val millis = 1704067200000L
        val localDate = nowLocalDate
        val formattedDate = "Jan 15, 2024"

        every { dateTimeUtils.fromMillisToLocalDate(millis) } returns localDate
        every { dateTimeUtils.formatToMediumFormat(localDate) } returns formattedDate

        sut.sprintDialogState.value.onEndDateConfirmButtonClick(millis)

        val state = sut.sprintDialogState.value
        assertFalse(state.isEndDateDialogVisible)
        assertEquals(localDate, state.endDate)
        assertEquals(formattedDate, state.endDateToDisplay)
    }

    private fun setupValidDates() {
        every { dateTimeUtils.formatToMediumFormat(any<LocalDate>()) } returns "date"
    }

    private fun setupSprintState(name: String, startDate: LocalDate, endDate: LocalDate) {
        every { dateTimeUtils.formatToMediumFormat(any<LocalDate>()) } returns "date"
        sut.setInitialSprint(startDate, endDate, name)
    }
}
