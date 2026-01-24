package com.grappim.taigamobile.feature.sprint.data

import com.grappim.taigamobile.testing.getRandomBoolean
import com.grappim.taigamobile.testing.getRandomInt
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.nowLocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SprintMapperTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var sut: SprintMapper

    @Before
    fun setup() {
        sut = SprintMapper(dispatcher = testDispatcher)
    }

    @Test
    fun `toDomain should map all fields correctly`() = runTest {
        val dto = getSprintResponseDTO()

        val result = sut.toDomain(dto)

        assertEquals(dto.id, result.id)
        assertEquals(dto.name, result.name)
        assertEquals(dto.order, result.order)
        assertEquals(dto.estimatedStart, result.start)
        assertEquals(dto.estimatedFinish, result.end)
        assertEquals(dto.userStories.size, result.storiesCount)
        assertEquals(dto.closed, result.isClosed)
    }

    @Test
    fun `toDomain should count user stories correctly`() = runTest {
        val userStories = listOf(
            SprintUserStoryDTO(id = getRandomLong()),
            SprintUserStoryDTO(id = getRandomLong()),
            SprintUserStoryDTO(id = getRandomLong())
        )
        val dto = getSprintResponseDTO().copy(userStories = userStories)

        val result = sut.toDomain(dto)

        assertEquals(3, result.storiesCount)
    }

    @Test
    fun `toDomain should handle empty user stories list`() = runTest {
        val dto = getSprintResponseDTO().copy(userStories = emptyList())

        val result = sut.toDomain(dto)

        assertEquals(0, result.storiesCount)
    }

    @Test
    fun `toDomainList should map list of DTOs`() = runTest {
        val dto1 = getSprintResponseDTO()
        val dto2 = getSprintResponseDTO()
        val dto3 = getSprintResponseDTO()

        val result = sut.toDomainList(listOf(dto1, dto2, dto3))

        assertEquals(3, result.size)
        assertEquals(dto1.id, result[0].id)
        assertEquals(dto2.id, result[1].id)
        assertEquals(dto3.id, result[2].id)
    }

    @Test
    fun `toDomainList should return empty list for empty input`() = runTest {
        val result = sut.toDomainList(emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `toDomain should map closed sprint correctly`() = runTest {
        val dto = getSprintResponseDTO().copy(closed = true)

        val result = sut.toDomain(dto)

        assertEquals(true, result.isClosed)
    }

    @Test
    fun `toDomain should map open sprint correctly`() = runTest {
        val dto = getSprintResponseDTO().copy(closed = false)

        val result = sut.toDomain(dto)

        assertEquals(false, result.isClosed)
    }

    @Test
    fun `toDomain should map dates correctly`() = runTest {
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 14)
        val dto = getSprintResponseDTO().copy(
            estimatedStart = startDate,
            estimatedFinish = endDate
        )

        val result = sut.toDomain(dto)

        assertEquals(startDate, result.start)
        assertEquals(endDate, result.end)
    }

    private fun getSprintResponseDTO(): SprintResponseDTO = SprintResponseDTO(
        id = getRandomLong(),
        name = getRandomString(),
        estimatedStart = nowLocalDate,
        estimatedFinish = nowLocalDate.plusDays(14),
        closed = getRandomBoolean(),
        order = getRandomInt(),
        userStories = listOf(
            SprintUserStoryDTO(id = getRandomLong()),
            SprintUserStoryDTO(id = getRandomLong())
        )
    )
}
