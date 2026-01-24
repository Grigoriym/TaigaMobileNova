package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.filters.domain.FiltersRepository
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.filters.StatusFilters
import com.grappim.taigamobile.feature.filters.dto.FiltersDataResponseDTO
import com.grappim.taigamobile.feature.filters.mapper.FiltersMapper
import com.grappim.taigamobile.feature.workitem.domain.WorkItemPathPlural
import com.grappim.taigamobile.testing.getFiltersData
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getStatusFilters
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FiltersRepositoryImplTest {

    private val filtersApi: FiltersApi = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val filtersMapper: FiltersMapper = mockk()

    private lateinit var sut: FiltersRepository

    private val projectId = getRandomLong()

    @Before
    fun setup() {
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId

        sut = FiltersRepositoryImpl(
            filtersApi = filtersApi,
            taigaSessionStorage = taigaSessionStorage,
            filtersMapper = filtersMapper
        )
    }

    @Test
    fun `getFiltersData should return mapped filters data`() = runTest {
        val taskType = CommonTaskType.UserStory
        val taskPath = WorkItemPathPlural(taskType)
        val mockResponse = mockk<FiltersDataResponseDTO>()
        val expectedFiltersData = getFiltersData()

        coEvery {
            filtersApi.getCommonTaskFiltersData(
                taskPath = taskPath,
                project = projectId,
                milestone = null
            )
        } returns mockResponse
        coEvery { filtersMapper.toDomain(mockResponse) } returns expectedFiltersData

        val actual = sut.getFiltersData(taskType)

        assertEquals(expectedFiltersData, actual)
        coVerify {
            filtersApi.getCommonTaskFiltersData(
                taskPath = taskPath,
                project = projectId,
                milestone = null
            )
        }
        coVerify { filtersMapper.toDomain(mockResponse) }
    }

    @Test
    fun `getFiltersData should pass null milestone when not from backlog`() = runTest {
        val taskType = CommonTaskType.Task
        val taskPath = WorkItemPathPlural(taskType)
        val mockResponse = mockk<FiltersDataResponseDTO>()
        val expectedFiltersData = getFiltersData()

        coEvery {
            filtersApi.getCommonTaskFiltersData(
                taskPath = taskPath,
                project = projectId,
                milestone = null
            )
        } returns mockResponse
        coEvery { filtersMapper.toDomain(mockResponse) } returns expectedFiltersData

        sut.getFiltersData(taskType, isCommonTaskFromBacklog = false)

        coVerify {
            filtersApi.getCommonTaskFiltersData(
                taskPath = taskPath,
                project = projectId,
                milestone = null
            )
        }
    }

    @Test
    fun `getFiltersData should pass null string milestone when from backlog`() = runTest {
        val taskType = CommonTaskType.UserStory
        val taskPath = WorkItemPathPlural(taskType)
        val mockResponse = mockk<FiltersDataResponseDTO>()
        val expectedFiltersData = getFiltersData()

        coEvery {
            filtersApi.getCommonTaskFiltersData(
                taskPath = taskPath,
                project = projectId,
                milestone = "null"
            )
        } returns mockResponse
        coEvery { filtersMapper.toDomain(mockResponse) } returns expectedFiltersData

        sut.getFiltersData(taskType, isCommonTaskFromBacklog = true)

        coVerify {
            filtersApi.getCommonTaskFiltersData(
                taskPath = taskPath,
                project = projectId,
                milestone = "null"
            )
        }
    }

    @Test
    fun `getFiltersData should work with Epic task type`() = runTest {
        val taskType = CommonTaskType.Epic
        val taskPath = WorkItemPathPlural(taskType)
        val mockResponse = mockk<FiltersDataResponseDTO>()
        val expectedFiltersData = getFiltersData()

        coEvery {
            filtersApi.getCommonTaskFiltersData(
                taskPath = taskPath,
                project = projectId,
                milestone = null
            )
        } returns mockResponse
        coEvery { filtersMapper.toDomain(mockResponse) } returns expectedFiltersData

        val actual = sut.getFiltersData(taskType)

        assertEquals(expectedFiltersData, actual)
    }

    @Test
    fun `getFiltersData should work with Issue task type`() = runTest {
        val taskType = CommonTaskType.Issue
        val taskPath = WorkItemPathPlural(taskType)
        val mockResponse = mockk<FiltersDataResponseDTO>()
        val expectedFiltersData = getFiltersData()

        coEvery {
            filtersApi.getCommonTaskFiltersData(
                taskPath = taskPath,
                project = projectId,
                milestone = null
            )
        } returns mockResponse
        coEvery { filtersMapper.toDomain(mockResponse) } returns expectedFiltersData

        val actual = sut.getFiltersData(taskType)

        assertEquals(expectedFiltersData, actual)
    }

    @Test
    fun `getStatuses should return mapped statuses from filters data`() = runTest {
        val taskType = CommonTaskType.UserStory
        val taskPath = WorkItemPathPlural(taskType)
        val mockResponse = mockk<FiltersDataResponseDTO>()

        val statusFilter1 = getStatusFilters()
        val statusFilter2 = getStatusFilters()
        val filtersData = getFiltersData().copy(
            statuses = persistentListOf(statusFilter1, statusFilter2)
        )

        coEvery {
            filtersApi.getCommonTaskFiltersData(
                taskPath = taskPath,
                project = projectId,
                milestone = null
            )
        } returns mockResponse
        coEvery { filtersMapper.toDomain(mockResponse) } returns filtersData

        val actual = sut.getStatuses(taskType)

        assertEquals(2, actual.size)
        assertEquals(statusFilter1.id, actual[0].id)
        assertEquals(statusFilter1.name, actual[0].name)
        assertEquals(statusFilter1.color, actual[0].color)
        assertEquals(statusFilter2.id, actual[1].id)
        assertEquals(statusFilter2.name, actual[1].name)
        assertEquals(statusFilter2.color, actual[1].color)
    }

    @Test
    fun `getStatuses should return empty list when no statuses`() = runTest {
        val taskType = CommonTaskType.Task
        val taskPath = WorkItemPathPlural(taskType)
        val mockResponse = mockk<FiltersDataResponseDTO>()
        val filtersData = getFiltersData().copy(statuses = persistentListOf())

        coEvery {
            filtersApi.getCommonTaskFiltersData(
                taskPath = taskPath,
                project = projectId,
                milestone = null
            )
        } returns mockResponse
        coEvery { filtersMapper.toDomain(mockResponse) } returns filtersData

        val actual = sut.getStatuses(taskType)

        assertEquals(0, actual.size)
    }
}
