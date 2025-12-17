package com.grappim.taigamobile.feature.users.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MemberStatsResponseDTO(
    // because api returns "null" key along with id keys, so...
    @Json(name = "closed_bugs")
    val closedBugs: Map<String, Int>,
    @Json(name = "closed_tasks")
    val closedTasks: Map<String, Int>,
    @Json(name = "created_bugs")
    val createdBugs: Map<String, Int>,
    @Json(name = "iocaine_tasks")
    val iocaineTasks: Map<String, Int>,
    @Json(name = "wiki_changes")
    val wikiChanges: Map<String, Int>
)
