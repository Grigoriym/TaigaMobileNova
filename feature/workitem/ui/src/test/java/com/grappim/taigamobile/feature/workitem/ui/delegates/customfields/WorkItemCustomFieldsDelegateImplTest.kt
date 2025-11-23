package com.grappim.taigamobile.feature.workitem.ui.delegates.customfields

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemCustomFieldsDelegateImplTest {

    private lateinit var sut: WorkItemCustomFieldsDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()
    private val dateTimeUtils: DateTimeUtils = mockk()

    @Before
    fun setup() {
        sut = WorkItemCustomFieldsDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator,
            dateTimeUtils = dateTimeUtils
        )
    }

    @Test
    fun `on setIsCustomFieldsWidgetExpanded, should update field`() {
        assertFalse(sut.customFieldsState.value.isCustomFieldsWidgetExpanded)

        sut.customFieldsState.value.setIsCustomFieldsWidgetExpanded(true)

        assertTrue(sut.customFieldsState.value.isCustomFieldsWidgetExpanded)
    }
}
