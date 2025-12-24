package com.grappim.taigamobile.feature.wiki.ui.create

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import com.grappim.taigamobile.feature.workitem.domain.PatchDataGenerator
import com.grappim.taigamobile.feature.workitem.domain.WorkItemRepository
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
class WikiCreatePageViewModel @Inject constructor(
    private val wikiRepository: WikiRepository,
    private val workItemRepository: WorkItemRepository,
    private val patchDataGenerator: PatchDataGenerator
) : ViewModel() {

    private val _state = MutableStateFlow(
        WikiCreatePageState(
            setTitle = ::setTitle,
            setDescription = ::setDescription,
            onCreateWikiPage = ::createWikiPage
        )
    )
    val state = _state.asStateFlow()

    private val _creationResult = Channel<WikiPage>()
    val creationResult = _creationResult.receiveAsFlow()

    private fun setTitle(title: TextFieldValue) {
        _state.update {
            it.copy(title = title)
        }
    }

    private fun setDescription(description: TextFieldValue) {
        _state.update {
            it.copy(description = description)
        }
    }

    private fun createWikiPage() {
        createWikiPage(
            title = _state.value.title.text,
            content = _state.value.description.text
        )
    }

    private fun createWikiPage(title: String, content: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = NativeText.Empty
                )
            }
            resultOf {
                val slug = title
                    .trimEnd()
                    .replace(" ", "-").lowercase()
                wikiRepository.createWikiLink(
                    href = slug,
                    title = title
                )
//                Need it, because we can't put content to page
//                and create link for it at the same time :(
                val wikiPage = wikiRepository.getProjectWikiPageBySlug(slug)

                workItemRepository.patchWikiPage(
                    pageId = wikiPage.id,
                    payload = patchDataGenerator.getWikiContent(content),
                    version = wikiPage.version
                )
                wikiPage
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
