package com.grappim.taigamobile.feature.settings.ui.trustedcerts

import androidx.navigation.NavController
import kotlinx.serialization.Serializable

@Serializable
data object TrustedCertificatesNavDestination

fun NavController.goToTrustedCertificatesScreen() {
    navigate(route = TrustedCertificatesNavDestination)
}
