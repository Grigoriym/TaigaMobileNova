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
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
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
    isOffline: Boolean,
    onClick: (slug: String, id: Long) -> Unit,
    modifier: Modifier = Modifier,
    canDeleteItem: Boolean = false,
    onDeleteItemClick: ((Long) -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularLoaderWidget()
                }
            }

            error.isNotEmpty() && items.isEmpty() -> {
                ErrorStateWidget(
                    onRetry = onRetry,
                    message = error
                )
            }

            items.isEmpty() -> {
                EmptyWikiDialogWidget(
                    createNewPage = navigateToCreate,
                    isButtonAvailable = canCreate,
                    isOffline = isOffline
                )
            }

            else -> {
                WikiList(
                    items = items,
                    onClick = onClick,
                    onDeleteItemClick = onDeleteItemClick,
                    canDeleteItem = canDeleteItem,
                    isOffline = isOffline
                )
            }
        }
    }
}

@Composable
private fun WikiList(
    items: ImmutableList<WikiUIItem>,
    onClick: (slug: String, id: Long) -> Unit,
    onDeleteItemClick: ((Long) -> Unit)?,
    canDeleteItem: Boolean,
    isOffline: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        items(items) { item ->
            WikiItemWidget(
                onDeleteItemClick = {
                    onDeleteItemClick?.invoke(item.id)
                },
                canDeleteItem = canDeleteItem,
                title = item.title,
                onClick = {
                    onClick(item.slug, item.id)
                },
                isOffline = isOffline
            )
        }
    }
}

@PreviewTaigaDarkLight
@Composable
private fun WikiListContentLoadingPreview() = TaigaMobileTheme {
    WikiListContentWidget(
        items = persistentListOf(),
        isLoading = true,
        error = NativeText.Empty,
        onRetry = {},
        navigateToCreate = {},
        canCreate = false,
        onClick = { _, _ -> },
        isOffline = false
    )
}

@PreviewTaigaDarkLight
@Composable
private fun WikiListContentErrorPreview() = TaigaMobileTheme {
    WikiListContentWidget(
        items = persistentListOf(),
        isLoading = false,
        error = NativeText.Resource(RString.error_loading_data),
        onRetry = {},
        navigateToCreate = {},
        canCreate = false,
        onClick = { _, _ -> },
        isOffline = false
    )
}

@PreviewTaigaDarkLight
@Composable
private fun WikiListContentEmptyPreview() = TaigaMobileTheme {
    WikiListContentWidget(
        items = persistentListOf(),
        isLoading = false,
        error = NativeText.Empty,
        onRetry = {},
        navigateToCreate = {},
        canCreate = true,
        onClick = { _, _ -> },
        isOffline = false
    )
}

@PreviewTaigaDarkLight
@Composable
private fun WikiListContentWithItemsPreview() = TaigaMobileTheme {
    WikiListContentWidget(
        items = persistentListOf(
            WikiUIItem(id = 1, title = "Getting Started", slug = "getting-started"),
            WikiUIItem(id = 2, title = "API Documentation", slug = "api-docs"),
            WikiUIItem(id = 3, title = "Contributing Guide", slug = "contributing")
        ),
        isLoading = false,
        error = NativeText.Empty,
        onRetry = {},
        navigateToCreate = {},
        canCreate = true,
        onClick = { _, _ -> },
        isOffline = false
    )
}
