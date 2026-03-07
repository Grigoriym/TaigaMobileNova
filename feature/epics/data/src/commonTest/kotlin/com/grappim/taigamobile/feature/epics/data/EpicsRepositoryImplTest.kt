package com.grappim.taigamobile.feature.epics.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.epics.dto.LinkToEpicRequestDTO
import com.grappim.taigamobile.feature.epics.mapper.EpicMapper
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.filters.mapper.TagsMapper
import com.grappim.taigamobile.feature.projects.mapper.ProjectMapper
import com.grappim.taigamobile.feature.users.mapper.UserMapper
import com.grappim.taigamobile.feature.workitem.domain.getPluralPath
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.api.FakeEpicsApi
import com.grappim.taigamobile.testing.api.FakeWorkItemApi
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.storage.FakeServerStorage
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EpicsRepositoryImplTest {

    private val fakeEpicsApi = FakeEpicsApi()
    private val fakeWorkItemApi = FakeWorkItemApi()
    private val fakeServerStorage = FakeServerStorage()
    private val epicMapper = EpicMapper(
        serverStorage = fakeServerStorage,
        statusesMapper = StatusesMapper(),
        userMapper = UserMapper(),
        projectMapper = ProjectMapper(),
        tagsMapper = TagsMapper()
    )
    private val workItemMapper = WorkItemMapper(
        statusesMapper = StatusesMapper(),
        userMapper = UserMapper(),
        tagsMapper = TagsMapper(),
        projectMapper = ProjectMapper()
    )

    private lateinit var sut: EpicsRepository

    private val epicTaskPath = CommonTaskType.Epic.getPluralPath()

    @BeforeTest
    fun setup() {
        sut = EpicsRepositoryImpl(
            epicsApi = fakeEpicsApi,
            taigaSessionStorage = FakeTaigaSessionStorage(),
            workItemApi = fakeWorkItemApi,
            epicMapper = epicMapper,
            workItemMapper = workItemMapper
        )
    }

    @Test
    fun `getEpic should return correct Epic`() = runTest {
        val epicId = getRandomLong()
        val dto = getWorkItemResponseDTO()
        fakeWorkItemApi.workItemByIdResponse = dto

        val actual = sut.getEpic(epicId)

        assertEquals(epicMapper.toDomain(dto), actual)
    }

    @Test
    fun `getEpics should return list of epics`() = runTest {
        val projectId = getRandomLong()
        val dtos = listOf(getWorkItemResponseDTO(), getWorkItemResponseDTO())
        fakeWorkItemApi.workItemsResponse = dtos

        val actual = sut.getEpics(projectId = projectId)

        assertEquals(epicMapper.toDomainList(dtos), actual)
        assertEquals(2, actual.size)
    }

    @Test
    fun `getEpics should pass all filter parameters`() = runTest {
        val projectId = getRandomLong()
        val assignedId = getRandomLong()
        val watcherId = getRandomLong()
        val isClosed = true
        fakeWorkItemApi.workItemsResponse = listOf(getWorkItemResponseDTO())

        sut.getEpics(
            projectId = projectId,
            assignedId = assignedId,
            isClosed = isClosed,
            watcherId = watcherId
        )

        assertEquals(1, fakeWorkItemApi.getWorkItemsCalls.size)
        val call = fakeWorkItemApi.getWorkItemsCalls.first()
        assertEquals(epicTaskPath, call.taskPath)
        assertEquals(projectId, call.project)
        assertEquals(assignedId, call.assignedId)
        assertEquals(isClosed, call.isClosed)
        assertEquals(watcherId, call.watcherId)
    }

    @Test
    fun `getEpics should use null filters by default`() = runTest {
        val projectId = getRandomLong()
        fakeWorkItemApi.workItemsResponse = emptyList()

        sut.getEpics(projectId = projectId)

        assertEquals(1, fakeWorkItemApi.getWorkItemsCalls.size)
        val call = fakeWorkItemApi.getWorkItemsCalls.first()
        assertEquals(projectId, call.project)
        assertEquals(null, call.assignedId)
        assertEquals(null, call.isClosed)
        assertEquals(null, call.watcherId)
    }

    @Test
    fun `linkToEpic should call api with correct parameters`() = runTest {
        val epicId = getRandomLong()
        val userStoryId = getRandomLong()

        sut.linkToEpic(epicId, userStoryId)

        assertEquals(1, fakeEpicsApi.linkCalls.size)
        val call = fakeEpicsApi.linkCalls.first()
        assertEquals(epicId, call.epicId)
        assertEquals(LinkToEpicRequestDTO(epicId.toString(), userStoryId), call.request)
    }

    @Test
    fun `unlinkFromEpic should call api with correct parameters`() = runTest {
        val epicId = getRandomLong()
        val userStoryId = getRandomLong()

        sut.unlinkFromEpic(epicId, userStoryId)

        assertEquals(1, fakeEpicsApi.unlinkCalls.size)
        val call = fakeEpicsApi.unlinkCalls.first()
        assertEquals(epicId, call.epicId)
        assertEquals(userStoryId, call.userStoryId)
    }

    @Test
    fun `getEpics should return empty list when no epics found`() = runTest {
        val projectId = getRandomLong()
        fakeWorkItemApi.workItemsResponse = emptyList()

        val actual = sut.getEpics(projectId = projectId)

        assertTrue(actual.isEmpty())
    }
}
