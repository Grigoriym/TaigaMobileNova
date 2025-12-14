package com.grappim.taigamobile.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.ProjectDTO
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.TaigaStorage
import com.grappim.taigamobile.feature.dashboard.domain.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val session: Session,
    private val taigaStorage: TaigaStorage,
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                taigaStorage.currentProjectIdFlow.distinctUntilChanged().collect { id ->
                    dashboardRepository.getData()
                        .onSuccess { result ->
                            _state.update {
                                it.copy(
                                    currentProjectId = id,
                                    workingOn = result.workingOn,
                                    watching = result.watching,
                                    myProjectDTOS = result.myProjectDTOS,
                                    isLoading = false,
                                    isError = false
                                )
                            }
                        }.onFailure {
                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    isError = true
                                )
                            }
                        }
                }
            }
            launch {
                session.taskEdit.onEach {
                    _state.update {
                        it.copy(
                            workingOn = emptyList(),
                            watching = emptyList(),
                            myProjectDTOS = emptyList()

                        )
                    }
                }.launchIn(viewModelScope)
            }
        }
    }

    @Deprecated("let's temporarily turn it off since it can break stuff")
    fun changeCurrentProject(projectDTO: ProjectDTO) {
        viewModelScope.launch {
            projectDTO.apply {
                session.changeCurrentProjectName(name)
                taigaStorage.setCurrentProjectId(id)
            }
        }
    }
}
