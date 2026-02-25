package com.grappim.taigamobile.core.storage.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.grappim.taigamobile.core.storage.KmpSession
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.TaigaSessionStorageImpl
import com.grappim.taigamobile.core.storage.auth.AuthStorage
import com.grappim.taigamobile.core.storage.auth.AuthStorageImpl
import com.grappim.taigamobile.utils.ui.ColorMapper
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
    fun provideAuthStorage(): AuthStorage = AuthStorageImpl(createAuthDataStore())

    @Single
    fun provideSessionStorage(colorMapper: ColorMapper): TaigaSessionStorage =
        TaigaSessionStorageImpl(createSessionDataStore(), colorMapper)

    @Single
    fun provideSession(@StorageJsonQualifier json: Json): KmpSession = KmpSession(createSessionFiltersDataStore(), json)
}

fun createSessionDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        File(System.getProperty("java.io.tmpdir"), "$TAIGA_SESSION_STORAGE$PREFS_EXT").absolutePath
    }
)

fun createSessionFiltersDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        File(
            System.getProperty("java.io.tmpdir"),
            "$SESSION_FILTERS_DATA_STORE_FILE_NAME$PREFS_EXT"
        ).absolutePath
    }
)

fun createAuthDataStore(): DataStore<Preferences> = createDataStore(
    producePath = { File(System.getProperty("java.io.tmpdir"), "$AUTH_DATA_STORE_FILE_NAME$PREFS_EXT").absolutePath }
)
