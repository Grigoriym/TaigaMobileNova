package com.grappim.taigamobile.uikit.widgets.container

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.Tab
import kotlinx.coroutines.launch

/**
 * Swipeable tabs
 */
@Composable
fun HorizontalTabbedPager(
    tabs: Array<out Tab>,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    edgePadding: Dp = mainHorizontalScreenPadding,
    content: @Composable PagerScope.(page: Int) -> Unit
) {
    Column(modifier = modifier) {
        val coroutineScope = rememberCoroutineScope()
        val selectedTabIndex by remember { derivedStateOf { pagerState.currentPage } }

        val indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(
                    currentTabPosition = tabPositions[selectedTabIndex]
                )
            )
        }

        val tabsRow: @Composable () -> Unit = {
            tabs.forEachIndexed { index, tab ->
                val selected = selectedTabIndex == index
                Tab(
                    selected = selected,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = stringResource(tab.titleId),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                )
            }
        }

        if (scrollable) {
            ScrollableTabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = selectedTabIndex,
                contentColor = MaterialTheme.colorScheme.primary,
//                backgroundColor = MaterialTheme.colorScheme.surface,
                indicator = indicator,
                tabs = tabsRow,
                divider = {},
                edgePadding = edgePadding
            )
        } else {
            TabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = selectedTabIndex,
                contentColor = MaterialTheme.colorScheme.primary,
//                backgroundColor = MaterialTheme.colorScheme.surface,
                indicator = indicator,
                tabs = tabsRow,
                divider = {}
            )
        }

        Spacer(Modifier.height(8.dp))

        HorizontalPager(
            state = pagerState,
            pageContent = content
        )
    }
}
