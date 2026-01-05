package com.grappim.taigamobile.core.storage

import android.content.Context
import androidx.core.content.edit
import com.grappim.taigamobile.core.storage.di.StorageJsonQualifier
import com.grappim.taigamobile.feature.filters.domain.model.filters.FiltersData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Global app state
 */
@Singleton
class Session @Inject constructor(
    @ApplicationContext private val context: Context,
    @StorageJsonQualifier private val json: Json
) {

    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    // Filters
    private fun getFiltersOrEmpty(key: String) = sharedPreferences.getString(key, null)?.takeIf { it.isNotBlank() }
        ?.let { json.decodeFromString(it) } ?: FiltersData()

    private val _scrumFilters = MutableStateFlow(getFiltersOrEmpty(FILTERS_SCRUM))
    val scrumFilters: StateFlow<FiltersData> = _scrumFilters
    fun changeScrumFilters(filters: FiltersData) {
        sharedPreferences.edit {
            putString(FILTERS_SCRUM, json.encodeToString(filters))
        }
        _scrumFilters.value = filters
    }

    private val _epicsFilters = MutableStateFlow(getFiltersOrEmpty(FILTERS_EPICS))
    val epicsFilters: StateFlow<FiltersData> = _epicsFilters
    fun changeEpicsFilters(filters: FiltersData) {
        sharedPreferences.edit {
            putString(FILTERS_EPICS, json.encodeToString(filters))
        }
        _epicsFilters.value = filters
    }

    private val _issuesFilters = MutableStateFlow(getFiltersOrEmpty(FILTERS_ISSUES))
    val issuesFilters: StateFlow<FiltersData> = _issuesFilters
    fun changeIssuesFilters(filters: FiltersData) {
        sharedPreferences.edit {
            putString(FILTERS_ISSUES, json.encodeToString(filters))
        }
        _issuesFilters.value = filters
    }

    fun resetFilters() {
        changeScrumFilters(FiltersData())
        changeEpicsFilters(FiltersData())
        changeIssuesFilters(FiltersData())
    }

    fun reset() {
        sharedPreferences.edit { clear() }
    }

    companion object {
        private const val PREFERENCES_NAME = "session"

        private const val FILTERS_SCRUM = "filters_scrum"
        private const val FILTERS_EPICS = "filters_epics"
        private const val FILTERS_ISSUES = "filters_issues"
    }
}
