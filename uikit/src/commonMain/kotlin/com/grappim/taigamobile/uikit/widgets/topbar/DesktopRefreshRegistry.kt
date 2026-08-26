package com.grappim.taigamobile.uikit.widgets.topbar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.grappim.taigamobile.uikit.generated.resources.ic_refresh
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.utils.ui.isDesktopPlatform

/**
 * The desktop build has no pull-to-refresh gesture, so screens using it also get an icon button
 * ([buildDesktopRefreshTopBarAction]) and a Ctrl+R/F5 shortcut ([DesktopRefreshEffect] +
 * [DesktopRefreshRegistry]) on desktop only. No-op on Android/iOS, which keep the swipe gesture as
 * their only refresh trigger.
 *
 * The shortcut is dispatched at the window level (see `TaigaMobileDesktop.kt`'s
 * `onPreviewKeyEvent`), not via Compose focus — a focus-based `onKeyEvent` modifier only receives
 * the shortcut while its own hidden node holds focus, which any other click in the app silently
 * steals (confirmed 2026-08-22 GUI-testing the desktop build: the shortcut stopped firing after
 * clicking a nav item, with no error or visible symptom). [DesktopRefreshRegistry] instead tracks
 * whichever screen is currently composed, independent of focus.
 *
 * [unregister] only clears an entry that is still its own — an outgoing screen's `onDispose` can
 * run after the incoming screen has already registered (confirmed 2026-08-22: Nav3's transition
 * kept the outgoing screen's effect alive briefly after the new one mounted), and an unconditional
 * null-out there would silently wipe the new registration, leaving the shortcut dead until the
 * next navigation.
 */
object DesktopRefreshRegistry {
    private var current: (() -> Unit)? = null

    fun register(onRefresh: (() -> Unit)?) {
        current = onRefresh
    }

    fun unregister(onRefresh: () -> Unit) {
        if (current === onRefresh) {
            current = null
        }
    }

    fun trigger() {
        current?.invoke()
    }
}

fun buildDesktopRefreshTopBarAction(enabled: Boolean = true, onClick: () -> Unit): TopBarAction? =
    if (isDesktopPlatform()) {
        TopBarActionIconButton(
            drawable = RDrawable.ic_refresh,
            contentDescription = "Refresh",
            enabled = enabled,
            onClick = onClick
        )
    } else {
        null
    }

@Composable
fun DesktopRefreshEffect(onRefresh: () -> Unit) {
    if (isDesktopPlatform()) {
        DisposableEffect(onRefresh) {
            DesktopRefreshRegistry.register(onRefresh)
            onDispose { DesktopRefreshRegistry.unregister(onRefresh) }
        }
    }
}
