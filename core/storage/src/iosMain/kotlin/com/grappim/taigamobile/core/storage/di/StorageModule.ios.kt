@file:OptIn(ExperimentalForeignApi::class)

package com.grappim.taigamobile.core.storage.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.grappim.taigamobile.core.storage.KmpSession
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.auth.AuthStorage
import com.grappim.taigamobile.utils.ui.ColorMapper
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@Module(includes = [AuthDataStoreModule::class, StorageModule::class])
@Configuration
@ComponentScan
actual class PlatformStorageModule

@Module
@ComponentScan
class AuthDataStoreModule {

    @Single
    fun provideAuthStorage(): AuthStorage = AuthStorage(createAuthDataStore())

    @Single
    fun provideSessionStorage(colorMapper: ColorMapper): TaigaSessionStorage =
        TaigaSessionStorage(createSessionDataStore(), colorMapper)

    @Single
    fun provideSession(@StorageJsonQualifier json: Json): KmpSession = KmpSession(createSessionFiltersDataStore(), json)
}

fun createSessionDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        requireNotNull(documentDirectory).path + "/$TAIGA_SESSION_STORAGE$PREFS_EXT"
    }
)

fun createSessionFiltersDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        requireNotNull(documentDirectory).path + "/$SESSION_FILTERS_DATA_STORE_FILE_NAME$PREFS_EXT"
    }
)

fun createAuthDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        requireNotNull(documentDirectory).path + "/$AUTH_DATA_STORE_FILE_NAME$PREFS_EXT"
    }
)
