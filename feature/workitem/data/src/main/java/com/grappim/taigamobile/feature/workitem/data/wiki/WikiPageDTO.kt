package com.grappim.taigamobile.feature.workitem.data.wiki

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class WikiPageDTO(
    val id: Long,
    val version: Long,
    val content: String,
    val editions: Long,
    @Json(name = "created_date") val cratedDate: LocalDateTime,
    @Json(name = "is_watcher") val isWatcher: Boolean,
    @Json(name = "last_modifier") val lastModifier: Long,
    @Json(name = "modified_date") val modifiedDate: LocalDateTime,
    @Json(name = "total_watchers") val totalWatchers: Long,
    @Json(name = "slug") val slug: String
)

@JsonClass(generateAdapter = true)
data class WikiLinkDTO(@Json(name = "href") val ref: String, val id: Long, val order: Long, val title: String)
