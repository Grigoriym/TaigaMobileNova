package com.grappim.taigamobile.feature.wiki.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class EditWikiPageRequest(val content: String, val version: Int)
