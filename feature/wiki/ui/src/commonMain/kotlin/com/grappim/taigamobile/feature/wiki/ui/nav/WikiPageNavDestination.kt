package com.grappim.taigamobile.feature.wiki.ui.nav

import androidx.navigation3.runtime.NavKey
import com.grappim.kit.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data class WikiPageNavDestination(val slug: String, val id: Long) : NavKey

fun Navigator.navigateToWikiPage(slug: String, id: Long, replaceCurrent: Boolean = false) {
    val route = WikiPageNavDestination(slug, id)
    if (replaceCurrent) {
        replaceCurrent(route)
    } else {
        navigate(route)
    }
}
