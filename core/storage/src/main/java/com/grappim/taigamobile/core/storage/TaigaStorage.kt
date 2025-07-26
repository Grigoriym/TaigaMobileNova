package com.grappim.taigamobile.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaigaStorage @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private const val TAIGA_STORAGE_NAME = "taiga_storage"

        private const val CURRENT_PROJECT_ID_KEY = "current_project_id"
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = TAIGA_STORAGE_NAME
    )

    private val isNewUiUsedKey = booleanPreferencesKey("is_new_ui_used")
    val isNewUIUsed: Flow<Boolean> = context.dataStore.data
        .map {
            it[isNewUiUsedKey] ?: false
        }

    private val themeSettingKey = stringPreferencesKey("theme_settings")
    val themeSettings: Flow<ThemeSettings> = context.dataStore.data
        .map { preferences ->
            ThemeSettings.fromValue(preferences[themeSettingKey]) ?: ThemeSettings.default()
        }

    suspend fun setThemSetting(themeSettings: ThemeSettings) {
        context.dataStore.edit { settings ->
            settings[themeSettingKey] = themeSettings.value
        }
    }

    private val currentProjectIdKey = longPreferencesKey(CURRENT_PROJECT_ID_KEY)

    val currentProjectIdFlow: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[currentProjectIdKey] ?: -1
        }

    val currentProjectId: Long
        get() = runBlocking { currentProjectIdFlow.first() }

    suspend fun setCurrentProjectId(projectId: Long) {
        context.dataStore.edit { settings ->
            settings[currentProjectIdKey] = projectId
        }
    }

    suspend fun clearData() {
        context.dataStore.edit {
            it.clear()
        }
    }

    suspend fun setIsUIUsed(isEnabled: Boolean) {
        Timber.d("setCrashesCollectionEnabled: $isEnabled")
        context.dataStore.edit { settings ->
            settings[isNewUiUsedKey] = isEnabled
        }
    }
}
