package com.grappim.taigamobile.uikit.widgets.editor

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import com.grappim.taigamobile.uikit.widgets.AppBarWithBackButton
import com.grappim.taigamobile.uikit.widgets.loader.DotsLoaderWidget
import com.grappim.taigamobile.utils.ui.OnBackPressed

/**
 * Selector list, which expands from bottom to top.
 * Could be used to search and select something
 */
@Composable
fun <T : Any> SelectorList(
    @StringRes titleHintId: Int,
    modifier: Modifier = Modifier,
    items: List<T> = emptyList(),
    itemsLazy: LazyPagingItems<T>? = null,
    // used to preserve position with lazy items
    key: ((index: Int) -> Any)? = null,
    isVisible: Boolean = false,
    isItemsLoading: Boolean = false,
    isSearchable: Boolean = true,
    searchData: (String) -> Unit = {},
    navigateBack: () -> Unit = {},
    animationDurationMillis: Int = SelectorListConstants.DEFAULT_ANIM_DURATION_MILLIS,
    itemContent: @Composable (T) -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visibleState = remember { MutableTransitionState(false) }
            .apply { targetState = isVisible },
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(animationDurationMillis)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(animationDurationMillis)
        )
    ) {
        var query by rememberSaveable(stateSaver = TextFieldValue.Saver) {
            mutableStateOf(TextFieldValue())
        }

        OnBackPressed(navigateBack)

        val isLoading = itemsLazy
            ?.run {
                loadState.refresh is LoadState.Loading ||
                    loadState.append is LoadState.Loading
            }
            ?: isItemsLoading

        val lastIndex = itemsLazy?.itemCount?.minus(1) ?: items.lastIndex

        val listItemContent: @Composable LazyItemScope.(Int, T?) -> Unit = lambda@{ index, item ->
            if (item == null) return@lambda

            itemContent(item)

            if (index < lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppBarWithBackButton(
                title = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (isSearchable) {
                            TextFieldWithHint(
                                hintId = titleHintId,
                                value = query,
                                onValueChange = { query = it },
                                singleLine = true,
                                onSearchClick = { searchData(query.text) }
                            )
                        } else {
                            Text(stringResource(titleHintId))
                        }
                    }
                },
                navigateBack = navigateBack
            )

            LazyColumn {
                itemsLazy?.let { item ->
                    items(
                        count = item.itemCount,
                        key = key,
                        contentType = item.itemContentType()
                    ) { index ->
                        val item = item[index]
                        listItemContent(index, item)
                    }
                } ?: itemsIndexed(items, itemContent = listItemContent)

                item {
                    if (isLoading) {
                        DotsLoaderWidget()
                    }
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }
}

object SelectorListConstants {
    const val DEFAULT_ANIM_DURATION_MILLIS = 200
}
