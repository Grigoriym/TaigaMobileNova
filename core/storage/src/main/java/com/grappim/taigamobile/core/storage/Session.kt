package com.grappim.taigamobile.core.storage

import android.content.Context
import androidx.core.content.edit
import com.grappim.taigamobile.core.domain.FiltersData
import com.grappim.taigamobile.core.domain.FiltersDataJsonAdapter
import com.grappim.taigamobile.core.storage.utils.long
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Global app state
 */
@Singleton
class Session @Inject constructor(@ApplicationContext private val context: Context, moshi: Moshi) {

    private val sharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _refreshToken =
        MutableStateFlow(sharedPreferences.getString(REFRESH_TOKEN_KEY, "").orEmpty())
    private val _token = MutableStateFlow(sharedPreferences.getString(TOKEN_KEY, "").orEmpty())

    var userId: Long by sharedPreferences.long(USER_ID_KEY, -1)

    val refreshToken: StateFlow<String> = _refreshToken
    val token: StateFlow<String> = _token

    fun changeAuthCredentials(token: String, refreshToken: String) {
        sharedPreferences.edit {
            putString(TOKEN_KEY, token)
            putString(REFRESH_TOKEN_KEY, refreshToken)
        }
        _token.value = token
        _refreshToken.value = refreshToken
    }

    fun changeCurrentUserId(value: Long) {
        userId = value
    }

    private val _currentProjectName =
        MutableStateFlow(sharedPreferences.getString(PROJECT_NAME_KEY, "").orEmpty())

    val currentProjectName: StateFlow<String> = _currentProjectName

    fun changeCurrentProjectName(name: String) {
        sharedPreferences.edit {
            putString(PROJECT_NAME_KEY, name)
        }
        _currentProjectName.value = name

        resetFilters()
    }

    private fun checkLogged(token: String, refresh: String) =
        listOf(token, refresh).all { it.isNotEmpty() }

    @Deprecated("find another way of checking that")
    val isLogged = combine(token, refreshToken, ::checkLogged)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            initialValue = checkLogged(token.value, refreshToken.value)
        )

//    private fun checkProjectSelected(id: Long) = id >= 0
//    val isProjectSelected = currentProjectId.map(::checkProjectSelected)
//        .stateIn(
//            scope,
//            SharingStarted.Eagerly,
//            initialValue = checkProjectSelected(currentProjectId.value)
//        )

    // Filters
    private val filtersJsonAdapter = FiltersDataJsonAdapter(moshi)
    private fun getFiltersOrEmpty(key: String) =
        sharedPreferences.getString(key, null)?.takeIf { it.isNotBlank() }
            ?.let { filtersJsonAdapter.fromJson(it) } ?: FiltersData()

    private val _scrumFilters = MutableStateFlow(getFiltersOrEmpty(FILTERS_SCRUM))
    val scrumFilters: StateFlow<FiltersData> = _scrumFilters
    fun changeScrumFilters(filters: FiltersData) {
        sharedPreferences.edit {
            putString(FILTERS_SCRUM, filtersJsonAdapter.toJson(filters))
        }
        _scrumFilters.value = filters
    }

    private val _epicsFilters = MutableStateFlow(getFiltersOrEmpty(FILTERS_EPICS))
    val epicsFilters: StateFlow<FiltersData> = _epicsFilters
    fun changeEpicsFilters(filters: FiltersData) {
        sharedPreferences.edit {
            putString(FILTERS_EPICS, filtersJsonAdapter.toJson(filters))
        }
        _epicsFilters.value = filters
    }

    private val _issuesFilters = MutableStateFlow(getFiltersOrEmpty(FILTERS_ISSUES))
    val issuesFilters: StateFlow<FiltersData> = _issuesFilters
    fun changeIssuesFilters(filters: FiltersData) {
        sharedPreferences.edit {
            putString(FILTERS_ISSUES, filtersJsonAdapter.toJson(filters))
        }
        _issuesFilters.value = filters
    }

    private fun resetFilters() {
        changeScrumFilters(FiltersData())
        changeEpicsFilters(FiltersData())
        changeIssuesFilters(FiltersData())
    }

    fun reset() {
        sharedPreferences.edit { clear() }

        // TODO remove Flows
        _currentProjectName.value = ""
        _refreshToken.value = ""
        _token.value = ""
    }

    companion object {
        private const val PREFERENCES_NAME = "session"
        private const val TOKEN_KEY = "token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val PROJECT_NAME_KEY = "project_name"
        private const val USER_ID_KEY = "user_id"

        private const val FILTERS_SCRUM = "filters_scrum"
        private const val FILTERS_EPICS = "filters_epics"
        private const val FILTERS_ISSUES = "filters_issues"
    }

    // Events (no data, just dispatch update to subscribers)

    val taskEdit = eventFlow() // some task was edited
    val sprintEdit = eventFlow() // sprint was edited
}

/**
 * An empty class which describes basic event without any data (for the sake of update only)
 */
class Event

fun eventFlow() = MutableSharedFlow<Event>()

suspend fun MutableSharedFlow<Event>.postUpdate() = emit(Event())
fun MutableSharedFlow<Event>.tryPostUpdate() = tryEmit(Event())

fun CoroutineScope.subscribeToAll(vararg flows: Flow<*>, action: () -> Unit) {
    flows.forEach {
        launch {
            it.collect { action() }
        }
    }
}
