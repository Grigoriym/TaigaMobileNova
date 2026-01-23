package com.grappim.taigamobile.feature.filters.mapper

import com.grappim.taigamobile.feature.filters.domain.model.filters.StatusFilters
import com.grappim.taigamobile.testing.getFiltersData
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getWorkItemResponseDTO
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class StatusesMapperTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var sut: StatusesMapper

    @Before
    fun setup() {
        sut = StatusesMapper(testDispatcher)
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
