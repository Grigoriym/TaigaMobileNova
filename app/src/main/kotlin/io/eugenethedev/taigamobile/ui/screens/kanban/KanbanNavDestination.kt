package io.eugenethedev.taigamobile.ui.screens.kanban

import androidx.navigation.NavController
import io.eugenethedev.taigamobile.core.nav.popUpToTop
import kotlinx.serialization.Serializable

@Serializable
data object KanbanNavDestination

fun NavController.navigateToKanbanAsTopDestination() {
    navigate(route = KanbanNavDestination) {
        popUpToTop(this@navigateToKanbanAsTopDestination)
    }
}
