package io.eugenethedev.taigamobile.ui.screens.main

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.eugenethedev.taigamobile.core.nav.Screens
import kotlinx.coroutines.launch

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
                navBackStackEntry?.destination?.hierarchy?.first()?.route

            // hide bottom bar for other screens
            if (currentRoute !in routes) return@Scaffold

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
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { }
                                }
                            }
                        }
                    )
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
                }
            )
        }
    )
}
