package com.grappim.taigamobile.core.storage.cert

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.grappim.taigamobile.core.domain.PendingCertTrust
import com.grappim.taigamobile.core.storage.di.StorageJsonQualifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

interface TrustedCertStorage {
    suspend fun isTrusted(host: String, sha256Fingerprint: String): Boolean
    suspend fun trust(pendingCertTrust: PendingCertTrust)
    fun getAllFlow(): Flow<List<PendingCertTrust>>
    suspend fun untrust(host: String, sha256Fingerprint: String)
}

class TrustedCertStorageImpl(
    private val dataStore: DataStore<Preferences>,
    @param:StorageJsonQualifier private val json: Json
) : TrustedCertStorage {

    private val trustedEntriesFlow = dataStore.data.map { prefs ->
        decodeEntries(prefs[TRUSTED_CERTS_KEY])
    }

    override suspend fun isTrusted(host: String, sha256Fingerprint: String): Boolean =
        trustedEntriesFlow.first().any { it.matches(host, sha256Fingerprint) }

    override suspend fun trust(pendingCertTrust: PendingCertTrust) {
        dataStore.edit { prefs ->
            val entries = decodeEntries(prefs[TRUSTED_CERTS_KEY])
                .filterNot { it.matches(pendingCertTrust.host, pendingCertTrust.sha256Fingerprint) }
            prefs[TRUSTED_CERTS_KEY] = json.encodeToString(entries + pendingCertTrust)
        }
    }

    override fun getAllFlow(): Flow<List<PendingCertTrust>> = trustedEntriesFlow

    override suspend fun untrust(host: String, sha256Fingerprint: String) {
        dataStore.edit { prefs ->
            val entries = decodeEntries(prefs[TRUSTED_CERTS_KEY]).filterNot { it.matches(host, sha256Fingerprint) }
            prefs[TRUSTED_CERTS_KEY] = json.encodeToString(entries)
        }
    }

    private fun decodeEntries(value: String?): List<PendingCertTrust> =
        value?.takeIf { it.isNotBlank() }?.let { json.decodeFromString(it) } ?: emptyList()

    private fun PendingCertTrust.matches(host: String, sha256Fingerprint: String) =
        this.host == host && this.sha256Fingerprint == sha256Fingerprint

    companion object {
        private val TRUSTED_CERTS_KEY = stringPreferencesKey("trusted_certs")
    }
}
