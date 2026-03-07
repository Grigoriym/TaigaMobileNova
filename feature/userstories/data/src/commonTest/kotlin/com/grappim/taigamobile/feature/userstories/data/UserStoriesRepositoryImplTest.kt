package com.grappim.taigamobile.feature.userstories.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesRepository
import com.grappim.taigamobile.feature.userstories.mapper.UserStoryMapper
import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.domain.PatchedData
import com.grappim.taigamobile.feature.workitem.domain.getPluralPath
import com.grappim.taigamobile.feature.workitem.mapper.DueDateStatusMapper
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.api.FakeUserStoriesApi
import com.grappim.taigamobile.testing.api.FakeWorkItemApi
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.repo.FakeWorkItemRepository
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class UserStoriesRepositoryImplTest {

    private val fakeUserStoriesApi = FakeUserStoriesApi()
    private val fakeTaigaSessionStorage = FakeTaigaSessionStorage()
    private val fakeWorkItemApi = FakeWorkItemApi()
    private val fakeWorkItemRepository = FakeWorkItemRepository()

    private val userStoryMapper = UserStoryMapper(
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

    private val taskPath = CommonTaskType.UserStory.getPluralPath()

    private lateinit var sut: UserStoriesRepository

    @BeforeTest
    fun setup() {
        sut = UserStoriesRepositoryImpl(
            userStoriesApi = fakeUserStoriesApi,
            taigaSessionStorage = fakeTaigaSessionStorage,
            userStoryMapper = userStoryMapper,
            workItemApi = fakeWorkItemApi,
            workItemRepository = fakeWorkItemRepository,
            workItemMapper = workItemMapper
        )
    }

    @Test
    fun `getUserStory should return correct UserStory`() = runTest {
        val userStoryId = getRandomLong()
        val dto = getWorkItemResponseDTO()
        fakeWorkItemApi.workItemByIdResponse = dto

        val actual = sut.getUserStory(userStoryId)

        assertEquals(dto.id, actual.id)
    }

    @Test
    fun `deleteUserStory should call workItemApi deleteWorkItem`() = runTest {
        val userStoryId = getRandomLong()

        sut.deleteUserStory(userStoryId)

        assertEquals(1, fakeWorkItemApi.deleteWorkItemCalls.size)
        val call = fakeWorkItemApi.deleteWorkItemCalls.last()
        assertEquals(taskPath, call.first)
        assertEquals(userStoryId, call.second)
    }

    @Test
    fun `patchData should delegate to workItemRepository`() = runTest {
        val version = getRandomLong()
        val userStoryId = getRandomLong()
        val payload = mapOf("subject" to "New Subject").toPersistentMap()
        val expectedPatchedData = PatchedData(newVersion = getRandomLong(), dueDateStatus = DueDateStatus.NotSet)
        fakeWorkItemRepository.patchDataResult = expectedPatchedData

        val actual = sut.patchData(version, userStoryId, payload)

        assertEquals(expectedPatchedData, actual)
        assertEquals(1, fakeWorkItemRepository.patchDataCalls.size)
        val call = fakeWorkItemRepository.patchDataCalls.last()
        assertEquals(CommonTaskType.UserStory, call.commonTaskType)
        assertEquals(userStoryId, call.workItemId)
        assertEquals(version, call.version)
    }

    @Test
    fun `getUserStories should return correct data`() = runTest {
        val dto1 = getWorkItemResponseDTO()
        val dto2 = getWorkItemResponseDTO()
        fakeUserStoriesApi.getUserStoriesResult = listOf(dto1, dto2)

        val assignedId = getRandomLong()
        val isClosed = false
        val isDashboard = true
        val watcherId = getRandomLong()
        val epicId = getRandomLong()
        val projectId = getRandomLong()
        val sprint = getRandomLong()

        val actual = sut.getUserStories(
            assignedId = assignedId,
            isClosed = isClosed,
            isDashboard = isDashboard,
            watcherId = watcherId,
            epicId = epicId,
            project = projectId,
            sprint = sprint
        )

        assertEquals(2, actual.size)
        assertEquals(dto1.id, actual[0].id)
        assertEquals(dto2.id, actual[1].id)
    }
}
