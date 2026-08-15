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

class NavigatorTest {

    private fun navigator(): Navigator = Navigator(
        NavigationState(
            startKey = HomeRoute,
            topLevelStack = NavBackStack(HomeRoute),
            subStacks = mapOf(
                HomeRoute to NavBackStack(HomeRoute),
                SettingsRoute to NavBackStack(SettingsRoute),
                AboutRoute to NavBackStack(AboutRoute)
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
}
