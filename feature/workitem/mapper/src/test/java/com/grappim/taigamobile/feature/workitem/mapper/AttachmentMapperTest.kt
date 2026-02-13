package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.workitem.dto.AttachmentDTO
import com.grappim.taigamobile.testing.getAttachmentDTO
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttachmentMapperTest {

    private lateinit var sut: AttachmentMapper

    @Before
    fun setup() {
        sut = AttachmentMapper()
    }

    @Test
    fun `toDomain should map single AttachmentDTO correctly`() {
        val dto = getAttachmentDTO()

        val result = sut.toDomain(dto)

        assertEquals(dto.id, result.id)
        assertEquals(dto.name, result.name)
        assertEquals(dto.sizeInBytes, result.sizeInBytes)
        assertEquals(dto.url, result.url)
    }

    @Test
    fun `toDomain should map list of AttachmentDTOs correctly`() {
        val dto1 = getAttachmentDTO()
        val dto2 = getAttachmentDTO()
        val dto3 = getAttachmentDTO()
        val dtoList = listOf(dto1, dto2, dto3)

        val result = sut.toDomain(dtoList)

        assertEquals(3, result.size)
        assertEquals(dto1.id, result[0].id)
        assertEquals(dto1.name, result[0].name)
        assertEquals(dto1.sizeInBytes, result[0].sizeInBytes)
        assertEquals(dto1.url, result[0].url)

        assertEquals(dto2.id, result[1].id)
        assertEquals(dto2.name, result[1].name)
        assertEquals(dto2.sizeInBytes, result[1].sizeInBytes)
        assertEquals(dto2.url, result[1].url)

        assertEquals(dto3.id, result[2].id)
        assertEquals(dto3.name, result[2].name)
        assertEquals(dto3.sizeInBytes, result[2].sizeInBytes)
        assertEquals(dto3.url, result[2].url)
    }

    @Test
    fun `toDomain should return empty list for empty input`() {
        val dtoList = emptyList<AttachmentDTO>()

        val result = sut.toDomain(dtoList)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `toDomain should handle attachment with zero size`() {
        val dto = AttachmentDTO(
            id = getRandomLong(),
            name = getRandomString(),
            sizeInBytes = 0L,
            url = getRandomString()
        )

        val result = sut.toDomain(dto)

        assertEquals(0L, result.sizeInBytes)
    }

    @Test
    fun `toDomain should handle attachment with special characters in name`() {
        val specialName = "file (1) [copy].txt"
        val dto = AttachmentDTO(
            id = getRandomLong(),
            name = specialName,
            sizeInBytes = getRandomLong(),
            url = getRandomString()
        )

        val result = sut.toDomain(dto)

        assertEquals(specialName, result.name)
    }
}
