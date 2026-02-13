package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiLinkDTO
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WikiLinkMapperTest {

    private lateinit var sut: WikiLinkMapper

    @Before
    fun setup() {
        sut = WikiLinkMapper()
    }

    @Test
    fun `toDomain should map all fields correctly`() = runTest {
        val dto = createWikiLinkDTO()

        val result = sut.toDomain(dto)

        assertEquals(dto.id, result.id)
        assertEquals(dto.order, result.order)
        assertEquals(dto.href, result.ref)
        assertEquals(dto.title, result.title)
    }

    @Test
    fun `toDomainList should map list of DTOs correctly`() = runTest {
        val dto1 = createWikiLinkDTO()
        val dto2 = createWikiLinkDTO()
        val dto3 = createWikiLinkDTO()
        val dtos = listOf(dto1, dto2, dto3)

        val result = sut.toDomainList(dtos)

        assertEquals(3, result.size)
        assertEquals(dto1.id, result[0].id)
        assertEquals(dto1.href, result[0].ref)
        assertEquals(dto2.id, result[1].id)
        assertEquals(dto2.href, result[1].ref)
        assertEquals(dto3.id, result[2].id)
        assertEquals(dto3.href, result[2].ref)
    }

    @Test
    fun `toDomainList should return empty list for empty input`() = runTest {
        val result = sut.toDomainList(emptyList())

        assertTrue(result.isEmpty())
    }

    private fun createWikiLinkDTO(
        id: Long = getRandomLong(),
        projectId: Long = getRandomLong(),
        title: String = getRandomString(),
        href: String = getRandomString(),
        order: Long = getRandomLong()
    ) = WikiLinkDTO(
        id = id,
        projectId = projectId,
        title = title,
        href = href,
        order = order
    )
}
