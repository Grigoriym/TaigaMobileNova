package com.grappim.taigamobile.testing

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class SavedStateHandleRule(private val route: Any) : TestWatcher() {
    val savedStateHandleMock: SavedStateHandle = mockk()
    override fun starting(description: Description?) {
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        every { savedStateHandleMock.toRoute(route::class, any()) } returns route
        super.starting(description)
    }

    override fun finished(description: Description?) {
        unmockkStatic("androidx.navigation.SavedStateHandleKt")
        super.finished(description)
    }
}
