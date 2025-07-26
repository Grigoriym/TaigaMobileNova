package com.grappim.taigamobile.feature.workitem.ui.screens.edittags

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

const val WORK_ITEM_TAGS_CHANGED_KEY = "work_item_edit_tags_changed_value"

@Serializable
data object WorkItemEditTagsNavDestination

fun NavController.navigateToWorkItemEditTags() {
    navigate(route = WorkItemEditTagsNavDestination)
}
