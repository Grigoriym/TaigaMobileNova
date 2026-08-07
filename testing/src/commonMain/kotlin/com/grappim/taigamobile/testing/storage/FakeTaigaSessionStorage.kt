package com.grappim.taigamobile.testing.storage

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.ThemeSettings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeTaigaSessionStorage(
    var currentProjectId: Long = -1L,
    var currentUserId: Long? = null,
    themeSettings: ThemeSettings = ThemeSettings.default(),
    crashReportingEnabled: Boolean = true,
) : TaigaSessionStorage {

    override val kanbanDefaultSwimline: Flow<Long?> = flowOf(null)
    override val tagPresetColors: Flow<List<String>> = flowOf(emptyList())
    override val themeSettings: Flow<ThemeSettings> = flowOf(themeSettings)
    override val userId: Flow<Long?> = flowOf(currentUserId)
    private val _currentProjectIdFlow = MutableStateFlow(currentProjectId)
    override val currentProjectIdFlow: Flow<Long> = _currentProjectIdFlow.asStateFlow()
    override val crashReportingEnabled: Flow<Boolean> = flowOf(crashReportingEnabled)

    var setKanbanDefaultSwimlineCalls: MutableList<Long> = mutableListOf()
    override suspend fun setKanbanDefaultSwimline(value: Long) {
        setKanbanDefaultSwimlineCalls.add(value)
    }
    var presetColorsResult: ImmutableList<String> = persistentListOf()
    override suspend fun getPresetColors(): ImmutableList<String> = presetColorsResult
    var tagPresetColorsResult: ImmutableList<Color> = persistentListOf()
    override suspend fun getPresetColorsAsColor(): ImmutableList<Color> = tagPresetColorsResult
    override suspend fun setTagPresetColors(colors: List<String>): Unit = error("not used in this test")
    override suspend fun addTagPresetColor(hexColor: String): Unit = error("not used in this test")
    override suspend fun addTagPresetColor(color: Color): Unit = error("not used in this test")
    override suspend fun removeTagPresetColor(hexColor: String): Unit = error("not used in this test")
    override suspend fun requireUserId(): Long = currentUserId ?: error("User not logged in")
    override suspend fun userIdOrNull(): Long? = currentUserId
    override suspend fun setUserId(value: Long) { currentUserId = value }
    override suspend fun setThemSetting(themeSettings: ThemeSettings): Unit = error("not used in this test")
    override suspend fun getCurrentProjectId(): Long = currentProjectId
    override suspend fun setCurrentProjectId(projectId: Long) {
        currentProjectId = projectId
        _currentProjectIdFlow.value = projectId
    }
    var setCrashReportingEnabledCalls: MutableList<Boolean> = mutableListOf()
    override suspend fun setCrashReportingEnabled(enabled: Boolean) {
        setCrashReportingEnabledCalls.add(enabled)
    }

    var clearDataCalled = false

    override suspend fun clearData() {
        clearDataCalled = true
    }
}
