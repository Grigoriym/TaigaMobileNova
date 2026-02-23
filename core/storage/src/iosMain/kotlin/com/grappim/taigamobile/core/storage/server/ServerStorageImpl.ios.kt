@file:OptIn(ExperimentalForeignApi::class)

package com.grappim.taigamobile.core.storage.server

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.grappim.taigamobile.core.appinfoapi.AppInfoProvider
import com.grappim.taigamobile.core.storage.di.PREFS_EXT
import com.grappim.taigamobile.core.storage.di.createDataStore
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Single(binds = [ServerStorage::class])
class ServerStorageImpl(private val appInfoProvider: AppInfoProvider) : ServerStorage {

    companion object {
        private const val SERVER_STORAGE_NAME = "taiga_server_storage_name$PREFS_EXT"
    }

    private val serverKey = stringPreferencesKey("server_key")

    private val dataStore: DataStore<Preferences> = createDataStore(
        producePath = {
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null
            )
            requireNotNull(documentDirectory).path + "/$SERVER_STORAGE_NAME"
        }
    )

    override val server: String
        get() = runBlocking {
            dataStore.data.map { prefs ->
                prefs[serverKey] ?: getServerDefaultValue()
            }.first()
        }

    private fun getServerDefaultValue(): String =
        if (appInfoProvider.isDebug() && appInfoProvider.getDebugLocalHost().isNotEmpty()) {
            appInfoProvider.getDebugLocalHost()
        } else {
            "https://api.taiga.io"
        }

    override fun defineServer(value: String) {
        runBlocking {
            dataStore.edit { prefs ->
                prefs[serverKey] = value
            }
        }
    }
}
