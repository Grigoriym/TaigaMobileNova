package com.grappim.taigamobile.feature.settings.ui.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.utils.ui.SnackbarDelegate
import com.grappim.taigamobile.utils.ui.SnackbarDelegateImpl
import com.grappim.taigamobile.utils.ui.getErrorMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ModulesViewModel(private val projectsRepository: ProjectsRepository) :
    ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl() {

    private val _state = MutableStateFlow(
        ModulesState(
            onEpicsActivatedChange = ::onEpicsActivatedChange,
            onBacklogActivatedChange = ::onBacklogActivatedChange,
            onKanbanActivatedChange = ::onKanbanActivatedChange,
            onIssuesActivatedChange = ::onIssuesActivatedChange,
            onWikiActivatedChange = ::onWikiActivatedChange,
            onTotalMilestonesChange = ::onTotalMilestonesChange,
            onTotalStoryPointsChange = ::onTotalStoryPointsChange,
            onSaveClick = ::save
        )
    )
    val state = _state.asStateFlow()

    private val _navigateBack = Channel<Unit>()
    val navigateBack = _navigateBack.receiveAsFlow()

    init {
        loadModules()
    }

    private fun loadModules() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            resultOf {
                projectsRepository.getProjectModules()
            }.onSuccess { modules ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        isEpicsActivated = modules.isEpicsActivated,
                        isBacklogActivated = modules.isBacklogActivated,
                        isKanbanActivated = modules.isKanbanActivated,
                        isIssuesActivated = modules.isIssuesActivated,
                        isWikiActivated = modules.isWikiActivated,
                        totalMilestones = modules.totalMilestones?.toString() ?: "",
                        totalStoryPoints = modules.totalStoryPoints?.toString() ?: ""
                    )
                }
            }.onFailure { error ->
                logcat(throwable = error) { "Error loading modules" }
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = getErrorMessage(error)
                    )
                }
            }
        }
    }

    private fun save() {
        viewModelScope.launch {
            val current = _state.value
            _state.update { it.copy(isSaving = true) }
            resultOf {
                projectsRepository.updateModules(
                    isEpicsActivated = current.isEpicsActivated,
                    isBacklogActivated = current.isBacklogActivated,
                    isKanbanActivated = current.isKanbanActivated,
                    isIssuesActivated = current.isIssuesActivated,
                    isWikiActivated = current.isWikiActivated,
                    totalMilestones = current.totalMilestones.toIntOrNull(),
                    totalStoryPoints = current.totalStoryPoints.toDoubleOrNull()
                )
            }.onSuccess {
                _state.update { it.copy(isSaving = false) }
                _navigateBack.send(Unit)
            }.onFailure { error ->
                logcat(throwable = error) { "Error saving modules" }
                showSnackbarSuspend(getErrorMessage(error))
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun onEpicsActivatedChange(value: Boolean) {
        _state.update { it.copy(isEpicsActivated = value) }
    }

    private fun onBacklogActivatedChange(value: Boolean) {
        _state.update { it.copy(isBacklogActivated = value) }
    }

    private fun onKanbanActivatedChange(value: Boolean) {
        _state.update { it.copy(isKanbanActivated = value) }
    }

    private fun onIssuesActivatedChange(value: Boolean) {
        _state.update { it.copy(isIssuesActivated = value) }
    }

    private fun onWikiActivatedChange(value: Boolean) {
        _state.update { it.copy(isWikiActivated = value) }
    }

    private fun onTotalMilestonesChange(value: String) {
        _state.update { it.copy(totalMilestones = value) }
    }

    private fun onTotalStoryPointsChange(value: String) {
        _state.update { it.copy(totalStoryPoints = value) }
    }
}
