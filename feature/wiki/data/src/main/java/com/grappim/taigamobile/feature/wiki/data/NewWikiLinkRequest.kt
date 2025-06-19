package com.grappim.taigamobile.feature.wiki.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewWikiLinkRequest(val href: String, val project: Long, val title: String)
