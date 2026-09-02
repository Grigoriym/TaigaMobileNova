package com.grappim.taigamobile.feature.filters.domain

import com.grappim.taigamobile.feature.filters.domain.model.StatusFilters
import com.grappim.taigamobile.feature.filters.domain.model.TagFilters
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class UtilsTest {

    private fun statusFilters(id: Long, count: Long = 0) = StatusFilters(
        id = id,
        name = getRandomString(),
        count = count,
        color = "#FF0000"
    )

    private fun tagFilters(name: String) = TagFilters(
        id = null,
        name = name,
        count = 0,
        color = "#FF0000"
    )

    @Test
    fun `commaString should join ids with comma`() {
        val result = listOf(statusFilters(id = 1), statusFilters(id = 2)).commaString()

        assertEquals("1,2", result)
    }

    @Test
    fun `commaString should return null for empty list`() {
        val result = emptyList<StatusFilters>().commaString()

        assertNull(result)
    }

    @Test
    fun `tagsCommaString should join names with comma and replace spaces with plus`() {
        val result = listOf(tagFilters("bug"), tagFilters("in progress")).tagsCommaString()

        assertEquals("bug,in+progress", result)
    }

    @Test
    fun `tagsCommaString should return null for empty list`() {
        val result = emptyList<TagFilters>().tagsCommaString()

        assertNull(result)
    }

    @Test
    fun `hasData should return true when any filter has positive count`() {
        val result = listOf(statusFilters(id = 1, count = 0), statusFilters(id = 2, count = 5)).hasData()

        assertTrue(result)
    }

    @Test
    fun `hasData should return false when all filters have zero count`() {
        val result = listOf(statusFilters(id = 1), statusFilters(id = 2)).hasData()

        assertFalse(result)
    }

    @Test
    fun `hasData should return false for empty list`() {
        val result = emptyList<StatusFilters>().hasData()

        assertFalse(result)
    }
}
