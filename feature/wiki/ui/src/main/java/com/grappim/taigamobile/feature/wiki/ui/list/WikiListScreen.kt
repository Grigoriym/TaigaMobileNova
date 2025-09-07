package com.grappim.taigamobile.feature.wiki.ui.list

import androidx.annotation.StringRes
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.grappim.taigamobile.feature.wiki.ui.widgets.EmptyWikiDialogWidget
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.utils.Tab
import com.grappim.taigamobile.uikit.widgets.container.ContainerBox
import com.grappim.taigamobile.uikit.widgets.container.HorizontalTabbedPager
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoader
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.LoadingResult
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SubscribeOnError

@Composable
fun WikiListScreen(
    showMessage: (message: Int) -> Unit,
    goToWikiCreatePage: () -> Unit,
    goToWikiPage: (slug: String) -> Unit,
    viewModel: WikiListViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current

    val wikiLinks by viewModel.wikiLinks.collectAsState()
    wikiLinks.SubscribeOnError(showMessage)

    val wikiPages by viewModel.wikiPages.collectAsState()
    wikiPages.SubscribeOnError(showMessage)

    LaunchedEffect(Unit) {
        viewModel.onOpen()

        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.wiki),
                actions = listOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_add,
                        contentDescription = "Add",
                        onClick = goToWikiCreatePage
                    )
                )
            )
        )
    }

    val wikiPagesSlug = wikiPages.data.orEmpty().map { it.slug }

    WikiListScreenContent(
        bookmarks = wikiLinks.data.orEmpty().filter { it.ref in wikiPagesSlug }
            .map { it.title to it.ref },
        allPages = wikiPagesSlug,
        isLoading = wikiLinks is LoadingResult || wikiPages is LoadingResult,
        navigateToCreatePage = goToWikiCreatePage,
        navigateToPageBySlug = goToWikiPage
    )
}

@Composable
fun WikiListScreenContent(
    modifier: Modifier = Modifier,
    bookmarks: List<Pair<String, String>> = emptyList(),
    allPages: List<String> = emptyList(),
    isLoading: Boolean = false,
    navigateToCreatePage: () -> Unit = {},
    navigateToPageBySlug: (slug: String) -> Unit = {}
) = Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.Start
) {
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularLoader()
        }
    }

    if (bookmarks.isEmpty() && allPages.isEmpty()) {
        EmptyWikiDialogWidget(
            createNewPage = navigateToCreatePage
        )
    }
    val tabs = WikiTabs.entries.toTypedArray()

    HorizontalTabbedPager(
        modifier = Modifier.fillMaxSize(),
        tabs = tabs,
        pagerState = rememberPagerState(pageCount = { tabs.size })
    ) { page ->
        when (WikiTabs.entries[page]) {
            WikiTabs.Bookmarks -> WikiSelectorList(
                titles = bookmarks.map { it.first },
                bookmarks = bookmarks,
                onClick = navigateToPageBySlug
            )

            WikiTabs.AllWikiPages -> WikiSelectorList(
                titles = allPages,
                onClick = navigateToPageBySlug
            )
        }
    }
}

private enum class WikiTabs(@StringRes override val titleId: Int) : Tab {
    Bookmarks(RString.bookmarks),
    AllWikiPages(RString.all_wiki_pages)
}

@Composable
private fun WikiSelectorList(
    titles: List<String> = emptyList(),
    bookmarks: List<Pair<String, String>> = emptyList(),
    onClick: (name: String) -> Unit = {}
) = Box(
    Modifier.fillMaxSize(),
    contentAlignment = Alignment.TopStart
) {
    val listItemContent: @Composable LazyItemScope.(Int, String) -> Unit = lambda@{ index, item ->
        WikiSelectorItem(
            title = item,
            onClick = { onClick(bookmarks.getOrNull(index)?.second ?: item) }
        )

        if (index < titles.lastIndex) {
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
        LazyColumn {
            itemsIndexed(titles, itemContent = listItemContent)
        }
    }

    if (titles.isEmpty()) {
        EmptyWikiDialogWidget(
            isButtonAvailable = false
        )
    }
}

@Composable
private fun WikiSelectorItem(title: String, onClick: () -> Unit = {}) = ContainerBox(
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

@Preview
@Composable
private fun WikiPageSelectorPreview() {
    WikiListScreenContent()
}
