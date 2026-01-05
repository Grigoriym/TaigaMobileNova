package com.grappim.taigamobile.feature.wiki.ui.page.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import com.grappim.taigamobile.feature.workitem.domain.wiki.WikiPage
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
class WikiCreatePageViewModel @Inject constructor(private val wikiRepository: WikiRepository) : ViewModel() {

    private val _state = MutableStateFlow(
        WikiCreatePageState(
            setSlug = ::setSlug,
            setContent = ::setContent,
            onCreateWikiPage = ::createWikiPage
        )
    )
    val state = _state.asStateFlow()

    private val _creationResult = Channel<WikiPage>()
    val creationResult = _creationResult.receiveAsFlow()

    private fun setSlug(slug: String) {
        _state.update {
            it.copy(
                slug = slug,
                error = NativeText.Empty
            )
        }
    }

    private fun setContent(content: String) {
        _state.update {
            it.copy(content = content)
        }
    }

    private fun createWikiPage() {
        createWikiPage(
            slug = _state.value.slug,
            content = _state.value.content
        )
    }

    private fun createWikiPage(slug: String, content: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = NativeText.Empty
                )
            }
            resultOf {
                wikiRepository.createWikiPage(
                    slug = slug,
                    content = content
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
