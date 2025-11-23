package com.grappim.taigamobile.feature.issues.data

import com.grappim.taigamobile.core.api.AttachmentMapper
import com.grappim.taigamobile.core.api.CommonTaskMapper
import com.grappim.taigamobile.core.api.CustomFieldsMapper
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.issues.domain.IssuesRepository
import com.grappim.taigamobile.feature.workitem.data.PatchedDataMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.testing.getAttachment
import com.grappim.taigamobile.testing.getCommonTask
import com.grappim.taigamobile.testing.getCommonTaskResponse
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
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
    private val attachmentMapper: AttachmentMapper = mockk()
    private val customFieldsMapper: CustomFieldsMapper = mockk()
    private val patchedDataMapper: PatchedDataMapper = mockk()
    private val workItemApi: WorkItemApi = mockk()
    private val workItemRepository: WorkItemRepository = mockk()

    private lateinit var sut: IssuesRepository

    private val taskPath = WorkItemPathPlural(CommonTaskType.Issue)

    @Before
    fun setup() {
        sut = IssuesRepositoryImpl(
            issuesApi = issuesApi,
            taigaStorage = taigaStorage,
            commonTaskMapper = commonTaskMapper,
            issueTaskMapper = issueTaskMapper,
            attachmentMapper = attachmentMapper,
            customFieldsMapper = customFieldsMapper,
            patchedDataMapper = patchedDataMapper,
            workItemApi = workItemApi,
            workItemRepository = workItemRepository
        )
    }

    @Test
    fun `watchIssue should call issuesApi watchIssue`() = runTest {
        val issueId = getRandomLong()
        coEvery { workItemApi.watchWorkItem(workItemId = issueId, taskPath = taskPath) } just Runs

        sut.watchIssue(issueId)

        coVerify { workItemApi.watchWorkItem(workItemId = issueId, taskPath = taskPath) }
    }

    @Test
    fun `unwatchIssue should call issuesApi unwatchIssue`() = runTest {
        val issueId = getRandomLong()
        coEvery { workItemApi.unwatchWorkItem(workItemId = issueId, taskPath = taskPath) } just Runs

        sut.unwatchIssue(issueId)

        coVerify { workItemApi.unwatchWorkItem(workItemId = issueId, taskPath = taskPath) }
    }

    @Test
    fun `deleteIssue should call issuesApi deleteCommonTask`() = runTest {
        val issueId = getRandomLong()
        coEvery { workItemApi.deleteWorkItem(workItemId = issueId, taskPath = taskPath) } just Runs

        sut.deleteIssue(issueId)

        coVerify { workItemApi.deleteWorkItem(workItemId = issueId, taskPath = taskPath) }
    }

    @Test
    fun `deleteAttachment should call issuesApi deleteAttachment`() = runTest {
        val attachment = getAttachment()
        coEvery {
            workItemApi.deleteAttachment(
                attachmentId = attachment.id,
                taskPath = taskPath
            )
        } just Runs

        sut.deleteAttachment(attachment)

        coVerify { workItemApi.deleteAttachment(attachmentId = attachment.id, taskPath = taskPath) }
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
}
