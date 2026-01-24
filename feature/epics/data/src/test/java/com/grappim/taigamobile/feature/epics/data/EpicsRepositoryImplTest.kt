package com.grappim.taigamobile.feature.epics.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.epics.domain.Epic
import com.grappim.taigamobile.feature.epics.domain.EpicsRepository
import com.grappim.taigamobile.feature.epics.dto.LinkToEpicRequestDTO
import com.grappim.taigamobile.feature.epics.mapper.EpicMapper
import com.grappim.taigamobile.feature.workitem.data.WorkItemApi
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapper
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class EpicsRepositoryImplTest {

    private val epicsApi: EpicsApi = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val workItemApi: WorkItemApi = mockk()
    private val epicMapper: EpicMapper = mockk()
    private val workItemMapper: WorkItemMapper = mockk()

    private lateinit var sut: EpicsRepository

    private val taskPath = WorkItemPathPlural(CommonTaskType.Epic)

    @Before
    fun setup() {
        sut = EpicsRepositoryImpl(
            epicsApi = epicsApi,
            taigaSessionStorage = taigaSessionStorage,
            workItemApi = workItemApi,
            epicMapper = epicMapper,
            workItemMapper = workItemMapper
        )
    }

    @Test
    fun `getEpic should return correct Epic`() = runTest {
        val epicId = getRandomLong()
        val mockResponse = getWorkItemResponseDTO()
        val expectedEpic = mockk<Epic>()

        coEvery {
            workItemApi.getWorkItemById(
                taskPath = taskPath,
                id = epicId
            )
        } returns mockResponse
        coEvery { epicMapper.toDomain(mockResponse) } returns expectedEpic

        val actual = sut.getEpic(epicId)

        assertEquals(expectedEpic, actual)
        coVerify { workItemApi.getWorkItemById(taskPath = taskPath, id = epicId) }
        coVerify { epicMapper.toDomain(mockResponse) }
    }

    @Test
    fun `getEpics should return list of epics`() = runTest {
        val projectId = getRandomLong()
        val mockResponses = listOf(getWorkItemResponseDTO(), getWorkItemResponseDTO())
        val expectedEpics = persistentListOf(mockk<Epic>(), mockk<Epic>())

        coEvery {
            workItemApi.getWorkItems(
                taskPath = taskPath,
                project = projectId,
                assignedId = null,
                isClosed = null,
                watcherId = null
            )
        } returns mockResponses
        coEvery { epicMapper.toDomainList(mockResponses) } returns expectedEpics

        val actual = sut.getEpics(projectId = projectId)

        assertEquals(expectedEpics, actual)
        coVerify {
            workItemApi.getWorkItems(
                taskPath = taskPath,
                project = projectId,
                assignedId = null,
                isClosed = null,
                watcherId = null
            )
        }
        coVerify { epicMapper.toDomainList(mockResponses) }
    }

    @Test
    fun `getEpics should pass all filter parameters`() = runTest {
        val projectId = getRandomLong()
        val assignedId = getRandomLong()
        val watcherId = getRandomLong()
        val isClosed = true
        val mockResponses = listOf(getWorkItemResponseDTO())
        val expectedEpics = persistentListOf(mockk<Epic>())

        coEvery {
            workItemApi.getWorkItems(
                taskPath = taskPath,
                project = projectId,
                assignedId = assignedId,
                isClosed = isClosed,
                watcherId = watcherId
            )
        } returns mockResponses
        coEvery { epicMapper.toDomainList(mockResponses) } returns expectedEpics

        val actual = sut.getEpics(
            projectId = projectId,
            assignedId = assignedId,
            isClosed = isClosed,
            watcherId = watcherId
        )

        assertEquals(expectedEpics, actual)
        coVerify {
            workItemApi.getWorkItems(
                taskPath = taskPath,
                project = projectId,
                assignedId = assignedId,
                isClosed = isClosed,
                watcherId = watcherId
            )
        }
    }

    @Test
    fun `linkToEpic should call api with correct parameters`() = runTest {
        val epicId = getRandomLong()
        val userStoryId = getRandomLong()

        coJustRun {
            epicsApi.linkToEpic(
                epicId = epicId,
                linkToEpicRequest = LinkToEpicRequestDTO(epicId.toString(), userStoryId)
            )
        }

        sut.linkToEpic(epicId, userStoryId)

        coVerify {
            epicsApi.linkToEpic(
                epicId = epicId,
                linkToEpicRequest = LinkToEpicRequestDTO(epicId.toString(), userStoryId)
            )
        }
    }

    @Test
    fun `unlinkFromEpic should call api with correct parameters`() = runTest {
        val epicId = getRandomLong()
        val userStoryId = getRandomLong()

        coEvery { epicsApi.unlinkFromEpic(epicId, userStoryId) } returns mockk()

        sut.unlinkFromEpic(epicId, userStoryId)

        coVerify { epicsApi.unlinkFromEpic(epicId, userStoryId) }
    }

    @Test
    fun `getEpics should return empty list when no epics found`() = runTest {
        val projectId = getRandomLong()
        val mockResponses = emptyList<com.grappim.taigamobile.feature.workitem.dto.WorkItemResponseDTO>()
        val expectedEpics = persistentListOf<Epic>()

        coEvery {
            workItemApi.getWorkItems(
                taskPath = taskPath,
                project = projectId,
                assignedId = null,
                isClosed = null,
                watcherId = null
            )
        } returns mockResponses
        coEvery { epicMapper.toDomainList(mockResponses) } returns expectedEpics

        val actual = sut.getEpics(projectId = projectId)

        assertEquals(0, actual.size)
    }
}
