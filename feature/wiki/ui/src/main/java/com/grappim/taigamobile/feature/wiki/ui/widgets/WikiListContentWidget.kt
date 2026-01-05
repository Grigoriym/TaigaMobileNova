package com.grappim.taigamobile.feature.wiki.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.grappim.taigamobile.feature.wiki.ui.model.WikiUIItem
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WikiListContentWidget(
    items: ImmutableList<WikiUIItem>,
    isLoading: Boolean,
    error: NativeText,
    onRetry: () -> Unit,
    navigateToCreate: () -> Unit,
    canCreate: Boolean,
    onClick: (slug: String, id: Long) -> Unit,
    modifier: Modifier = Modifier,
    canDeleteItem: Boolean = false,
    onDeleteItemClick: ((Long) -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularLoaderWidget()
            }
        }

        if (items.isEmpty() && !isLoading && error.isEmpty()) {
            EmptyWikiDialogWidget(
                createNewPage = navigateToCreate,
                isButtonAvailable = canCreate
            )
        }

        if (error.isNotEmpty() && items.isEmpty()) {
            ErrorStateWidget(
                onRetry = onRetry,
                message = error
            )
        }

        WikiList(
            items = items,
            onClick = onClick,
            onDeleteItemClick = onDeleteItemClick,
            canDeleteItem = canDeleteItem
        )
    }
}

@Composable
private fun WikiList(
    onDeleteItemClick: ((Long) -> Unit)? = null,
    canDeleteItem: Boolean = false,
    items: ImmutableList<WikiUIItem> = persistentListOf(),
    onClick: (slug: String, id: Long) -> Unit = { _, _ -> }
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn {
                items(items) { item ->
                    WikiItemWidget(
                        onDeleteItemClick = {
                            onDeleteItemClick?.invoke(item.id)
                        },
                        canDeleteItem = canDeleteItem,
                        title = item.title,
                        onClick = {
                            onClick(item.slug, item.id)
                        }
                    )
                }
            }
        }

        if (items.isEmpty()) {
            EmptyWikiDialogWidget(
                isButtonAvailable = false
            )
        }
    }
}
