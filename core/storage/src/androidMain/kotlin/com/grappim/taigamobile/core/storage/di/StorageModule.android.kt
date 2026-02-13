package com.grappim.taigamobile.core.storage.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.grappim.taigamobile.core.storage.KmpSession
import com.grappim.taigamobile.core.storage.KmpTaigaSessionStorage
import com.grappim.taigamobile.core.storage.auth.AuthStorage
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
    fun provideAuthStorage(context: Context): AuthStorage = AuthStorage(createAuthDataStore(context))

    @Single
    fun provideSessionStorage(
        context: Context,
        colorMapper: ColorMapper
    ): KmpTaigaSessionStorage = KmpTaigaSessionStorage(createSessionDataStore(context), colorMapper)

    @Single
    fun provideSession(
        context: Context,
        @StorageJsonQualifier json: Json
    ): KmpSession = KmpSession(createSessionFiltersDataStore(context), json)
}

fun createSessionDataStore(context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        migrations = listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "taiga_session_storage"
            )
        ),
        produceFile = {
            context.filesDir.resolve(SESSION_DATA_STORE_FILE_NAME).absolutePath.toPath()
        }
    )

fun createSessionFiltersDataStore(context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        migrations = listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "session"
            )
        ),
        produceFile = {
            context.filesDir.resolve(SESSION_FILTERS_DATA_STORE_FILE_NAME).absolutePath.toPath()
        }
    )

fun createAuthDataStore(context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        migrations = listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "auth_storage"
            )
        ),
        produceFile = {
            context.filesDir.resolve(AUTH_DATA_STORE_FILE_NAME).absolutePath.toPath()
        }
    )

internal const val SERVER_DATA_STORE_FILE_NAME = "taiga_server_storage.preferences_pb"
fun createServerDataStore(context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(
        migrations = listOf(
            SharedPreferencesMigration(
                context = context,
                sharedPreferencesName = "taiga_server_storage_name"
            )
        ),
        produceFile = {
            context.filesDir.resolve(SERVER_DATA_STORE_FILE_NAME).absolutePath.toPath()
        }
    )
