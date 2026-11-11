package com.grappim.taigamobile.core.storage.customheaders

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.grappim.kit.logger.LogPriority
import com.grappim.kit.logger.logcat
import com.grappim.kit.storage.SecretCipher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.concurrent.Volatile

/**
 * The whole list is one encrypted JSON blob: the values are proxy credentials, and the names alone
 * say which proxy guards the server.
 *
 * A blob that no longer decrypts or decodes reads as no headers rather than throwing — the user
 * sees the proxy's rejection on the next request and re-enters them.
 */
class CustomHeadersStorageImpl(
    private val dataStore: DataStore<Preferences>,
    private val secretCipher: SecretCipher,
    private val json: Json
) : CustomHeadersStorage {

    @Volatile
    private var cached: List<CustomHeader>? = null

    override val headers: Flow<List<CustomHeader>> = dataStore.data
        .map { prefs -> decode(prefs[KEY_CUSTOM_HEADERS]) }
        .distinctUntilChanged()

    /**
     * Blocks on the *first* read only, then serves the cache. Two threads racing here both read
     * the same value, so the write is idempotent.
     */
    override val currentHeaders: List<CustomHeader>
        get() = cached ?: runBlocking { headers.first() }.also { cached = it }

    override suspend fun setHeaders(headers: List<CustomHeader>) {
        dataStore.edit { prefs ->
            if (headers.isEmpty()) {
                prefs.remove(KEY_CUSTOM_HEADERS)
            } else {
                prefs[KEY_CUSTOM_HEADERS] = secretCipher.encrypt(json.encodeToString(headers))
            }
        }
        cached = headers
    }

    /**
     * The decoder's exception message embeds the offending JSON — decrypted credentials — so the
     * failure is logged with a fixed message and no throwable.
     */
    private fun decode(stored: String?): List<CustomHeader> {
        if (stored == null) return emptyList()
        val plain = secretCipher.decrypt(stored)
        if (plain == null) {
            logcat(priority = LogPriority.WARN) { "stored custom headers no longer decrypt; reading as none" }
            return emptyList()
        }
        return try {
            json.decodeFromString<List<CustomHeader>>(plain)
        } catch (_: SerializationException) {
            logcat(priority = LogPriority.WARN) { "stored custom headers failed to decode; reading as none" }
            emptyList()
        }
    }

    private companion object {
        val KEY_CUSTOM_HEADERS = stringPreferencesKey("custom_headers")
    }
}
