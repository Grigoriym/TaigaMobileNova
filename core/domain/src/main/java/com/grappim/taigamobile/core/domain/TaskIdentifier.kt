package com.grappim.taigamobile.core.domain

import kotlinx.serialization.Serializable

/**
 * I don't want to include wiki in CommonTaskType, since it is a different thing
 * This wrapper was created to separate Wiki from WorkItem and any future items
 * that don't fall under CommonTaskType
 */
@Serializable
sealed interface TaskIdentifier {
    @Serializable
    data class WorkItem(val commonTaskType: CommonTaskType) : TaskIdentifier

    @Serializable
    data object Wiki : TaskIdentifier
}
