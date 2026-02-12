package com.grappim.taigamobile.uikit.state

import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal providing the current offline state.
 *
 * When offline:
 * - Write actions (create, edit, delete, comment) should be **disabled** (not hidden)
 * - This is different from permissions where actions are hidden
 *
 * Usage:
 * ```
 * val isOffline = LocalOfflineState.current
 * Button(enabled = !isOffline) { ... }
 * ```
 */
val LocalOfflineState = compositionLocalOf { false }
