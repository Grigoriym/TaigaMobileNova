package com.grappim.taigamobile.wiki.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grappim.taigamobile.feature.wiki.domain.IWikiRepository
import com.grappim.taigamobile.feature.wiki.domain.WikiLink
import com.grappim.taigamobile.feature.wiki.domain.WikiPage
import com.grappim.taigamobile.utils.ui.loadOrError
import com.grappim.taigamobile.utils.ui.mutableResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WikiListViewModel @Inject constructor(private val wikiRepository: IWikiRepository) :
    ViewModel() {

    val wikiPages = mutableResultFlow<List<WikiPage>>()
    val wikiLinks = mutableResultFlow<List<WikiLink>>()

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
