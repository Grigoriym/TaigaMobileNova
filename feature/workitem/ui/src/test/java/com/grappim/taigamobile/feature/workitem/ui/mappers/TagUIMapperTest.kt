package com.grappim.taigamobile.feature.workitem.ui.mappers

import com.grappim.taigamobile.testing.getTag
import com.grappim.taigamobile.testing.getTagFilters
import com.grappim.taigamobile.utils.ui.toColor
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TagUIMapperTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var sut: TagUIMapper

    @Before
    fun setUp() {
        sut = TagUIMapper(
            dispatcher = testDispatcher
        )
    }

    @Test
    fun `toUI with Tag should return TagUI correctly`() = runTest {
        val colorString = "#RRGGBB"

        val tag = getTag(color = colorString)
        val actual = sut.toUI(tag)

        assertEquals(tag.name, actual.name)
        assertEquals(tag.color.toColor(), actual.color)
    }

    @Test
    fun `toUIFromFilters with TagFilters should return TagUI correctly`() = runTest {
        val colorString = "#RRGGBB"

        val tag = getTagFilters(color = colorString)
        val actual = sut.toUIFromFilters(tag)

        assertEquals(tag.name, actual.name)
        assertEquals(tag.color.toColor(), actual.color)
    }

    @Test
    fun `toUI with list of Tags should return list of TagUI correctly`() = runTest {
        val colorString1 = "#AABBCC"
        val colorString2 = "#DDEEFF"

        val tag1 = getTag(color = colorString1)
        val tag2 = getTag(color = colorString2)
        val tags = persistentListOf(tag1, tag2)

        val actual = sut.toUI(tags)

        assertEquals(2, actual.size)
        assertEquals(tag1.name, actual[0].name)
        assertEquals(tag1.color.toColor(), actual[0].color)
        assertEquals(tag2.name, actual[1].name)
        assertEquals(tag2.color.toColor(), actual[1].color)
    }

    @Test
    fun `toUI with empty list should return empty list`() = runTest {
        val actual = sut.toUI(persistentListOf())

        assertEquals(0, actual.size)
    }

    @Test
    fun `toUIFromFilters with list of TagFilters should return list of TagUI correctly`() = runTest {
        val colorString1 = "#112233"
        val colorString2 = "#445566"

        val tag1 = getTagFilters(color = colorString1)
        val tag2 = getTagFilters(color = colorString2)
        val tags = persistentListOf(tag1, tag2)

        val actual = sut.toUIFromFilters(tags)

        assertEquals(2, actual.size)
        assertEquals(tag1.name, actual[0].name)
        assertEquals(tag1.color.toColor(), actual[0].color)
        assertEquals(tag2.name, actual[1].name)
        assertEquals(tag2.color.toColor(), actual[1].color)
    }

    @Test
    fun `toUIFromFilters with empty list should return empty list`() = runTest {
        val actual = sut.toUIFromFilters(persistentListOf())

        assertEquals(0, actual.size)
    }
}
