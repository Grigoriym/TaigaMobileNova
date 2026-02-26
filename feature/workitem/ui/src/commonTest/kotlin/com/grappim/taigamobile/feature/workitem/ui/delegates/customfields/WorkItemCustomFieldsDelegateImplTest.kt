package com.grappim.taigamobile.feature.workitem.ui.delegates.customfields

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.utils.FakeDateTimeUtils
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class WorkItemCustomFieldsDelegateImplTest {

    private lateinit var sut:
        WorkItemCustomFieldsDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = FakeWorkItemRepository()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()
    private val dateTimeUtils: DateTimeUtils = FakeDateTimeUtils()

    @BeforeTest
    fun setup() {
        sut =
            WorkItemCustomFieldsDelegateImpl(
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
