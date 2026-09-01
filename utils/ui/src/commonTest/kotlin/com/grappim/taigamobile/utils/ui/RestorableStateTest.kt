package com.grappim.taigamobile.utils.ui

import androidx.lifecycle.SavedStateHandle
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlin.test.Test
import kotlin.test.assertEquals

class RestorableStateTest {

    @Test
    fun `restore - no prior value - returns the default`() {
        val restorableState = RestorableState(SavedStateHandle())

        assertEquals("default", restorableState.restore("key", "default"))
    }

    @Test
    fun `restore - prior value present - returns it instead of the default`() {
        val restored = getRandomString()
        val restorableState = RestorableState(SavedStateHandle(mapOf("key" to restored)))

        assertEquals(restored, restorableState.restore("key", "default"))
    }

    @Test
    fun `save - writes the value into the SavedStateHandle under the given key`() {
        val savedStateHandle = SavedStateHandle()
        val restorableState = RestorableState(savedStateHandle)
        val value = getRandomString()

        restorableState.save("key", value)

        assertEquals(value, savedStateHandle.get<String>("key"))
    }
}
