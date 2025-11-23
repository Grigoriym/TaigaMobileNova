package com.grappim.taigamobile.feature.workitem.ui.delegates.description

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import io.mockk.mockk
import org.junit.Before

class WorkItemDescriptionDelegateImplTest {

    private lateinit var sut: WorkItemDescriptionDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()

    @Before
    fun setup() {
        sut = WorkItemDescriptionDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator
        )
    }
}
