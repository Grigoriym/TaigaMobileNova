package com.grappim.taigamobile.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import kotlin.reflect.KClass

fun NavOptionsBuilder.popUpToTop(navController: NavController) {
    popUpTo(navController.graph.id) {
        inclusive = true
        saveState = false
    }
    launchSingleTop = true
    restoreState = false
}

inline fun <reified PopUpTo : Any> NavController.navigateAndPopCurrent(
    route: Any,
    noinline builder: NavOptionsBuilder.() -> Unit = {}
) {
    val options = navOptions {
        popUpTo<PopUpTo> {
            inclusive = true
        }
        builder()
    }
    navigate(route = route, navOptions = options)
}

fun NavController.navigateAndPopCurrent(route: Any, popUpToRoute: Any, builder: NavOptionsBuilder.() -> Unit = {}) {
    val options = navOptions {
        when (popUpToRoute) {
            is KClass<*> -> popUpTo(popUpToRoute) { inclusive = true }
            else -> popUpTo(popUpToRoute) { inclusive = true }
        }
        builder()
    }
    navigate(route = route, navOptions = options)
}
