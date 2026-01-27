package com.grappim.taigamobile.core.storage

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.grappim.taigamobile.utils.ui.ColorMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaigaSessionStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val colorMapper: ColorMapper
) {
    companion object {
        private const val TAIGA_STORAGE_NAME = "taiga_session_storage"

        private const val CURRENT_PROJECT_ID_KEY = "current_project_id"
        private const val USER_ID = "user_id"
        private const val THEME_SETTINGS = "theme_settings"

        private const val KANBAN_DEFAULT_SWIMLINE = "kanban_default_swimline"
        private const val TAG_PRESET_COLORS = "tag_preset_colors"
    }

    private val defaultPresetColors = persistentListOf(
        Color(0xFFE57373),
        Color(0xFF81C784),
        Color(0xFF64B5F6),
        Color(0xFFFFD54F),
        Color(0xFFBA68C8),
        Color(0xFF4DB6AC),
        Color(0xFFFF8A65),
        Color(0xFF90A4AE)
    ).map { colorMapper.fromColorToString(it) }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = TAIGA_STORAGE_NAME
    )

    private val kanbanDefaultSwimlineKey = longPreferencesKey(KANBAN_DEFAULT_SWIMLINE)
    val kanbanDefaultSwimline: Flow<Long?> = context.dataStore.data
        .map { prefs ->
            prefs[kanbanDefaultSwimlineKey]
        }

    suspend fun setKanbanDefaultSwimline(value: Long) {
        context.dataStore.edit { prefs ->
            prefs[kanbanDefaultSwimlineKey] = value
        }
    }

    private val tagPresetColorsKey = stringPreferencesKey(TAG_PRESET_COLORS)
    val tagPresetColors: Flow<List<String>> = context.dataStore.data
        .map { prefs ->
            val colorsString = prefs[tagPresetColorsKey]
            if (colorsString.isNullOrBlank()) {
                defaultPresetColors
            } else {
                colorsString.split(",").filter { it.isNotBlank() }
            }
        }

    suspend fun getTagPresetColors(): ImmutableList<String> = tagPresetColors.first().toImmutableList()

    suspend fun getTagPresetColorsAsColor(): ImmutableList<Color> =
        getTagPresetColors().map { colorMapper.fromStringToColor(it) }.toImmutableList()

    suspend fun setTagPresetColors(colors: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[tagPresetColorsKey] = colors.joinToString(",")
        }
    }

    suspend fun addTagPresetColor(hexColor: String) {
        val current = getTagPresetColors().toMutableList()
        if (hexColor !in current) {
            current.add(hexColor)
            setTagPresetColors(current)
        }
    }

    suspend fun addTagPresetColor(color: Color) {
        addTagPresetColor(colorMapper.fromColorToString(color))
    }

    suspend fun removeTagPresetColor(hexColor: String) {
        val current = getTagPresetColors().toMutableList()
        current.remove(hexColor)
        setTagPresetColors(current)
    }

    private val themeSettingKey = stringPreferencesKey(THEME_SETTINGS)
    val themeSettings: Flow<ThemeSettings> = context.dataStore.data
        .map { preferences ->
            ThemeSettings.fromValue(preferences[themeSettingKey]) ?: ThemeSettings.default()
        }

    private val userIdKey = longPreferencesKey(USER_ID)
    val userId: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[userIdKey]
        }

    suspend fun requireUserId(): Long = userId.first() ?: error("User not logged in")
    suspend fun userIdOrNull(): Long? = userId.first()

    suspend fun setUserId(value: Long) {
        context.dataStore.edit { prefs ->
            prefs[userIdKey] = value
        }
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

    suspend fun getCurrentProjectId(): Long = currentProjectIdFlow.first()

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
}
