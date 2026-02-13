package com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.multiple

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
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
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkItemMultipleAssigneesDelegateImplTest {

    private val workItemRepository: WorkItemRepository = mockk()
    private val usersRepository: UsersRepository = mockk()
    private val patchDataGenerator: PatchDataGenerator = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()

    private fun createSut(
        commonTaskType: CommonTaskType = CommonTaskType.UserStory
    ): WorkItemMultipleAssigneesDelegateImpl = WorkItemMultipleAssigneesDelegateImpl(
        commonTaskType = commonTaskType,
        workItemRepository = workItemRepository,
        usersRepository = usersRepository,
        patchDataGenerator = patchDataGenerator,
        taigaSessionStorage = taigaSessionStorage
    )

    @Test
    fun `initial state should have empty assignees and no loading`() {
        val sut = createSut()

        val state = sut.multipleAssigneesState.value

        assertTrue(state.assignees.isEmpty())
        assertFalse(state.isAssigneesLoading)
        assertFalse(state.isAssignedToMe)
        assertFalse(state.isRemoveAssigneeDialogVisible)
        assertNull(state.assigneeToRemove)
    }

    @Test
    fun `setInitialAssignees should update assignees and isAssignedToMe`() {
        val sut = createSut()
        val users = listOf(getUser(), getUser())

        sut.setInitialAssignees(assignees = users, isAssignedToMe = true)

        val state = sut.multipleAssigneesState.value
        assertEquals(users, state.assignees)
        assertTrue(state.isAssignedToMe)
    }

    @Test
    fun `setInitialAssignees with empty list should set empty assignees`() {
        val sut = createSut()

        sut.setInitialAssignees(assignees = emptyList(), isAssignedToMe = false)

        val state = sut.multipleAssigneesState.value
        assertTrue(state.assignees.isEmpty())
        assertFalse(state.isAssignedToMe)
    }

    @Test
    fun `handleUpdateAssignees should call doOnPreExecute`() = runTest {
        val sut = createSut()
        var preExecuteCalled = false
        val newAssignees = persistentListOf(getRandomLong(), getRandomLong())
        val newVersion = getRandomLong()
        val users = listOf(getUser(), getUser())
        val payload = persistentMapOf<String, Any?>("assigned_users" to newAssignees)

        every { patchDataGenerator.getAssignedUsersPatchPayload(newAssignees) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(newAssignees.toList()) } returns persistentListOf(*users.toTypedArray())
        coEvery { usersRepository.isAnyAssignedToMe(any()) } returns false

        sut.handleUpdateAssignees(
            newAssignees = newAssignees,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = { preExecuteCalled = true },
            doOnSuccess = null,
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleUpdateAssignees should update state on success`() = runTest {
        val sut = createSut()
        val newAssignees = persistentListOf(getRandomLong(), getRandomLong())
        val newVersion = getRandomLong()
        val users = listOf(getUser(), getUser())
        val payload = persistentMapOf<String, Any?>("assigned_users" to newAssignees)

        every { patchDataGenerator.getAssignedUsersPatchPayload(newAssignees) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(newAssignees.toList()) } returns persistentListOf(*users.toTypedArray())
        coEvery { usersRepository.isAnyAssignedToMe(any()) } returns true

        sut.handleUpdateAssignees(
            newAssignees = newAssignees,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.multipleAssigneesState.value
        assertEquals(2, state.assignees.size)
        assertTrue(state.isAssignedToMe)
        assertFalse(state.isAssigneesLoading)
    }

    @Test
    fun `handleUpdateAssignees should call doOnSuccess with new version`() = runTest {
        val sut = createSut()
        var receivedVersion: Long? = null
        val newAssignees = persistentListOf(getRandomLong())
        val newVersion = getRandomLong()
        val user = getUser()
        val payload = persistentMapOf<String, Any?>("assigned_users" to newAssignees)

        every { patchDataGenerator.getAssignedUsersPatchPayload(newAssignees) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(newAssignees.toList()) } returns persistentListOf(user)
        coEvery { usersRepository.isAnyAssignedToMe(any()) } returns false

        sut.handleUpdateAssignees(
            newAssignees = newAssignees,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = { receivedVersion = it },
            doOnError = {}
        )

        assertEquals(newVersion, receivedVersion)
    }

    @Test
    fun `handleUpdateAssignees should call repository with correct parameters`() = runTest {
        val commonTaskType = CommonTaskType.UserStory
        val sut = createSut(commonTaskType)
        val newAssignees = persistentListOf(getRandomLong(), getRandomLong())
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val newVersion = getRandomLong()
        val users = listOf(getUser(), getUser())
        val payload = persistentMapOf<String, Any?>("assigned_users" to newAssignees)

        every { patchDataGenerator.getAssignedUsersPatchPayload(newAssignees) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(newAssignees.toList()) } returns persistentListOf(*users.toTypedArray())
        coEvery { usersRepository.isAnyAssignedToMe(any()) } returns false

        sut.handleUpdateAssignees(
            newAssignees = newAssignees,
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
    fun `handleUpdateAssignees should clear loading on error`() = runTest {
        val sut = createSut()
        val newAssignees = persistentListOf(getRandomLong())
        val payload = persistentMapOf<String, Any?>("assigned_users" to newAssignees)

        every { patchDataGenerator.getAssignedUsersPatchPayload(newAssignees) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.handleUpdateAssignees(
            newAssignees = newAssignees,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertFalse(sut.multipleAssigneesState.value.isAssigneesLoading)
    }

    @Test
    fun `handleUpdateAssignees should call doOnError on failure`() = runTest {
        val sut = createSut()
        var errorCalled = false
        val newAssignees = persistentListOf(getRandomLong())
        val payload = persistentMapOf<String, Any?>("assigned_users" to newAssignees)

        every { patchDataGenerator.getAssignedUsersPatchPayload(newAssignees) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } throws testException

        sut.handleUpdateAssignees(
            newAssignees = newAssignees,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = { errorCalled = true }
        )

        assertTrue(errorCalled)
    }

    @Test
    fun `handleAssignToMe should add current user to existing assignees`() = runTest {
        val sut = createSut()
        val existingUser = getUser()
        val currentUserId = getRandomLong()
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val newVersion = getRandomLong()

        sut.setInitialAssignees(listOf(existingUser), false)

        val expectedAssignees = persistentListOf(existingUser.id!!, currentUserId)
        val payload = persistentMapOf<String, Any?>("assigned_users" to expectedAssignees)

        coEvery { taigaSessionStorage.requireUserId() } returns currentUserId
        every { patchDataGenerator.getAssignedUsersPatchPayload(any()) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(any()) } returns persistentListOf(existingUser, getUser())
        coEvery { usersRepository.isAnyAssignedToMe(any()) } returns true

        sut.handleAssignToMe(
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify { taigaSessionStorage.requireUserId() }
        coVerify { patchDataGenerator.getAssignedUsersPatchPayload(any()) }
    }

    @Test
    fun `handleAssignToMe with no existing assignees should assign only current user`() = runTest {
        val sut = createSut()
        val currentUserId = getRandomLong()
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val newVersion = getRandomLong()
        val user = getUser()

        val expectedAssignees = persistentListOf(currentUserId)
        val payload = persistentMapOf<String, Any?>("assigned_users" to expectedAssignees)

        coEvery { taigaSessionStorage.requireUserId() } returns currentUserId
        every { patchDataGenerator.getAssignedUsersPatchPayload(expectedAssignees) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(listOf(currentUserId)) } returns persistentListOf(user)
        coEvery { usersRepository.isAnyAssignedToMe(any()) } returns true

        sut.handleAssignToMe(
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify {
            patchDataGenerator.getAssignedUsersPatchPayload(expectedAssignees)
        }
    }

    @Test
    fun `handleRemoveAssignee should return early when no assignee to remove`() = runTest {
        val sut = createSut()

        sut.handleRemoveAssignee(
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        coVerify(exactly = 0) { workItemRepository.patchData(any(), any(), any(), any()) }
    }

    @Test
    fun `handleRemoveAssignee should remove assignee and update state`() = runTest {
        val sut = createSut()
        val userToRemove = getUser()
        val userToKeep = getUser()
        val newVersion = getRandomLong()

        sut.setInitialAssignees(listOf(userToRemove, userToKeep), false)
        sut.multipleAssigneesState.value.onRemoveAssigneeClick(userToRemove)

        val remainingAssignees = listOf(userToKeep.actualId).toImmutableList()
        val payload = persistentMapOf<String, Any?>("assigned_users" to remainingAssignees)

        every { patchDataGenerator.getAssignedUsersPatchPayload(remainingAssignees) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(remainingAssignees.toList()) } returns persistentListOf(userToKeep)
        coEvery { usersRepository.isAnyAssignedToMe(any()) } returns false

        sut.handleRemoveAssignee(
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.multipleAssigneesState.value
        assertFalse(state.isRemoveAssigneeDialogVisible)
    }

    @Test
    fun `onRemoveAssigneeClick should show dialog and set assignee to remove`() {
        val sut = createSut()
        val user = getUser()

        sut.multipleAssigneesState.value.onRemoveAssigneeClick(user)

        val state = sut.multipleAssigneesState.value
        assertTrue(state.isRemoveAssigneeDialogVisible)
        assertEquals(user, state.assigneeToRemove)
    }

    @Test
    fun `setIsRemoveAssigneeDialogVisible should update dialog visibility and clear assignee`() {
        val sut = createSut()
        val user = getUser()

        sut.multipleAssigneesState.value.onRemoveAssigneeClick(user)
        assertTrue(sut.multipleAssigneesState.value.isRemoveAssigneeDialogVisible)
        assertEquals(user, sut.multipleAssigneesState.value.assigneeToRemove)

        sut.multipleAssigneesState.value.setIsRemoveAssigneeDialogVisible(false)

        val state = sut.multipleAssigneesState.value
        assertFalse(state.isRemoveAssigneeDialogVisible)
        assertNull(state.assigneeToRemove)
    }

    @Test
    fun `handleUpdateAssignees with empty list should clear assignees`() = runTest {
        val sut = createSut()
        val existingUsers = listOf(getUser(), getUser())
        sut.setInitialAssignees(existingUsers, true)

        val emptyAssignees = persistentListOf<Long>()
        val newVersion = getRandomLong()
        val payload = persistentMapOf<String, Any?>("assigned_users" to emptyAssignees)

        every { patchDataGenerator.getAssignedUsersPatchPayload(emptyAssignees) } returns payload
        coEvery {
            workItemRepository.patchData(any(), any(), any(), any())
        } returns PatchedData(newVersion = newVersion, dueDateStatus = null)
        coEvery { usersRepository.getUsersList(emptyList()) } returns persistentListOf()
        coEvery { usersRepository.isAnyAssignedToMe(persistentListOf()) } returns false

        sut.handleUpdateAssignees(
            newAssignees = emptyAssignees,
            version = 1L,
            workItemId = 123L,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val state = sut.multipleAssigneesState.value
        assertTrue(state.assignees.isEmpty())
        assertFalse(state.isAssignedToMe)
        assertFalse(state.isAssigneesLoading)
    }
}
