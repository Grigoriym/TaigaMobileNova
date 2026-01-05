package com.grappim.taigamobile.feature.wiki.ui.bookmark.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiLink
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
import javax.inject.Inject

@HiltViewModel
class WikiCreateBookmarkViewModel @Inject constructor(private val wikiRepository: WikiRepository) : ViewModel() {

    private val _state = MutableStateFlow(
        WikiCreateBookmarkState(
            setTitle = ::setTitle,
            onCreateWikiBookmark = ::createWikiBookmark
        )
    )
    val state = _state.asStateFlow()

    private val _creationResult = Channel<WikiLink>()
    val creationResult = _creationResult.receiveAsFlow()

    private fun setTitle(title: String) {
        _state.update {
            it.copy(
                title = title,
                error = NativeText.Empty
            )
        }
    }

    private fun createWikiBookmark() {
        createWikiBookmark(
            title = _state.value.title
        )
    }

    private fun createWikiBookmark(title: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = NativeText.Empty
                )
            }
            resultOf {
                wikiRepository.createWikiLink(
                    title = title
                )
            }.onSuccess { result ->
                _state.update {
                    it.copy(isLoading = false)
                }
                _creationResult.send(result)
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
