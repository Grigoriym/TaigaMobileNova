package io.eugenethedev.taigamobile.main.topbar

import androidx.annotation.DrawableRes
import io.eugenethedev.taigamobile.core.ui.NativeText

/**
 * A config file to control the global state of the top bar.
 * Each screen has to have its own config.
 */
data class TopBarConfig(
    val title: NativeText = NativeText.Empty,
    // Maybe instead of a bool flag, it is better to use a state?
    val showBackButton: Boolean = false,
    /**
     * If you need to have your own navigation, just override it and your action will be called
     * when you click the topBar prefix button.
     */
    val overrideBackHandlerAction: (() -> Unit)? = null,
    val actions: List<TopBarAction> = emptyList()
)

interface TopBarAction {
    val contentDescription: String
    val onClick: () -> Unit
}

data class TopBarActionResource(
    @DrawableRes val drawable: Int,
    override val contentDescription: String = "",
    override val onClick: () -> Unit
) : TopBarAction
