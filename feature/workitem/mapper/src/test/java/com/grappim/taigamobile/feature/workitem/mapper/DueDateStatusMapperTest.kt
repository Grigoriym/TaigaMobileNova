package com.grappim.taigamobile.feature.workitem.mapper

import com.grappim.taigamobile.feature.workitem.domain.DueDateStatus
import com.grappim.taigamobile.feature.workitem.dto.DueDateStatusDTO
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DueDateStatusMapperTest {

    private lateinit var sut: DueDateStatusMapper

    @Before
    fun setup() {
        sut = DueDateStatusMapper()
    }

    @Test
    fun `toDomain should map NotSet correctly`() {
        val result = sut.toDomain(DueDateStatusDTO.NotSet)

        assertEquals(DueDateStatus.NotSet, result)
    }

    @Test
    fun `toDomain should map Set correctly`() {
        val result = sut.toDomain(DueDateStatusDTO.Set)

        assertEquals(DueDateStatus.Set, result)
    }

    @Test
    fun `toDomain should map DueSoon correctly`() {
        val result = sut.toDomain(DueDateStatusDTO.DueSoon)

        assertEquals(DueDateStatus.DueSoon, result)
    }

    @Test
    fun `toDomain should map PastDue correctly`() {
        val result = sut.toDomain(DueDateStatusDTO.PastDue)

        assertEquals(DueDateStatus.PastDue, result)
    }

    @Test
    fun `toDomain should map NoLongerApplicable correctly`() {
        val result = sut.toDomain(DueDateStatusDTO.NoLongerApplicable)

        assertEquals(DueDateStatus.NoLongerApplicable, result)
    }

    @Test
    fun `toDomain should return null for null input`() {
        val result = sut.toDomain(null)

        assertNull(result)
    }
}
