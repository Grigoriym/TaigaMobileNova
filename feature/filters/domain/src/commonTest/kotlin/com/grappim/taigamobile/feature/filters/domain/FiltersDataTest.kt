package com.grappim.taigamobile.feature.filters.domain

import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import com.grappim.taigamobile.feature.filters.domain.model.StatusFilters
import com.grappim.taigamobile.feature.filters.domain.model.TagFilters
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class FiltersDataTest {

    private fun status(id: Long, name: String = "New", count: Long = 1, color: String = "#FF0000") =
        StatusFilters(id = id, name = name, count = count, color = color)

    private fun tag(name: String, count: Long = 1, color: String = "#FF0000") =
        TagFilters(id = null, name = name, count = count, color = color)

    @Test
    fun `selectsSameItemsAs ignores names, colors and counts`() {
        val saved = FiltersData(statuses = persistentListOf(status(id = 1)), tags = persistentListOf(tag("ui")))
        val refreshed = FiltersData(
            statuses = persistentListOf(status(id = 1, name = "Renamed", count = 7, color = "#00FF00")),
            tags = persistentListOf(tag("ui", count = 4, color = "#0000FF"))
        )

        assertTrue(saved.selectsSameItemsAs(refreshed))
    }

    @Test
    fun `selectsSameItemsAs is false when a different item is selected`() {
        val a = FiltersData(statuses = persistentListOf(status(id = 1)))
        val b = FiltersData(statuses = persistentListOf(status(id = 2)))

        assertFalse(a.selectsSameItemsAs(b))
    }

    @Test
    fun `selectsSameItemsAs identifies tags by name`() {
        val a = FiltersData(tags = persistentListOf(tag("ui")))
        val b = FiltersData(tags = persistentListOf(tag("backend")))

        assertFalse(a.selectsSameItemsAs(b))
    }

    @Test
    fun `selectsSameItemsAs tells apart the same id in different sections`() {
        val a = FiltersData(statuses = persistentListOf(status(id = 1)))
        val b = FiltersData(priorities = persistentListOf(status(id = 1)))

        assertFalse(a.selectsSameItemsAs(b))
    }

    @Test
    fun `selectsSameItemsAs is false when an item is added`() {
        val a = FiltersData(statuses = persistentListOf(status(id = 1)))
        val b = FiltersData(statuses = persistentListOf(status(id = 1), status(id = 2)))

        assertFalse(a.selectsSameItemsAs(b))
    }
}
