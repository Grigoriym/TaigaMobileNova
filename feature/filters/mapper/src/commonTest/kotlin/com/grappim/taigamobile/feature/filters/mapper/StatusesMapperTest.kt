package com.grappim.taigamobile.feature.filters.mapper

import com.grappim.taigamobile.feature.filters.domain.model.StatusFilters
import com.grappim.taigamobile.testing.models.getFiltersData
import com.grappim.taigamobile.testing.models.getWorkItemResponseDTO
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StatusesMapperTest {

    private lateinit var sut: StatusesMapper

    @BeforeTest
    fun setup() {
        sut = StatusesMapper()
    }

    @Test
    fun `on getStatus should return correct value`() = runTest {
        val response = getWorkItemResponseDTO()

        val result = sut.getStatus(response)

        assertEquals(response.status, result.id)
        assertEquals(response.statusExtraInfo.name, result.name)
        assertEquals(response.statusExtraInfo.color, result.color)
    }

    @Test
    fun `on getType should return correct value`() {
        val response = getWorkItemResponseDTO()
        val item = StatusFilters(
            id = response.type!!,
            name = getRandomString(),
            color = getRandomString(),
            count = 9
        )
        val filtersData = getFiltersData().copy(
            types = persistentListOf(item)
        )

        val result = sut.getType(filtersData, response)

        assertEquals(response.type, result?.id)
        assertEquals(item.name, result?.name)
        assertEquals(item.color, result?.color)
    }

    @Test
    fun `on getType with null type should return null`() {
        val response = getWorkItemResponseDTO().copy(type = null)

        val result = sut.getType(getFiltersData(), response)

        assertNull(result)
    }

    @Test
    fun `on getType with no matching type should return null`() {
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData().copy(
            types = persistentListOf(statusFilters(id = response.type!! + 1))
        )

        val result = sut.getType(filtersData, response)

        assertNull(result)
    }

    @Test
    fun `on getSeverity should return correct value`() {
        val response = getWorkItemResponseDTO()
        val item = statusFilters(id = response.severity!!)
        val filtersData = getFiltersData().copy(
            severities = persistentListOf(item)
        )

        val result = sut.getSeverity(filtersData, response)

        assertEquals(response.severity, result?.id)
        assertEquals(item.name, result?.name)
        assertEquals(item.color, result?.color)
    }

    @Test
    fun `on getSeverity with null severity should return null`() {
        val response = getWorkItemResponseDTO().copy(severity = null)

        val result = sut.getSeverity(getFiltersData(), response)

        assertNull(result)
    }

    @Test
    fun `on getSeverity with no matching severity should return null`() {
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData().copy(
            severities = persistentListOf(statusFilters(id = response.severity!! + 1))
        )

        val result = sut.getSeverity(filtersData, response)

        assertNull(result)
    }

    @Test
    fun `on getPriority should return correct value`() {
        val response = getWorkItemResponseDTO()
        val item = statusFilters(id = response.priority!!)
        val filtersData = getFiltersData().copy(
            priorities = persistentListOf(item)
        )

        val result = sut.getPriority(filtersData, response)

        assertEquals(response.priority, result?.id)
        assertEquals(item.name, result?.name)
        assertEquals(item.color, result?.color)
    }

    @Test
    fun `on getPriority with null priority should return null`() {
        val response = getWorkItemResponseDTO().copy(priority = null)

        val result = sut.getPriority(getFiltersData(), response)

        assertNull(result)
    }

    @Test
    fun `on getPriority with no matching priority should return null`() {
        val response = getWorkItemResponseDTO()
        val filtersData = getFiltersData().copy(
            priorities = persistentListOf(statusFilters(id = response.priority!! + 1))
        )

        val result = sut.getPriority(filtersData, response)

        assertNull(result)
    }

    private fun statusFilters(id: Long) = StatusFilters(
        id = id,
        name = getRandomString(),
        color = getRandomString(),
        count = 9
    )
}
