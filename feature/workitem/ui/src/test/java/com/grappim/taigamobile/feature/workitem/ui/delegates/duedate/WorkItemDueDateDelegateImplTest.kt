package com.grappim.taigamobile.feature.workitem.ui.delegates.duedate

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemDueDateDelegateImplTest {

    private lateinit var sut: WorkItemDueDateDelegate
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
    fun `on setDueDateDatePickerVisibility, should set isDueDateDatePickerVisible`() {
        assertFalse(sut.dueDateState.value.isDueDateDatePickerVisible)

        sut.dueDateState.value.setDueDateDatePickerVisibility(true)

        assertTrue(sut.dueDateState.value.isDueDateDatePickerVisible)
    }
}
