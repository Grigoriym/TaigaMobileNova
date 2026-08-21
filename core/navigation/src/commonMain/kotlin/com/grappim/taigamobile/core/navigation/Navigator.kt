package com.grappim.taigamobile.core.navigation

import androidx.navigation3.runtime.NavKey

/**
 * The only writer of [NavigationState]. Screens call [navigate]/[goBack] and never touch a stack.
 */
class Navigator(val state: NavigationState) {

    /**
     * Re-tapping the active section resets it to its root; any other top-level key switches
     * section; anything else is pushed onto the active section's sub-stack.
     *
     * All three branches compare by [key]'s runtime class, not `equals()` — a top-level route can
     * carry a payload (`ProjectSelectorNavDestination(isFromLogin: Boolean)`), and the instance
     * navigated to at runtime is rarely equal to whichever instance was used to seed
     * [NavigationState.subStacks]. Comparing by class is what "is this the same section" means;
     * the first two branches also write [key] itself into the relevant sub-stack root so its
     * payload actually reaches the rendered screen (`toEntries` renders from `subStacks`, not
     * `topLevelStack`).
     */
    fun navigate(key: NavKey) {
        when {
            key::class == state.currentTopLevelKey::class -> resetSubStackTo(key)
            key::class in state.topLevelKeyClasses -> goToTopLevel(key)
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

    /**
     * Replaces the current sub-stack entry with [key] instead of pushing on top of it — for
     * "this screen is done, hand off to the next one" transitions (e.g. a wiki create-page screen
     * handing off to the page it just created) where the replaced screen should not be reachable
     * via back.
     */
    fun replaceCurrent(key: NavKey) {
        state.currentSubStack.removeLastOrNull()
        goToKey(key)
    }

    /**
     * Wipes every section's history and lands on [key] alone — for "forget everything, start
     * fresh" transitions (login success, logout) that [navigate] can't express, since it only ever
     * touches the currently active section's own sub-stack or switches which section is active.
     */
    fun resetTo(key: NavKey) {
        state.subStacks.forEach { (keyClass, stack) ->
            if (stack.size > 1) stack.subList(1, stack.size).clear()
            if (keyClass == key::class) {
                stack[0] = key
            }
        }
        state.topLevelStack.apply {
            clear()
            add(key)
        }
    }

    private fun goToKey(key: NavKey) {
        state.currentSubStack.apply {
            // single-top: a key already in the sub-stack moves to the top rather than duplicating.
            // Value equality here (not class) is deliberate: unlike top-level keys, two leaf routes
            // of the same class but different payload (e.g. two different task ids) are genuinely
            // different destinations, not the same one revisited.
            remove(key)
            add(key)
        }
    }

    private fun goToTopLevel(key: NavKey) {
        state.topLevelStack.apply {
            if (key::class == state.startKey::class) {
                clear()
            } else {
                removeAll { it::class == key::class }
            }
            add(key)
        }
        // carry the fresh payload (if any) into the target section's own sub-stack root too —
        // toEntries() renders from subStacks, not topLevelStack, so this is what the screen
        // actually sees.
        state.subStacks[key::class]?.let { stack ->
            if (stack.isNotEmpty()) stack[0] = key
        }
    }

    private fun resetSubStackTo(key: NavKey) {
        val topLevelStack = state.topLevelStack
        topLevelStack[topLevelStack.lastIndex] = key
        state.currentSubStack.run {
            if (size > 1) subList(1, size).clear()
            this[0] = key
        }
    }
}
