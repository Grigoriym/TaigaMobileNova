package com.grappim.taigamobile.kanban

import androidx.navigation.NavController
import com.grappim.taigamobile.core.nav.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object KanbanNavDestination

fun NavController.navigateToKanbanAsTopDestination() {
    navigate(route = KanbanNavDestination) {
        popUpToTop(this@navigateToKanbanAsTopDestination)
    }
}
