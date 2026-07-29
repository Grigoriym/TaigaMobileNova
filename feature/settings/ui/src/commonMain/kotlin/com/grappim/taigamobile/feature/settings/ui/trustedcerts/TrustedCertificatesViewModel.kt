package com.grappim.taigamobile.feature.settings.ui.trustedcerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.PendingCertTrust
import com.grappim.taigamobile.core.storage.cert.TrustedCertStorage
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class TrustedCertificatesViewModel(private val trustedCertStorage: TrustedCertStorage) : ViewModel() {

    private val _state = MutableStateFlow(
        TrustedCertificatesState(
            onRevokeClick = ::onRevokeClick,
            revokeEntry = ::revokeEntry,
            closeRevokeDialog = ::closeRevokeDialog
        )
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            trustedCertStorage.getAllFlow().collect { entries ->
                _state.update { it.copy(entries = entries.toImmutableList()) }
            }
        }
    }

    private fun onRevokeClick(entry: PendingCertTrust) {
        _state.update { it.copy(entryToRevoke = entry, isRevokeDialogVisible = true) }
    }

    private fun closeRevokeDialog() {
        _state.update { it.copy(entryToRevoke = null, isRevokeDialogVisible = false) }
    }

    private fun revokeEntry() {
        val entry = requireNotNull(_state.value.entryToRevoke)
        viewModelScope.launch {
            trustedCertStorage.untrust(entry.host, entry.sha256Fingerprint)
            _state.update { it.copy(entryToRevoke = null, isRevokeDialogVisible = false) }
        }
    }
}
