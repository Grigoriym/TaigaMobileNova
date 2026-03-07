package com.grappim.taigamobile.feature.issues.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.issues.mapper.IssueMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.mapper.DueDateStatusMapper
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.api.FakeIssuesApi
import com.grappim.taigamobile.testing.api.FakeWorkItemApi
import com.grappim.taigamobile.testing.models.getFiltersData
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class IssuesRepositoryImplTest {

    private val fakeIssuesApi = FakeIssuesApi()
    private val fakeWorkItemApi = FakeWorkItemApi()
    private val fakeSessionStorage = FakeTaigaSessionStorage()
    private val issueMapper = IssueMapper(
        userMapper = UserMapper(),
        statusesMapper = StatusesMapper(),
        projectMapper = ProjectMapper(),
        tagsMapper = TagsMapper(),
        dueDateStatusMapper = DueDateStatusMapper(),
        serverStorage = FakeServerStorage()
    )
    private val workItemMapper = WorkItemMapper(
        statusesMapper = StatusesMapper(),
        userMapper = UserMapper(),
        tagsMapper = TagsMapper(),
        projectMapper = ProjectMapper()
    )

    private lateinit var sut: IssuesRepository

    @BeforeTest
    fun setup() {
        sut = IssuesRepositoryImpl(
            issuesApi = fakeIssuesApi,
            taigaSessionStorage = fakeSessionStorage,
            issueMapper = issueMapper,
            workItemApi = fakeWorkItemApi,
            workItemMapper = workItemMapper
        )
    }

    @Test
    fun `getIssue returns mapped Issue`() = runTest {
        val issueId = getRandomLong()
        val filtersData = getFiltersData()
        val dto = getWorkItemResponseDTO()
        fakeWorkItemApi.workItemByIdResponse = dto

        val result = sut.getIssue(issueId, filtersData)

        assertEquals(issueMapper.toDomain(dto, filtersData), result)
    }

    @Test
    fun `createIssue returns mapped WorkItem and sends correct request`() = runTest {
        val projectId = getRandomLong()
        val title = getRandomString()
        val description = getRandomString()
        val sprintId = getRandomLong()
        val dto = getWorkItemResponseDTO()

        fakeSessionStorage.currentProjectId = projectId
        fakeIssuesApi.createIssueResponse = dto

        val result = sut.createIssue(title, description, sprintId)

        assertEquals(workItemMapper.toDomain(dto, CommonTaskType.Issue), result)
        val request = assertNotNull(fakeIssuesApi.lastCreateIssueRequest)
        assertEquals(projectId, request.project)
        assertEquals(title, request.subject)
        assertEquals(description, request.description)
        assertEquals(sprintId, request.milestone)
    }

    @Test
    fun `createIssue with null sprintId sends null milestone`() = runTest {
        val dto = getWorkItemResponseDTO()
        fakeIssuesApi.createIssueResponse = dto

        sut.createIssue(getRandomString(), getRandomString(), null)

        val request = assertNotNull(fakeIssuesApi.lastCreateIssueRequest)
        assertEquals(null, request.milestone)
    }
}
