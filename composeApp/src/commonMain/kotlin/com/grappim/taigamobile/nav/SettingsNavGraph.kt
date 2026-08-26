package com.grappim.taigamobile.nav

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
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

fun EntryProviderScope<NavKey>.settingsNavGraph(navigator: Navigator, showSnackbar: (NativeText) -> Unit) {
    entry<SettingsNavDestination> {
        SettingsScreen(
            goToAboutScreen = {
                navigator.goToSettingsAboutScreen()
            },
            goToInterfaceScreen = {
                navigator.goToSettingsInterfaceScreen()
            },
            goToUserScreen = {
                navigator.goToSettingsUserScreen()
            },
            goToAttributesScreen = {
                navigator.goToAttributesScreen()
            },
            goToProjectDetailsScreen = {
                navigator.navigateToProjectDetails()
            },
            goToModulesScreen = {
                navigator.navigateToModules()
            },
            goToTrustedCertificatesScreen = {
                navigator.goToTrustedCertificatesScreen()
            }
        )
    }

    entry<ProjectDetailsNavDestination> {
        ProjectDetailsScreen(
            onNavigateBack = { navigator.goBack() },
            showSnackbar = showSnackbar
        )
    }

    entry<ModulesNavDestination> {
        ModulesScreen(
            onNavigateBack = { navigator.goBack() },
            showSnackbar = showSnackbar
        )
    }

    entry<SettingsAboutScreenRouteNavDestination> {
        SettingsAboutScreen()
    }

    entry<SettingsInterfaceScreenNavDestination> {
        SettingsInterfaceScreen()
    }

    entry<SettingsUserScreenNavDestination> {
        SettingsUserScreen()
    }

    entry<AttributesScreenNavDestination> {
        AttributesScreen(
            goToTagsScreen = {
                navigator.goToTagsScreen()
            },
            goToProjectValuesMenuScreen = {
                navigator.navigateToProjectValuesMenu()
            }
        )
    }

    entry<TagsScreenRouteNavDestination> {
        TagsScreen(showSnackbar = showSnackbar)
    }

    entry<ProjectValuesMenuNavDestination> {
        ProjectValuesMenuScreen(
            goToProjectValues = { type ->
                navigator.navigateToProjectValues(type)
            }
        )
    }

    entry<ProjectValuesNavDestination> { route ->
        ProjectValuesScreen(route = route, showSnackbar = showSnackbar)
    }

    entry<TrustedCertificatesNavDestination> {
        TrustedCertificatesScreen()
    }
}
