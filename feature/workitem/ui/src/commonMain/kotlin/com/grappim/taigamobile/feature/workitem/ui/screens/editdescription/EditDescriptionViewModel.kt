package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.feature.users.domain.UsersRepository
import com.grappim.taigamobile.feature.workitem.ui.delegates.mentions.WorkItemMentionsDelegate
import com.grappim.taigamobile.feature.workitem.ui.delegates.mentions.WorkItemMentionsDelegateImpl
import com.grappim.taigamobile.feature.workitem.ui.screens.WorkItemEditStateRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class EditDescriptionViewModel(
    @InjectedParam private val route: WorkItemEditDescriptionNavDestination,
    private val workItemEditStateRepository: WorkItemEditStateRepository,
    usersRepository: UsersRepository
) : ViewModel(),
    WorkItemMentionsDelegate by WorkItemMentionsDelegateImpl(usersRepository = usersRepository) {
    private val _state = MutableStateFlow(
        EditDescriptionState(
            originalDescription = route.description,
            currentDescription = TextFieldValue(route.description),
            onDescriptionChange = ::onDescriptionChange,
            setIsDialogVisible = ::setIsDialogVisible,
            shouldGoBackWithCurrentValue = ::onGoingBack
        )
    )
    val state = _state.asStateFlow()

    private val _onBackAction = Channel<Unit>()
    val onBackAction = _onBackAction.receiveAsFlow()

    init {
        viewModelScope.launch { loadMembers() }
    }

    private fun onGoingBack(shouldReturnCurrentValue: Boolean) {
        viewModelScope.launch {
            setIsDialogVisible(false)
            val wasDescriptionChanged =
                _state.value.currentDescription.text != _state.value.originalDescription
            if (shouldReturnCurrentValue && wasDescriptionChanged) {
                workItemEditStateRepository.updateDescription(
                    workItemId = route.workItemId,
                    type = route.taskIdentifier,
                    description = _state.value.currentDescription.text
                )
            }
            _onBackAction.send(Unit)
        }
    }

    private fun onDescriptionChange(newValue: TextFieldValue) {
        _state.update {
            it.copy(currentDescription = newValue)
        }
    }

    private fun setIsDialogVisible(newValue: Boolean) {
        _state.update {
            it.copy(isDialogVisible = newValue)
        }
    }
}
