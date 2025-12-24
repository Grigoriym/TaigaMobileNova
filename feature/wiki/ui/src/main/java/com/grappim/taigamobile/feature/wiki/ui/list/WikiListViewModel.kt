package com.grappim.taigamobile.feature.wiki.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.core.domain.resultOf
import com.grappim.taigamobile.feature.wiki.domain.WikiRepository
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegate
import com.grappim.taigamobile.utils.ui.delegates.SnackbarDelegateImpl
import com.grappim.taigamobile.utils.ui.getErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WikiListViewModel @Inject constructor(private val wikiRepository: WikiRepository) :
    ViewModel(),
    SnackbarDelegate by SnackbarDelegateImpl() {

    private val _state = MutableStateFlow(
        WikiListState(
            onOpen = ::onOpen
        )
    )
    val state: StateFlow<WikiListState> = _state.asStateFlow()

    private fun onOpen() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    error = NativeText.Empty
                )
            }
            resultOf {
                coroutineScope {
                    val pages = async { wikiRepository.getProjectWikiPages() }
                    val links = async { wikiRepository.getWikiLinks() }
                    Pair(pages.await(), links.await())
                }
            }.onSuccess { result ->
                val allPages = result.first.map { page ->
                    WikiUIItem(
                        id = page.id,
                        title = page.slug,
                        slug = page.slug
                    )
                }.toImmutableList()

                val slugToPageId = result.first.associate { it.slug to it.id }
                val bookmarks = result.second
                    .mapNotNull { link ->
                        slugToPageId[link.ref]?.let { pageId ->
                            WikiUIItem(
                                id = pageId,
                                title = link.title,
                                slug = link.ref
                            )
                        }
                    }.toImmutableList()

                _state.update {
                    it.copy(
                        allPages = allPages,
                        bookmarks = bookmarks,
                        isLoading = false
                    )
                }
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
