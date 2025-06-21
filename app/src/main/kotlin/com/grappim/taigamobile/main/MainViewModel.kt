package com.grappim.taigamobile.main

import androidx.lifecycle.ViewModel
import com.grappim.taigamobile.core.storage.AuthStateManager
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val session: Session,
    private val settings: Settings,
    authStateManager: AuthStateManager
) : ViewModel() {

    val logoutEvent = authStateManager.logoutEvents

    val isLogged by lazy { session.isLogged }

    val theme by lazy { settings.themeSetting }
}
