package io.eugenethedev.taigamobile.ui.screens.more

import androidx.navigation.NavController
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable

@Serializable
data object MoreNavDestination

fun NavController.navigateToMore(navOptions: NavOptions) {
    navigate(route = MoreNavDestination, navOptions = navOptions)
}
