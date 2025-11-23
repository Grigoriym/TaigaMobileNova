package com.grappim.taigamobile.feature.userstories.data

import com.grappim.taigamobile.core.api.AttachmentMapper
import com.grappim.taigamobile.core.api.CommonTaskMapper
import com.grappim.taigamobile.core.api.CustomFieldsMapper
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.patch.PatchedCustomAttributes
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.data.WorkItemResponseDTO
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.testing.getAttachment
import com.grappim.taigamobile.testing.getCommonTask
import com.grappim.taigamobile.testing.getCommonTaskResponse
import com.grappim.taigamobile.testing.getRandomLong
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class UserStoriesRepositoryImplTest {

    private val userStoriesApi: UserStoriesApi = mockk()
    private val taigaStorage: TaigaStorage = mockk()
    private val filtersRepository: FiltersRepository = mockk()
    private val swimlanesRepository: SwimlanesRepository = mockk()
    private val serverStorage: ServerStorage = mockk()
    private val commonTaskMapper: CommonTaskMapper = mockk()
    private val userStoryMapper: UserStoryMapper = mockk()
    private val attachmentMapper: AttachmentMapper = mockk()
    private val workItemApi: WorkItemApi = mockk()
    private val customFieldsMapper: CustomFieldsMapper = mockk()
    private val workItemRepository: WorkItemRepository = mockk()

    private lateinit var sut: UserStoriesRepository

    private val taskPath = WorkItemPathPlural(CommonTaskType.UserStory)

    @Before
    fun setup() {
        sut = UserStoriesRepositoryImpl(
            userStoriesApi = userStoriesApi,
            taigaStorage = taigaStorage,
            filtersRepository = filtersRepository,
            swimlanesRepository = swimlanesRepository,
            serverStorage = serverStorage,
            commonTaskMapper = commonTaskMapper,
            userStoryMapper = userStoryMapper,
            attachmentMapper = attachmentMapper,
            workItemApi = workItemApi,
            customFieldsMapper = customFieldsMapper,
            workItemRepository = workItemRepository
        )
    }

    @Test
    fun `getUserStory should return correct UserStory`() = runTest {
        val userStoryId = getRandomLong()
        val mockResponse = mockk<WorkItemResponseDTO>()
        val expectedUserStory = mockk<UserStory>()

        coEvery {
            workItemApi.getWorkItemById(
                taskPath = taskPath,
                id = userStoryId
            )
        } returns mockResponse
        coEvery { userStoryMapper.toDomain(mockResponse) } returns expectedUserStory

        val actual = sut.getUserStory(userStoryId)

        assertEquals(expectedUserStory, actual)
        coVerify { workItemApi.getWorkItemById(taskPath = taskPath, id = userStoryId) }
        coVerify { userStoryMapper.toDomain(mockResponse) }
    }

    @Test
    fun `unwatchUserStory should call workItemApi unwatchWorkItem`() = runTest {
        val userStoryId = getRandomLong()
        coEvery { workItemApi.unwatchWorkItem(workItemId = userStoryId, taskPath = taskPath) } just Runs

        sut.unwatchUserStory(userStoryId)

        coVerify { workItemApi.unwatchWorkItem(workItemId = userStoryId, taskPath = taskPath) }
    }

    @Test
    fun `watchUserStory should call workItemRepository watchWorkItem`() = runTest {
        val userStoryId = getRandomLong()
        coEvery {
            workItemRepository.watchWorkItem(
                workItemId = userStoryId,
                commonTaskType = CommonTaskType.UserStory
            )
        } just Runs

        sut.watchUserStory(userStoryId)

        coVerify {
            workItemRepository.watchWorkItem(
                workItemId = userStoryId,
                commonTaskType = CommonTaskType.UserStory
            )
        }
    }

    @Test
    fun `deleteIssue should call workItemApi deleteWorkItem`() = runTest {
        val userStoryId = getRandomLong()
        coEvery { workItemApi.deleteWorkItem(workItemId = userStoryId, taskPath = taskPath) } just Runs

        sut.deleteIssue(userStoryId)

        coVerify { workItemApi.deleteWorkItem(workItemId = userStoryId, taskPath = taskPath) }
    }

    @Test
    fun `deleteAttachment should call workItemRepository deleteAttachment`() = runTest {
        val attachment = getAttachment()
        coEvery {
            workItemRepository.deleteAttachment(
                commonTaskType = CommonTaskType.UserStory,
                attachment = attachment
            )
        } just Runs

        sut.deleteAttachment(attachment)

        coVerify {
            workItemRepository.deleteAttachment(
                commonTaskType = CommonTaskType.UserStory,
                attachment = attachment
            )
        }
    }

    @Test
    fun `patchData should delegate to workItemRepository`() = runTest {
        val version = getRandomLong()
        val userStoryId = getRandomLong()
        val payload = mapOf("subject" to "New Subject").toPersistentMap()
        val expectedPatchedData = mockk<PatchedData>()

        coEvery {
            workItemRepository.patchData(
                commonTaskType = CommonTaskType.UserStory,
                workItemId = userStoryId,
                payload = payload,
                version = version
            )
        } returns expectedPatchedData

        val actual = sut.patchData(version, userStoryId, payload)

        assertEquals(expectedPatchedData, actual)
        coVerify {
            workItemRepository.patchData(
                commonTaskType = CommonTaskType.UserStory,
                workItemId = userStoryId,
                payload = payload,
                version = version
            )
        }
    }

    @Test
    fun `patchCustomAttributes should delegate to workItemRepository`() = runTest {
        val version = getRandomLong()
        val userStoryId = getRandomLong()
        val payload = mapOf("1" to "value1").toPersistentMap()
        val expectedPatchedCustomAttrs = mockk<PatchedCustomAttributes>()

        coEvery {
            workItemRepository.patchCustomAttributes(
                commonTaskType = CommonTaskType.UserStory,
                workItemId = userStoryId,
                payload = payload,
                version = version
            )
        } returns expectedPatchedCustomAttrs

        val actual = sut.patchCustomAttributes(version, userStoryId, payload)

        assertEquals(expectedPatchedCustomAttrs, actual)
        coVerify {
            workItemRepository.patchCustomAttributes(
                commonTaskType = CommonTaskType.UserStory,
                workItemId = userStoryId,
                payload = payload,
                version = version
            )
        }
    }

    @Test
    fun `getUserStories should return correct data`() = runTest {
        val responses = listOf(getCommonTaskResponse(), getCommonTaskResponse())
        val assignedId = getRandomLong()
        val isClosed = false
        val isDashboard = true
        val watcherId = getRandomLong()
        val epicId = getRandomLong()
        val projectId = getRandomLong()
        val sprint = getRandomLong()

        coEvery {
            userStoriesApi.getUserStories(
                assignedId = assignedId,
                isClosed = isClosed,
                isDashboard = isDashboard,
                watcherId = watcherId,
                epic = epicId,
                project = projectId,
                sprint = sprint
            )
        } returns responses
        responses.forEach {
            coEvery {
                commonTaskMapper.toDomain(
                    it,
                    CommonTaskType.UserStory
                )
            } returns getCommonTask(it.id)
        }

        val actuals = sut.getUserStories(
            assignedId = assignedId,
            isClosed = isClosed,
            isDashboard = isDashboard,
            watcherId = watcherId,
            epicId = epicId,
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
