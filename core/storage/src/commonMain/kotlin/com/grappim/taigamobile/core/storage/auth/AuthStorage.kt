package com.grappim.taigamobile.core.storage.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface AuthStorage {
    val isLoggedIn: Flow<Boolean>
    suspend fun getToken(): String
    suspend fun getRefreshToken(): String
    suspend fun setAuthCredentials(token: String, refreshToken: String)
    suspend fun clear()
}

class AuthStorageImpl(private val dataStore: DataStore<Preferences>, private val tokenCipher: TokenCipher) :
    AuthStorage {

    private val tokenFlow = dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]?.let(tokenCipher::decrypt).orEmpty()
    }

    private val refreshTokenFlow = dataStore.data.map { prefs ->
        prefs[REFRESH_TOKEN_KEY]?.let(tokenCipher::decrypt).orEmpty()
    }

    override suspend fun getToken(): String = tokenFlow.first()
    override suspend fun getRefreshToken(): String = refreshTokenFlow.first()

    override val isLoggedIn: Flow<Boolean> = combine(tokenFlow, refreshTokenFlow) { token, refresh ->
        token.isNotEmpty() && refresh.isNotEmpty()
    }

    override suspend fun setAuthCredentials(token: String, refreshToken: String) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = tokenCipher.encrypt(token)
            prefs[REFRESH_TOKEN_KEY] = tokenCipher.encrypt(refreshToken)
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }
}
