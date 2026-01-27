package com.grappim.taigamobile.main.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.grappim.taigamobile.feature.settings.ui.SettingsNavDestination
import com.grappim.taigamobile.feature.settings.ui.SettingsScreen
import com.grappim.taigamobile.feature.settings.ui.about.SettingsAboutScreen
import com.grappim.taigamobile.feature.settings.ui.about.SettingsAboutScreenRouteNavDestination
import com.grappim.taigamobile.feature.settings.ui.about.goToSettingsAboutScreen
import com.grappim.taigamobile.feature.settings.ui.attributes.AttributesScreen
import com.grappim.taigamobile.feature.settings.ui.attributes.AttributesScreenNavDestination
import com.grappim.taigamobile.feature.settings.ui.attributes.goToAttributesScreen
import com.grappim.taigamobile.feature.settings.ui.attributes.tags.TagsScreen
import com.grappim.taigamobile.feature.settings.ui.attributes.tags.TagsScreenRouteNavDestination
import com.grappim.taigamobile.feature.settings.ui.attributes.tags.goToTagsScreen
import com.grappim.taigamobile.feature.settings.ui.interfacescreen.SettingsInterfaceScreen
import com.grappim.taigamobile.feature.settings.ui.interfacescreen.SettingsInterfaceScreenNavDestination
import com.grappim.taigamobile.feature.settings.ui.interfacescreen.goToSettingsInterfaceScreen
import com.grappim.taigamobile.feature.settings.ui.user.SettingsUserScreen
import com.grappim.taigamobile.feature.settings.ui.user.SettingsUserScreenNavDestination
import com.grappim.taigamobile.feature.settings.ui.user.goToSettingsUserScreen
import com.grappim.taigamobile.utils.ui.NativeText

fun NavGraphBuilder.settingsNavGraph(navController: NavHostController, showSnackbar: (NativeText) -> Unit) {
    composable<SettingsNavDestination> {
        SettingsScreen(
            goToAboutScreen = {
                navController.goToSettingsAboutScreen()
            },
            goToInterfaceScreen = {
                navController.goToSettingsInterfaceScreen()
            },
            goToUserScreen = {
                navController.goToSettingsUserScreen()
            },
            goToAttributesScreen = {
                navController.goToAttributesScreen()
            }
        )
    }

    composable<SettingsAboutScreenRouteNavDestination> {
        SettingsAboutScreen()
    }

    composable<SettingsInterfaceScreenNavDestination> {
        SettingsInterfaceScreen()
    }

    composable<SettingsUserScreenNavDestination> {
        SettingsUserScreen()
    }

    composable<AttributesScreenNavDestination> {
        AttributesScreen(
            goToTagsScreen = {
                navController.goToTagsScreen()
            }
        )
    }

    composable<TagsScreenRouteNavDestination> {
        TagsScreen(showSnackbar = showSnackbar)
    }
}
