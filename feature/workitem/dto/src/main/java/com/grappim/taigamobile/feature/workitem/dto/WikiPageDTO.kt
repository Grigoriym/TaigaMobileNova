package com.grappim.taigamobile.feature.workitem.dto

import com.grappim.taigamobile.core.serialization.LocalDateTimeSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class WikiPageDTO(
    val id: Long,
    val version: Long,
    val content: String,
    val editions: Long,
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName(value = "created_date")
    val cratedDate: LocalDateTime,
    @SerialName(value = "is_watcher") val isWatcher: Boolean,
    @SerialName(value = "last_modifier") val lastModifier: Long,
    @Serializable(with = LocalDateTimeSerializer::class)
    @SerialName(value = "modified_date")
    val modifiedDate: LocalDateTime,
    @SerialName(value = "total_watchers") val totalWatchers: Long,
    @SerialName(value = "slug") val slug: String
)

@Serializable
data class WikiLinkDTO(@SerialName(value = "href") val ref: String, val id: Long, val order: Long, val title: String)
