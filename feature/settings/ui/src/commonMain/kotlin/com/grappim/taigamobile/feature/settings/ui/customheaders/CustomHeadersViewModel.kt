package com.grappim.taigamobile.feature.settings.ui.customheaders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.kit.customheaders.CustomHeader
import com.grappim.kit.customheaders.CustomHeaderValidator
import com.grappim.kit.customheaders.CustomHeadersStorage
import com.grappim.kit.domain.resultOf
import com.grappim.kit.logger.LogPriority
import com.grappim.kit.logger.logcat
import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.custom_headers_save_failed
import com.grappim.taigamobile.utils.ui.SnackbarDelegate
import com.grappim.taigamobile.utils.ui.SnackbarDelegateImpl
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

/**
 * A save needs no further action: the HTTP clients read [CustomHeadersStorage.currentHeaders] per
 * request, so the next request carries the new headers.
 */
@KoinViewModel
class CustomHeadersViewModel(
    private val customHeadersStorage: CustomHeadersStorage,
    private val customHeaderValidator: CustomHeaderValidator
) : ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl() {

    private val _state = MutableStateFlow(
        CustomHeadersState(
            onAdd = ::onAdd,
            onRemove = ::onRemove,
            onNameChange = ::onNameChange,
            onValueChange = ::onValueChange,
            onSaveClick = ::onSaveClick
        )
    )
    val state = _state.asStateFlow()

    init {
        customHeadersStorage.headers
            .onEach(::onStoredHeaders)
            .launchIn(viewModelScope)
    }

    /** The rows follow storage only while they still match what was saved — an edit in progress stays put. */
    private fun onStoredHeaders(headers: List<CustomHeader>) {
        _state.update { state ->
            val rows = if (state.headers == state.saved) {
                headers.map { CustomHeaderRow(it.name, it.value) }.toImmutableList()
            } else {
                state.rows
            }
            state.copy(rows = rows, saved = headers.toImmutableList())
        }
    }

    private fun onAdd() {
        updateRows { it + CustomHeaderRow() }
    }

    private fun onRemove(index: Int) {
        updateRows { rows -> rows.filterIndexed { i, _ -> i != index } }
    }

    private fun onNameChange(index: Int, name: String) {
        updateRows { rows -> rows.mapIndexed { i, row -> if (i == index) row.copy(name = name) else row } }
    }

    private fun onValueChange(index: Int, value: String) {
        updateRows { rows -> rows.mapIndexed { i, row -> if (i == index) row.copy(value = value) else row } }
    }

    /** Every edit re-validates the whole list, since a duplicate is a property of two rows, not one. */
    private fun updateRows(transform: (List<CustomHeaderRow>) -> List<CustomHeaderRow>) {
        val rows = transform(_state.value.rows)
        val errors = customHeaderValidator.validate(rows.map { CustomHeader(it.name.trim(), it.value.trim()) })
        _state.update { state ->
            state.copy(rows = rows.mapIndexed { i, row -> row.copy(error = errors[i]) }.toImmutableList())
        }
    }

    private fun onSaveClick() {
        val state = _state.value
        if (!state.isSaveEnabled) return
        val headers = state.headers

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }
            resultOf { customHeadersStorage.setHeaders(headers) }
                .onSuccess {
                    // Unfilled rows were never part of the save; dropping them lets the rows match
                    // storage again, which is what turns Save back off.
                    _state.update { current ->
                        current.copy(
                            rows = headers.map { CustomHeaderRow(it.name, it.value) }.toImmutableList(),
                            isSaving = false
                        )
                    }
                }
                .onFailure { error ->
                    // The rows keep what the user typed and Save stays offered, so retrying is possible.
                    logcat(priority = LogPriority.ERROR, throwable = error) { "Saving custom headers failed" }
                    _state.update { it.copy(isSaving = false) }
                    showSnackbarSuspend(NativeText.Resource(RString.custom_headers_save_failed))
                }
        }
    }
}
