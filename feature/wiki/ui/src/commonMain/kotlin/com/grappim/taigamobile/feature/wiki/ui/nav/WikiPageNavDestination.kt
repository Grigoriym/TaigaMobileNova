package com.grappim.taigamobile.feature.wiki.ui.nav

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
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
