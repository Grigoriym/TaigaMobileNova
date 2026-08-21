package com.grappim.taigamobile.nav

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.core.navigation.Navigator
import com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen
import com.grappim.taigamobile.feature.wiki.ui.bookmark.create.WikiCreateBookmarkScreen
import com.grappim.taigamobile.feature.wiki.ui.bookmark.list.WikiBookmarksScreen
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiCreateLinkNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiCreatePageNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiLinksNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiPageNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiPagesNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.navigateToWikiPage
import com.grappim.taigamobile.feature.wiki.ui.page.create.WikiCreatePageScreen
import com.grappim.taigamobile.feature.wiki.ui.page.details.WikiPageScreen
import com.grappim.taigamobile.feature.wiki.ui.page.list.WikiPagesScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.navigateToWorkItemEditDescription
import com.grappim.taigamobile.utils.ui.NativeText

fun EntryProviderScope<NavKey>.wikiNavGraph(showSnackbar: (NativeText) -> Unit, navigator: Navigator) {
    entry<WikiCreatePageNavDestination> {
        WikiCreatePageScreen(
            goToWikiPage = { slug, id ->
                navigator.navigateToWikiPage(
                    slug = slug,
                    id = id,
                    replaceCurrent = true
                )
            }
        )
    }

    entry<WikiCreateLinkNavDestination> {
        WikiCreateBookmarkScreen(
            goToWikiPage = { href, id ->
                navigator.navigateToWikiPage(
                    slug = href,
                    id = id,
                    replaceCurrent = true
                )
            }
        )
    }

    entry<WikiPageNavDestination> { route ->
        WikiPageScreen(
            route = route,
            showSnackbar = showSnackbar,
            goToProfile = { userId ->
                navigator.navigateToProfileScreen(userId)
            },
            goBack = { navigator.goBack() },
            goToEditDescription = { description: String, id: Long ->
                navigator.navigateToWorkItemEditDescription(
                    description = description,
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.Wiki
                )
            }
        )
    }

    entry<WikiPagesNavDestination> {
        WikiPagesScreen(
            showSnackbar = showSnackbar,
            goToWikiCreatePage = {
                navigator.navigate(WikiCreatePageNavDestination)
            },
            goToWikiPage = { slug, id ->
                navigator.navigateToWikiPage(slug, id)
            }
        )
    }

    entry<WikiLinksNavDestination> {
        WikiBookmarksScreen(
            showSnackbar = showSnackbar,
            goToWikiCreateBookmark = {
                navigator.navigate(WikiCreateLinkNavDestination)
            },
            goToWikiPage = { slug, id ->
                navigator.navigateToWikiPage(slug, id)
            }
        )
    }
}
