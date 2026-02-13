package com.grappim.taigamobile.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.navigation.NavController
import com.grappim.taigamobile.core.nav.DrawerDestination
import com.grappim.taigamobile.feature.login.ui.navigateToLoginAsTopDestination
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.state.LocalOfflineState
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.banner.OfflineIndicatorBanner
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TaigaTopAppBar
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarController
import com.grappim.taigamobile.utils.ui.asString
import com.grappim.taigamobile.widget.TaigaDrawerWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun MainContent(viewModel: MainViewModel) {
    val topBarController = remember { TopBarController() }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val initialNavState by viewModel.initialNavState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalTopBarConfig provides topBarController,
        LocalOfflineState provides isOffline
    ) {
        val topBarConfig = topBarController.config
        MainScreenContent(
            viewModel = viewModel,
            topBarConfig = topBarConfig,
            state = state,
            initialNavState = initialNavState,
            isOffline = isOffline
        )
    }
}

@Composable
private fun MainScreenContent(
    viewModel: MainViewModel,
    topBarConfig: TopBarConfig,
    state: MainScreenState,
    initialNavState: InitialNavState,
    isOffline: Boolean
) {
    val appState = rememberMainAppState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val resources = LocalResources.current
    val drawerState by appState.drawerState.collectAsStateWithLifecycle()
    val drawerItems by viewModel.drawerItems.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.logoutEvent.onEach {
            Timber.d("Logout Event with $it")
            appState.navController.navigateToLoginAsTopDestination()
        }.launchIn(this)
    }

    RegisterOnDestinationChangedListenerSideEffect(
        navController = appState.navController,
        coroutineScope = scope
    )

    ConfirmActionDialog(
        title = stringResource(RString.logout_title),
        description = stringResource(RString.logout_text),
        onConfirm = {
            state.onLogout()
        },
        onDismiss = { state.setIsLogoutConfirmationVisible(false) },
        iconId = RDrawable.ic_logout,
        isVisible = state.isLogoutConfirmationVisible
    )

    TaigaDrawerWidget(
        drawerItems = drawerItems,
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
        gesturesEnabled = appState.areDrawerGesturesEnabled &&
            initialNavState.isReady &&
            initialNavState.isProjectSelected
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
                Column(modifier = Modifier.padding(paddingValues)) {
                    OfflineIndicatorBanner(isOffline = isOffline)

                    MainNavHost(
                        initialNavState = initialNavState,
                        navController = appState.navController,
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
                        }
                    )
                }

                /**
                 * It is required to place it below MainNavHost because as per documentation
                 * "If multiple BackHandler are present in the composition,
                 * the one that is composed last among all enabled handlers will be invoked."
                 * And with that this one will be called, otherwise on clicking back
                 * we will go back in navigation but drawer will stay opened
                 *
                 * The second condition drawerState.isAnimationRunning is needed to fix an issue
                 * when the drawer is visibly fully opened but is not opened actually
                 */
                BackHandler(drawerState.isOpen || drawerState.isAnimationRunning) {
                    scope.launch {
                        drawerState.close()
                    }
                }
            }
        )
    }
}

@Composable
private fun RegisterOnDestinationChangedListenerSideEffect(
    navController: NavController,
    coroutineScope: CoroutineScope
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    DisposableEffect(navController, coroutineScope) {
        val listener = NavController.OnDestinationChangedListener { _, _, _ ->

            keyboardController?.hide()
        }

        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
}
