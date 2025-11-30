package com.grappim.taigamobile.feature.issues.data

import com.grappim.taigamobile.core.api.CommonTaskMapper
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.issues.domain.IssueTask
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.data.WorkItemResponseDTO
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.testing.getCommonTask
import com.grappim.taigamobile.testing.getCommonTaskResponse
import com.grappim.taigamobile.testing.getFiltersData
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class IssuesRepositoryImplTest {

    private val issuesApi: IssuesApi = mockk()
    private val taigaStorage: TaigaStorage = mockk()
    private val commonTaskMapper: CommonTaskMapper = mockk()
    private val issueTaskMapper: IssueTaskMapper = mockk()
    private val workItemApi: WorkItemApi = mockk()

    private lateinit var sut: IssuesRepository

    private val taskPath = WorkItemPathPlural(CommonTaskType.Issue)

    @Before
    fun setup() {
        sut = IssuesRepositoryImpl(
            issuesApi = issuesApi,
            taigaStorage = taigaStorage,
            commonTaskMapper = commonTaskMapper,
            issueTaskMapper = issueTaskMapper,
            workItemApi = workItemApi
        )
    }

    @Test
    fun `on getIssues return correct data`() = runTest {
        val responses = listOf(getCommonTaskResponse(), getCommonTaskResponse())
        val isClosed = false
        val assignedIds = getRandomString()
        val watcherId = getRandomLong()
        val projectId = getRandomLong()
        val sprint = getRandomLong()

        coEvery {
            issuesApi.getIssues(
                assignedIds = assignedIds,
                isClosed = isClosed,
                watcherId = watcherId,
                project = projectId,
                sprint = sprint
            )
        } returns responses
        responses.forEach {
            coEvery {
                commonTaskMapper.toDomain(
                    it,
                    CommonTaskType.Issue
                )
            } returns getCommonTask(it.id)
        }

        val actuals = sut.getIssues(
            isClosed = isClosed,
            assignedIds = assignedIds,
            watcherId = watcherId,
            project = projectId,
            sprint = sprint
        )

        for (i in responses.indices) {
            val response = responses[i]
            val actual = actuals[i]
            assertEquals(response.id, actual.id)
        }
    }

    @Test
    fun `getIssue should return correct IssueTask`() = runTest {
        val issueId = getRandomLong()
        val filtersData = getFiltersData()
        val mockResponse = mockk<WorkItemResponseDTO>()
        val expectedIssueTask = mockk<IssueTask>()

        coEvery {
            workItemApi.getWorkItemById(
                taskPath = taskPath,
                id = issueId
            )
        } returns mockResponse
        coEvery { issueTaskMapper.toDomain(mockResponse, filtersData) } returns expectedIssueTask

        val actual = sut.getIssue(issueId, filtersData)

        assertEquals(expectedIssueTask, actual)
        coVerify { workItemApi.getWorkItemById(taskPath = taskPath, id = issueId) }
        coVerify { issueTaskMapper.toDomain(mockResponse, filtersData) }
    }
}
