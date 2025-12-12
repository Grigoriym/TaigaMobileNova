package com.grappim.taigamobile.feature.wiki.ui.nav

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class WikiPageNavDestination(val slug: String)

fun NavController.navigateToWikiPage(slug: String, popUpToRoute: Any? = null) {
    navigate(route = WikiPageNavDestination(slug)) {
        popUpToRoute?.let { route ->
            popUpTo(route = route) {
                inclusive = true
            }
        }
    }
}
