package com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemSingleAssigneeDelegateImplTest {

    private lateinit var sut: WorkItemSingleAssigneeDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = mockk()
    private val usersRepository: UsersRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()

    private val workItemEditShared: WorkItemEditShared = mockk()

    @Before
    fun setup() {
        sut = WorkItemSingleAssigneeDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            usersRepository = usersRepository,
            patchDataGenerator = patchDataGenerator,
            workItemEditShared = workItemEditShared
        )
    }

    @Test
    fun `on setIsRemoveAssigneeDialogVisible, should update field`() {
        assertFalse(sut.singleAssigneeState.value.isRemoveAssigneeDialogVisible)

        sut.singleAssigneeState.value.setIsRemoveAssigneeDialogVisible(true)

        assertTrue(sut.singleAssigneeState.value.isRemoveAssigneeDialogVisible)
    }

    @Test
    fun `on onRemoveAssigneeClick, should show remove assignee dialog`() {
        assertFalse(sut.singleAssigneeState.value.isRemoveAssigneeDialogVisible)

        sut.singleAssigneeState.value.onRemoveAssigneeClick()

        assertTrue(sut.singleAssigneeState.value.isRemoveAssigneeDialogVisible)
    }
}
