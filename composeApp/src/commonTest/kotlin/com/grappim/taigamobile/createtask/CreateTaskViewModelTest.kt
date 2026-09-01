package com.grappim.taigamobile.createtask

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.create_epic
import com.grappim.taigamobile.strings.generated.resources.create_issue
import com.grappim.taigamobile.strings.generated.resources.create_task
import com.grappim.taigamobile.strings.generated.resources.create_userstory
import com.grappim.taigamobile.strings.generated.resources.title_is_empty
import com.grappim.taigamobile.testing.MainDispatcherRule
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.FakeIssuesRepository
import com.grappim.taigamobile.testing.repo.FakeTasksRepository
import com.grappim.taigamobile.testing.repo.FakeUserStoriesRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CreateTaskViewModelTest {

    private val parentId = getRandomLong()
    private val sprintId = getRandomLong()
    private val statusId = getRandomLong()
    private val swimlaneId = getRandomLong()

    private val tasksRepository = FakeTasksRepository()
    private val issuesRepository = FakeIssuesRepository()
    private val userStoriesRepository = FakeUserStoriesRepository()
    private val workItemRepository = FakeWorkItemRepository()
    private val mainDispatcherRule = MainDispatcherRule()

    private lateinit var sut: CreateTaskViewModel

    @BeforeTest
    fun setup() {
        mainDispatcherRule.setup()
    }

    @AfterTest
    fun tearDown() {
        mainDispatcherRule.tearDown()
    }

    private fun createViewModel(
        type: CommonTaskType = CommonTaskType.Task,
        parentId: Long? = this.parentId,
        sprintId: Long? = this.sprintId,
        statusId: Long? = this.statusId,
        swimlaneId: Long? = this.swimlaneId,
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ) {
        sut = CreateTaskViewModel(
            route = CreateTaskNavDestination(
                type = type,
                parentId = parentId,
                sprintId = sprintId,
                statusId = statusId,
                swimlaneId = swimlaneId
            ),
            createWorkItemUseCase = CreateWorkItemUseCase(
                tasksRepository = tasksRepository,
                issuesRepository = issuesRepository,
                userStoriesRepository = userStoriesRepository,
                workItemRepository = workItemRepository
            ),
            savedStateHandle = savedStateHandle
        )
    }

    // --- route parsing and the toolbar title it drives ---

    @Test
    fun `initial state - UserStory route - toolbar title is create_userstory`() = runTest {
        createViewModel(type = CommonTaskType.UserStory)

        assertEquals(NativeText.Resource(RString.create_userstory), sut.state.value.toolbarTitle)
        assertEquals(CommonTaskType.UserStory, sut.route.type)
    }

    @Test
    fun `initial state - Task route - toolbar title is create_task`() = runTest {
        createViewModel(type = CommonTaskType.Task)

        assertEquals(NativeText.Resource(RString.create_task), sut.state.value.toolbarTitle)
    }

    @Test
    fun `initial state - Epic route - toolbar title is create_epic`() = runTest {
        createViewModel(type = CommonTaskType.Epic)

        assertEquals(NativeText.Resource(RString.create_epic), sut.state.value.toolbarTitle)
    }

    @Test
    fun `initial state - Issue route - toolbar title is create_issue`() = runTest {
        createViewModel(type = CommonTaskType.Issue)

        assertEquals(NativeText.Resource(RString.create_issue), sut.state.value.toolbarTitle)
    }

    @Test
    fun `initial state - empty title and description, not loading, no error`() = runTest {
        createViewModel()

        val state = sut.state.value
        assertEquals("", state.title)
        assertEquals("", state.description)
        assertFalse(state.isLoading)
        assertEquals(NativeText.Empty, state.error)
    }

    @Test
    fun `initial state - null route arguments are parsed as null`() = runTest {
        createViewModel(parentId = null, sprintId = null, statusId = null, swimlaneId = null)

        assertEquals(null, sut.route.parentId)
        assertEquals(null, sut.route.sprintId)
        assertEquals(null, sut.route.statusId)
        assertEquals(null, sut.route.swimlaneId)
    }

    // --- setters ---

    @Test
    fun `setTitle - updates title`() = runTest {
        createViewModel()
        val title = getRandomString()

        sut.state.value.setTitle(title)

        assertEquals(title, sut.state.value.title)
    }

    @Test
    fun `setDescription - updates description`() = runTest {
        createViewModel()
        val description = getRandomString()

        sut.state.value.setDescription(description)

        assertEquals(description, sut.state.value.description)
    }

    // --- onCreateTask: title validation ---

    @Test
    fun `onCreateTask - empty title - sets error and does not create anything`() = runTest {
        createViewModel()

        sut.state.value.onCreateTask()

        assertEquals(NativeText.Resource(RString.title_is_empty), sut.state.value.error)
        assertFalse(sut.state.value.isLoading)
        assertTrue(tasksRepository.createTaskCalls.isEmpty())
    }

    @Test
    fun `onCreateTask - blank title - is treated as empty after trimming`() = runTest {
        createViewModel()
        sut.state.value.setTitle("   ")

        sut.state.value.onCreateTask()

        assertEquals(NativeText.Resource(RString.title_is_empty), sut.state.value.error)
        assertTrue(tasksRepository.createTaskCalls.isEmpty())
    }

    // --- onCreateTask: success ---

    /**
     * State-init restores title/description from `savedStateHandle` (see the process-death
     * restoration tests below) — pre-seeding it here reaches the target input in one step, with
     * no `setTitle`/`setDescription` chain needed.
     */
    @Test
    fun `onCreateTask - success - trims input, passes route arguments and emits creationResult`() = runTest {
        val created = getWorkItem()
        tasksRepository.createTaskResult = created
        createViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf("title" to "  a title  ", "description" to "  a description  ")
            )
        )

        // `_creationResult` is a rendezvous channel, so the send only completes while collected.
        sut.creationResult.test {
            sut.state.value.onCreateTask()

            assertEquals(
                CreateWorkItemData(
                    id = created.id,
                    type = CommonTaskType.Task,
                    ref = created.ref
                ),
                awaitItem()
            )
        }

        assertEquals(
            FakeTasksRepository.CreateTaskCall(
                title = "a title",
                description = "a description",
                parentId = parentId,
                sprintId = sprintId
            ),
            tasksRepository.createTaskCalls.single()
        )
        assertFalse(sut.state.value.isLoading)
        assertEquals(NativeText.Empty, sut.state.value.error)
    }

    @Test
    fun `onCreateTask - success - route type selects the UserStory path`() = runTest {
        val created = getWorkItem(taskType = CommonTaskType.UserStory)
        userStoriesRepository.createUserStoryResult = created
        createViewModel(type = CommonTaskType.UserStory)
        sut.state.value.setTitle(getRandomString())

        sut.creationResult.test {
            sut.state.value.onCreateTask()

            assertEquals(CommonTaskType.UserStory, awaitItem().type)
        }

        val call = userStoriesRepository.createUserStoryCalls.single()
        assertEquals(statusId, call.status)
        assertEquals(swimlaneId, call.swimlane)
    }

    // --- process-death restoration ---

    @Test
    fun `initial state - restores title and description from a prior SavedStateHandle`() = runTest {
        val restoredTitle = getRandomString()
        val restoredDescription = getRandomString()
        val savedStateHandle = SavedStateHandle(
            mapOf("title" to restoredTitle, "description" to restoredDescription)
        )

        createViewModel(savedStateHandle = savedStateHandle)

        assertEquals(restoredTitle, sut.state.value.title)
        assertEquals(restoredDescription, sut.state.value.description)
    }

    @Test
    fun `setTitle - persists the new value to the SavedStateHandle`() = runTest {
        val savedStateHandle = SavedStateHandle()
        createViewModel(savedStateHandle = savedStateHandle)
        val title = getRandomString()

        sut.state.value.setTitle(title)

        assertEquals(title, savedStateHandle.get<String>("title"))
    }

    @Test
    fun `setDescription - persists the new value to the SavedStateHandle`() = runTest {
        val savedStateHandle = SavedStateHandle()
        createViewModel(savedStateHandle = savedStateHandle)
        val description = getRandomString()

        sut.state.value.setDescription(description)

        assertEquals(description, savedStateHandle.get<String>("description"))
    }

    // --- onCreateTask: failure ---

    @Test
    fun `onCreateTask - use case fails - sets the mapped error and stops loading`() = runTest {
        tasksRepository.createTaskThrows = testException
        createViewModel()
        sut.state.value.setTitle(getRandomString())

        sut.state.value.onCreateTask()

        assertEquals(NativeText.Simple(testException.message.toString()), sut.state.value.error)
        assertFalse(sut.state.value.isLoading)
    }

    @Test
    fun `onCreateTask - use case fails - emits no creationResult`() = runTest {
        issuesRepository.createIssueThrows = testException
        createViewModel(type = CommonTaskType.Issue)
        sut.state.value.setTitle(getRandomString())

        sut.creationResult.test {
            sut.state.value.onCreateTask()

            expectNoEvents()
        }

        assertTrue(issuesRepository.createIssueCalls.isNotEmpty())
    }
}
