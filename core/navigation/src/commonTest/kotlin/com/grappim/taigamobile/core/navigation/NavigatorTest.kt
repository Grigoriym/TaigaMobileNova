package com.grappim.taigamobile.core.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private data object HomeRoute : NavKey

private data object SettingsRoute : NavKey

private data object AboutRoute : NavKey

private data class DetailRoute(val id: Int) : NavKey

private data class PayloadTopLevelRoute(val flag: Boolean = false) : NavKey

class NavigatorTest {

    private fun navigator(): Navigator = Navigator(
        NavigationState(
            startKey = HomeRoute,
            topLevelStack = NavBackStack(HomeRoute),
            subStacks = mapOf(
                HomeRoute::class to NavBackStack(HomeRoute),
                SettingsRoute::class to NavBackStack(SettingsRoute),
                AboutRoute::class to NavBackStack(AboutRoute),
                PayloadTopLevelRoute::class to NavBackStack(PayloadTopLevelRoute(flag = false))
            )
        )
    )

    @Test
    fun `navigate pushes a non top level key onto the current sub stack`() {
        val navigator = navigator()

        navigator.navigate(DetailRoute(1))

        assertEquals(listOf(HomeRoute, DetailRoute(1)), navigator.state.currentSubStack.toList())
        assertEquals(listOf(HomeRoute), navigator.state.topLevelStack.toList())
        assertEquals(DetailRoute(1), navigator.state.currentKey)
    }

    @Test
    fun `navigate is single top - an existing key moves to the top instead of duplicating`() {
        val navigator = navigator()

        navigator.navigate(DetailRoute(1))
        navigator.navigate(DetailRoute(2))
        navigator.navigate(DetailRoute(1))

        assertEquals(
            listOf(HomeRoute, DetailRoute(2), DetailRoute(1)),
            navigator.state.currentSubStack.toList()
        )
    }

    @Test
    fun `navigate to the current top level key resets its sub stack to the root`() {
        val navigator = navigator()
        navigator.navigate(DetailRoute(1))
        navigator.navigate(DetailRoute(2))

        navigator.navigate(HomeRoute)

        assertEquals(listOf(HomeRoute), navigator.state.currentSubStack.toList())
        assertEquals(listOf(HomeRoute), navigator.state.topLevelStack.toList())
    }

    @Test
    fun `navigate to another top level key switches section and keeps each sub stack`() {
        val navigator = navigator()
        navigator.navigate(DetailRoute(1))

        navigator.navigate(SettingsRoute)

        assertEquals(listOf(HomeRoute, SettingsRoute), navigator.state.topLevelStack.toList())
        assertEquals(listOf(SettingsRoute), navigator.state.currentSubStack.toList())

        navigator.navigate(HomeRoute)
        // back on Home, its sub stack is untouched — the re-tap reset only applies to the section
        // that is already current, and Home was not
        assertEquals(
            listOf(HomeRoute, DetailRoute(1)),
            navigator.state.currentSubStack.toList()
        )
    }

    @Test
    fun `navigate to the start key clears the top level stack`() {
        val navigator = navigator()
        navigator.navigate(SettingsRoute)
        navigator.navigate(AboutRoute)

        navigator.navigate(HomeRoute)

        assertEquals(listOf(HomeRoute), navigator.state.topLevelStack.toList())
    }

    @Test
    fun `navigate to a top level key already in the stack moves it to the top`() {
        val navigator = navigator()
        navigator.navigate(SettingsRoute)
        navigator.navigate(AboutRoute)

        navigator.navigate(SettingsRoute)

        assertEquals(
            listOf(HomeRoute, AboutRoute, SettingsRoute),
            navigator.state.topLevelStack.toList()
        )
    }

    @Test
    fun `goBack pops the current sub stack first`() {
        val navigator = navigator()
        navigator.navigate(SettingsRoute)
        navigator.navigate(DetailRoute(1))

        val handled = navigator.goBack()

        assertTrue(handled)
        assertEquals(listOf(SettingsRoute), navigator.state.currentSubStack.toList())
        assertEquals(listOf(HomeRoute, SettingsRoute), navigator.state.topLevelStack.toList())
    }

    @Test
    fun `goBack at a sub stack root pops the top level stack`() {
        val navigator = navigator()
        navigator.navigate(SettingsRoute)

        val handled = navigator.goBack()

        assertTrue(handled)
        assertEquals(listOf(HomeRoute), navigator.state.topLevelStack.toList())
        assertEquals(HomeRoute, navigator.state.currentKey)
    }

    @Test
    fun `goBack at the start destination is not handled`() {
        val navigator = navigator()

        val handled = navigator.goBack()

        assertFalse(handled)
        assertEquals(listOf(HomeRoute), navigator.state.topLevelStack.toList())
    }

    @Test
    fun `canGoBack is false only at the start destination`() {
        val navigator = navigator()
        assertFalse(navigator.canGoBack())

        navigator.navigate(DetailRoute(1))
        assertTrue(navigator.canGoBack())

        navigator.goBack()
        assertFalse(navigator.canGoBack())

        navigator.navigate(SettingsRoute)
        assertTrue(navigator.canGoBack())
    }

    @Test
    fun `navigate to a top level key with a different payload is treated as the same section`() {
        val navigator = navigator()

        // seeded with flag = false; navigating with flag = true must still be recognised as the
        // same top-level section (class-based identity), not fall through to a sub-stack push
        navigator.navigate(PayloadTopLevelRoute(flag = true))

        assertEquals(
            listOf(HomeRoute, PayloadTopLevelRoute(flag = true)),
            navigator.state.topLevelStack.toList()
        )
        assertEquals(PayloadTopLevelRoute(flag = true), navigator.state.currentKey)

        // re-navigating to the same section with yet another payload must not leave a stale
        // duplicate entry behind
        navigator.navigate(SettingsRoute)
        navigator.navigate(PayloadTopLevelRoute(flag = false))

        assertEquals(
            listOf(HomeRoute, SettingsRoute, PayloadTopLevelRoute(flag = false)),
            navigator.state.topLevelStack.toList()
        )
    }

    @Test
    fun `replaceCurrent swaps the top of the current sub stack instead of pushing`() {
        val navigator = navigator()
        navigator.navigate(DetailRoute(1))

        navigator.replaceCurrent(DetailRoute(2))

        assertEquals(listOf(HomeRoute, DetailRoute(2)), navigator.state.currentSubStack.toList())
    }

    @Test
    fun `resetTo wipes every section and lands on the given key alone`() {
        val navigator = navigator()
        navigator.navigate(DetailRoute(1))
        navigator.navigate(SettingsRoute)
        navigator.navigate(DetailRoute(2))
        navigator.navigate(AboutRoute)

        navigator.resetTo(HomeRoute)

        assertEquals(listOf(HomeRoute), navigator.state.topLevelStack.toList())
        assertEquals(HomeRoute, navigator.state.currentKey)
        assertFalse(navigator.canGoBack())

        // switching back into a previously-visited section confirms its history was wiped too,
        // not just hidden behind the reset top-level stack
        navigator.navigate(SettingsRoute)
        assertEquals(listOf(SettingsRoute), navigator.state.currentSubStack.toList())
    }
}
