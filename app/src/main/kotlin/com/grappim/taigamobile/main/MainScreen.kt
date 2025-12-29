package com.grappim.taigamobile.main

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
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.nav.DrawerDestination
import com.grappim.taigamobile.feature.login.ui.navigateToLoginAsTopDestination
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TaigaTopAppBar
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import com.grappim.taigamobile.utils.ui.asString
import com.grappim.taigamobile.widget.TaigaDrawerWidget
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun MainContent(viewModel: MainViewModel) {
    val topBarController = remember { TopBarController() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isLogged by viewModel.isLogged.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalTopBarConfig provides topBarController
    ) {
        val topBarConfig = topBarController.config
        MainScreenContent(
            viewModel = viewModel,
            topBarConfig = topBarConfig,
            state = state,
            isLogged = isLogged
        )
    }
}

@Composable
private fun MainScreenContent(
    viewModel: MainViewModel,
    topBarConfig: TopBarConfig,
    state: MainScreenState,
    isLogged: Boolean
) {
    val appState = rememberMainAppState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val resources = LocalResources.current
    val drawerState by appState.drawerState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    /**
     * On any navigation event hide the keyboard, close the drawer if it is open
     */
    LaunchedEffect(Unit) {
        appState.navController.addOnDestinationChangedListener({ nc, _, _ ->
            keyboardController?.hide()
            if (drawerState.isOpen) {
                scope.launch {
                    drawerState.close()
                }
            }
        })

        viewModel.logoutEvent.onEach {
            Timber.d("Logout Event with $it")
            appState.navController.navigateToLoginAsTopDestination()
        }.launchIn(this)
    }

    if (state.isLogoutConfirmationVisible) {
        ConfirmActionDialog(
            title = stringResource(RString.logout_title),
            description = stringResource(RString.logout_text),
            onConfirm = {
                state.onLogout()
            },
            onDismiss = { state.setIsLogoutConfirmationVisible(false) },
            iconId = RDrawable.ic_logout
        )
    }

    TaigaDrawerWidget(
        drawerItems = appState.drawerItems,
        currentTopLevelDestination = appState.currentTopLevelDestination,
        drawerState = drawerState,
        onDrawerItemClick = { item: DrawerDestination ->
            scope.launch {
                drawerState.close()
            }
            if (item == DrawerDestination.Logout) {
                state.setIsLogoutConfirmationVisible(true)
            } else {
                appState.navigateToTopLevelDestination(item)
            }
        },
        gesturesEnabled = appState.areDrawerGesturesEnabled
    ) {
        Scaffold(
            modifier = Modifier.imePadding(),
            topBar = {
                TaigaTopAppBar(
                    isVisible = appState.isTopBarVisible,
                    topBarConfig = topBarConfig,
                    drawerState = drawerState,
                    defaultGoBack = { appState.navController.popBackStack() }
                )
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
                    isLoggedIn = isLogged,
                    navController = appState.navController,
                    showMessage = { message ->
                        scope.launch {
                            val strMessage = resources.getString(message)
                            snackbarHostState.showSnackbar(
                                message = strMessage,
                                actionLabel = null,
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    showSnackbar = { text ->
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = text.asString(context),
                                actionLabel = resources.getString(RString.close),
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                snackbarHostState.currentSnackbarData?.dismiss()
                            }
                        }
                    },
                    showSnackbarAction = { text, action ->
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = text.asString(context),
                                actionLabel = action,
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                snackbarHostState.currentSnackbarData?.dismiss()
                            }
                        }
                    }
                )
            }
        )
    }
}
