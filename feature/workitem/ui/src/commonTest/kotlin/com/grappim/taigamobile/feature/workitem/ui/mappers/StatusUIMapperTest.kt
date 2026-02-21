package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.testing.getRandomLong
import com.grappim.taigamobile.testing.getRandomString
import com.grappim.taigamobile.testing.getStatusFilters
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticStringColor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class StatusUIMapperTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var sut: StatusUIMapper

    @Before
    fun setUp() {
        sut = StatusUIMapper(
            ioDispatcher = testDispatcher
        )
    }

    @Test
    fun `toUI with Statuses should return StatusUI correctly`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()
        val color = "#AABBCC"

        val status = Status(
            id = id,
            name = name,
            color = color
        )

        val actual = sut.toUI(status)

        assertEquals(id, actual.id)
        assertEquals(NativeText.Simple(name), actual.title)
        assertEquals(StaticStringColor(color), actual.color)
    }

    @Test
    fun `toUI with StatusFilters should return StatusUI correctly`() = runTest {
        val id = getRandomLong()
        val name = getRandomString()
        val color = "#DDEEFF"

        val statusFilters = getStatusFilters(
            id = id,
            name = name,
            color = color
        )

        val actual = sut.toUI(statusFilters)

        assertEquals(id, actual.id)
        assertEquals(NativeText.Simple(name), actual.title)
        assertEquals(StaticStringColor(color), actual.color)
    }
}
