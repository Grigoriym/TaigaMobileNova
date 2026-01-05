package com.grappim.taigamobile.main.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.grappim.taigamobile.core.domain.TaskIdentifier
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

fun NavGraphBuilder.wikiNavGraph(showSnackbar: (NativeText) -> Unit, navController: NavHostController) {
    composable<WikiCreatePageNavDestination> {
        WikiCreatePageScreen(
            goToWikiPage = { slug, id ->
                navController.navigateToWikiPage(
                    slug = slug,
                    id = id,
                    popUpToRoute = WikiCreatePageNavDestination
                )
            }
        )
    }

    composable<WikiCreateLinkNavDestination> {
        WikiCreateBookmarkScreen(
            goToWikiPage = { href, id ->
                navController.navigateToWikiPage(
                    slug = href,
                    id = id,
                    popUpToRoute = WikiCreateLinkNavDestination
                )
            }
        )
    }

    composable<WikiPageNavDestination> {
        WikiPageScreen(
            showSnackbar = showSnackbar,
            goToProfile = { userId ->
                navController.navigateToProfileScreen(userId)
            },
            goBack = navController::popBackStack,
            goToEditDescription = { description: String, id: Long ->
                navController.navigateToWorkItemEditDescription(
                    description = description,
                    workItemId = id,
                    taskIdentifier = TaskIdentifier.Wiki
                )
            }
        )
    }

    composable<WikiPagesNavDestination> {
        WikiPagesScreen(
            goToWikiCreatePage = {
                navController.navigate(route = WikiCreatePageNavDestination)
            },
            goToWikiPage = { slug, id ->
                navController.navigateToWikiPage(slug, id)
            }
        )
    }

    composable<WikiLinksNavDestination> {
        WikiBookmarksScreen(
            showSnackbar = showSnackbar,
            goToWikiCreateBookmark = {
                navController.navigate(route = WikiCreateLinkNavDestination)
            },
            goToWikiPage = { slug, id ->
                navController.navigateToWikiPage(slug, id)
            }
        )
    }
}
