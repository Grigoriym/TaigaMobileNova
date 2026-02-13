package com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getUser
import com.grappim.taigamobile.testing.testException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemSingleAssigneeDelegateImplTest {

    private val workItemRepository: WorkItemRepository = mockk()
    private val usersRepository: UsersRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()

    private fun createSut(commonTaskType: CommonTaskType = CommonTaskType.Task): WorkItemSingleAssigneeDelegateImpl =
        WorkItemSingleAssigneeDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            usersRepository = usersRepository,
            patchDataGenerator = patchDataGenerator
        )

    @Test
    fun `initial state should have empty assignees and no loading`() {
        val sut = createSut()

        val state = sut.singleAssigneeState.value

        assertTrue(state.assignees.isEmpty())
        assertFalse(state.isAssigneesLoading)
        assertFalse(state.isAssignedToMe)
        assertFalse(state.isRemoveAssigneeDialogVisible)
    }

    @Test
    fun `setInitialAssignees should update assignees and isAssignedToMe`() {
        val sut = createSut()
        val users = listOf(getUser(), getUser())

        sut.setInitialAssignees(assignees = users, isAssignedToMe = true)

        val state = sut.singleAssigneeState.value
        assertEquals(users, state.assignees)
        assertTrue(state.isAssignedToMe)
    }

    @Test
    fun `handleUpdateAssignee should call doOnPreExecute`() = runTest {
        val sut = createSut()
        var preExecuteCalled = false
        val newAssigneeId = getRandomLong()
        val newVersion = getRandomLong()
        val user = getUser()
        val payload = persistentMapOf<String, Any?>("assigned_to" to newAssigneeId)

        every { patchDataGenerator.getAssignedToPatchPayload(newAssigneeId) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(listOf(newAssigneeId)) } returns persistentListOf(user)
        coEvery { usersRepository.isAnyAssignedToMe(any()) } returns false

        sut.handleUpdateAssignee(
            newAssigneeId = newAssigneeId,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleUpdateAssignee with assignee should update state on success`() = runTest {
        val sut = createSut()
        val newAssigneeId = getRandomLong()
        val newVersion = getRandomLong()
        val user = getUser()
        val payload = persistentMapOf<String, Any?>("assigned_to" to newAssigneeId)

        every { patchDataGenerator.getAssignedToPatchPayload(newAssigneeId) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(listOf(newAssigneeId)) } returns persistentListOf(user)
        coEvery { usersRepository.isAnyAssignedToMe(any()) } returns true

        sut.handleUpdateAssignee(
            newAssigneeId = newAssigneeId,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.singleAssigneeState.value
        assertEquals(1, state.assignees.size)
        assertEquals(user, state.assignees[0])
        assertTrue(state.isAssignedToMe)
        assertFalse(state.isAssigneesLoading)
    }

    @Test
    fun `handleUpdateAssignee with null assignee should clear assignees`() = runTest {
        val sut = createSut()
        val newVersion = getRandomLong()
        val payload = persistentMapOf<String, Any?>("assigned_to" to null)

        every { patchDataGenerator.getAssignedToPatchPayload(null) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.isAnyAssignedToMe(persistentListOf()) } returns false

        sut.handleUpdateAssignee(
            newAssigneeId = null,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.singleAssigneeState.value
        assertTrue(state.assignees.isEmpty())
        assertFalse(state.isAssignedToMe)
        assertFalse(state.isAssigneesLoading)
    }

    @Test
    fun `handleUpdateAssignee should call doOnSuccess with new version`() = runTest {
        val sut = createSut()
        var receivedVersion: Long? = null
        val newAssigneeId = getRandomLong()
        val newVersion = getRandomLong()
        val user = getUser()
        val payload = persistentMapOf<String, Any?>("assigned_to" to newAssigneeId)

        every { patchDataGenerator.getAssignedToPatchPayload(newAssigneeId) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(listOf(newAssigneeId)) } returns persistentListOf(user)
        coEvery { usersRepository.isAnyAssignedToMe(any()) } returns false

        sut.handleUpdateAssignee(
            newAssigneeId = newAssigneeId,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = { receivedVersion = it },
            doOnError = {}
        )

        assertEquals(newVersion, receivedVersion)
    }

    @Test
    fun `handleUpdateAssignee should call repository with correct parameters`() = runTest {
        val commonTaskType = CommonTaskType.Issue
        val sut = createSut(commonTaskType)
        val newAssigneeId = getRandomLong()
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val newVersion = getRandomLong()
        val user = getUser()
        val payload = persistentMapOf<String, Any?>("assigned_to" to newAssigneeId)

        every { patchDataGenerator.getAssignedToPatchPayload(newAssigneeId) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(listOf(newAssigneeId)) } returns persistentListOf(user)
        coEvery { usersRepository.isAnyAssignedToMe(any()) } returns false

        sut.handleUpdateAssignee(
            newAssigneeId = newAssigneeId,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify {
            workItemRepository.patchData(
                version = version,
                workItemId = workItemId,
                payload = payload,
                commonTaskType = commonTaskType
            )
        }
    }

    @Test
    fun `handleUpdateAssignee should clear loading on error`() = runTest {
        val sut = createSut()
        val newAssigneeId = getRandomLong()
        val payload = persistentMapOf<String, Any?>("assigned_to" to newAssigneeId)

        every { patchDataGenerator.getAssignedToPatchPayload(newAssigneeId) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.handleUpdateAssignee(
            newAssigneeId = newAssigneeId,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertFalse(sut.singleAssigneeState.value.isAssigneesLoading)
    }

    @Test
    fun `handleAssignToMe should call handleUpdateAssignee with currentUserId`() = runTest {
        val sut = createSut()
        val currentUserId = getRandomLong()
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val newVersion = getRandomLong()
        val user = getUser()
        val payload = persistentMapOf<String, Any?>("assigned_to" to currentUserId)

        every { patchDataGenerator.getAssignedToPatchPayload(currentUserId) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(listOf(currentUserId)) } returns persistentListOf(user)
        coEvery { usersRepository.isAnyAssignedToMe(any()) } returns true

        sut.handleAssignToMe(
            currentUserId = currentUserId,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify {
            workItemRepository.patchData(
                version = version,
                workItemId = workItemId,
                payload = payload,
                commonTaskType = any()
            )
        }
    }

    @Test
    fun `handleUnassign should call handleUpdateAssignee with null assignee`() = runTest {
        val sut = createSut()
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val newVersion = getRandomLong()
        val payload = persistentMapOf<String, Any?>("assigned_to" to null)

        every { patchDataGenerator.getAssignedToPatchPayload(null) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.isAnyAssignedToMe(persistentListOf()) } returns false

        sut.handleUnassign(
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify {
            patchDataGenerator.getAssignedToPatchPayload(null)
        }
    }

    @Test
    fun `onRemoveAssigneeClick should show remove assignee dialog`() {
        val sut = createSut()

        sut.singleAssigneeState.value.onRemoveAssigneeClick()

        assertTrue(sut.singleAssigneeState.value.isRemoveAssigneeDialogVisible)
    }

    @Test
    fun `setIsRemoveAssigneeDialogVisible should update dialog visibility`() {
        val sut = createSut()

        sut.singleAssigneeState.value.setIsRemoveAssigneeDialogVisible(true)
        assertTrue(sut.singleAssigneeState.value.isRemoveAssigneeDialogVisible)

        sut.singleAssigneeState.value.setIsRemoveAssigneeDialogVisible(false)
        assertFalse(sut.singleAssigneeState.value.isRemoveAssigneeDialogVisible)
    }
}
