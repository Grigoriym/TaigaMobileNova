package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiPageDTO
import com.grappim.taigamobile.testing.getRandomBoolean
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WikiPageMapperTest {

    private lateinit var sut: WikiPageMapper

    @Before
    fun setup() {
        sut = WikiPageMapper()
    }

    @Test
    fun `toDomain should map all fields correctly`() = runTest {
        val dto = createWikiPageDTO()

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
        val dto = createWikiPageDTO(lastModifierId = null)

        val result = sut.toDomain(dto)

        assertEquals(null, result.lastModifier)
    }

    @Test
    fun `toDomain should map isWatcher true correctly`() = runTest {
        val dto = createWikiPageDTO(isWatcher = true)

        val result = sut.toDomain(dto)

        assertEquals(true, result.isWatcher)
    }

    @Test
    fun `toDomain should map isWatcher false correctly`() = runTest {
        val dto = createWikiPageDTO(isWatcher = false)

        val result = sut.toDomain(dto)

        assertEquals(false, result.isWatcher)
    }

    @Test
    fun `toDomainList should map list of DTOs correctly`() = runTest {
        val dto1 = createWikiPageDTO()
        val dto2 = createWikiPageDTO()
        val dto3 = createWikiPageDTO()
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

    private fun createWikiPageDTO(
        id: Long = getRandomLong(),
        projectId: Long = getRandomLong(),
        slug: String = getRandomString(),
        content: String = getRandomString(),
        ownerId: Long? = getRandomLong(),
        lastModifierId: Long? = getRandomLong(),
        createdDate: LocalDateTime = LocalDateTime.now(),
        modifiedDate: LocalDateTime = LocalDateTime.now(),
        html: String = getRandomString(),
        editions: Long = getRandomLong(),
        version: Long = getRandomLong(),
        isWatcher: Boolean = getRandomBoolean(),
        totalWatchers: Long = getRandomLong()
    ) = WikiPageDTO(
        id = id,
        projectId = projectId,
        slug = slug,
        content = content,
        ownerId = ownerId,
        lastModifierId = lastModifierId,
        createdDate = createdDate,
        modifiedDate = modifiedDate,
        html = html,
        editions = editions,
        version = version,
        isWatcher = isWatcher,
        totalWatchers = totalWatchers
    )
}
