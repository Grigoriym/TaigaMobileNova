package com.grappim.taigamobile.core.storage.di

import kotlinx.serialization.json.Json
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

@Module
@ComponentScan
class StorageModule {

    @[Single StorageJsonQualifier]
    fun provideStorageJson(): Json = Json {
        ignoreUnknownKeys = true
    }
}

@Module
expect class PlatformStorageModule
