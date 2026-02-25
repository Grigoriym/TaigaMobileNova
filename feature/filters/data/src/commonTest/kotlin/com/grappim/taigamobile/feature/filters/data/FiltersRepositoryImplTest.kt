package com.grappim.taigamobile.feature.filters.data

import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.feature.filters.domain.repo.FiltersRepository
import com.grappim.taigamobile.feature.filters.mapper.FiltersMapper
import com.grappim.taigamobile.feature.filters.mapper.StatusesMapper
import com.grappim.taigamobile.feature.workitem.domain.getPluralPath
import com.grappim.taigamobile.testing.api.FakeFiltersApi
import com.grappim.taigamobile.testing.models.getFiltersDataResponseDTO
import com.grappim.taigamobile.testing.models.getFiltersResponseFilter
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FiltersRepositoryImplTest {

    private val projectId = getRandomLong()
    private val filtersApi = FakeFiltersApi()
    private val taigaSessionStorage = FakeTaigaSessionStorage(
        currentProjectId = projectId
    )
    private val filtersMapper: FiltersMapper = FiltersMapper()
    private val statusesMapper: StatusesMapper = StatusesMapper()

    private lateinit var sut: FiltersRepository

    @BeforeTest
    fun setup() {
        sut = FiltersRepositoryImpl(
            filtersApi = filtersApi,
            taigaSessionStorage = taigaSessionStorage,
            filtersMapper = filtersMapper,
            statusesMapper = statusesMapper
        )
    }

    @Test
    fun `getFiltersData should return mapped filters data`() = runTest {
        val dto = getFiltersDataResponseDTO()
        filtersApi.response = dto
        val taskType = CommonTaskType.UserStory

        val actual = sut.getFiltersData(taskType)

        val call = filtersApi.calls.first()

        assertEquals(taskType.getPluralPath(), call.taskPath)
        assertEquals(projectId, call.project)
        assertEquals(null, call.milestone)
        assertEquals(filtersMapper.toDomain(dto), actual)
    }

    @Test
    fun `getFiltersData should pass null milestone when not from backlog`() = runTest {
        val taskType = CommonTaskType.Task
        val dto = getFiltersDataResponseDTO()
        filtersApi.response = dto

        val actual = sut.getFiltersData(taskType, isCommonTaskFromBacklog = false)

        val call = filtersApi.calls.first()

        assertEquals(taskType.getPluralPath(), call.taskPath)
        assertEquals(projectId, call.project)
        assertEquals(null, call.milestone)
        assertEquals(filtersMapper.toDomain(dto), actual)
    }

    @Test
    fun `getFiltersData should pass null string milestone when from backlog`() = runTest {
        val taskType = CommonTaskType.Task
        val dto = getFiltersDataResponseDTO()
        filtersApi.response = dto

        val actual = sut.getFiltersData(taskType, isCommonTaskFromBacklog = true)

        val call = filtersApi.calls.first()

        assertEquals(taskType.getPluralPath(), call.taskPath)
        assertEquals(projectId, call.project)
        assertEquals("null", call.milestone)
        assertEquals(filtersMapper.toDomain(dto), actual)
    }

    @Test
    fun `getFiltersData should work with Epic task type`() = runTest {
        val dto = getFiltersDataResponseDTO()
        filtersApi.response = dto
        val taskType = CommonTaskType.Epic

        val actual = sut.getFiltersData(taskType)

        val call = filtersApi.calls.first()

        assertEquals(taskType.getPluralPath(), call.taskPath)
        assertEquals(projectId, call.project)
        assertEquals(null, call.milestone)
        assertEquals(filtersMapper.toDomain(dto), actual)
    }

    @Test
    fun `getFiltersData should work with Issue task type`() = runTest {
        val dto = getFiltersDataResponseDTO()
        filtersApi.response = dto
        val taskType = CommonTaskType.Issue

        val actual = sut.getFiltersData(taskType)

        val call = filtersApi.calls.first()

        assertEquals(taskType.getPluralPath(), call.taskPath)
        assertEquals(projectId, call.project)
        assertEquals(null, call.milestone)
        assertEquals(filtersMapper.toDomain(dto), actual)
    }

    @Test
    fun `getStatuses should return mapped statuses from filters data`() = runTest {
        val taskType = CommonTaskType.UserStory

        val filter1 = getFiltersResponseFilter()
        val filter2 = getFiltersResponseFilter()
        filtersApi.response = getFiltersDataResponseDTO(statuses = listOf(filter1, filter2))

        val actual = sut.getStatuses(taskType)

        val call = filtersApi.calls.first()

        assertEquals(taskType.getPluralPath(), call.taskPath)
        assertEquals(projectId, call.project)
        assertEquals(null, call.milestone)

        assertEquals(2, actual.size)
        assertEquals(filter1.id, actual[0].id)
        assertEquals(filter1.name, actual[0].name)
        assertEquals(filter2.id, actual[1].id)
        assertEquals(filter2.name, actual[1].name)
    }

    @Test
    fun `getStatuses should return empty list when no statuses`() = runTest {
        val taskType = CommonTaskType.UserStory
        filtersApi.response = getFiltersDataResponseDTO(statuses = persistentListOf())

        val actual = sut.getStatuses(taskType)

        assertEquals(0, actual.size)
    }
}
