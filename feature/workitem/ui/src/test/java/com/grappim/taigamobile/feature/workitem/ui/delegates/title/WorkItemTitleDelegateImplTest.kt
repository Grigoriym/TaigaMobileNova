package com.grappim.taigamobile.feature.workitem.ui.delegates.title

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import io.mockk.mockk
import org.junit.Before

class WorkItemTitleDelegateImplTest {

    private lateinit var sut: WorkItemTitleDelegate
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
}
