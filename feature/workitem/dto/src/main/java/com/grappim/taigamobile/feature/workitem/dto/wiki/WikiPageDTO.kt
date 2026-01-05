package com.grappim.taigamobile.feature.workitem.dto.wiki

import com.grappim.taigamobile.core.serialization.LocalDateTimeSerializer
import com.grappim.taigamobile.feature.projects.dto.ProjectExtraInfoDTO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class WikiPageDTO(
    val id: Long,
    @SerialName("project")
    val projectId: Long,
    val slug: String,
    val content: String,
    @SerialName("owner")
    val ownerId: Long?,
    @SerialName("last_modifier")
    val lastModifierId: Long?,
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName("created_date")
    val createdDate: LocalDateTime, // ISO 8601 datetime string
    @SerialName("modified_date")
    @Serializable(with = LocalDateTimeSerializer::class)
    val modifiedDate: LocalDateTime, // ISO 8601 datetime string
    val html: String, // Rendered markdown content
    val editions: Long, // Number of edits (history count + 1)
    val version: Long, // For optimistic concurrency control
    @SerialName("is_watcher")
    val isWatcher: Boolean = false,
    val watchers: List<Long> = emptyList(),
    @SerialName("total_watchers")
    val totalWatchers: Long = 0L,
    @SerialName("project_extra_info")
    val projectExtraInfo: ProjectExtraInfoDTO? = null
)
