package com.grappim.taigamobile.feature.userstories.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.db.dao.WorkItemDao
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.userstories.domain.UserStory
import com.grappim.taigamobile.feature.userstories.mapper.UserStoryMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.data.WorkItemEntityMapper
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getUserStory
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
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
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val userStoryMapper: UserStoryMapper = mockk()
    private val workItemApi: WorkItemApi = mockk()
    private val workItemRepository: WorkItemRepository = mockk()
    private val workItemMapper: WorkItemMapper = mockk()
    private val workItemDao: WorkItemDao = mockk()
    private val workItemEntityMapper: WorkItemEntityMapper = mockk()

    private lateinit var sut: UserStoriesRepository

    private val taskPath = WorkItemPathPlural(CommonTaskType.UserStory)

    @Before
    fun setup() {
        sut = UserStoriesRepositoryImpl(
            userStoriesApi = userStoriesApi,
            taigaSessionStorage = taigaSessionStorage,
            userStoryMapper = userStoryMapper,
            workItemApi = workItemApi,
            workItemRepository = workItemRepository,
            workItemMapper = workItemMapper,
            workItemDao = workItemDao,
            workItemEntityMapper = workItemEntityMapper
        )
    }

    @Test
    fun `getUserStory should return correct UserStory`() = runTest {
        val userStoryId = getRandomLong()
        val mockResponse = getWorkItemResponseDTO()
        val expectedUserStory = mockk<UserStory>()

        coEvery {
            workItemApi.getWorkItemById(
                taskPath = taskPath,
                id = userStoryId
            )
        } returns mockResponse
        every { userStoryMapper.toDomain(mockResponse) } returns expectedUserStory

        val actual = sut.getUserStory(userStoryId)

        assertEquals(expectedUserStory, actual)
        coVerify { workItemApi.getWorkItemById(taskPath = taskPath, id = userStoryId) }
        verify { userStoryMapper.toDomain(mockResponse) }
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
        val responses = listOf(getWorkItemResponseDTO(), getWorkItemResponseDTO())
        val assignedId = getRandomLong()
        val isClosed = false
        val isDashboard = true
        val watcherId = getRandomLong()
        val epicId = getRandomLong()
        val projectId = getRandomLong()
        val sprint = getRandomLong()
        val expected = persistentListOf(
            getUserStory(),
            getUserStory()
        )

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

        every { userStoryMapper.toListDomain(responses) } returns expected

        val actual = sut.getUserStories(
            assignedId = assignedId,
            isClosed = isClosed,
            isDashboard = isDashboard,
            watcherId = watcherId,
            epicId = epicId,
            project = projectId,
            sprint = sprint
        )

        for (i in expected.indices) {
            val response = expected[i]
            val actual = actual[i]
            assertEquals(response.id, actual.id)
        }
    }
}
