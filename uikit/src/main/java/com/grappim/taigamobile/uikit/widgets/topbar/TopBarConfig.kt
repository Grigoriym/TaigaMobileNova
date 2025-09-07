package com.grappim.taigamobile.uikit.widgets.topbar

import androidx.annotation.DrawableRes
import com.grappim.taigamobile.utils.ui.NativeText

/**
 * A config file to control the global state of the top bar.
 * Each screen has to have its own config.
 */
data class TopBarConfig(
    val title: NativeText = NativeText.Empty,
    val subtitle: NativeText = NativeText.Empty,
    // Maybe instead of a bool flag, it is better to use a state?
    val showBackButton: Boolean = false,
    /**
     * If you need to have your own navigation, just override it and your action will be called
     * when you click the topBar prefix button.
     * This does not affect the BackHandler (when you are clicking the system back button), for that
     * you have to manually setup [androidx.activity.compose.BackHandler]
     */
    val overrideBackHandlerAction: (() -> Unit)? = null,
    val actions: List<TopBarAction> = emptyList()
)

sealed interface TopBarAction {
    @Deprecated("should move to a icon actions")
    val contentDescription: String
    val onClick: () -> Unit
}

data class TopBarActionIconButton(
    @DrawableRes val drawable: Int,
    @Deprecated("should move to a icon actions")
    override val contentDescription: String = "",
    override val onClick: () -> Unit
) : TopBarAction

data class TopBarActionTextButton(
    val text: NativeText,
    @Deprecated("should move to a icon actions")
    override val contentDescription: String = "",
    override val onClick: () -> Unit
) : TopBarAction
