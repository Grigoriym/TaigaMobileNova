package com.grappim.taigamobile.utils.ui

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce

const val SEARCH_QUERY_DEBOUNCE_MILLIS = 300L

/**
 * Passes a search query on only once typing pauses for [SEARCH_QUERY_DEBOUNCE_MILLIS], so a list is
 * requested once per search rather than once per keystroke. An empty query passes immediately: it
 * is either the initial value or a cleared field, and neither should wait.
 */
@OptIn(FlowPreview::class)
fun Flow<String>.debounceSearchQuery(): Flow<String> =
    debounce { query -> if (query.isEmpty()) 0L else SEARCH_QUERY_DEBOUNCE_MILLIS }
