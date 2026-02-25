package com.grappim.taigamobile.feature.issues.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.issues.domain.Issue
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.issues.mapper.IssueMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.getFiltersData
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.internal.tls.OkHostnameVerifier.verify
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class IssuesRepositoryImplTest {

    private val issuesApi: IssuesApi = mockk()
    private val taigaSessionStorage: KmpTaigaSessionStorage = mockk()
    private val issueMapper: IssueMapper = mockk()
    private val workItemApi: WorkItemApi = mockk()
    private val workItemMapper: WorkItemMapper = mockk()

    private lateinit var sut: IssuesRepository

    private val taskPath = WorkItemPathPlural(CommonTaskType.Issue)

    @Before
    fun setup() {
        sut = IssuesRepositoryImpl(
            issuesApi = issuesApi,
            taigaSessionStorage = taigaSessionStorage,
            issueMapper = issueMapper,
            workItemApi = workItemApi,
            workItemMapper = workItemMapper
        )
    }

    @Test
    fun `getIssue should return correct IssueTask`() = runTest {
        val issueId = getRandomLong()
        val filtersData = getFiltersData()
        val mockResponse = getWorkItemResponseDTO()
        val expectedIssue = mockk<Issue>()

        coEvery {
            workItemApi.getWorkItemById(
                taskPath = taskPath,
                id = issueId
            )
        } returns mockResponse
        every { issueMapper.toDomain(mockResponse, filtersData) } returns expectedIssue

        val actual = sut.getIssue(issueId, filtersData)

        assertEquals(expectedIssue, actual)
        coVerify { workItemApi.getWorkItemById(taskPath = taskPath, id = issueId) }
        verify { issueMapper.toDomain(mockResponse, filtersData) }
    }
}
