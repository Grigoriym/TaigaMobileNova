package com.grappim.taigamobile.feature.settings.ui.trustedcerts

import com.grappim.taigamobile.core.domain.PendingCertTrust
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class TrustedCertificatesState(
    val entries: ImmutableList<PendingCertTrust> = persistentListOf(),

    val onRevokeClick: (PendingCertTrust) -> Unit = {},
    val revokeEntry: () -> Unit = {},
    val entryToRevoke: PendingCertTrust? = null,
    val isRevokeDialogVisible: Boolean = false,
    val closeRevokeDialog: () -> Unit = {}
)
