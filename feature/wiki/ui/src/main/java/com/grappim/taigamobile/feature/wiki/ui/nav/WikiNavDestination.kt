package com.grappim.taigamobile.feature.wiki.ui.nav

import androidx.navigation.NavController
import com.grappim.taigamobile.core.navigation.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object WikiNavDestination

fun NavController.navigateToWikiAsTopDestination() {
    navigate(route = WikiNavDestination) {
        popUpToTop(this@navigateToWikiAsTopDestination)
    }
}
