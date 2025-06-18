package com.grappim.taigamobile.wiki.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.domain.entities.WikiLink
import com.grappim.taigamobile.domain.entities.WikiPage
import com.grappim.taigamobile.domain.repositories.IWikiRepository
import com.grappim.taigamobile.ui.utils.MutableResultFlow
import com.grappim.taigamobile.ui.utils.loadOrError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WikiListViewModel @Inject constructor(
    private val wikiRepository: IWikiRepository
) : ViewModel() {

    val wikiPages = MutableResultFlow<List<WikiPage>>()
    val wikiLinks = MutableResultFlow<List<WikiLink>>()

    fun onOpen() {
        getWikiPage()
        getWikiLinks()
    }

    fun getWikiPage() = viewModelScope.launch {
        wikiPages.loadOrError {
            wikiRepository.getProjectWikiPages()
        }
    }

    fun getWikiLinks() = viewModelScope.launch {
        wikiLinks.loadOrError {
            wikiRepository.getWikiLinks()
        }
    }
}
