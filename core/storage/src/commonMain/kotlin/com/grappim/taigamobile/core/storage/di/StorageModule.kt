package com.grappim.taigamobile.core.storage.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

internal const val SESSION_FILTERS_DATA_STORE_FILE_NAME = "taiga_session_filters_storage"
internal const val AUTH_DATA_STORE_FILE_NAME = "auth_storage"
internal const val TAIGA_SESSION_STORAGE = "taiga_session_storage"

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class StorageJsonQualifier

expect class PlatformStorageModule

@Module
@ComponentScan
class StorageModule {

    @[Single StorageJsonQualifier]
    fun provideStorageJson(): Json = Json {
        ignoreUnknownKeys = true
    }
}

fun createDataStore(producePath: () -> String): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    produceFile = { producePath().toPath() }
)

const val PREFS_EXT = ".preferences_pb"
