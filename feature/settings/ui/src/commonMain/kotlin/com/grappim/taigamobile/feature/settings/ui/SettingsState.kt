package com.grappim.taigamobile.feature.settings.ui

import com.grappim.taigamobile.core.api.supportsCertificateTrustManagement

data class SettingsState(
    val canSeeAttributes: Boolean = false,
    val canSeeTrustedCertificates: Boolean = supportsCertificateTrustManagement
)
