package com.grappim.taigamobile.testing.storage

import com.grappim.taigamobile.core.domain.PendingCertTrust
import com.grappim.taigamobile.core.storage.cert.TrustedCertStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeTrustedCertStorage : TrustedCertStorage {

    private val trustedFlow = MutableStateFlow<List<PendingCertTrust>>(emptyList())
    var trustCalledWith: PendingCertTrust? = null
    var untrustCalledWith: Pair<String, String>? = null

    override suspend fun isTrusted(host: String, sha256Fingerprint: String): Boolean =
        trustedFlow.value.any { it.host == host && it.sha256Fingerprint == sha256Fingerprint }

    override suspend fun trust(pendingCertTrust: PendingCertTrust) {
        trustCalledWith = pendingCertTrust
        trustedFlow.value = trustedFlow.value.filterNot {
            it.host == pendingCertTrust.host && it.sha256Fingerprint == pendingCertTrust.sha256Fingerprint
        } + pendingCertTrust
    }

    override fun getAllFlow() = trustedFlow.asStateFlow()

    override suspend fun untrust(host: String, sha256Fingerprint: String) {
        untrustCalledWith = host to sha256Fingerprint
        trustedFlow.value = trustedFlow.value.filterNot {
            it.host == host && it.sha256Fingerprint == sha256Fingerprint
        }
    }
}
