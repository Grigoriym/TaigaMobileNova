package com.grappim.taigamobile.feature.wiki.ui.nav

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data class WikiPageNavDestination(val slug: String)

fun NavController.navigateToWikiPage(slug: String) {
    navigate(route = WikiPageNavDestination(slug))
}
