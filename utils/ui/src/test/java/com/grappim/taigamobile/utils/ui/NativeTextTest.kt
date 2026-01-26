package com.grappim.taigamobile.utils.ui

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class NativeTextTest {

    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun `empty NativeText should return empty string`() {
        val empty = NativeText.Empty

        assertEquals(empty.asString(context), "")
    }

    @Test
    fun `simple NativeText should return provided text`() {
        val text = "some text to check"
        val simple = NativeText.Simple(text)

        assertEquals(simple.asString(context), text)
    }

    @Test
    fun `multi NativeText should return correct text`() {
        val multi = NativeText.Multi(
            listOf(
                NativeText.Simple("simple"),
                NativeText.Empty,
                NativeText.Simple("text")
            )
        )

        assertEquals(multi.asString(context), "simpletext")
    }

    @Test
    fun `resource NativeText should return provided text`() {
        val idRes = R.string.test_string
        val simple = NativeText.Resource(idRes)

        assertEquals(simple.asString(context), context.getString(idRes))
    }
}
