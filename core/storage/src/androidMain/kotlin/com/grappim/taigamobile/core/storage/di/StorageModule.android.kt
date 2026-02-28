package com.grappim.taigamobile.core.storage.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.grappim.taigamobile.core.storage.FiltersStorage
import com.grappim.taigamobile.core.storage.FiltersStorageImpl
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.TaigaSessionStorageImpl
import com.grappim.taigamobile.core.storage.auth.AuthStorage
import com.grappim.taigamobile.core.storage.auth.AuthStorageImpl
import com.grappim.taigamobile.utils.ui.ColorMapper
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(includes = [AuthDataStoreModule::class, StorageModule::class])
@Configuration
@ComponentScan
actual class PlatformStorageModule

@Module
@ComponentScan
class AuthDataStoreModule {

    @Single
    fun provideAuthStorage(context: Context): AuthStorage = AuthStorageImpl(createAuthDataStore(context))

    @Single
    fun provideSessionStorage(context: Context, colorMapper: ColorMapper): TaigaSessionStorage =
        TaigaSessionStorageImpl(createSessionDataStore(context), colorMapper)

    @Single
    fun provideSession(context: Context, @StorageJsonQualifier json: Json): FiltersStorage =
        FiltersStorageImpl(createSessionFiltersDataStore(context), json)
}

fun createSessionDataStore(context: Context): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    produceFile = {
        context.preferencesDataStoreFile(TAIGA_SESSION_STORAGE).absolutePath.toPath()
    }
)

fun createSessionFiltersDataStore(context: Context): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    migrations = listOf(
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = "session"
        )
    ),
    produceFile = {
        context.preferencesDataStoreFile(SESSION_FILTERS_DATA_STORE_FILE_NAME).absolutePath.toPath()
    }
)

fun createAuthDataStore(context: Context): DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath(
    migrations = listOf(
        SharedPreferencesMigration(
            context = context,
            sharedPreferencesName = "auth_storage"
        )
    ),
    produceFile = {
        context.preferencesDataStoreFile(AUTH_DATA_STORE_FILE_NAME).absolutePath.toPath()
    }
)
