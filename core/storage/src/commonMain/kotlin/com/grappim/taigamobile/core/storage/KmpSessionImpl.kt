package com.grappim.taigamobile.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.grappim.taigamobile.core.storage.di.StorageJsonQualifier
import com.grappim.taigamobile.feature.filters.domain.model.FiltersData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class KmpSessionImpl(
    private val dataStore: DataStore<Preferences>,
    @param:StorageJsonQualifier private val json: Json
) : KmpSession {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        private val FILTERS_SCRUM_KEY = stringPreferencesKey("filters_scrum")
        private val FILTERS_EPICS_KEY = stringPreferencesKey("filters_epics")
        private val FILTERS_ISSUES_KEY = stringPreferencesKey("filters_issues")
        private val FILTERS_KANBAN_KEY = stringPreferencesKey("filters_kanban")
    }

    private fun decodeFilters(value: String?): FiltersData =
        value?.takeIf { it.isNotBlank() }?.let { json.decodeFromString(it) } ?: FiltersData()

    override val scrumFilters: StateFlow<FiltersData> = dataStore.data
        .map { prefs -> decodeFilters(prefs[FILTERS_SCRUM_KEY]) }
        .stateIn(scope, SharingStarted.Eagerly, FiltersData())

    override fun changeScrumFilters(filters: FiltersData) {
        scope.launch {
            dataStore.edit { prefs -> prefs[FILTERS_SCRUM_KEY] = json.encodeToString(filters) }
        }
    }

    override val epicsFilters: StateFlow<FiltersData> = dataStore.data
        .map { prefs -> decodeFilters(prefs[FILTERS_EPICS_KEY]) }
        .stateIn(scope, SharingStarted.Eagerly, FiltersData())

    override fun changeEpicsFilters(filters: FiltersData) {
        scope.launch {
            dataStore.edit { prefs -> prefs[FILTERS_EPICS_KEY] = json.encodeToString(filters) }
        }
    }

    override val issuesFilters: StateFlow<FiltersData> = dataStore.data
        .map { prefs -> decodeFilters(prefs[FILTERS_ISSUES_KEY]) }
        .stateIn(scope, SharingStarted.Eagerly, FiltersData())

    override fun changeIssuesFilters(filters: FiltersData) {
        scope.launch {
            dataStore.edit { prefs -> prefs[FILTERS_ISSUES_KEY] = json.encodeToString(filters) }
        }
    }

    override val kanbanFilters: StateFlow<FiltersData> = dataStore.data
        .map { prefs -> decodeFilters(prefs[FILTERS_KANBAN_KEY]) }
        .stateIn(scope, SharingStarted.Eagerly, FiltersData())

    override fun changeKanbanFilters(filters: FiltersData) {
        scope.launch {
            dataStore.edit { prefs -> prefs[FILTERS_KANBAN_KEY] = json.encodeToString(filters) }
        }
    }

    override fun resetFilters() {
        changeScrumFilters(FiltersData())
        changeEpicsFilters(FiltersData())
        changeIssuesFilters(FiltersData())
        changeKanbanFilters(FiltersData())
    }

    override fun reset() {
        scope.launch { dataStore.edit { it.clear() } }
    }
}
