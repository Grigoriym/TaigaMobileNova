package com.grappim.taigamobile.feature.issues.domain

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.testing.FakePatchDataGenerator
import com.grappim.taigamobile.testing.models.getCustomFields
import com.grappim.taigamobile.testing.models.getFiltersData
import com.grappim.taigamobile.testing.models.getIssueTask
import com.grappim.taigamobile.testing.models.getSprint
import com.grappim.taigamobile.testing.models.getUser
import com.grappim.taigamobile.testing.repo.FakeFiltersRepository
import com.grappim.taigamobile.testing.repo.FakeHistoryRepository
import com.grappim.taigamobile.testing.repo.FakeIssuesRepository
import com.grappim.taigamobile.testing.repo.FakeProjectsRepository
import com.grappim.taigamobile.testing.repo.FakeSprintsRepository
import com.grappim.taigamobile.testing.repo.FakeUsersRepository
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IssueDetailsDataUseCaseImplTest {

    private val fakeFiltersRepository = FakeFiltersRepository()
    private val fakeIssuesRepository = FakeIssuesRepository()
    private val fakeHistoryRepository = FakeHistoryRepository()
    private val fakeSprintsRepository = FakeSprintsRepository()
    private val fakeUsersRepository = FakeUsersRepository()
    private val fakeWorkItemRepository = FakeWorkItemRepository()
    private val fakePatchDataGenerator = FakePatchDataGenerator()
    private val fakeProjectsRepository = FakeProjectsRepository()

    private val useCase = IssueDetailsDataUseCaseImpl(
        issuesRepository = fakeIssuesRepository,
        sprintsRepository = fakeSprintsRepository,
        historyRepository = fakeHistoryRepository,
        usersRepository = fakeUsersRepository,
        filtersRepository = fakeFiltersRepository,
        workItemRepository = fakeWorkItemRepository,
        patchDataGenerator = fakePatchDataGenerator,
        projectsRepository = fakeProjectsRepository
    )

    private fun setupGetIssueDataDefaults() {
        fakeFiltersRepository.filtersDataResult = getFiltersData()
        fakeIssuesRepository.getIssueResult = getIssueTask()
        fakeWorkItemRepository.getWorkItemAttachmentsResult = persistentListOf()
        fakeWorkItemRepository.getCustomFieldsResult = getCustomFields()
        fakeHistoryRepository.getCommentsResult = persistentListOf()
        fakeSprintsRepository.getSprintResult = getSprint()
        fakeUsersRepository.getUserResult = getUser()
        fakeUsersRepository.getUsersListResult = persistentListOf()
        fakeUsersRepository.isAnyAssignedToMeResult = false
    }

    @Test
    fun `getIssueData returns success with assembled data`() = runTest {
        val issue = getIssueTask()
        val filtersData = getFiltersData()
        val customFields = getCustomFields()

        fakeFiltersRepository.filtersDataResult = filtersData
        fakeIssuesRepository.getIssueResult = issue
        fakeWorkItemRepository.getWorkItemAttachmentsResult = persistentListOf()
        fakeWorkItemRepository.getCustomFieldsResult = customFields
        fakeHistoryRepository.getCommentsResult = persistentListOf()
        fakeSprintsRepository.getSprintResult = getSprint()
        fakeUsersRepository.getUserResult = getUser()
        fakeUsersRepository.getUsersListResult = persistentListOf()
        fakeUsersRepository.isAnyAssignedToMeResult = false

        val result = useCase.getIssueData(issue.id)

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(issue, data.issue)
        assertEquals(filtersData, data.filtersData)
        assertEquals(customFields, data.customFields)
    }

    @Test
    fun `getIssueData returns failure when issuesRepository throws`() = runTest {
        setupGetIssueDataDefaults()
        fakeIssuesRepository.getIssueThrows = RuntimeException("network error")

        val result = useCase.getIssueData(1L)

        assertTrue(result.isFailure)
    }

    @Test
    fun `getIssueData maps permission flags from projectsRepository`() = runTest {
        setupGetIssueDataDefaults()
        fakeProjectsRepository.permissions = persistentListOf(
            TaigaPermission.MODIFY_ISSUE,
            TaigaPermission.COMMENT_ISSUE
        )

        val result = useCase.getIssueData(1L)

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertTrue(data.canModifyIssue)
        assertTrue(data.canComment)
        assertFalse(data.canDeleteIssue)
    }

    @Test
    fun `updateSprint returns success with sprint when sprintId is not null`() = runTest {
        val sprint = getSprint()
        val patchedData = PatchedData(newVersion = 2L, dueDateStatus = null)
        fakeSprintsRepository.getSprintResult = sprint
        fakeWorkItemRepository.patchDataResult = patchedData

        val result = useCase.updateSprint(
            sprintId = sprint.id,
            version = 1L,
            workItemId = 10L,
            commonTaskType = CommonTaskType.Issue
        )

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(patchedData, data.patchedData)
        assertEquals(sprint, data.sprint)
    }

    @Test
    fun `updateSprint returns success with null sprint when sprintId is null`() = runTest {
        val patchedData = PatchedData(newVersion = 2L, dueDateStatus = null)
        fakeWorkItemRepository.patchDataResult = patchedData

        val result = useCase.updateSprint(
            sprintId = null,
            version = 1L,
            workItemId = 10L,
            commonTaskType = CommonTaskType.Issue
        )

        assertTrue(result.isSuccess)
        val data = result.getOrThrow()
        assertEquals(patchedData, data.patchedData)
        assertNull(data.sprint)
    }

    @Test
    fun `updateSprint returns failure when workItemRepository throws`() = runTest {
        fakeWorkItemRepository.patchDataThrows = RuntimeException("patch failed")

        val result = useCase.updateSprint(
            sprintId = null,
            version = 1L,
            workItemId = 10L,
            commonTaskType = CommonTaskType.Issue
        )

        assertTrue(result.isFailure)
    }
}
