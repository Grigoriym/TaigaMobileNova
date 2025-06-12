package io.eugenethedev.taigamobile.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eugenethedev.taigamobile.state.Session
import io.eugenethedev.taigamobile.state.Settings
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val session: Session,
    private val settings: Settings
) : ViewModel() {
    val isLogged by lazy { session.isLogged }
    val isProjectSelected by lazy { session.isProjectSelected }

    val theme by lazy { settings.themeSetting }
}
