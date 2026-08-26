package com.grappim.taigamobile.feature.settings.ui

import com.grappim.taigamobile.core.api.supportsCertificateTrustManagement

data class SettingsState(
    val canSeeAttributes: Boolean = false,
    val canSeeTrustedCertificates: Boolean = supportsCertificateTrustManagement,
    val isLogoutConfirmationVisible: Boolean = false,
    val setIsLogoutConfirmationVisible: (Boolean) -> Unit = {},
    val onLogout: () -> Unit = {}
)
