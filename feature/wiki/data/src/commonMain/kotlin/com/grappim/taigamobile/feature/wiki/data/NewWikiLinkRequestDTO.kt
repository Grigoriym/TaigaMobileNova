package com.grappim.taigamobile.feature.wiki.data

import kotlinx.serialization.Serializable

@Serializable
data class NewWikiLinkRequestDTO(val project: Long, val title: String)
