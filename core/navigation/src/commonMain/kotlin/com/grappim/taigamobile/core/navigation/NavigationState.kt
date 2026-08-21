package com.grappim.taigamobile.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlin.reflect.KClass

/**
 * The dual back stack: [topLevelStack] records which drawer section is active, and each section
 * owns an independent sub-stack in [subStacks], so switching sections keeps each one's history.
 *
 * [subStacks] is keyed by [NavKey]'s runtime class, not by instance equality. A top-level route
 * can carry a payload (e.g. `ProjectSelectorNavDestination(isFromLogin: Boolean)`), and the
 * instance navigated to at runtime is rarely `equals()` to whichever instance seeded this map in
 * [rememberNavigationState] — class identity is what "which section is this" actually means here.
 */
class NavigationState(
    val startKey: NavKey,
    val topLevelStack: NavBackStack<NavKey>,
    val subStacks: Map<KClass<out NavKey>, NavBackStack<NavKey>>
) {
    val currentTopLevelKey: NavKey by derivedStateOf { topLevelStack.last() }

    val topLevelKeyClasses: Set<KClass<out NavKey>>
        get() = subStacks.keys

    val currentSubStack: NavBackStack<NavKey>
        get() = subStacks[currentTopLevelKey::class]
            ?: error("Sub stack for ${currentTopLevelKey::class} does not exist")

    val currentKey: NavKey by derivedStateOf { currentSubStack.last() }
}

/**
 * [configuration] is a parameter rather than something built here: it carries the polymorphic
 * `SerializersModule` listing every route in the app (see `NavKeySerializers.kt`, composeApp —
 * it can't live in this module without a dependency cycle, since it needs to reference every
 * feature module's route classes and those modules depend on this one), which is what keeps this
 * module free of route imports.
 */
@Composable
fun rememberNavigationState(
    startKey: NavKey,
    topLevelKeys: Set<NavKey>,
    configuration: SavedStateConfiguration
): NavigationState {
    val topLevelStack = rememberNavBackStack(configuration, startKey)
    val subStacks = topLevelKeys.associate { key -> key::class to rememberNavBackStack(configuration, key) }

    return remember(startKey, topLevelKeys) {
        NavigationState(
            startKey = startKey,
            topLevelStack = topLevelStack,
            subStacks = subStacks
        )
    }
}

/**
 * Flattens the dual back stack into the single list of decorated entries `NavDisplay` renders.
 * Every sub-stack is decorated on every composition — a decorator dropped and recreated loses its
 * state — and only then are the active sections' entries concatenated.
 */
@Composable
fun NavigationState.toEntries(entryProvider: (NavKey) -> NavEntry<NavKey>): SnapshotStateList<NavEntry<NavKey>> {
    val decoratedEntries = subStacks.mapValues { (_, stack) ->
        val decorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator<NavKey>()
        )
        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider
        )
    }

    return topLevelStack
        .flatMap { decoratedEntries[it::class] ?: emptyList() }
        .toMutableStateList()
}
