package com.grappim.taigamobile.feature.workitem.ui.delegates.block

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemBlockDelegateImplTest {

    private lateinit var sut: WorkItemBlockDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()

    @Before
    fun setup() {
        sut = WorkItemBlockDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            patchDataGenerator = patchDataGenerator
        )
    }

    @Test
    fun `on setIsBlockDialogVisible, should update field`() {
        assertFalse(sut.blockState.value.isBlockDialogVisible)

        sut.blockState.value.setIsBlockDialogVisible(true)

        assertTrue(sut.blockState.value.isBlockDialogVisible)
    }
}
