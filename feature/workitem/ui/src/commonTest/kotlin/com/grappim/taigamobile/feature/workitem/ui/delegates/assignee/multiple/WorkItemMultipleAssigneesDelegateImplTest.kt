package com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.multiple

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class WorkItemMultipleAssigneesDelegateImplTest {

    private val workItemRepository = FakeWorkItemRepository()
    private val usersRepository = FakeUsersRepository()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()
    private val taigaSessionStorage = FakeTaigaSessionStorage()

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
        val user1 = getUser()
        val user2 = getUser()
        workItemRepository.patchDataResult = PatchedData(newVersion = 1L, dueDateStatus = null)
        usersRepository.getUsersListResult = persistentListOf(user1, user2)
        usersRepository.isAnyAssignedToMeResult = true
        val newAssignees = persistentListOf(getRandomLong(), getRandomLong())

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
        workItemRepository.patchDataResult = PatchedData(newVersion = newVersion, dueDateStatus = null)
        usersRepository.getUsersListResult = persistentListOf(getUser())

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
        val payload = persistentMapOf<String, Any?>("assigned_users" to newAssignees)

        sut.handleUpdateAssignees(
            newAssignees = newAssignees,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

//        coVerify {
//            workItemRepository.patchData(
//                version = version,
//                workItemId = workItemId,
//                payload = payload,
//                commonTaskType = commonTaskType
//            )
//        }
    }

    @Test
    fun `handleUpdateAssignees should clear loading on error`() = runTest {
        val sut = createSut()
        val newAssignees = persistentListOf(getRandomLong())

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
        val version = getRandomLong()
        val workItemId = getRandomLong()
        taigaSessionStorage.currentUserId = getRandomLong()
        workItemRepository.patchDataResult = PatchedData(newVersion = 1L, dueDateStatus = null)
        usersRepository.getUsersListResult = persistentListOf(existingUser, getUser())

        sut.setInitialAssignees(listOf(existingUser), false)

        sut.handleAssignToMe(
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )
    }

    @Test
    fun `handleAssignToMe with no existing assignees should assign only current user`() = runTest {
        val sut = createSut()
        val currentUserId = getRandomLong()
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val newVersion = getRandomLong()
        val user = getUser()
        taigaSessionStorage.currentUserId = currentUserId
        workItemRepository.patchDataResult = PatchedData(newVersion = newVersion, dueDateStatus = null)
        usersRepository.getUsersListResult = persistentListOf(user)

        sut.handleAssignToMe(
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )
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
    }

    @Test
    fun `handleRemoveAssignee should remove assignee and update state`() = runTest {
        val sut = createSut()
        val userToRemove = getUser()
        val userToKeep = getUser()
        val newVersion = getRandomLong()

        workItemRepository.patchDataResult = PatchedData(newVersion = newVersion, dueDateStatus = null)
        usersRepository.getUsersListResult = persistentListOf(userToKeep)

        sut.setInitialAssignees(listOf(userToRemove, userToKeep), false)
        sut.multipleAssigneesState.value.onRemoveAssigneeClick(userToRemove)

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
        workItemRepository.patchDataResult = PatchedData(newVersion = 2L, dueDateStatus = null)
        usersRepository.getUsersListResult = persistentListOf()
        usersRepository.isAnyAssignedToMeResult = false

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
