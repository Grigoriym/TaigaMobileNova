package com.grappim.taigamobile.feature.workitem.ui.delegates.watchers

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditShared
import com.grappim.taigamobile.testing.getRandomLong
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkItemWatchersDelegateImplTest {

    private lateinit var sut: WorkItemWatchersDelegate
    private val commonTaskType = CommonTaskType.Issue
    private val workItemRepository: WorkItemRepository = mockk()
    private val usersRepository: UsersRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()
    private val session: Session = mockk()
    private val workItemEditShared: WorkItemEditShared = mockk()

    @Before
    fun setup() {
        sut = WorkItemWatchersDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            usersRepository = usersRepository,
            patchDataGenerator = patchDataGenerator,
            session = session,
            workItemEditShared = workItemEditShared
        )
    }

    @Test
    fun `on setIsRemoveWatcherDialogVisible, should update field`() {
        assertFalse(sut.watchersState.value.isRemoveWatcherDialogVisible)

        sut.watchersState.value.setIsRemoveWatcherDialogVisible(true)

        assertTrue(sut.watchersState.value.isRemoveWatcherDialogVisible)
    }

    @Test
    fun `on onRemoveWatcherClick, should set watcherIdToRemove and show dialog`() {
        val watcherId = getRandomLong()
        assertNull(sut.watchersState.value.watcherIdToRemove)
        assertFalse(sut.watchersState.value.isRemoveWatcherDialogVisible)

        sut.watchersState.value.onRemoveWatcherClick(watcherId)

        assertEquals(watcherId, sut.watchersState.value.watcherIdToRemove)
        assertTrue(sut.watchersState.value.isRemoveWatcherDialogVisible)
    }
}
