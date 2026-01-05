package com.grappim.taigamobile.feature.sprint.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.projects.domain.ProjectsRepository
import com.grappim.taigamobile.feature.projects.domain.canAddIssue
import com.grappim.taigamobile.feature.projects.domain.canAddTask
import com.grappim.taigamobile.feature.projects.domain.canDeleteMilestone
import com.grappim.taigamobile.feature.projects.domain.canModifyMilestone
import com.grappim.taigamobile.feature.sprint.domain.SprintsRepository
import com.grappim.taigamobile.feature.workitem.ui.delegates.sprint.WorkItemSprintDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.sprint.WorkItemSprintDelegateImpl
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeUtils
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class SprintViewModel @Inject constructor(
    private val sprintsRepository: SprintsRepository,
    private val projectsRepository: ProjectsRepository,
    private val dateTimeUtils: DateTimeUtils,
    savedStateHandle: SavedStateHandle
) : ViewModel(),
    WorkItemSprintDelegate by WorkItemSprintDelegateImpl(
        dateTimeUtils = dateTimeUtils,
        sprintsRepository = sprintsRepository
    ) {

    private val _state = MutableStateFlow(
        SprintState(
            setIsMenuExpanded = ::setIsMenuExpanded,
            setIsDeleteDialogVisible = ::setIsDeleteDialogVisible,
            onDeleteSprint = ::deleteSprint,
            onEditSprintClick = ::onEditSprintClick,
            onEditSprintConfirm = ::editSprint
        )
    )
    val state = _state.asStateFlow()

    private val route = savedStateHandle.toRoute<SprintNavDestination>()

    private val sprintId: Long = route.sprintId
    private val _deleteResult = Channel<Unit>()
    val deleteResult = _deleteResult.receiveAsFlow()

    private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

    init {
        getPermissions()
        loadData()
    }

    private fun getPermissions() {
        viewModelScope.launch {
            val perm = projectsRepository.getPermissions()
            val canEdit = perm.canModifyMilestone()
            val canDelete = perm.canDeleteMilestone()
            val canShowTopBarActions = canEdit || canDelete
            _state.update {
                it.copy(
                    canEdit = canEdit,
                    canDelete = canDelete,
                    canShowTopBarActions = canShowTopBarActions,
                    canCreateIssue = perm.canAddIssue(),
                    canCreateTasks = perm.canAddTask()
                )
            }
        }
    }

    private fun setIsMenuExpanded(isExpanded: Boolean) {
        _state.update {
            it.copy(isMenuExpanded = isExpanded)
        }
    }

    private fun setIsDeleteDialogVisible(isVisible: Boolean) {
        _state.update {
            it.copy(isDeleteDialogVisible = isVisible)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
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
    }

    private fun editSprint() {
        viewModelScope.launch {
            editSprint(
                sprintId = sprintId,
                doOnPreExecute = {
                    _state.update {
                        it.copy(
                            isLoading = true
                        )
                    }
                },
                doOnSuccess = {
                    _state.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                    loadData()
                },
                doOnError = {
                    _state.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    private fun onEditSprintClick() {
        setInitialSprint(
            start = _state.value.sprint?.start,
            end = _state.value.sprint?.end,
            sprintName = _state.value.sprint?.name ?: ""
        )
        setSprintDialogVisibility(true)
    }

    private fun deleteSprint() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = NativeText.Empty
                )
            }

            resultOf {
                sprintsRepository.deleteSprint(sprintId)
            }.onSuccess {
                _state.update {
                    it.copy(
                        isLoading = false
                    )
                }
                _deleteResult.send(Unit)
            }.onFailure { error ->
                Timber.e(error)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = getErrorMessage(error)
                    )
                }
            }
        }
    }
}
