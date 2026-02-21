package com.grappim.taigamobile.core.storage

import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.grappim.taigamobile.utils.ui.ColorMapper
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TaigaSessionStorage(private val dataStore: DataStore<Preferences>, private val colorMapper: ColorMapper) {
    companion object {
        private val CURRENT_PROJECT_ID_KEY = longPreferencesKey("current_project_id")
        private val USER_ID_KEY = longPreferencesKey("user_id")
        private val THEME_SETTINGS_KEY = stringPreferencesKey("theme_settings")
        private val KANBAN_DEFAULT_SWIMLINE_KEY = longPreferencesKey("kanban_default_swimline")
        private val TAG_PRESET_COLORS_KEY = stringPreferencesKey("tag_preset_colors")
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

    val kanbanDefaultSwimline: Flow<Long?> = dataStore.data
        .map { prefs -> prefs[KANBAN_DEFAULT_SWIMLINE_KEY] }

    suspend fun setKanbanDefaultSwimline(value: Long) {
        dataStore.edit { prefs ->
            prefs[KANBAN_DEFAULT_SWIMLINE_KEY] = value
        }
    }

    val tagPresetColors: Flow<List<String>> = dataStore.data
        .map { prefs ->
            val colorsString = prefs[TAG_PRESET_COLORS_KEY]
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
        dataStore.edit { prefs ->
            prefs[TAG_PRESET_COLORS_KEY] = colors.joinToString(",")
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

    val themeSettings: Flow<ThemeSettings> = dataStore.data
        .map { prefs ->
            ThemeSettings.fromValue(prefs[THEME_SETTINGS_KEY]) ?: ThemeSettings.default()
        }

    val userId: Flow<Long?> = dataStore.data
        .map { prefs -> prefs[USER_ID_KEY] }

    suspend fun requireUserId(): Long = userId.first() ?: error("User not logged in")
    suspend fun userIdOrNull(): Long? = userId.first()

    suspend fun setUserId(value: Long) {
        dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = value
        }
    }

    suspend fun setThemSetting(themeSettings: ThemeSettings) {
        dataStore.edit { prefs ->
            prefs[THEME_SETTINGS_KEY] = themeSettings.value
        }
    }

    val currentProjectIdFlow: Flow<Long> = dataStore.data
        .map { prefs -> prefs[CURRENT_PROJECT_ID_KEY] ?: -1 }

    suspend fun getCurrentProjectId(): Long = currentProjectIdFlow.first()

    suspend fun setCurrentProjectId(projectId: Long) {
        dataStore.edit { prefs ->
            prefs[CURRENT_PROJECT_ID_KEY] = projectId
        }
    }

    suspend fun clearData() {
        dataStore.edit { it.clear() }
    }
}
