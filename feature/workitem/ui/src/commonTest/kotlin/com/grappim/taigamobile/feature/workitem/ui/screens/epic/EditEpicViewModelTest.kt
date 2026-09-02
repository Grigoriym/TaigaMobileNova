package com.grappim.taigamobile.feature.workitem.ui.screens.epic

import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getEpic
import com.grappim.taigamobile.testing.repo.FakeEpicsRepository
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class EditEpicViewModelTest {

    private val workItemId = getRandomLong()
    private val taskIdentifier: TaskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)

    private val epicsRepository = FakeEpicsRepository()
    private val workItemEditStateRepository = WorkItemEditStateRepository()
    private val sessionStorage = FakeTaigaSessionStorage()
    private val mainDispatcherRule = MainDispatcherRule()

    private val route = WorkItemEditEpicNavDestination(workItemId = workItemId, taskIdentifier = taskIdentifier)

    private lateinit var sut: EditEpicViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel() {
        sut = EditEpicViewModel(
            epicsRepository = epicsRepository,
            workItemEditStateRepository = workItemEditStateRepository,
            taigaSessionStorage = sessionStorage,
            route = route
        )
    }

    // --- init ---

    @Test
    fun `on init success epics are loaded into state`() {
        val epic = getEpic()
        epicsRepository.getEpicsResult = persistentListOf(epic)

        createViewModel()

        assertEquals(1, sut.state.value.itemsToShow.size)
        assertEquals(epic.id, sut.state.value.itemsToShow[0].id)
    }

    @Test
    fun `on init success selectedItems and originalSelectedItems loaded from repository`() {
        val epicId = getRandomLong()
        workItemEditStateRepository.setCurrentEpics(workItemId, taskIdentifier, persistentListOf(epicId))

        createViewModel()

        assertTrue(sut.state.value.selectedItems.contains(epicId))
        assertTrue(sut.state.value.originalSelectedItems.contains(epicId))
    }

    @Test
    fun `on init failure state remains empty`() {
        epicsRepository.getEpicsThrows = testException

        createViewModel()

        assertTrue(sut.state.value.itemsToShow.isEmpty())
    }

    // --- onEpicClick ---

    @Test
    fun `onEpicClick adds epic to selection when not selected`() {
        createViewModel()
        val epicId = getRandomLong()

        sut.state.value.onEpicClick(epicId)

        assertTrue(sut.state.value.selectedItems.contains(epicId))
    }

    @Test
    fun `onEpicClick removes epic from selection when already selected`() {
        createViewModel()
        val epicId = getRandomLong()
        sut.state.value.onEpicClick(epicId)

        sut.state.value.onEpicClick(epicId)

        assertFalse(sut.state.value.selectedItems.contains(epicId))
    }

    // --- isItemSelected ---

    @Test
    fun `isItemSelected returns true for selected epic`() {
        createViewModel()
        val epicId = getRandomLong()
        sut.state.value.onEpicClick(epicId)

        assertTrue(sut.state.value.isItemSelected(epicId))
    }

    @Test
    fun `isItemSelected returns false for unselected epic`() {
        createViewModel()

        assertFalse(sut.state.value.isItemSelected(getRandomLong()))
    }

    // --- setIsDialogVisible ---

    @Test
    fun `setIsDialogVisible true shows dialog`() {
        createViewModel()

        sut.state.value.setIsDialogVisible(true)

        assertTrue(sut.state.value.isDialogVisible)
    }

    @Test
    fun `setIsDialogVisible false hides dialog`() {
        createViewModel()
        sut.state.value.setIsDialogVisible(true)

        sut.state.value.setIsDialogVisible(false)

        assertFalse(sut.state.value.isDialogVisible)
    }

    // --- shouldGoBackWithCurrentValue ---

    @Test
    fun `shouldGoBackWithCurrentValue emits back action`() = runTest {
        createViewModel()

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }
    }

    @Test
    fun `shouldGoBackWithCurrentValue hides dialog before navigating back`() = runTest {
        createViewModel()
        sut.state.value.setIsDialogVisible(true)

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }

        assertFalse(sut.state.value.isDialogVisible)
    }

    @Test
    fun `shouldGoBackWithCurrentValue true and changed state sends epics to repository`() = runTest {
        createViewModel()
        val epicId = getRandomLong()
        sut.state.value.onEpicClick(epicId)

        var receivedEpics: PersistentList<Long>? = null
        val collectJob = launch {
            workItemEditStateRepository.getEpicsFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { receivedEpics = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.join()
        val epics = assertNotNull(receivedEpics)
        assertTrue(epics.contains(epicId))
    }

    @Test
    fun `shouldGoBackWithCurrentValue true and unchanged state does not send epics to repository`() = runTest {
        createViewModel()

        var receivedEpics: PersistentList<Long>? = null
        val collectJob = launch {
            workItemEditStateRepository.getEpicsFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { receivedEpics = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.cancel()
        assertNull(receivedEpics)
    }

    @Test
    fun `shouldGoBackWithCurrentValue false does not send epics to repository even when state changed`() = runTest {
        createViewModel()
        val epicId = getRandomLong()
        sut.state.value.onEpicClick(epicId)

        var receivedEpics: PersistentList<Long>? = null
        val collectJob = launch {
            workItemEditStateRepository.getEpicsFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { receivedEpics = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }

        collectJob.cancel()
        assertNull(receivedEpics)
    }
}
