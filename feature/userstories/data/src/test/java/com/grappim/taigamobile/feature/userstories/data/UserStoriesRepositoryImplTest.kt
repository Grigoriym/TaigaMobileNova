package com.grappim.taigamobile.feature.userstories.data

import com.grappim.taigamobile.core.api.CommonTaskMapper
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.patch.PatchedData
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.filters.data.StatusMapper
import com.grappim.taigamobile.feature.filters.data.TagsMapper
import com.grappim.taigamobile.feature.users.data.mappers.UserMapper
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.data.WorkItemResponseDTO
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.testing.getCommonTask
import com.grappim.taigamobile.testing.getCommonTaskResponse
import com.grappim.taigamobile.testing.getRandomLong
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
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
    private val commonTaskMapper: CommonTaskMapper = mockk()
    private val userStoryMapper: UserStoryMapper = mockk()
    private val workItemApi: WorkItemApi = mockk()
    private val workItemRepository: WorkItemRepository = mockk()

    private val statusMapper: StatusMapper = mockk()
    private val userMapper: UserMapper = mockk()
    private val tagsMapper: TagsMapper = mockk()

    private lateinit var sut: UserStoriesRepository

    private val taskPath = WorkItemPathPlural(CommonTaskType.UserStory)

    @Before
    fun setup() {
        sut = UserStoriesRepositoryImpl(
            userStoriesApi = userStoriesApi,
            taigaStorage = taigaStorage,
            commonTaskMapper = commonTaskMapper,
            userStoryMapper = userStoryMapper,
            workItemApi = workItemApi,
            workItemRepository = workItemRepository,
            statusMapper = statusMapper,
            userMapper = userMapper,
            tagsMapper = tagsMapper
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
    fun `deleteIssue should call workItemApi deleteWorkItem`() = runTest {
        val userStoryId = getRandomLong()
        coEvery { workItemApi.deleteWorkItem(workItemId = userStoryId, taskPath = taskPath) } just Runs

        sut.deleteUserStory(userStoryId)

        coVerify { workItemApi.deleteWorkItem(workItemId = userStoryId, taskPath = taskPath) }
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
            userStoriesApi.getUserStoriesOld(
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

        val actuals = sut.getUserStoriesOld(
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
