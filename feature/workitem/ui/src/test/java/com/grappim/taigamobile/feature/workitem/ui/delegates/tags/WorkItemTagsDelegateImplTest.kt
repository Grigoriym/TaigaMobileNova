package com.grappim.taigamobile.feature.workitem.ui.delegates.tags

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import io.mockk.mockk
import org.junit.Before

class WorkItemTagsDelegateImplTest {

    private lateinit var sut: WorkItemTagsDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()
    private val workItemEditShared: WorkItemEditShared = mockk()

    @Before
    fun setup() {
        sut = WorkItemTagsDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator,
            workItemEditShared = workItemEditShared
        )
    }
}
