package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.feature.filters.domain.model.Status
import com.grappim.taigamobile.testing.models.getStatusFilters
import com.grappim.taigamobile.testing.utils.getRandomLong
import com.grappim.taigamobile.testing.utils.getRandomString
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.StaticStringColor
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusUIMapperTest {

    private lateinit var sut: StatusUIMapper

    @BeforeTest
    fun setUp() {
        sut = StatusUIMapper()
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
