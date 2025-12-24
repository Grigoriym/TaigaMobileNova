package com.grappim.taigamobile.feature.wiki.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.feature.wiki.ui.widgets.EmptyWikiDialogWidget
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.container.ContainerBoxWidget
import com.grappim.taigamobile.uikit.widgets.container.HorizontalTabbedPagerWidget
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun WikiListScreen(
    showSnackbar: (message: NativeText) -> Unit,
    goToWikiCreatePage: () -> Unit,
    goToWikiPage: (slug: String, id: Long) -> Unit,
    viewModel: WikiListViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        state.onOpen()

        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.wiki),
                navigationIcon = NavigationIconConfig.Menu,
                actions = persistentListOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_add,
                        contentDescription = "Add",
                        onClick = goToWikiCreatePage
                    )
                )
            )
        )
    }

    LaunchedEffect(state.error) {
        if (state.error.isNotEmpty()) {
            showSnackbar(state.error)
        }
    }

    WikiListScreenContent(
        state = state,
        isLoading = state.isLoading,
        navigateToCreatePage = goToWikiCreatePage,
        goToPage = goToWikiPage
    )
}

@Composable
fun WikiListScreenContent(
    state: WikiListState,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    navigateToCreatePage: () -> Unit = {},
    goToPage: (slug: String, id: Long) -> Unit = { _, _ -> }
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

        if (state.bookmarks.isEmpty() && state.allPages.isEmpty()) {
            EmptyWikiDialogWidget(
                createNewPage = navigateToCreatePage
            )
        }
        val tabs = WikiTabs.entries.toTypedArray()

        HorizontalTabbedPagerWidget(
            modifier = Modifier.fillMaxSize(),
            tabs = tabs,
            pagerState = rememberPagerState(pageCount = { tabs.size })
        ) { page ->
            when (WikiTabs.entries[page]) {
                WikiTabs.Bookmarks -> WikiSelectorList(
                    items = state.bookmarks,
                    onClick = goToPage
                )

                WikiTabs.AllWikiPages -> WikiSelectorList(
                    items = state.allPages,
                    onClick = goToPage
                )
            }
        }
    }
}

@Composable
private fun WikiSelectorList(
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
                itemsIndexed(items) { index, item ->
                    WikiSelectorItem(
                        title = item.title,
                        onClick = {
                            onClick(item.slug, item.id)
                        }
                    )

                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
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

@Composable
private fun WikiSelectorItem(title: String, onClick: () -> Unit = {}) {
    ContainerBoxWidget(
        verticalPadding = 16.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(0.8f)) {
                Text(
                    text = title
                )
            }
        }
    }
}

@Preview
@Composable
private fun WikiPageSelectorPreview() {
    WikiListScreenContent(state = WikiListState())
}
