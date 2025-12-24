package com.grappim.taigamobile.feature.users.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberStatsResponseDTO(
    // because api returns "null" key along with id keys, so...
    @SerialName(value = "closed_bugs")
    val closedBugs: Map<String, Int>,
    @SerialName(value = "closed_tasks")
    val closedTasks: Map<String, Int>,
    @SerialName(value = "created_bugs")
    val createdBugs: Map<String, Int>,
    @SerialName(value = "iocaine_tasks")
    val iocaineTasks: Map<String, Int>,
    @SerialName(value = "wiki_changes")
    val wikiChanges: Map<String, Int>
)
