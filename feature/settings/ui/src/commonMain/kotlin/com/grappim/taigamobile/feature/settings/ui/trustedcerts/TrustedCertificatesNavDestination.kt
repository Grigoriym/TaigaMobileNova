package com.grappim.taigamobile.feature.settings.ui.trustedcerts

import androidx.navigation3.runtime.NavKey
import com.grappim.taigamobile.core.navigation.Navigator
import kotlinx.serialization.Serializable

@Serializable
data object TrustedCertificatesNavDestination : NavKey

fun Navigator.goToTrustedCertificatesScreen() {
    navigate(TrustedCertificatesNavDestination)
}
