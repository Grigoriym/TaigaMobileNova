package com.grappim.taigamobile.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.window.core.layout.WindowSizeClass
import com.grappim.kit.logger.logcat
import com.grappim.kit.navigation.NavigationState
import com.grappim.kit.uikit.NativeText
import com.grappim.kit.uikit.asStringBlocking
import com.grappim.kit.uikit.widgets.drawer.DrawerWidget
import com.grappim.kit.uikit.widgets.drawer.NavigationSuiteWidget
import com.grappim.kit.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.kit.uikit.widgets.topbar.TopBar
import com.grappim.kit.uikit.widgets.topbar.TopBarConfig
import com.grappim.kit.uikit.widgets.topbar.TopBarController
import com.grappim.taigamobile.DrawerDestination
import com.grappim.taigamobile.feature.login.ui.LoginNavDestination
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.app_name
import com.grappim.taigamobile.strings.generated.resources.app_update_downloaded
import com.grappim.taigamobile.strings.generated.resources.close
import com.grappim.taigamobile.strings.generated.resources.collapse_navigation
import com.grappim.taigamobile.strings.generated.resources.expand_navigation
import com.grappim.taigamobile.strings.generated.resources.restart
import com.grappim.taigamobile.uikit.state.LocalOfflineState
import com.grappim.taigamobile.uikit.widgets.banner.OfflineIndicatorBanner
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun MainContent(viewModel: MainViewModel, isUpdateDownloaded: Boolean, onRestartForUpdate: () -> Unit) {
    val topBarController = remember { TopBarController() }
    val initialNavState by viewModel.initialNavState.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val connectedServer by viewModel.connectedServer.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalTopBarConfig provides topBarController,
        LocalOfflineState provides isOffline
    ) {
        val topBarConfig = topBarController.config
        MainScreenContent(
            viewModel = viewModel,
            topBarConfig = topBarConfig,
            initialNavState = initialNavState,
            isOffline = isOffline,
            connectedServer = connectedServer,
            isUpdateDownloaded = isUpdateDownloaded,
            onRestartForUpdate = onRestartForUpdate
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
private fun MainScreenContent(
    viewModel: MainViewModel,
    topBarConfig: TopBarConfig,
    initialNavState: InitialNavState,
    isOffline: Boolean,
    connectedServer: String,
    isUpdateDownloaded: Boolean,
    onRestartForUpdate: () -> Unit
) {
    val appState = rememberMainAppState()

    val scope = rememberCoroutineScope()
    val drawerState by appState.drawerState.collectAsStateWithLifecycle()
    val drawerItems by viewModel.drawerItems.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Its own host, so the restart prompt stays up until acted on without queueing every other
    // snackbar behind it.
    val updateSnackbarHostState = remember { SnackbarHostState() }
    val updateDownloadedMessage = stringResource(RString.app_update_downloaded)
    val restartLabel = stringResource(RString.restart)
    LaunchedEffect(isUpdateDownloaded) {
        if (isUpdateDownloaded) {
            val result = updateSnackbarHostState.showSnackbar(
                message = updateDownloadedMessage,
                actionLabel = restartLabel,
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                onRestartForUpdate()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.logoutEvent.onEach {
            logcat {
                "Logout Event with $it"
            }
            appState.navigator.resetTo(LoginNavDestination)
        }.launchIn(this)
    }

    HideKeyboardOnNavigationChangeEffect(navigationState = appState.navigator.state)

    val snackbarActionLabel = stringResource(RString.close)

    val onDrawerItemClick: (DrawerDestination) -> Unit = { item ->
        scope.launch {
            drawerState.close()
        }
        appState.navigateToTopLevelDestination(item)
    }

    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    // A rail needs both width and height: a compact-height window (a phone in landscape) keeps the
    // modal drawer, since the project can have more destinations than a navigation bar holds.
    val useModalDrawer =
        !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ||
            !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

    val paneTopBars = remember { PaneTopBars() }

    val mainContent: @Composable () -> Unit = {
        Scaffold(
            modifier = Modifier.imePadding(),
            topBar = {
                TopBar(
                    isVisible = appState.isTopBarVisible && paneTopBars.activeCount == 0,
                    topBarConfig = if (useModalDrawer) topBarConfig else topBarConfig.withoutMenuIcon(),
                    drawerState = drawerState,
                    defaultGoBack = { appState.navigator.goBack() },
                    backContentDescription = "Back",
                    menuContentDescription = "Menu"
                )
            },
            snackbarHost = {
                Column(modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)) {
                    SnackbarHost(hostState = snackbarHostState) { AppSnackbar(it) }
                    SnackbarHost(hostState = updateSnackbarHostState) { AppSnackbar(it) }
                }
            },
            content = { paddingValues ->
                Column(modifier = Modifier.padding(paddingValues)) {
                    OfflineIndicatorBanner(isOffline = isOffline)
                    LocalNetworkBanner(serverUrl = connectedServer)

                    MainNavHost(
                        initialNavState = initialNavState,
                        paneTopBars = paneTopBars,
                        navigator = appState.navigator,
                        navigationState = appState.navigator.state,
                        showSnackbar = { text ->
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = text.asStringBlocking(),
                                    actionLabel = snackbarActionLabel,
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                }
                            }
                        }
                    )
                }
            }
        )
    }

    if (useModalDrawer) {
        DrawerWidget(
            drawerItems = drawerItems,
            currentTopLevelDestination = appState.currentTopLevelDestination,
            drawerState = drawerState,
            onDrawerItemClick = onDrawerItemClick,
            headerTitle = NativeText.Resource(RString.app_name),
            gesturesEnabled = appState.areDrawerGesturesEnabled &&
                initialNavState.isReady &&
                initialNavState.isProjectSelected
        ) {
            mainContent()

            /**
             * It is required to place it below MainNavHost because as per documentation
             * "If multiple BackHandler are present in the composition,
             * the one that is composed last among all enabled handlers will be invoked."
             * And with that this one will be called, otherwise on clicking back
             * we will go back in navigation but drawer will stay opened
             *
             * The second condition drawerState.isAnimationRunning is needed to fix an issue
             * when the drawer is visibly fully opened but is not opened actually
             *
             * Only needed for the modal drawer above: a rail/permanent drawer has no open/close
             * animation state for back to intercept.
             */
            NavigationBackHandler(
                state = rememberNavigationEventState(NavigationEventInfo.None),
                isBackEnabled = drawerState.isOpen || drawerState.isAnimationRunning,
                onBackCompleted = {
                    scope.launch {
                        drawerState.close()
                    }
                }
            )
        }
    } else if (initialNavState.isReady && initialNavState.isProjectSelected) {
        NavigationSuiteWidget(
            drawerItems = drawerItems,
            currentTopLevelDestination = appState.currentTopLevelDestination,
            onDrawerItemClick = onDrawerItemClick,
            layoutType = NavigationSuiteType.WideNavigationRailCollapsed,
            expandRailContentDescription = NativeText.Resource(RString.expand_navigation),
            collapseRailContentDescription = NativeText.Resource(RString.collapse_navigation)
        ) {
            mainContent()
        }
    } else {
        mainContent()
    }
}

/**
 * Nav3 has no `NavController.OnDestinationChangedListener` equivalent — [NavigationState.currentKey]
 * changing is the Nav3-shaped signal that navigation happened, so a [LaunchedEffect] keyed on it
 * replaces the old listener-based side effect.
 */
@Composable
private fun HideKeyboardOnNavigationChangeEffect(navigationState: NavigationState) {
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(navigationState.currentKey) {
        keyboardController?.hide()
    }
}

@Composable
private fun AppSnackbar(snackbarData: SnackbarData) {
    Snackbar(
        snackbarData = snackbarData,
        shape = MaterialTheme.shapes.small,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = contentColorFor(MaterialTheme.colorScheme.surface),
        actionColor = MaterialTheme.colorScheme.primary
    )
}
