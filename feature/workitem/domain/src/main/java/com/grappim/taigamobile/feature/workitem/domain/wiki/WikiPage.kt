package com.grappim.taigamobile.feature.workitem.domain.wiki

import java.time.LocalDateTime

data class WikiPage(
    val id: Long,
    val version: Long,
    val content: String,
    val editions: Long,
    val createdDate: LocalDateTime,
    val isWatcher: Boolean,
    val lastModifier: Long?,
    val modifiedDate: LocalDateTime,
    val totalWatchers: Long,
    val slug: String
)
