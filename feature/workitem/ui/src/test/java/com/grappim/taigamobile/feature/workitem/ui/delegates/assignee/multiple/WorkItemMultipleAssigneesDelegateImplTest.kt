package com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.multiple

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import io.mockk.mockk
import org.junit.Before

class WorkItemMultipleAssigneesDelegateImplTest {

    private lateinit var sut: WorkItemMultipleAssigneesDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = mockk()
    private val usersRepository: UsersRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()
    private val workItemEditShared: WorkItemEditShared = mockk()
    private val session: Session = mockk()

    @Before
    fun setup() {
        sut = WorkItemMultipleAssigneesDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            usersRepository = usersRepository,
            patchDataGenerator = patchDataGenerator,
            workItemEditShared = workItemEditShared,
            session = session
        )
    }
}
