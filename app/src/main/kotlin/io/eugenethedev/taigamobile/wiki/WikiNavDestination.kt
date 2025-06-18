package io.eugenethedev.taigamobile.wiki

import androidx.navigation.NavController
import io.eugenethedev.taigamobile.core.nav.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object WikiNavDestination

fun NavController.navigateToWikiAsTopDestination() {
    navigate(route = WikiNavDestination) {
        popUpToTop(this@navigateToWikiAsTopDestination)
    }
}
