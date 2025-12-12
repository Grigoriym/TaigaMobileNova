package com.grappim.taigamobile.feature.wiki.ui.list

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class WikiListState(
    val allPages: ImmutableList<String> = persistentListOf(),
    val bookmarks: ImmutableList<Pair<String, String>> = persistentListOf(),

    val onOpen: () -> Unit = {},
    val isLoading: Boolean = false
)
