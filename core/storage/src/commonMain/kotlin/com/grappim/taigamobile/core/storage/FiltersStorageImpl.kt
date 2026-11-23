package com.grappim.taigamobile.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.Preferences.Key
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.grappim.taigamobile.core.storage.di.StorageJsonQualifier
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class FiltersStorageImpl(
    private val dataStore: DataStore<Preferences>,
    @param:StorageJsonQualifier private val json: Json
) : FiltersStorage {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        private val FILTERS_SCRUM_KEY = stringPreferencesKey("filters_scrum")
        private val FILTERS_EPICS_KEY = stringPreferencesKey("filters_epics")
        private val FILTERS_ISSUES_KEY = stringPreferencesKey("filters_issues")
        private val FILTERS_KANBAN_KEY = stringPreferencesKey("filters_kanban")
    }

    private fun decodeFilters(value: String?): FiltersData =
        value?.takeIf { it.isNotBlank() }?.let { json.decodeFromString(it) } ?: FiltersData()

    private fun filtersFlow(key: Key<String>): Flow<FiltersData> = dataStore.data
        .map { prefs -> decodeFilters(prefs[key]) }
        .distinctUntilChanged()

    private suspend fun refreshFilters(key: Key<String>, serverData: FiltersData) {
        dataStore.edit { prefs ->
            prefs[key] = json.encodeToString(decodeFilters(prefs[key]).updateData(serverData))
        }
    }

    override val scrumFilters: Flow<FiltersData> = filtersFlow(FILTERS_SCRUM_KEY)

    override fun changeScrumFilters(filters: FiltersData) {
        scope.launch {
            dataStore.edit { prefs -> prefs[FILTERS_SCRUM_KEY] = json.encodeToString(filters) }
        }
    }

    override suspend fun refreshScrumFilters(serverData: FiltersData) = refreshFilters(FILTERS_SCRUM_KEY, serverData)

    override val epicsFilters: Flow<FiltersData> = filtersFlow(FILTERS_EPICS_KEY)

    override fun changeEpicsFilters(filters: FiltersData) {
        scope.launch {
            dataStore.edit { prefs -> prefs[FILTERS_EPICS_KEY] = json.encodeToString(filters) }
        }
    }

    override suspend fun refreshEpicsFilters(serverData: FiltersData) = refreshFilters(FILTERS_EPICS_KEY, serverData)

    override val issuesFilters: Flow<FiltersData> = filtersFlow(FILTERS_ISSUES_KEY)

    override fun changeIssuesFilters(filters: FiltersData) {
        scope.launch {
            dataStore.edit { prefs -> prefs[FILTERS_ISSUES_KEY] = json.encodeToString(filters) }
        }
    }

    override suspend fun refreshIssuesFilters(serverData: FiltersData) = refreshFilters(FILTERS_ISSUES_KEY, serverData)

    override val kanbanFilters: Flow<FiltersData> = filtersFlow(FILTERS_KANBAN_KEY)

    override fun changeKanbanFilters(filters: FiltersData) {
        scope.launch {
            dataStore.edit { prefs -> prefs[FILTERS_KANBAN_KEY] = json.encodeToString(filters) }
        }
    }

    override suspend fun resetFilters() {
        dataStore.edit { it.clear() }
    }
}
