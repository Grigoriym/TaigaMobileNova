package com.grappim.taigamobile.uikit.widgets.topbar

import androidx.annotation.DrawableRes
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * A config file to control the global state of the top bar.
 * Each screen has to have its own config.
 */
data class TopBarConfig(
    val title: NativeText = NativeText.Empty,
    val subtitle: NativeText = NativeText.Empty,
    val navigationIcon: NavigationIconConfig = NavigationIconConfig.None,
    val actions: ImmutableList<TopBarAction> = persistentListOf()
)

sealed interface TopBarAction {
    val onClick: () -> Unit
}

data class TopBarActionIconButton(
    @DrawableRes val drawable: Int,
    val contentDescription: String = "",
    override val onClick: () -> Unit
) : TopBarAction

data class TopBarActionTextButton(val text: NativeText, override val onClick: () -> Unit) : TopBarAction

sealed interface NavigationIconConfig {
    object None : NavigationIconConfig

    data class Back(val onBackClick: (() -> Unit)? = null) : NavigationIconConfig

    object Menu : NavigationIconConfig

    data class Custom(@DrawableRes val icon: Int, val contentDescription: String, val onClick: () -> Unit) :
        NavigationIconConfig
}
