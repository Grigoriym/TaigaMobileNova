package com.grappim.taigamobile.feature.swimlanes.data

import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import com.grappim.taigamobile.testing.api.FakeSwimlanesApi
import com.grappim.taigamobile.testing.models.getSwimlaneDTO
import com.grappim.taigamobile.testing.storage.FakeTaigaSessionStorage
import com.grappim.taigamobile.testing.utils.getRandomLong
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SwimlanesRepositoryImplTest {

    private val projectId = getRandomLong()
    private val swimlanesApi = FakeSwimlanesApi()
    private val taigaSessionStorage: TaigaSessionStorage = FakeTaigaSessionStorage(
        currentProjectId = projectId
    )
    private val swimlanesMapper: SwimlanesMapper = SwimlanesMapper()

    private lateinit var sut: SwimlanesRepository

    @BeforeTest
    fun setup() {
        sut = SwimlanesRepositoryImpl(
            swimlanesApi = swimlanesApi,
            taigaSessionStorage = taigaSessionStorage,
            swimlanesMapper = swimlanesMapper
        )
    }

    @Test
    fun `getSwimlanes should return mapped swimlanes`() = runTest {
        val dto1 = getSwimlaneDTO()
        val dto2 = getSwimlaneDTO()
        swimlanesApi.swimlanesResult = listOf(dto1, dto2)
        val expected = persistentListOf(
            Swimlane(id = dto1.id, name = dto1.name, order = dto1.order),
            Swimlane(id = dto2.id, name = dto2.name, order = dto2.order)
        )

        val result = sut.getSwimlanes()

        assertEquals(expected, result)
    }

    @Test
    fun `getSwimlanes should return empty list when no swimlanes`() = runTest {
        swimlanesApi.swimlanesResult = emptyList()

        val result = sut.getSwimlanes()

        assertEquals(0, result.size)
    }

    @Test
    fun `getSwimlanes should use current project id from session`() = runTest {
        swimlanesApi.swimlanesResult = emptyList()

        sut.getSwimlanes()

        assertEquals(projectId, swimlanesApi.lastProjectId)
    }
}
