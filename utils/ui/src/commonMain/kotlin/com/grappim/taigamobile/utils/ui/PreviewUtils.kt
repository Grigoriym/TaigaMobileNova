package com.grappim.taigamobile.utils.ui

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf

@Composable
fun <T : Any> getPagingPreviewItems() = flowOf(
    PagingData.empty<T>()
).collectAsLazyPagingItems()
