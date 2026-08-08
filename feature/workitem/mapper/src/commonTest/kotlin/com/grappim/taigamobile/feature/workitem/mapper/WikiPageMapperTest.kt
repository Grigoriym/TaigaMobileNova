package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.testing.models.getWikiPageDTO
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WikiPageMapperTest {

    private lateinit var sut: WikiPageMapper

    @BeforeTest
    fun setup() {
        sut = WikiPageMapper()
    }

    @Test
    fun `toDomain should map all fields correctly`() = runTest {
        val dto = getWikiPageDTO()

        val result = sut.toDomain(dto)

        assertEquals(dto.id, result.id)
        assertEquals(dto.version, result.version)
        assertEquals(dto.content, result.content)
        assertEquals(dto.editions, result.editions)
        assertEquals(dto.createdDate, result.createdDate)
        assertEquals(dto.isWatcher, result.isWatcher)
        assertEquals(dto.lastModifierId, result.lastModifier)
        assertEquals(dto.modifiedDate, result.modifiedDate)
        assertEquals(dto.totalWatchers, result.totalWatchers)
        assertEquals(dto.slug, result.slug)
    }

    @Test
    fun `toDomain should handle null lastModifierId`() = runTest {
        val dto = getWikiPageDTO(lastModifierId = null)

        val result = sut.toDomain(dto)

        assertEquals(null, result.lastModifier)
    }

    @Test
    fun `toDomain should map isWatcher true correctly`() = runTest {
        val dto = getWikiPageDTO(isWatcher = true)

        val result = sut.toDomain(dto)

        assertEquals(true, result.isWatcher)
    }

    @Test
    fun `toDomain should map isWatcher false correctly`() = runTest {
        val dto = getWikiPageDTO(isWatcher = false)

        val result = sut.toDomain(dto)

        assertEquals(false, result.isWatcher)
    }

    @Test
    fun `toDomainList should map list of DTOs correctly`() = runTest {
        val dto1 = getWikiPageDTO()
        val dto2 = getWikiPageDTO()
        val dto3 = getWikiPageDTO()
        val dtos = listOf(dto1, dto2, dto3)

        val result = sut.toDomainList(dtos)

        assertEquals(3, result.size)
        assertEquals(dto1.id, result[0].id)
        assertEquals(dto1.slug, result[0].slug)
        assertEquals(dto2.id, result[1].id)
        assertEquals(dto2.slug, result[1].slug)
        assertEquals(dto3.id, result[2].id)
        assertEquals(dto3.slug, result[2].slug)
    }

    @Test
    fun `toDomainList should return empty list for empty input`() = runTest {
        val result = sut.toDomainList(emptyList())

        assertTrue(result.isEmpty())
    }
}
