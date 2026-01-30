package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.dto.DueDateStatusDTO
import com.grappim.taigamobile.feature.workitem.dto.customattribute.CustomAttributesValuesResponseDTO
import com.grappim.taigamobile.feature.workitem.dto.wiki.WikiPageDTO
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PatchedDataMapperTest {

    private val dueDateStatusMapper: DueDateStatusMapper = mockk()

    private lateinit var sut: PatchedDataMapper

    @Before
    fun setup() {
        sut = PatchedDataMapper(
            dueDateStatusMapper = dueDateStatusMapper
        )
    }

    @Test
    fun `toDomain should map version correctly`() {
        val response = getWorkItemResponseDTO()

        every { dueDateStatusMapper.toDomain(response.dueDateStatusDTO) } returns DueDateStatus.Set

        val result = sut.toDomain(response)

        assertEquals(response.version, result.newVersion)
    }

    @Test
    fun `toDomain should map dueDateStatus correctly`() {
        val response = getWorkItemResponseDTO().copy(dueDateStatusDTO = DueDateStatusDTO.DueSoon)

        every { dueDateStatusMapper.toDomain(DueDateStatusDTO.DueSoon) } returns DueDateStatus.DueSoon

        val result = sut.toDomain(response)

        assertEquals(DueDateStatus.DueSoon, result.dueDateStatus)
    }

    @Test
    fun `toDomain should handle null dueDateStatus`() {
        val response = getWorkItemResponseDTO().copy(dueDateStatusDTO = null)

        every { dueDateStatusMapper.toDomain(null) } returns null

        val result = sut.toDomain(response)

        assertNull(result.dueDateStatus)
    }

    @Test
    fun `toDomainCustomAttrs should map version correctly`() {
        val version = getRandomLong()
        val response = CustomAttributesValuesResponseDTO(
            attributesValues = emptyMap(),
            version = version
        )

        val result = sut.toDomainCustomAttrs(response)

        assertEquals(version, result.version)
    }

    @Test
    fun `fromWiki should map version correctly`() {
        val version = getRandomLong()
        val dto = createWikiPageDTO(version = version)

        val result = sut.fromWiki(dto)

        assertEquals(version, result.newVersion)
    }

    @Test
    fun `fromWiki should always set dueDateStatus to null`() {
        val dto = createWikiPageDTO()

        val result = sut.fromWiki(dto)

        assertNull(result.dueDateStatus)
    }

    private fun createWikiPageDTO(version: Long = getRandomLong()) = WikiPageDTO(
        id = getRandomLong(),
        projectId = getRandomLong(),
        slug = getRandomString(),
        content = getRandomString(),
        ownerId = getRandomLong(),
        lastModifierId = getRandomLong(),
        createdDate = LocalDateTime.now(),
        modifiedDate = LocalDateTime.now(),
        html = getRandomString(),
        editions = getRandomLong(),
        version = version,
        isWatcher = false
    )
}
