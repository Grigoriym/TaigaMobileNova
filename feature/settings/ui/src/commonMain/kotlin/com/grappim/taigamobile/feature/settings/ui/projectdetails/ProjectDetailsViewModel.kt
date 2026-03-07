package com.grappim.taigamobile.feature.settings.ui.projectdetails

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
class ProjectDetailsViewModel(private val projectsRepository: ProjectsRepository) :
    ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl() {

    private val _state = MutableStateFlow(
        ProjectDetailsState(
            onNameChange = ::onNameChange,
            onDescriptionChange = ::onDescriptionChange,
            onIsPrivateChange = ::onIsPrivateChange,
            onIsLookingForPeopleChange = ::onIsLookingForPeopleChange,
            onLookingForPeopleNoteChange = ::onLookingForPeopleNoteChange,
            onIsContactActivatedChange = ::onIsContactActivatedChange,
            onSaveClick = ::save
        )
    )
    val state = _state.asStateFlow()

    private val _navigateBack = Channel<Unit>()
    val navigateBack = _navigateBack.receiveAsFlow()

    init {
        loadProjectDetails()
    }

    private fun loadProjectDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            resultOf {
                projectsRepository.getProjectDetails()
            }.onSuccess { details ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        name = details.name,
                        description = details.description,
                        isPrivate = details.isPrivate,
                        isLookingForPeople = details.isLookingForPeople,
                        lookingForPeopleNote = details.lookingForPeopleNote,
                        isContactActivated = details.isContactActivated
                    )
                }
            }.onFailure { error ->
                logcat(throwable = error) { "Error loading project details" }
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
                projectsRepository.updateProject(
                    name = current.name,
                    description = current.description,
                    isPrivate = current.isPrivate,
                    isLookingForPeople = current.isLookingForPeople,
                    lookingForPeopleNote = current.lookingForPeopleNote,
                    isContactActivated = current.isContactActivated
                )
            }.onSuccess {
                _state.update { it.copy(isSaving = false) }
                _navigateBack.send(Unit)
            }.onFailure { error ->
                logcat(throwable = error) { "Error saving project details" }
                showSnackbarSuspend(getErrorMessage(error))
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun onNameChange(name: String) {
        _state.update { it.copy(name = name) }
    }

    private fun onDescriptionChange(desc: String) {
        _state.update { it.copy(description = desc) }
    }

    private fun onIsPrivateChange(value: Boolean) {
        _state.update { it.copy(isPrivate = value) }
    }

    private fun onIsLookingForPeopleChange(value: Boolean) {
        _state.update { it.copy(isLookingForPeople = value) }
    }

    private fun onLookingForPeopleNoteChange(note: String) {
        _state.update { it.copy(lookingForPeopleNote = note) }
    }

    private fun onIsContactActivatedChange(value: Boolean) {
        _state.update { it.copy(isContactActivated = value) }
    }
}
