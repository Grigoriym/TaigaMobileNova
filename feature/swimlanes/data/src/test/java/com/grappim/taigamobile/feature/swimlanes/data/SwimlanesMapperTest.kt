package com.grappim.taigamobile.feature.swimlanes.data

import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwimlanesMapperTest {

    private lateinit var sut: SwimlanesMapper

    @Before
    fun setup() {
        sut = SwimlanesMapper()
    }

    @Test
    fun `toDomain should map single SwimlaneDTO correctly`() {
        val dto = SwimlaneDTO(
            id = getRandomLong(),
            name = getRandomString(),
            order = getRandomLong()
        )

        val result = sut.toDomain(dto)

        assertEquals(dto.id, result.id)
        assertEquals(dto.name, result.name)
        assertEquals(dto.order, result.order)
    }

    @Test
    fun `toListDomain should map list of SwimlaneDTOs correctly`() {
        val dto1 = SwimlaneDTO(
            id = getRandomLong(),
            name = getRandomString(),
            order = getRandomLong()
        )
        val dto2 = SwimlaneDTO(
            id = getRandomLong(),
            name = getRandomString(),
            order = getRandomLong()
        )
        val dto3 = SwimlaneDTO(
            id = getRandomLong(),
            name = getRandomString(),
            order = getRandomLong()
        )
        val dtoList = listOf(dto1, dto2, dto3)

        val result = sut.toListDomain(dtoList)

        assertEquals(3, result.size)
        assertEquals(dto1.id, result[0].id)
        assertEquals(dto1.name, result[0].name)
        assertEquals(dto1.order, result[0].order)

        assertEquals(dto2.id, result[1].id)
        assertEquals(dto2.name, result[1].name)
        assertEquals(dto2.order, result[1].order)

        assertEquals(dto3.id, result[2].id)
        assertEquals(dto3.name, result[2].name)
        assertEquals(dto3.order, result[2].order)
    }

    @Test
    fun `toListDomain should return empty list for empty input`() {
        val dtoList = emptyList<SwimlaneDTO>()

        val result = sut.toListDomain(dtoList)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `toDomain should handle swimlane with zero order`() {
        val dto = SwimlaneDTO(
            id = getRandomLong(),
            name = getRandomString(),
            order = 0L
        )

        val result = sut.toDomain(dto)

        assertEquals(0L, result.order)
    }

    @Test
    fun `toDomain should handle swimlane with special characters in name`() {
        val specialName = "Sprint [1] (active)"
        val dto = SwimlaneDTO(
            id = getRandomLong(),
            name = specialName,
            order = getRandomLong()
        )

        val result = sut.toDomain(dto)

        assertEquals(specialName, result.name)
    }

    @Test
    fun `toListDomain should preserve order of items`() {
        val dtos = (1..5).map { index ->
            SwimlaneDTO(
                id = index.toLong(),
                name = "Swimlane $index",
                order = index.toLong()
            )
        }

        val result = sut.toListDomain(dtos)

        assertEquals(5, result.size)
        result.forEachIndexed { index, swimlane ->
            assertEquals((index + 1).toLong(), swimlane.id)
            assertEquals("Swimlane ${index + 1}", swimlane.name)
            assertEquals((index + 1).toLong(), swimlane.order)
        }
    }
}
