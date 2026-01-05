package com.grappim.taigamobile.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthStorage @Inject constructor(@ApplicationContext private val context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _token = MutableStateFlow(sharedPreferences.getString(TOKEN_KEY, "").orEmpty())
    private val _refreshToken = MutableStateFlow(sharedPreferences.getString(REFRESH_TOKEN_KEY, "").orEmpty())

    val token: String get() = _token.value
    val refreshToken: String get() = _refreshToken.value

    val isLoggedIn: StateFlow<Boolean> = combine(_token, _refreshToken, ::checkLogged)
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = checkLogged(_token.value, _refreshToken.value)
        )

    fun setAuthCredentials(token: String, refreshToken: String) {
        sharedPreferences.edit {
            putString(TOKEN_KEY, token)
            putString(REFRESH_TOKEN_KEY, refreshToken)
        }
        _token.value = token
        _refreshToken.value = refreshToken
    }

    fun clear() {
        sharedPreferences.edit { clear() }
        _token.value = ""
        _refreshToken.value = ""
    }

    private fun checkLogged(token: String, refresh: String) = listOf(token, refresh).all { it.isNotEmpty() }

    companion object {
        private const val PREFERENCES_NAME = "auth_storage"
        private const val TOKEN_KEY = "token"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
    }
}
