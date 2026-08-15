package com.grappim.taigamobile.core.navigation

import androidx.navigation3.runtime.NavKey

/**
 * The only writer of [NavigationState]. Screens call [navigate]/[goBack] and never touch a stack.
 */
class Navigator(val state: NavigationState) {

    /**
     * Re-tapping the active section resets it to its root; any other top-level key switches
     * section; anything else is pushed onto the active section's sub-stack.
     */
    fun navigate(key: NavKey) {
        when (key) {
            state.currentTopLevelKey -> clearSubStack()
            in state.topLevelKeys -> goToTopLevel(key)
            else -> goToKey(key)
        }
    }

    /**
     * Steps back through the active sub-stack first, then through the section stack.
     *
     * @return false at the start destination, so the caller can let the system handle back.
     */
    fun goBack(): Boolean = when (state.currentKey) {
        state.currentTopLevelKey -> if (state.topLevelStack.size > 1) {
            state.topLevelStack.removeLastOrNull()
            true
        } else {
            false
        }

        else -> {
            state.currentSubStack.removeLastOrNull()
            true
        }
    }

    /** For predictive back: whether [goBack] would handle the gesture. */
    fun canGoBack(): Boolean = state.currentKey != state.currentTopLevelKey || state.topLevelStack.size > 1

    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {
            // single-top: a key already in the sub-stack moves to the top rather than duplicating
            remove(key)
            add(key)
        }
    }

    private fun goToTopLevel(key: NavKey) {
        state.topLevelStack.apply {
            if (key == state.startKey) {
                clear()
            } else {
                remove(key)
            }
            add(key)
        }
    }

    private fun clearSubStack() {
        state.currentSubStack.run {
            if (size > 1) subList(1, size).clear()
        }
    }
}
