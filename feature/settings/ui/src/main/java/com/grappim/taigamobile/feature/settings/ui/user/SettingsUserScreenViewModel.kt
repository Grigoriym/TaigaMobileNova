package com.grappim.taigamobile.feature.settings.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.storage.server.ServerStorage
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsUserScreenViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    serverStorage: ServerStorage
) : ViewModel() {

    private val _state = MutableStateFlow(
        SettingsUserScreenState(
            serverUrl = serverStorage.server
        )
    )
    val state = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = NativeText.Empty) }

            resultOf { usersRepository.getMe() }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            user = result,
                            isLoading = false
                        )
                    }
                }.onFailure { e ->
                    Timber.e(e)
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = getErrorMessage(e)
                        )
                    }
                }
        }
    }
}
