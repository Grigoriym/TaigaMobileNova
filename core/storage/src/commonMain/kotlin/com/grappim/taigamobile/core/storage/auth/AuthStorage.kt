package com.grappim.taigamobile.core.storage.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthStorage(private val dataStore: DataStore<Preferences>) {

    private val tokenFlow = dataStore.data.map { prefs ->
        prefs[TOKEN_KEY].orEmpty()
    }

    private val refreshTokenFlow = dataStore.data.map { prefs ->
        prefs[REFRESH_TOKEN_KEY].orEmpty()
    }

    suspend fun getToken(): String = tokenFlow.first()
    suspend fun getRefreshToken(): String = refreshTokenFlow.first()

    val isLoggedIn = combine(tokenFlow, refreshTokenFlow) { token, refresh ->
        token.isNotEmpty() && refresh.isNotEmpty()
    }

    suspend fun setAuthCredentials(token: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[REFRESH_TOKEN_KEY] = refreshToken
        }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }
}
