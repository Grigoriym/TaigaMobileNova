@file:OptIn(ExperimentalForeignApi::class)

package com.grappim.taigamobile.core.storage.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.grappim.taigamobile.core.storage.FiltersStorage
import com.grappim.taigamobile.core.storage.FiltersStorageImpl
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.TaigaSessionStorageImpl
import com.grappim.taigamobile.core.storage.auth.AuthStorage
import com.grappim.taigamobile.core.storage.auth.AuthStorageImpl
import com.grappim.taigamobile.core.storage.auth.NoopTokenCipher
import com.grappim.taigamobile.core.storage.cert.TrustedCertStorage
import com.grappim.taigamobile.core.storage.cert.TrustedCertStorageImpl
import com.grappim.taigamobile.utils.ui.ColorMapper
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Module(includes = [AuthDataStoreModule::class, StorageModule::class])
@Configuration
actual class PlatformStorageModule

@Module
class AuthDataStoreModule {

    @Single
    fun provideAuthStorage(): AuthStorage = AuthStorageImpl(createAuthDataStore(), NoopTokenCipher())

    @Single
    fun provideSessionStorage(colorMapper: ColorMapper): TaigaSessionStorage =
        TaigaSessionStorageImpl(createSessionDataStore(), colorMapper)

    @Single
    fun provideSession(@StorageJsonQualifier json: Json): FiltersStorage =
        FiltersStorageImpl(createSessionFiltersDataStore(), json)

    @Single
    fun provideTrustedCertStorage(@StorageJsonQualifier json: Json): TrustedCertStorage =
        TrustedCertStorageImpl(createTrustedCertDataStore(), json)
}

fun createSessionDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        documentDirectory() + "/$TAIGA_SESSION_STORAGE$PREFS_EXT"
    }
)

fun createSessionFiltersDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        documentDirectory() + "/$SESSION_FILTERS_DATA_STORE_FILE_NAME$PREFS_EXT"
    }
)

fun createAuthDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        documentDirectory() + "/$AUTH_DATA_STORE_FILE_NAME$PREFS_EXT"
    }
)

fun createTrustedCertDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        documentDirectory() + "/$TRUSTED_CERT_DATA_STORE_FILE_NAME$PREFS_EXT"
    }
)

private fun documentDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return requireNotNull(requireNotNull(documentDirectory).path)
}
