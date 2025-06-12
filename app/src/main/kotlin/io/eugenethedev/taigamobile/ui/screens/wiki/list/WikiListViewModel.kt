package io.eugenethedev.taigamobile.ui.screens.wiki.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eugenethedev.taigamobile.domain.entities.WikiLink
import io.eugenethedev.taigamobile.domain.entities.WikiPage
import io.eugenethedev.taigamobile.domain.repositories.IWikiRepository
import io.eugenethedev.taigamobile.state.Session
import io.eugenethedev.taigamobile.ui.utils.MutableResultFlow
import io.eugenethedev.taigamobile.ui.utils.loadOrError
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WikiListViewModel @Inject constructor(
    private val session: Session,
    private val wikiRepository: IWikiRepository
) : ViewModel() {

    val projectName by lazy { session.currentProjectName }

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
