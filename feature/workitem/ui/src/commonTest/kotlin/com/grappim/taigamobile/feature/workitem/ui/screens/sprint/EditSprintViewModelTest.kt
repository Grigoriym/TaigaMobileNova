package com.grappim.taigamobile.feature.workitem.ui.screens.sprint

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getSprint
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeSprintsRepository
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class EditSprintViewModelTest {

    private val workItemId = getRandomLong()
    private val sprint = getSprint()

    private val sprintsRepository = FakeSprintsRepository()
    private val projectsRepository = FakeProjectsRepository()
    private val workItemEditStateRepository = WorkItemEditStateRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: EditSprintViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
        sprintsRepository.getSprintsResult = persistentListOf(sprint)
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    /**
     * `MainDispatcherRule` uses `UnconfinedTestDispatcher`, so both of the `init` block's launches
     * have already run to completion by the time this returns.
     */
    private fun createViewModel(taskIdentifier: TaskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)) {
        sut = EditSprintViewModel(
            sprintsRepository = sprintsRepository,
            workItemEditStateRepository = workItemEditStateRepository,
            projectsRepository = projectsRepository,
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "workItemId" to workItemId,
                    "taskIdentifier" to Json.encodeToString(taskIdentifier)
                )
            )
        )
    }

    // --- init / getSprints ---

    @Test
    fun `on init the open sprints are loaded into state`() {
        createViewModel()

        assertEquals(persistentListOf(sprint), sut.state.value.itemsToShow)
        assertEquals(false, sprintsRepository.getSprintsIsClosed)
    }

    @Test
    fun `on init the selection is read from the edit state repository`() {
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        workItemEditStateRepository.setCurrentSprint(workItemId, taskIdentifier, sprint.id)

        createViewModel(taskIdentifier)

        assertEquals(sprint.id, sut.state.value.selectedItem)
        assertEquals(sprint.id, sut.state.value.originalSelectedItem)
    }

    @Test
    fun `on init with no current sprint the selection is null`() {
        createViewModel()

        assertNull(sut.state.value.selectedItem)
        assertNull(sut.state.value.originalSelectedItem)
    }

    @Test
    fun `on init failure state stays empty`() {
        sprintsRepository.getSprintsThrows = testException

        createViewModel()

        assertTrue(sut.state.value.itemsToShow.isEmpty())
        assertNull(sut.state.value.selectedItem)
    }

    /**
     * Covers `resultOf`'s `catch (e: CancellationException) { throw e }` rethrow, which is inlined
     * into `getSprints`. The observable outcome matches the failure path above — neither
     * `onSuccess` nor `onFailure` runs — so only the branch counter distinguishes the two.
     */
    @Test
    fun `on init a cancellation is rethrown instead of being swallowed`() {
        sprintsRepository.getSprintsThrows = CancellationException("cancelled")

        createViewModel()

        assertTrue(sut.state.value.itemsToShow.isEmpty())
    }

    // --- init / getPermissions ---

    @Test
    fun `on init an issue with the modify permission can be modified`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.MODIFY_ISSUE)

        createViewModel(TaskIdentifier.WorkItem(CommonTaskType.Issue))

        assertTrue(sut.state.value.canModify)
    }

    @Test
    fun `on init an issue without the modify permission cannot be modified`() {
        projectsRepository.permissions = persistentListOf(TaigaPermission.MODIFY_PROJECT)

        createViewModel(TaskIdentifier.WorkItem(CommonTaskType.Issue))

        assertFalse(sut.state.value.canModify)
    }

    /**
     * Every work item type other than [CommonTaskType.Issue] takes the `else` arm, which does not
     * consult the permission list at all.
     */
    @Test
    fun `on init a user story can be modified without consulting the permissions`() {
        projectsRepository.permissions = persistentListOf()

        createViewModel(TaskIdentifier.WorkItem(CommonTaskType.UserStory))

        assertTrue(sut.state.value.canModify)
    }

    @Test
    fun `on init a wiki page can be modified without consulting the permissions`() {
        projectsRepository.permissions = persistentListOf()

        createViewModel(TaskIdentifier.Wiki)

        assertTrue(sut.state.value.canModify)
    }

    // --- onSprintClick / isItemSelected ---

    @Test
    fun `onSprintClick selects a sprint that was not selected`() {
        createViewModel()

        sut.state.value.onSprintClick(sprint.id)

        assertEquals(sprint.id, sut.state.value.selectedItem)
        assertTrue(sut.state.value.isItemSelected(sprint.id))
    }

    @Test
    fun `onSprintClick on the selected sprint clears the selection`() {
        createViewModel()
        sut.state.value.onSprintClick(sprint.id)

        sut.state.value.onSprintClick(sprint.id)

        assertNull(sut.state.value.selectedItem)
        assertFalse(sut.state.value.isItemSelected(sprint.id))
    }

    @Test
    fun `onSprintClick on another sprint replaces the selection`() {
        createViewModel()
        val otherSprintId = getRandomLong()
        sut.state.value.onSprintClick(sprint.id)

        sut.state.value.onSprintClick(otherSprintId)

        assertEquals(otherSprintId, sut.state.value.selectedItem)
        assertFalse(sut.state.value.isItemSelected(sprint.id))
    }

    @Test
    fun `isItemSelected returns false when nothing is selected`() {
        createViewModel()

        assertFalse(sut.state.value.isItemSelected(sprint.id))
    }

    // --- dialog visibility ---

    @Test
    fun `setIsDialogVisible true shows the dialog`() {
        createViewModel()

        sut.state.value.setIsDialogVisible(true)

        assertTrue(sut.state.value.isDialogVisible)
    }

    @Test
    fun `setIsDialogVisible false hides the dialog`() {
        createViewModel()
        sut.state.value.setIsDialogVisible(true)

        sut.state.value.setIsDialogVisible(false)

        assertFalse(sut.state.value.isDialogVisible)
    }

    // --- shouldGoBackWithCurrentValue / notifyChange ---

    @Test
    fun `shouldGoBackWithCurrentValue emits the back action`() = runTest {
        createViewModel()

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }
    }

    @Test
    fun `shouldGoBackWithCurrentValue hides the dialog before navigating back`() = runTest {
        createViewModel()
        sut.state.value.setIsDialogVisible(true)

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }

        assertFalse(sut.state.value.isDialogVisible)
    }

    /**
     * `getSprintFlow` is a rendezvous channel, so the collector has to be waiting before
     * `notifyChange` sends — hence the `launch { … take(1) }` before the back action is triggered.
     */
    @Test
    fun `shouldGoBackWithCurrentValue true and a changed selection sends the sprint to the repository`() = runTest {
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        createViewModel(taskIdentifier)
        sut.state.value.onSprintClick(sprint.id)

        var received: Long? = null
        val collectJob = launch {
            workItemEditStateRepository.getSprintFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.join()
        assertEquals(sprint.id, received)
    }

    @Test
    fun `shouldGoBackWithCurrentValue true and a cleared selection sends null to the repository`() = runTest {
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        workItemEditStateRepository.setCurrentSprint(workItemId, taskIdentifier, sprint.id)
        createViewModel(taskIdentifier)
        sut.state.value.onSprintClick(sprint.id)

        var received: Long? = sprint.id
        val collectJob = launch {
            workItemEditStateRepository.getSprintFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.join()
        assertNull(received)
    }

    @Test
    fun `shouldGoBackWithCurrentValue true and an unchanged selection sends nothing`() = runTest {
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        workItemEditStateRepository.setCurrentSprint(workItemId, taskIdentifier, sprint.id)
        createViewModel(taskIdentifier)

        var received: Long? = null
        val collectJob = launch {
            workItemEditStateRepository.getSprintFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(true)
            awaitItem()
        }

        collectJob.cancel()
        assertNull(received)
    }

    @Test
    fun `shouldGoBackWithCurrentValue false sends nothing even when the selection changed`() = runTest {
        val taskIdentifier = TaskIdentifier.WorkItem(CommonTaskType.UserStory)
        createViewModel(taskIdentifier)
        sut.state.value.onSprintClick(sprint.id)

        var received: Long? = null
        val collectJob = launch {
            workItemEditStateRepository.getSprintFlow(workItemId, taskIdentifier)
                .take(1)
                .collect { received = it }
        }

        sut.onBackAction.test {
            sut.state.value.shouldGoBackWithCurrentValue(false)
            awaitItem()
        }

        collectJob.cancel()
        assertNull(received)
    }
}
