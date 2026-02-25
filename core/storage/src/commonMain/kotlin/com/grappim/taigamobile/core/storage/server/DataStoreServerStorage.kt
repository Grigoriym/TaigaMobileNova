package com.grappim.taigamobile.core.storage.server

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.di.PREFS_EXT
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

internal const val SERVER_STORAGE_FILE_NAME = "taiga_server_storage_name$PREFS_EXT"

internal class DataStoreServerStorage(
    private val dataStore: DataStore<Preferences>,
    private val appInfoProvider: AppInfoProvider
) : ServerStorage {

    private val serverKey = stringPreferencesKey("server_key")

    override val server: String
        get() = runBlocking {
            dataStore.data.map { prefs ->
                prefs[serverKey] ?: getServerDefaultValue()
            }.first()
        }

    override fun defineServer(value: String) {
        runBlocking {
            dataStore.edit { prefs ->
                prefs[serverKey] = value
            }
        }
    }

    private fun getServerDefaultValue(): String =
        if (appInfoProvider.isDebug() && appInfoProvider.getDebugLocalHost().isNotEmpty()) {
            appInfoProvider.getDebugLocalHost()
        } else {
            "https://api.taiga.io"
        }
}
