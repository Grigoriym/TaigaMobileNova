package com.grappim.taigamobile.main.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.domain.TaskIdentifier
import com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen
import com.grappim.taigamobile.feature.wiki.ui.create.WikiCreatePageScreen
import com.grappim.taigamobile.feature.wiki.ui.list.WikiListScreen
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiCreatePageNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiPageNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.navigateToWikiPage
import com.grappim.taigamobile.feature.wiki.ui.page.WikiPageScreen
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.navigateToWorkItemEditDescription
import com.grappim.taigamobile.utils.ui.NativeText

fun NavGraphBuilder.wikiNavGraph(showSnackbar: (NativeText) -> Unit, navController: NavHostController) {
    composable<WikiNavDestination> {
        WikiListScreen(
            showSnackbar = showSnackbar,
            goToWikiCreatePage = {
                navController.navigate(route = WikiCreatePageNavDestination)
            },
            goToWikiPage = { slug, id ->
                navController.navigateToWikiPage(slug, id)
            }
        )
    }

    composable<WikiCreatePageNavDestination> {
        WikiCreatePageScreen(
            showSnackbar = showSnackbar,
            goToWikiPage = { slug, id ->
                navController.navigateToWikiPage(
                    slug = slug,
                    id = id,
                    popUpToRoute = WikiCreatePageNavDestination
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
}
