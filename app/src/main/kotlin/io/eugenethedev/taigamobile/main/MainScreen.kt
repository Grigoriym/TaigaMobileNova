package io.eugenethedev.taigamobile.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import io.eugenethedev.taigamobile.core.nav.Screens
import io.eugenethedev.taigamobile.dashboard.navigateToDashboard
import io.eugenethedev.taigamobile.ui.screens.epics.navigateToEpics
import io.eugenethedev.taigamobile.ui.screens.issues.navigateToIssues
import io.eugenethedev.taigamobile.ui.screens.more.navigateToMore
import io.eugenethedev.taigamobile.ui.screens.scrum.navigateToScrum
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer

@Composable
fun MainContent(viewModel: MainViewModel) {
    MainScreenContent(viewModel = viewModel)
}

@Composable
private fun MainScreenContent(viewModel: MainViewModel) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val navController = rememberNavController()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = Modifier
            .imePadding(),
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
        bottomBar = {
            val items = Screens.entries.toTypedArray()
            val routes = items.map { it.route }
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute =
                navBackStackEntry?.destination?.hierarchy?.first()

            val isNavBarEnabled = routes.any { route ->
                currentRoute?.hasRoute(route = route) == true
            }
            if (isNavBarEnabled) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            modifier = Modifier,
                            icon = {
                                Icon(
                                    painter = painterResource(screen.iconId),
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(screen.resourceId)) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                if (screen.route != currentRoute) {
                                    val navOptions = navOptions {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    when (screen) {
                                        Screens.Dashboard -> navController.navigateToDashboard(
                                            navOptions
                                        )

                                        Screens.Scrum -> navController.navigateToScrum(
                                            navOptions
                                        )

                                        Screens.Epics -> navController.navigateToEpics(
                                            navOptions
                                        )

                                        Screens.Issues -> navController.navigateToIssues(
                                            navOptions
                                        )

                                        Screens.More -> navController.navigateToMore(
                                            navOptions
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }

        },
        content = { paddingValues ->
            MainNavHost(
                modifier = Modifier.padding(paddingValues),
                viewModel = viewModel,
                navController = navController,
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
