package com.grappim.taigamobile.feature.filters.mapper

import com.grappim.taigamobile.testing.getRandomString
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TagsMapperTest {

    private lateinit var sut: TagsMapper

    @Before
    fun setup() {
        sut = TagsMapper()
    }

    @Test
    fun `toTags should map list of tags correctly`() {
        val tagName = getRandomString()
        val tagColor = "#FF0000"
        val tags = listOf(listOf(tagName, tagColor))

        val result = sut.toTags(tags)

        assertEquals(1, result.size)
        assertEquals(tagName, result[0].name)
        assertEquals(tagColor, result[0].color)
    }

    @Test
    fun `toTags should return empty list for null input`() {
        val result = sut.toTags(null)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `toTags should return empty list for empty input`() {
        val result = sut.toTags(emptyList())

        assertTrue(result.isEmpty())
    }

    @Test
    fun `toTags should use default gray color when color is null`() {
        val tagName = getRandomString()
        val tags = listOf(listOf<String?>(tagName, null))

        val result = sut.toTags(tags)

        assertEquals(1, result.size)
        assertEquals(tagName, result[0].name)
        assertEquals("#A9AABC", result[0].color)
    }

    @Test
    fun `toTags should use default gray color when color is empty`() {
        val tagName = getRandomString()
        val tags = listOf(listOf(tagName, ""))

        val result = sut.toTags(tags)

        assertEquals(1, result.size)
        assertEquals(tagName, result[0].name)
        assertEquals("#A9AABC", result[0].color)
    }

    @Test
    fun `toTags should use empty string when name is null`() {
        val tagColor = "#00FF00"
        val tags = listOf(listOf<String?>(null, tagColor))

        val result = sut.toTags(tags)

        assertEquals(1, result.size)
        assertEquals("", result[0].name)
        assertEquals(tagColor, result[0].color)
    }

    @Test
    fun `toTags should handle empty inner list`() {
        val tags = listOf(emptyList<String?>())

        val result = sut.toTags(tags)

        assertEquals(1, result.size)
        assertEquals("", result[0].name)
        assertEquals("#A9AABC", result[0].color)
    }

    @Test
    fun `toTags should map multiple tags correctly`() {
        val tag1Name = getRandomString()
        val tag1Color = "#FF0000"
        val tag2Name = getRandomString()
        val tag2Color = "#00FF00"
        val tag3Name = getRandomString()
        val tag3Color = "#0000FF"
        val tags = listOf(
            listOf(tag1Name, tag1Color),
            listOf(tag2Name, tag2Color),
            listOf(tag3Name, tag3Color)
        )

        val result = sut.toTags(tags)

        assertEquals(3, result.size)
        assertEquals(tag1Name, result[0].name)
        assertEquals(tag1Color, result[0].color)
        assertEquals(tag2Name, result[1].name)
        assertEquals(tag2Color, result[1].color)
        assertEquals(tag3Name, result[2].name)
        assertEquals(tag3Color, result[2].color)
    }

    @Test
    fun `toTags should handle list with only name`() {
        val tagName = getRandomString()
        val tags = listOf(listOf<String?>(tagName))

        val result = sut.toTags(tags)

        assertEquals(1, result.size)
        assertEquals(tagName, result[0].name)
        assertEquals("#A9AABC", result[0].color)
    }
}
