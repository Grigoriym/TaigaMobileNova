package com.grappim.taigamobile.uikit.widgets.topbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.grappim.taigamobile.utils.ui.asString
import kotlinx.coroutines.launch

/**
 * Global top bar for the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaigaTopAppBar(
    isVisible: Boolean,
    drawerState: DrawerState,
    topBarConfig: TopBarConfig,
    isMenuButton: Boolean,
    goBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    if (isVisible) {
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = {
                Text(
                    topBarConfig.title.asString(context),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                NavigationIcon(
                    drawerState = drawerState,
                    showMenuButton = isMenuButton,
                    goBack = goBack
                )
            },
            actions = {
                topBarConfig.actions.forEach { action ->
                    IconButton(onClick = action.onClick) {
                        when (action) {
                            is TopBarActionResource -> {
                                Icon(
                                    painter = painterResource(action.drawable),
                                    contentDescription = action.contentDescription
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun NavigationIcon(drawerState: DrawerState, showMenuButton: Boolean, goBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    if (showMenuButton) {
        IconButton(onClick = {
            keyboardController?.hide()
            scope.launch {
                if (drawerState.isClosed) {
                    drawerState.open()
                } else {
                    drawerState.close()
                }
            }
        }) {
            Icon(Icons.Default.Menu, contentDescription = "Menu")
        }
    } else {
        IconButton(onClick = goBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
    }
}
