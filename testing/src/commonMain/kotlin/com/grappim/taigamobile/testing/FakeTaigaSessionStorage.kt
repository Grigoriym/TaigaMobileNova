package com.grappim.taigamobile.testing

import androidx.compose.ui.graphics.Color
import com.grappim.taigamobile.core.storage.TaigaSessionStorage
import com.grappim.taigamobile.core.storage.ThemeSettings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeTaigaSessionStorage(
    var currentProjectId: Long = -1L,
    var currentUserId: Long? = null,
) : TaigaSessionStorage {

    override val kanbanDefaultSwimline: Flow<Long?> = flowOf(null)
    override val tagPresetColors: Flow<List<String>> = flowOf(emptyList())
    override val themeSettings: Flow<ThemeSettings> = flowOf(ThemeSettings.default())
    override val userId: Flow<Long?> = flowOf(currentUserId)
    override val currentProjectIdFlow: Flow<Long> = flowOf(currentProjectId)

    override suspend fun setKanbanDefaultSwimline(value: Long): Unit = error("not used in this test")
    override suspend fun getTagPresetColors(): ImmutableList<String> = persistentListOf()
    override suspend fun getTagPresetColorsAsColor(): ImmutableList<Color> = persistentListOf()
    override suspend fun setTagPresetColors(colors: List<String>): Unit = error("not used in this test")
    override suspend fun addTagPresetColor(hexColor: String): Unit = error("not used in this test")
    override suspend fun addTagPresetColor(color: Color): Unit = error("not used in this test")
    override suspend fun removeTagPresetColor(hexColor: String): Unit = error("not used in this test")
    override suspend fun requireUserId(): Long = currentUserId ?: error("User not logged in")
    override suspend fun userIdOrNull(): Long? = currentUserId
    override suspend fun setUserId(value: Long) { currentUserId = value }
    override suspend fun setThemSetting(themeSettings: ThemeSettings): Unit = error("not used in this test")
    override suspend fun getCurrentProjectId(): Long = currentProjectId
    override suspend fun setCurrentProjectId(projectId: Long) { currentProjectId = projectId }
    override suspend fun clearData() {}
}
