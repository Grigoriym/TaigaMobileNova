package com.grappim.taigamobile.core.storage.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.grappim.kit.storage.NetworkMonitor
import com.grappim.kit.storage.NetworkMonitorImpl
import com.grappim.kit.storage.NoopSecretCipher
import com.grappim.kit.storage.cert.TrustedCertStorage
import com.grappim.kit.storage.cert.TrustedCertStorageImpl
import com.grappim.taigamobile.core.asynckmp.ApplicationScope
import com.grappim.taigamobile.core.asynckmp.IoDispatcher
import com.grappim.taigamobile.core.storage.FiltersStorage
import com.grappim.taigamobile.core.storage.FiltersStorageImpl
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.TaigaSessionStorageImpl
import com.grappim.taigamobile.core.storage.auth.AuthStorage
import com.grappim.taigamobile.core.storage.auth.AuthStorageImpl
import com.grappim.taigamobile.core.storage.platform.appDataDir
import com.grappim.taigamobile.utils.ui.ColorMapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import java.io.File

@Module(includes = [AuthDataStoreModule::class, StorageModule::class])
@Configuration
actual class PlatformStorageModule

@Module
class AuthDataStoreModule {

    @Single
    fun provideAuthStorage(): AuthStorage = AuthStorageImpl(createAuthDataStore(), NoopSecretCipher())

    @Single
    fun provideSessionStorage(colorMapper: ColorMapper): TaigaSessionStorage =
        TaigaSessionStorageImpl(createSessionDataStore(), colorMapper)

    @Single
    fun provideSession(@StorageJsonQualifier json: Json): FiltersStorage =
        FiltersStorageImpl(createSessionFiltersDataStore(), json)

    @Single
    fun provideTrustedCertStorage(@StorageJsonQualifier json: Json): TrustedCertStorage =
        TrustedCertStorageImpl(createTrustedCertDataStore(), json)

    @Single
    fun provideNetworkMonitor(
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        @ApplicationScope applicationScope: CoroutineScope
    ): NetworkMonitor = NetworkMonitorImpl(ioDispatcher, applicationScope)
}

fun createSessionDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        File(appDataDir(), "$TAIGA_SESSION_STORAGE$PREFS_EXT").absolutePath
    }
)

fun createSessionFiltersDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        File(
            appDataDir(),
            "$SESSION_FILTERS_DATA_STORE_FILE_NAME$PREFS_EXT"
        ).absolutePath
    }
)

fun createAuthDataStore(): DataStore<Preferences> = createDataStore(
    producePath = { File(appDataDir(), "$AUTH_DATA_STORE_FILE_NAME$PREFS_EXT").absolutePath }
)

fun createTrustedCertDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        File(appDataDir(), "$TRUSTED_CERT_DATA_STORE_FILE_NAME$PREFS_EXT").absolutePath
    }
)
