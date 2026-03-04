package com.grappim.taigamobile.feature.tasks.domain

import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.testing.models.getCustomFields
import com.grappim.taigamobile.testing.models.getFiltersData
import com.grappim.taigamobile.testing.models.getSprint
import com.grappim.taigamobile.testing.models.getTask
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeSprintsRepository
import com.grappim.taigamobile.testing.repo.FakeTasksRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TaskDetailsDataUseCaseImplTest {

    private val fakeFiltersRepository = FakeFiltersRepository()
    private val fakeTasksRepository = FakeTasksRepository()
    private val fakeHistoryRepository = FakeHistoryRepository()
    private val fakeSprintsRepository = FakeSprintsRepository()
    private val fakeUsersRepository = FakeUsersRepository()
    private val fakeWorkItemRepository = FakeWorkItemRepository()
    private val fakeProjectsRepository = FakeProjectsRepository()

    private val useCase = TaskDetailsDataUseCaseImpl(
        filtersRepository = fakeFiltersRepository,
        tasksRepository = fakeTasksRepository,
        historyRepository = fakeHistoryRepository,
        sprintsRepository = fakeSprintsRepository,
        usersRepository = fakeUsersRepository,
        workItemRepository = fakeWorkItemRepository,
        projectsRepository = fakeProjectsRepository
    )

    private fun setupGetTaskDataDefaults() {
        fakeFiltersRepository.filtersDataResult = getFiltersData()
        fakeTasksRepository.getTaskResult = getTask()
        fakeWorkItemRepository.getWorkItemAttachmentsResult = persistentListOf()
        fakeWorkItemRepository.getCustomFieldsResult = getCustomFields()
        fakeHistoryRepository.getCommentsResult = persistentListOf()
        fakeSprintsRepository.getSprintResult = getSprint()
        fakeUsersRepository.getUserResult = getUser()
        fakeUsersRepository.getUsersListResult = persistentListOf()
        fakeUsersRepository.isAnyAssignedToMeResult = false
    }

    @Test
    fun `getTaskData returns success with assembled data`() = runTest {
        val task = getTask()
        val filtersData = getFiltersData()
        val customFields = getCustomFields()

        fakeFiltersRepository.filtersDataResult = filtersData
        fakeTasksRepository.getTaskResult = task
        fakeWorkItemRepository.getWorkItemAttachmentsResult = persistentListOf()
        fakeWorkItemRepository.getCustomFieldsResult = customFields
        fakeHistoryRepository.getCommentsResult = persistentListOf()
        fakeSprintsRepository.getSprintResult = getSprint()
        fakeUsersRepository.getUserResult = getUser()
        fakeUsersRepository.getUsersListResult = persistentListOf()
        fakeUsersRepository.isAnyAssignedToMeResult = false

        val result = useCase.getTaskData(task.id)

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(task, data.task)
        assertEquals(filtersData, data.filtersData)
        assertEquals(customFields, data.customFields)
    }

    @Test
    fun `getTaskData returns failure when tasksRepository throws`() = runTest {
        setupGetTaskDataDefaults()
        fakeTasksRepository.getTaskThrows = RuntimeException("network error")

        val result = useCase.getTaskData(1L)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getTaskData maps permission flags from projectsRepository`() = runTest {
        setupGetTaskDataDefaults()
        fakeProjectsRepository.permissions = persistentListOf(
            TaigaPermission.MODIFY_TASK,
            TaigaPermission.COMMENT_TASK
        )

        val result = useCase.getTaskData(1L)

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertTrue(data.canModifyTask)
        assertTrue(data.canComment)
        assertFalse(data.canDeleteTask)
    }

    @Test
    fun `deleteTask returns success`() = runTest {
        val result = useCase.deleteTask(42L)

        assertTrue(result.isSuccess)
        assertTrue(fakeTasksRepository.deleteTaskCalled)
    }

    @Test
    fun `deleteTask returns failure when tasksRepository throws`() = runTest {
        fakeTasksRepository.deleteTaskThrows = RuntimeException("delete failed")

        val result = useCase.deleteTask(42L)

        assertTrue(result.isFailure)
    }
}
