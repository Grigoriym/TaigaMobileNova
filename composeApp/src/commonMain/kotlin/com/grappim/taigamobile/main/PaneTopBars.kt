package com.grappim.taigamobile.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.LocalListDetailSceneScope
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.grappim.kit.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.kit.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.kit.uikit.widgets.topbar.TopBar
import com.grappim.kit.uikit.widgets.topbar.TopBarConfig
import com.grappim.kit.uikit.widgets.topbar.TopBarController

/**
 * Counts the list-detail panes that are currently drawing their own top bar. The shell hides its
 * own bar while the count is above zero, so each visible pane shows only its own title and actions.
 */
class PaneTopBars {
    var activeCount by mutableIntStateOf(0)
        private set

    fun register() {
        activeCount++
    }

    fun unregister() {
        activeCount--
    }
}

/**
 * Inside a multi-pane list-detail layout, gives [content] its own top bar at the top of its pane:
 * the screen's `topBarController.update(...)` calls go to a controller owned by this pane instead
 * of the shell's, and [paneTopBars] tells the shell to hide its own bar meanwhile. Outside such a
 * layout (phones, narrow windows) [content] keeps using the shell's bar.
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PaneTopBarHost(
    paneTopBars: PaneTopBars,
    defaultGoBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isInPane = LocalListDetailSceneScope.current != null
    val paneController = remember { TopBarController() }
    if (isInPane) {
        DisposableEffect(paneTopBars) {
            paneTopBars.register()
            onDispose { paneTopBars.unregister() }
        }
    }
    // The shell's Scaffold already pads its content below the status bar; consuming that inset here
    // keeps the pane's own bar from adding it a second time.
    val columnModifier = if (isInPane) modifier.consumeWindowInsets(WindowInsets.statusBars) else modifier
    Column(modifier = columnModifier.fillMaxSize()) {
        if (isInPane) {
            TopBar(
                isVisible = true,
                drawerState = rememberDrawerState(DrawerValue.Closed),
                topBarConfig = paneController.config.withoutMenuIcon(),
                defaultGoBack = defaultGoBack,
                backContentDescription = "Back",
                menuContentDescription = "Menu"
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            CompositionLocalProvider(
                LocalTopBarConfig provides if (isInPane) paneController else LocalTopBarConfig.current
            ) {
                content()
            }
        }
    }
}

/**
 * The menu icon opens the modal drawer, which only exists on compact layouts; wide layouts have the
 * navigation rail's own expand button instead.
 */
fun TopBarConfig.withoutMenuIcon(): TopBarConfig =
    if (navigationIcon == NavigationIconConfig.Menu) copy(navigationIcon = NavigationIconConfig.None) else this
