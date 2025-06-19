package com.grappim.taigamobile.commontask

import androidx.navigation.NavController
import com.grappim.taigamobile.core.domain.CommonTaskType
import kotlinx.serialization.Serializable

@Serializable
data class CommonTaskNavDestination(
    val taskId: Long,
    val taskType: CommonTaskType,
    val ref: Int
)

fun NavController.navigateToCommonTask(destination: CommonTaskNavDestination) {
    navigate(route = destination)
}

fun NavController.navigateToCommonTask(taskId: Long, taskType: CommonTaskType, ref: Int) {
    navigateToCommonTask(CommonTaskNavDestination(taskId, taskType, ref))
}
