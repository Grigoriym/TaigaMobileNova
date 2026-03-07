package com.grappim.taigamobile.feature.workitem.ui.delegates.watchers

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.workitem.data.PatchDataGeneratorImpl
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.UpdateWorkItem
import com.grappim.taigamobile.feature.workitem.domain.WatchersListUpdateData
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class WorkItemWatchersDelegateImplTest {

    private val workItemRepository = FakeWorkItemRepository()
    private val usersRepository = FakeUsersRepository()
    private val patchDataGenerator: PatchDataGenerator = PatchDataGeneratorImpl()
    private val taigaSessionStorage = FakeTaigaSessionStorage()

    private fun createSut(commonTaskType: CommonTaskType = CommonTaskType.UserStory): WorkItemWatchersDelegateImpl =
        WorkItemWatchersDelegateImpl(
            commonTaskType = commonTaskType,
            workItemRepository = workItemRepository,
            usersRepository = usersRepository,
            patchDataGenerator = patchDataGenerator,
            taigaSessionStorage = taigaSessionStorage
        )

    @Test
    fun `initial state should have empty watchers and no loading`() {
        val sut = createSut()

        val state = sut.watchersState.value

        assertTrue(state.watchers.isEmpty())
        assertFalse(state.areWatchersLoading)
        assertFalse(state.isWatchedByMe)
        assertFalse(state.isRemoveWatcherDialogVisible)
        assertNull(state.watcherIdToRemove)
    }

    @Test
    fun `setInitialWatchers should update watchers and isWatchedByMe`() {
        val sut = createSut()
        val users = listOf(getUser(), getUser())

        sut.setInitialWatchers(watchers = users, isWatchedByMe = true)

        val state = sut.watchersState.value
        assertEquals(users, state.watchers)
        assertTrue(state.isWatchedByMe)
    }

    @Test
    fun `setInitialWatchers with empty list should set empty watchers`() {
        val sut = createSut()

        sut.setInitialWatchers(watchers = emptyList(), isWatchedByMe = false)

        val state = sut.watchersState.value
        assertTrue(state.watchers.isEmpty())
        assertFalse(state.isWatchedByMe)
    }

    @Test
    fun `onRemoveWatcherClick should show dialog and set watcherIdToRemove`() {
        val sut = createSut()
        val watcherId = getRandomLong()

        sut.watchersState.value.onRemoveWatcherClick(watcherId)

        val state = sut.watchersState.value
        assertTrue(state.isRemoveWatcherDialogVisible)
        assertEquals(watcherId, state.watcherIdToRemove)
    }

    @Test
    fun `setIsRemoveWatcherDialogVisible false should hide dialog`() {
        val sut = createSut()
        sut.watchersState.value.onRemoveWatcherClick(getRandomLong())
        assertTrue(sut.watchersState.value.isRemoveWatcherDialogVisible)

        sut.watchersState.value.setIsRemoveWatcherDialogVisible(false)

        assertFalse(sut.watchersState.value.isRemoveWatcherDialogVisible)
    }

    // handleAddMeToWatchers

    @Test
    fun `handleAddMeToWatchers should call doOnPreExecute`() = runTest {
        val sut = createSut()
        var preExecuteCalled = false
        workItemRepository.getUpdateWorkItemResult = UpdateWorkItem(watcherUserIds = persistentListOf())

        sut.handleAddMeToWatchers(
            workItemId = 1L,
            doOnPreExecute = { preExecuteCalled = true },
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleAddMeToWatchers should update state on success`() = runTest {
        val sut = createSut()
        val watcher1 = getUser()
        val watcher2 = getUser()
        workItemRepository.getUpdateWorkItemResult =
            UpdateWorkItem(watcherUserIds = persistentListOf(watcher1.actualId, watcher2.actualId))
        usersRepository.getUsersListResult = persistentListOf(watcher1, watcher2)
        usersRepository.isAnyAssignedToMeResult = true

        sut.handleAddMeToWatchers(workItemId = 1L, doOnError = {})

        val state = sut.watchersState.value
        assertEquals(2, state.watchers.size)
        assertTrue(state.isWatchedByMe)
        assertFalse(state.areWatchersLoading)
    }

    @Test
    fun `handleAddMeToWatchers should call doOnSuccess on success`() = runTest {
        val sut = createSut()
        var successCalled = false
        workItemRepository.getUpdateWorkItemResult = UpdateWorkItem(watcherUserIds = persistentListOf())

        sut.handleAddMeToWatchers(
            workItemId = 1L,
            doOnSuccess = { successCalled = true },
            doOnError = {}
        )

        assertTrue(successCalled)
    }

    @Test
    fun `handleAddMeToWatchers should call doOnError on failure`() = runTest {
        val sut = createSut()
        var errorCalled = false
        workItemRepository.watchWorkItemThrows = RuntimeException("error")

        sut.handleAddMeToWatchers(workItemId = 1L, doOnError = { errorCalled = true })

        assertTrue(errorCalled)
    }

    @Test
    fun `handleAddMeToWatchers should clear loading on error`() = runTest {
        val sut = createSut()
        workItemRepository.watchWorkItemThrows = RuntimeException("error")

        sut.handleAddMeToWatchers(workItemId = 1L, doOnError = {})

        assertFalse(sut.watchersState.value.areWatchersLoading)
    }

    // handleRemoveMeFromWatchers

    @Test
    fun `handleRemoveMeFromWatchers should call doOnPreExecute`() = runTest {
        val sut = createSut()
        var preExecuteCalled = false
        workItemRepository.getUpdateWorkItemResult = UpdateWorkItem(watcherUserIds = persistentListOf())

        sut.handleRemoveMeFromWatchers(
            workItemId = 1L,
            doOnPreExecute = { preExecuteCalled = true },
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleRemoveMeFromWatchers should update state on success`() = runTest {
        val sut = createSut()
        val watcher = getUser()
        workItemRepository.getUpdateWorkItemResult = UpdateWorkItem(watcherUserIds = persistentListOf(watcher.actualId))
        usersRepository.getUsersListResult = persistentListOf(watcher)
        usersRepository.isAnyAssignedToMeResult = false

        sut.handleRemoveMeFromWatchers(workItemId = 1L, doOnError = {})

        val state = sut.watchersState.value
        assertFalse(state.isWatchedByMe)
        assertFalse(state.areWatchersLoading)
    }

    @Test
    fun `handleRemoveMeFromWatchers should call doOnError on failure`() = runTest {
        val sut = createSut()
        var errorCalled = false
        workItemRepository.unwatchWorkItemThrows = RuntimeException("error")

        sut.handleRemoveMeFromWatchers(workItemId = 1L, doOnError = { errorCalled = true })

        assertTrue(errorCalled)
    }

    @Test
    fun `handleRemoveMeFromWatchers should clear loading on error`() = runTest {
        val sut = createSut()
        workItemRepository.unwatchWorkItemThrows = RuntimeException("error")

        sut.handleRemoveMeFromWatchers(workItemId = 1L, doOnError = {})

        assertFalse(sut.watchersState.value.areWatchersLoading)
    }

    // handleRemoveWatcher

    @Test
    fun `handleRemoveWatcher should call doOnPreExecute`() = runTest {
        val sut = createSut()
        var preExecuteCalled = false
        val watcher = getUser()
        workItemRepository.patchDataResult = PatchedData(newVersion = 1L, dueDateStatus = null)
        taigaSessionStorage.currentUserId = getRandomLong()
        sut.setInitialWatchers(listOf(watcher), false)
        sut.watchersState.value.onRemoveWatcherClick(watcher.actualId)

        sut.handleRemoveWatcher(
            version = 1L,
            workItemId = 1L,
            doOnPreExecute = { preExecuteCalled = true },
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleRemoveWatcher should remove watcher from state on success`() = runTest {
        val sut = createSut()
        val watcherToRemove = getUser()
        val watcherToKeep = getUser()
        taigaSessionStorage.currentUserId = getRandomLong()
        workItemRepository.patchDataResult = PatchedData(newVersion = 2L, dueDateStatus = null)
        sut.setInitialWatchers(listOf(watcherToRemove, watcherToKeep), false)
        sut.watchersState.value.onRemoveWatcherClick(watcherToRemove.actualId)

        sut.handleRemoveWatcher(version = 1L, workItemId = 1L, doOnError = {})

        val state = sut.watchersState.value
        assertEquals(1, state.watchers.size)
        assertEquals(watcherToKeep, state.watchers[0])
        assertNull(state.watcherIdToRemove)
        assertFalse(state.areWatchersLoading)
    }

    @Test
    fun `handleRemoveWatcher should call doOnSuccess with new version`() = runTest {
        val sut = createSut()
        val newVersion = getRandomLong()
        var receivedVersion: Long? = null
        val watcher = getUser()
        taigaSessionStorage.currentUserId = getRandomLong()
        workItemRepository.patchDataResult = PatchedData(newVersion = newVersion, dueDateStatus = null)
        sut.setInitialWatchers(listOf(watcher), false)
        sut.watchersState.value.onRemoveWatcherClick(watcher.actualId)

        sut.handleRemoveWatcher(
            version = 1L,
            workItemId = 1L,
            doOnSuccess = { receivedVersion = it },
            doOnError = {}
        )

        assertEquals(newVersion, receivedVersion)
    }

    @Test
    fun `handleRemoveWatcher should call doOnError on failure`() = runTest {
        val sut = createSut()
        var errorCalled = false
        val watcher = getUser()
        workItemRepository.patchDataThrows = RuntimeException("error")
        sut.setInitialWatchers(listOf(watcher), false)
        sut.watchersState.value.onRemoveWatcherClick(watcher.actualId)

        sut.handleRemoveWatcher(version = 1L, workItemId = 1L, doOnError = { errorCalled = true })

        assertTrue(errorCalled)
        assertFalse(sut.watchersState.value.areWatchersLoading)
    }

    // handleUpdateWatchers

    @Test
    fun `handleUpdateWatchers should call doOnPreExecute`() = runTest {
        val sut = createSut()
        var preExecuteCalled = false
        workItemRepository.updateWatchersDataResult = WatchersListUpdateData(
            version = getRandomLong(),
            isWatchedByMe = false,
            watchers = persistentListOf()
        )

        sut.handleUpdateWatchers(
            version = 1L,
            workItemId = 1L,
            newWatchers = persistentListOf(),
            doOnPreExecute = { preExecuteCalled = true },
            doOnError = {}
        )

        assertTrue(preExecuteCalled)
    }

    @Test
    fun `handleUpdateWatchers should update state on success`() = runTest {
        val sut = createSut()
        val watcher1 = getUser()
        val watcher2 = getUser()
        workItemRepository.updateWatchersDataResult = WatchersListUpdateData(
            version = getRandomLong(),
            isWatchedByMe = true,
            watchers = persistentListOf(watcher1, watcher2)
        )

        sut.handleUpdateWatchers(
            version = 1L,
            workItemId = 1L,
            newWatchers = persistentListOf(watcher1.actualId, watcher2.actualId),
            doOnError = {}
        )

        val state = sut.watchersState.value
        assertEquals(2, state.watchers.size)
        assertTrue(state.isWatchedByMe)
        assertFalse(state.areWatchersLoading)
    }

    @Test
    fun `handleUpdateWatchers should call doOnSuccess with new version`() = runTest {
        val sut = createSut()
        val newVersion = getRandomLong()
        var receivedVersion: Long? = null
        workItemRepository.updateWatchersDataResult = WatchersListUpdateData(
            version = newVersion,
            isWatchedByMe = false,
            watchers = persistentListOf()
        )

        sut.handleUpdateWatchers(
            version = 1L,
            workItemId = 1L,
            newWatchers = persistentListOf(),
            doOnSuccess = { receivedVersion = it },
            doOnError = {}
        )

        assertEquals(newVersion, receivedVersion)
    }

    @Test
    fun `handleUpdateWatchers should call doOnError on failure`() = runTest {
        val sut = createSut()
        var errorCalled = false
        workItemRepository.updateWatchersDataThrows = RuntimeException("error")

        sut.handleUpdateWatchers(
            version = 1L,
            workItemId = 1L,
            newWatchers = persistentListOf(),
            doOnError = { errorCalled = true }
        )

        assertTrue(errorCalled)
    }

    @Test
    fun `handleUpdateWatchers should clear loading on error`() = runTest {
        val sut = createSut()
        workItemRepository.updateWatchersDataThrows = RuntimeException("error")

        sut.handleUpdateWatchers(
            version = 1L,
            workItemId = 1L,
            newWatchers = persistentListOf(),
            doOnError = {}
        )

        assertFalse(sut.watchersState.value.areWatchersLoading)
    }
}
