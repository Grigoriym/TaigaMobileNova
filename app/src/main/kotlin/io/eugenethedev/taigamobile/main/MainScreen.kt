package io.eugenethedev.taigamobile.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.eugenethedev.taigamobile.core.nav.DrawerDestination
import io.eugenethedev.taigamobile.ui.components.TaigaDrawer
import io.eugenethedev.taigamobile.ui.components.appbars.TaigaTopAppBar
import kotlinx.coroutines.launch

@Composable
fun MainContent(viewModel: MainViewModel) {
    val isLogged by viewModel.isLogged.collectAsStateWithLifecycle()
    MainScreenContent(isLogged = isLogged)
}

@Composable
private fun MainScreenContent(
    isLogged: Boolean
) {
    val appState = rememberMainAppState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val drawerState by appState.drawerState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    TaigaDrawer(
        screens = appState.topLevelDestinations,
        currentItem = appState.currentTopLevelDestination,
        drawerState = drawerState,
        onDrawerItemClicked = { item: DrawerDestination ->
            scope.launch {
                drawerState.close()
            }
            appState.navigateToTopLevelDestination(item)
        },
        gesturesEnabled = appState.areDrawerGesturesEnabled,
    ) {
        Scaffold(
            modifier = Modifier
                .imePadding(),
            topBar = {
                TaigaTopAppBar(drawerState = drawerState)
            },
            snackbarHost = {
                SnackbarHost(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                    hostState = snackbarHostState
                ) {
                    Snackbar(
                        snackbarData = it,
                        shape = MaterialTheme.shapes.small,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = contentColorFor(MaterialTheme.colorScheme.surface)
                    )
                }
            },
            content = { paddingValues ->
                MainNavHost(
                    modifier = Modifier.padding(paddingValues),
                    isLogged = isLogged,
                    navController = appState.navController,
                    showMessage = { message ->
                        scope.launch {
                            val strMessage = context.getString(message)
                            snackbarHostState.showSnackbar(
                                message = strMessage,
                                actionLabel = null,
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    onShowSnackbar = { message ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = message,
                                actionLabel = null,
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }
        )
    }
}
