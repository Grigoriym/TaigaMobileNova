package com.grappim.taigamobile.createtask

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.testing.models.getWorkItem
import com.grappim.taigamobile.testing.repo.FakeIssuesRepository
import com.grappim.taigamobile.testing.repo.FakeTasksRepository
import com.grappim.taigamobile.testing.repo.FakeUserStoriesRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.testException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

internal class CreateWorkItemUseCaseTest {

    private val tasksRepository = FakeTasksRepository()
    private val issuesRepository = FakeIssuesRepository()
    private val userStoriesRepository = FakeUserStoriesRepository()
    private val workItemRepository = FakeWorkItemRepository()

    private val title = getRandomString()
    private val description = getRandomString()
    private val parentId = getRandomLong()
    private val sprintId = getRandomLong()
    private val statusId = getRandomLong()
    private val swimlaneId = getRandomLong()

    private val sut = CreateWorkItemUseCase(
        tasksRepository = tasksRepository,
        issuesRepository = issuesRepository,
        userStoriesRepository = userStoriesRepository,
        workItemRepository = workItemRepository
    )

    private suspend fun createTask(commonTaskType: CommonTaskType) = sut.createTask(
        commonTaskType = commonTaskType,
        title = title,
        description = description,
        parentId = parentId,
        sprintId = sprintId,
        statusId = statusId,
        swimlaneId = swimlaneId
    )

    // --- routing per CommonTaskType ---

    @Test
    fun `createTask - Task - delegates to tasksRepository and maps id and ref`() = runTest {
        val created = getWorkItem()
        tasksRepository.createTaskResult = created

        val result = createTask(CommonTaskType.Task)

        assertEquals(
            FakeTasksRepository.CreateTaskCall(
                title = title,
                description = description,
                parentId = parentId,
                sprintId = sprintId
            ),
            tasksRepository.createTaskCalls.single()
        )
        assertEquals(
            CreateWorkItemData(id = created.id, type = CommonTaskType.Task, ref = created.ref),
            result.getOrThrow()
        )
    }

    @Test
    fun `createTask - Issue - delegates to issuesRepository and maps id and ref`() = runTest {
        val created = getWorkItem(taskType = CommonTaskType.Issue)
        issuesRepository.createIssueResult = created

        val result = createTask(CommonTaskType.Issue)

        assertEquals(
            FakeIssuesRepository.CreateIssueCall(
                title = title,
                description = description,
                sprintId = sprintId
            ),
            issuesRepository.createIssueCalls.single()
        )
        assertEquals(
            CreateWorkItemData(id = created.id, type = CommonTaskType.Issue, ref = created.ref),
            result.getOrThrow()
        )
    }

    @Test
    fun `createTask - UserStory - delegates to userStoriesRepository with status and swimlane`() = runTest {
        val created = getWorkItem(taskType = CommonTaskType.UserStory)
        userStoriesRepository.createUserStoryResult = created

        val result = createTask(CommonTaskType.UserStory)

        assertEquals(
            FakeUserStoriesRepository.CreateUserStoryCall(
                subject = title,
                description = description,
                status = statusId,
                swimlane = swimlaneId
            ),
            userStoriesRepository.createUserStoryCalls.single()
        )
        assertEquals(
            CreateWorkItemData(id = created.id, type = CommonTaskType.UserStory, ref = created.ref),
            result.getOrThrow()
        )
    }

    /**
     * [CommonTaskType.Epic] is the only type that falls through to the `else` branch, so it is what
     * covers the generic [com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository] path.
     */
    @Test
    fun `createTask - Epic - falls through to workItemRepository`() = runTest {
        val created = getWorkItem(taskType = CommonTaskType.Epic)
        workItemRepository.createWorkItemResult = created

        val result = createTask(CommonTaskType.Epic)

        val call = workItemRepository.createWorkItemCalls.single()
        assertEquals(CommonTaskType.Epic, call.commonTaskType)
        assertEquals(title, call.subject)
        assertEquals(description, call.description)
        assertEquals(statusId, call.status)
        assertEquals(
            CreateWorkItemData(id = created.id, type = CommonTaskType.Epic, ref = created.ref),
            result.getOrThrow()
        )
    }

    @Test
    fun `createTask - nullable route arguments are passed through as null`() = runTest {
        userStoriesRepository.createUserStoryResult = getWorkItem()

        val result = sut.createTask(
            commonTaskType = CommonTaskType.UserStory,
            title = title,
            description = description,
            parentId = null,
            sprintId = null,
            statusId = null,
            swimlaneId = null
        )

        assertTrue(result.isSuccess)
        val call = userStoriesRepository.createUserStoryCalls.single()
        assertEquals(null, call.status)
        assertEquals(null, call.swimlane)
    }

    // --- failure paths: resultOf wraps the throw ---

    @Test
    fun `createTask - Task - repository throws - returns failure`() = runTest {
        tasksRepository.createTaskThrows = testException

        assertFailure(createTask(CommonTaskType.Task))
    }

    @Test
    fun `createTask - Issue - repository throws - returns failure`() = runTest {
        issuesRepository.createIssueThrows = testException

        assertFailure(createTask(CommonTaskType.Issue))
    }

    @Test
    fun `createTask - UserStory - repository throws - returns failure`() = runTest {
        userStoriesRepository.createUserStoryThrows = testException

        assertFailure(createTask(CommonTaskType.UserStory))
    }

    @Test
    fun `createTask - Epic - repository throws - returns failure`() = runTest {
        workItemRepository.createWorkItemThrows = testException

        assertFailure(createTask(CommonTaskType.Epic))
    }

    /**
     * Asserted by type + message rather than identity: `assertFailsWithTestException` takes a
     * throwing block, and `resultOf` returns the throwable instead of rethrowing it.
     */
    private fun assertFailure(result: Result<CreateWorkItemData>) {
        val thrown = result.exceptionOrNull()
        assertIs<IllegalStateException>(thrown)
        assertEquals(testException.message, thrown.message)
    }
}
