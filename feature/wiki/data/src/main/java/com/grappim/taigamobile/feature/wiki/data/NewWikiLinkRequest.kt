package com.grappim.taigamobile.feature.wiki.data

import kotlinx.serialization.Serializable

@Serializable
data class NewWikiLinkRequest(val project: Long, val title: String)
