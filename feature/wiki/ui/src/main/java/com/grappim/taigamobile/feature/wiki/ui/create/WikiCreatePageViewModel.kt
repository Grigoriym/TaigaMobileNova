package com.grappim.taigamobile.feature.wiki.ui.create

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.feature.wiki.domain.WikiPage
import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import com.grappim.taigamobile.utils.ui.loadOrError
import com.grappim.taigamobile.utils.ui.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WikiCreatePageViewModel @Inject constructor(private val wikiRepository: WikiRepository) :
    ViewModel() {

    private val _state = MutableStateFlow(
        WikiCreatePageState(
            setTitle = ::setTitle,
            setDescription = ::setDescription,
            onCreateWikiPage = ::createWikiPage
        )
    )
    val state = _state.asStateFlow()

    val creationResult = mutableResultFlow<WikiPage>()

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
            creationResult.loadOrError {
                val slug = title.replace(" ", "-").lowercase()

                wikiRepository.createWikiLink(
                    href = slug,
                    title = title
                )

                // Need it, because we can't put content to page
                // and create link for it at the same time :(
                val wikiPage = wikiRepository.getProjectWikiPageBySlug(slug)

                wikiRepository.editWikiPage(
                    pageId = wikiPage.id,
                    content = content,
                    version = wikiPage.version
                )

                wikiPage
            }
        }
    }
}
