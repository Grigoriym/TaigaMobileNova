package com.grappim.taigamobile.feature.wiki.ui.nav

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class WikiPageNavDestination(val slug: String, val id: Long)

fun NavController.navigateToWikiPage(slug: String, id: Long, popUpToRoute: Any? = null) {
    navigate(route = WikiPageNavDestination(slug, id)) {
        popUpToRoute?.let { route ->
            popUpTo(route = route) {
                inclusive = true
            }
        }
    }
}
