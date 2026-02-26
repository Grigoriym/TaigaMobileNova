package com.grappim.taigamobile.feature.workitem.ui.delegates.assignee.single

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.utils.FakePatchDataGenerator
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkItemSingleAssigneeDelegateImplTest {

    private val workItemRepository = FakeWorkItemRepository()
    private val usersRepository = FakeUsersRepository()
    private val patchDataGenerator = FakePatchDataGenerator()

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

        workItemRepository.patchDataResult = PatchedData(newVersion = newVersion, dueDateStatus = null)
        usersRepository.getUsersListResult = persistentListOf(user)

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

        workItemRepository.patchDataResult = PatchedData(newVersion = newVersion, dueDateStatus = null)
        usersRepository.getUsersListResult = persistentListOf(user)
        usersRepository.isAnyAssignedToMeResult = true

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

        workItemRepository.patchDataResult = PatchedData(newVersion = newVersion, dueDateStatus = null)

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

        workItemRepository.patchDataResult = PatchedData(newVersion = newVersion, dueDateStatus = null)
        usersRepository.getUsersListResult = persistentListOf(user)

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

        patchDataGenerator.assignedToPatchPayloadResult = payload
        workItemRepository.patchDataResult = PatchedData(newVersion = newVersion, dueDateStatus = null)
        usersRepository.getUsersListResult = persistentListOf(user)

        sut.handleUpdateAssignee(
            newAssigneeId = newAssigneeId,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val call = workItemRepository.patchDataCalls.last()
        assertEquals(version, call.version)
        assertEquals(workItemId, call.workItemId)
        assertEquals(payload, call.payload)
        assertEquals(commonTaskType, call.commonTaskType)
    }

    @Test
    fun `handleUpdateAssignee should clear loading on error`() = runTest {
        val sut = createSut()
        val newAssigneeId = getRandomLong()

        workItemRepository.patchDataThrows = testException

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

        patchDataGenerator.assignedToPatchPayloadResult = payload
        workItemRepository.patchDataResult = PatchedData(newVersion = newVersion, dueDateStatus = null)
        usersRepository.getUsersListResult = persistentListOf(user)
        usersRepository.isAnyAssignedToMeResult = true

        sut.handleAssignToMe(
            currentUserId = currentUserId,
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        val call = workItemRepository.patchDataCalls.last()
        assertEquals(version, call.version)
        assertEquals(workItemId, call.workItemId)
        assertEquals(payload, call.payload)
    }

    @Test
    fun `handleUnassign should call handleUpdateAssignee with null assignee`() = runTest {
        val sut = createSut()
        val version = getRandomLong()
        val workItemId = getRandomLong()
        val newVersion = getRandomLong()

        workItemRepository.patchDataResult = PatchedData(newVersion = newVersion, dueDateStatus = null)

        sut.handleUnassign(
            version = version,
            workItemId = workItemId,
            doOnPreExecute = null,
            doOnSuccess = null,
            doOnError = {}
        )

        assertEquals(null, patchDataGenerator.assignedToPatchPayloadCalls.last())
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
