package com.grappim.taigamobile.feature.settings.ui.trustedcerts

import androidx.navigation.NavController
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object TrustedCertificatesNavDestination : NavKey

fun NavController.goToTrustedCertificatesScreen() {
    navigate(route = TrustedCertificatesNavDestination)
}
