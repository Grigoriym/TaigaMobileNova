package com.grappim.taigamobile.feature.sprint.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.storage.Session
import com.grappim.taigamobile.core.storage.postUpdate
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.loadOrError
import com.grappim.taigamobile.utils.ui.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class SprintViewModel @Inject constructor(
    private val sprintsRepository: SprintsRepository,
    private val session: Session,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(
        SprintState(
            setIsMenuExpanded = ::setIsMenuExpanded,
            setIsEditDialogVisible = ::setIsEditDialogVisible,
            setIsDeleteDialogVisible = ::setIsDeleteDialogVisible
        )
    )
    val state = _state.asStateFlow()

    private val route = savedStateHandle.toRoute<SprintNavDestination>()

    private val sprintId: Long = route.sprintId

    val editResult = mutableResultFlow<Unit>()
    val deleteResult = mutableResultFlow<Unit>()

    private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    init {
        session.taskEdit.onEach {
            loadData()
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            loadData()
        }
    }

    private fun setIsMenuExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isMenuExpanded = isExpanded)
        }
    }

    private fun setIsEditDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isEditDialogVisible = isVisible)
        }
    }

    private fun setIsDeleteDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isDeleteDialogVisible = isVisible)
        }
    }

    private suspend fun loadData() {
        _state.update { it.copy(isLoading = true) }

        sprintsRepository.getSprintData(sprintId)
            .onSuccess { result ->
                _state.update {
                    it.copy(
                        sprint = result.sprint,
                        sprintToolbarTitle = NativeText.Simple(result.sprint.name),
                        sprintToolbarSubtitle = NativeText.Arguments(
                            id = RString.sprint_dates_template,
                            args = listOf(
                                result.sprint.start.format(dateFormatter),
                                result.sprint.end.format(dateFormatter)
                            )
                        ),
                        isLoading = false,
                        statuses = result.statuses,
                        storiesWithTasks = result.storiesWithTasks,
                        storylessTasks = result.storylessTasks,
                        issues = result.issues
                    )
                }
            }.onFailure {
                _state.update { it.copy(isLoading = false) }
            }
    }

    fun editSprint(name: String, start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            editResult.loadOrError(RString.permission_error) {
                sprintsRepository.editSprint(sprintId, name, start, end)
                session.sprintEdit.postUpdate()
                loadData()
            }
        }
    }

    fun deleteSprint() = viewModelScope.launch {
        deleteResult.loadOrError(RString.permission_error) {
            sprintsRepository.deleteSprint(sprintId)
            session.sprintEdit.postUpdate()
        }
    }
}
