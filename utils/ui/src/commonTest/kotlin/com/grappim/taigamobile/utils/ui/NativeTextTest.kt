package com.grappim.taigamobile.utils.ui

import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.error_not_found
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * [NativeText.asString] is `@Composable` and is not covered here — only its non-composable twin
 * [NativeText.asStringBlocking] is. The two `when` bodies are written separately, so this proves
 * nothing about the composable one.
 */
class NativeTextTest {

    @Test
    fun `only Empty is empty`() {
        assertTrue(NativeText.Empty.isEmpty())
        assertFalse(NativeText.Empty.isNotEmpty())

        val nonEmpty = listOf(
            NativeText.Simple(getRandomString()),
            NativeText.Resource(RString.error_not_found),
            NativeText.Arguments(RString.error_not_found, emptyList()),
            NativeText.Multi(emptyList())
        )
        nonEmpty.forEach {
            assertFalse(it.isEmpty(), "$it should not be empty")
            assertTrue(it.isNotEmpty(), "$it should be not empty")
        }
    }

    @Test
    fun `asStringBlocking returns the text of Simple`() {
        val text = getRandomString()

        assertEquals(text, NativeText.Simple(text).asStringBlocking())
    }

    @Test
    fun `asStringBlocking returns an empty string for Empty`() {
        assertEquals("", NativeText.Empty.asStringBlocking())
    }

    @Test
    fun `asStringBlocking concatenates Multi in order`() {
        val first = getRandomString()
        val second = getRandomString()
        val multi = NativeText.Multi(
            listOf(
                NativeText.Simple(first),
                NativeText.Empty,
                NativeText.Simple(second)
            )
        )

        assertEquals(first + second, multi.asStringBlocking())
    }

    @Test
    fun `asStringBlocking flattens nested Multi`() {
        val inner = getRandomString()
        val outer = getRandomString()
        val multi = NativeText.Multi(
            listOf(
                NativeText.Multi(listOf(NativeText.Simple(inner))),
                NativeText.Simple(outer)
            )
        )

        assertEquals(inner + outer, multi.asStringBlocking())
    }

    @Test
    fun `asStringBlocking of an empty Multi is an empty string`() {
        assertEquals("", NativeText.Multi(emptyList()).asStringBlocking())
    }
}
