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
}
