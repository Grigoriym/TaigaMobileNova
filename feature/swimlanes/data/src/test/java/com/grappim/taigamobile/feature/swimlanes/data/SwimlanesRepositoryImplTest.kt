package com.grappim.taigamobile.feature.swimlanes.data

import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.feature.swimlanes.domain.Swimlane
import com.grappim.taigamobile.feature.swimlanes.domain.SwimlanesRepository
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getSwimlane
import com.grappim.taigamobile.testing.getSwimlaneDTO
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SwimlanesRepositoryImplTest {

    private val swimlanesApi: SwimlanesApi = mockk()
    private val taigaSessionStorage: TaigaSessionStorage = mockk()
    private val swimlanesMapper: SwimlanesMapper = mockk()

    private lateinit var sut: SwimlanesRepository

    private val projectId = getRandomLong()

    @Before
    fun setup() {
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns projectId

        sut = SwimlanesRepositoryImpl(
            swimlanesApi = swimlanesApi,
            taigaSessionStorage = taigaSessionStorage,
            swimlanesMapper = swimlanesMapper
        )
    }

    @Test
    fun `getSwimlanes should return mapped swimlanes`() = runTest {
        val swimlaneDTO1 = getSwimlaneDTO()
        val swimlaneDTO2 = getSwimlaneDTO()
        val apiResponse = listOf(swimlaneDTO1, swimlaneDTO2)
        val expectedSwimlanes = persistentListOf(
            getSwimlane(),
            getSwimlane()
        )

        coEvery { swimlanesApi.getSwimlanes(projectId) } returns apiResponse
        every { swimlanesMapper.toListDomain(apiResponse) } returns expectedSwimlanes

        val result = sut.getSwimlanes()

        assertEquals(expectedSwimlanes, result)
        coVerify { taigaSessionStorage.getCurrentProjectId() }
        coVerify { swimlanesApi.getSwimlanes(projectId) }
        verify { swimlanesMapper.toListDomain(apiResponse) }
    }

    @Test
    fun `getSwimlanes should return empty list when no swimlanes`() = runTest {
        val apiResponse = emptyList<SwimlaneDTO>()
        val expectedSwimlanes = persistentListOf<Swimlane>()

        coEvery { swimlanesApi.getSwimlanes(projectId) } returns apiResponse
        every { swimlanesMapper.toListDomain(apiResponse) } returns expectedSwimlanes

        val result = sut.getSwimlanes()

        assertEquals(0, result.size)
    }

    @Test
    fun `getSwimlanes should use current project id from session`() = runTest {
        val customProjectId = getRandomLong()
        coEvery { taigaSessionStorage.getCurrentProjectId() } returns customProjectId

        val apiResponse = emptyList<SwimlaneDTO>()
        coEvery { swimlanesApi.getSwimlanes(customProjectId) } returns apiResponse
        every { swimlanesMapper.toListDomain(apiResponse) } returns persistentListOf()

        sut.getSwimlanes()

        coVerify { swimlanesApi.getSwimlanes(customProjectId) }
    }
}
