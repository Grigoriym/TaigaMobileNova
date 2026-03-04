package com.grappim.taigamobile.feature.sprint.data

import com.grappim.taigamobile.testing.utils.getRandomBoolean
import com.grappim.taigamobile.testing.utils.getRandomInt
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.testing.utils.nowLocalDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SprintMapperTest {

    private lateinit var sut: SprintMapper

    @BeforeTest
    fun setup() {
        sut = SprintMapper()
    }

    @Test
    fun `toDomain should map all fields correctly`() {
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
    fun `toDomain should count user stories correctly`() {
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
    fun `toDomain should handle empty user stories list`() {
        val dto = getSprintResponseDTO().copy(userStories = emptyList())

        val result = sut.toDomain(dto)

        assertEquals(0, result.storiesCount)
    }

    @Test
    fun `toDomainList should map list of DTOs`() {
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
    fun `toDomainList should return empty list for empty input`() {
        val result = sut.toDomainList(emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `toDomain should map closed sprint correctly`() {
        val dto = getSprintResponseDTO().copy(closed = true)

        val result = sut.toDomain(dto)

        assertEquals(true, result.isClosed)
    }

    @Test
    fun `toDomain should map open sprint correctly`() {
        val dto = getSprintResponseDTO().copy(closed = false)

        val result = sut.toDomain(dto)

        assertEquals(false, result.isClosed)
    }

    @Test
    fun `toDomain should map dates correctly`() {
        val startDate = LocalDate.parse("2024-01-01")
        val endDate = LocalDate.parse("2024-01-14")
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
        estimatedFinish = nowLocalDate.plus(14, DateTimeUnit.DAY),
        closed = getRandomBoolean(),
        order = getRandomInt(),
        userStories = listOf(
            SprintUserStoryDTO(id = getRandomLong()),
            SprintUserStoryDTO(id = getRandomLong())
        )
    )
}
