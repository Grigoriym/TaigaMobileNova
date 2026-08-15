package com.grappim.taigamobile.nav

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.grappim.taigamobile.feature.settings.ui.SettingsNavDestination
import com.grappim.taigamobile.feature.settings.ui.SettingsScreen
import com.grappim.taigamobile.feature.settings.ui.about.SettingsAboutScreen
import com.grappim.taigamobile.feature.settings.ui.about.SettingsAboutScreenRouteNavDestination
import com.grappim.taigamobile.feature.settings.ui.about.goToSettingsAboutScreen
import com.grappim.taigamobile.feature.settings.ui.attributes.AttributesScreen
import com.grappim.taigamobile.feature.settings.ui.attributes.AttributesScreenNavDestination
import com.grappim.taigamobile.feature.settings.ui.attributes.goToAttributesScreen
import com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues.ProjectValuesMenuNavDestination
import com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues.ProjectValuesMenuScreen
import com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues.ProjectValuesNavDestination
import com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues.ProjectValuesScreen
import com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues.navigateToProjectValues
import com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues.navigateToProjectValuesMenu
import com.grappim.taigamobile.feature.settings.ui.attributes.tags.TagsScreen
import com.grappim.taigamobile.feature.settings.ui.attributes.tags.TagsScreenRouteNavDestination
import com.grappim.taigamobile.feature.settings.ui.attributes.tags.goToTagsScreen
import com.grappim.taigamobile.feature.settings.ui.interfacescreen.SettingsInterfaceScreen
import com.grappim.taigamobile.feature.settings.ui.interfacescreen.SettingsInterfaceScreenNavDestination
import com.grappim.taigamobile.feature.settings.ui.interfacescreen.goToSettingsInterfaceScreen
import com.grappim.taigamobile.feature.settings.ui.modules.ModulesNavDestination
import com.grappim.taigamobile.feature.settings.ui.modules.ModulesScreen
import com.grappim.taigamobile.feature.settings.ui.modules.navigateToModules
import com.grappim.taigamobile.feature.settings.ui.projectdetails.ProjectDetailsNavDestination
import com.grappim.taigamobile.feature.settings.ui.projectdetails.ProjectDetailsScreen
import com.grappim.taigamobile.feature.settings.ui.projectdetails.navigateToProjectDetails
import com.grappim.taigamobile.feature.settings.ui.trustedcerts.TrustedCertificatesNavDestination
import com.grappim.taigamobile.feature.settings.ui.trustedcerts.TrustedCertificatesScreen
import com.grappim.taigamobile.feature.settings.ui.trustedcerts.goToTrustedCertificatesScreen
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
            },
            goToProjectDetailsScreen = {
                navController.navigateToProjectDetails()
            },
            goToModulesScreen = {
                navController.navigateToModules()
            },
            goToTrustedCertificatesScreen = {
                navController.goToTrustedCertificatesScreen()
            }
        )
    }

    composable<ProjectDetailsNavDestination> {
        ProjectDetailsScreen(
            onNavigateBack = { navController.popBackStack() },
            showSnackbar = showSnackbar
        )
    }

    composable<ModulesNavDestination> {
        ModulesScreen(
            onNavigateBack = { navController.popBackStack() },
            showSnackbar = showSnackbar
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
            },
            goToProjectValuesMenuScreen = {
                navController.navigateToProjectValuesMenu()
            }
        )
    }

    composable<TagsScreenRouteNavDestination> {
        TagsScreen(showSnackbar = showSnackbar)
    }

    composable<ProjectValuesMenuNavDestination> {
        ProjectValuesMenuScreen(
            goToProjectValues = { type ->
                navController.navigateToProjectValues(type)
            }
        )
    }

    composable<ProjectValuesNavDestination> { backStackEntry ->
        ProjectValuesScreen(route = backStackEntry.toRoute(), showSnackbar = showSnackbar)
    }

    composable<TrustedCertificatesNavDestination> {
        TrustedCertificatesScreen()
    }
}
