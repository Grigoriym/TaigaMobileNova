package com.grappim.taigamobile.feature.dashboard.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.Project
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
                                    myProjects = result.myProjects,
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
                            myProjects = emptyList()

                        )
                    }
                }.launchIn(viewModelScope)
            }
        }
    }

    fun changeCurrentProject(project: Project) {
        viewModelScope.launch {
            project.apply {
                session.changeCurrentProjectName(name)
                taigaStorage.setCurrentProjectId(id)
            }
        }
    }
}
