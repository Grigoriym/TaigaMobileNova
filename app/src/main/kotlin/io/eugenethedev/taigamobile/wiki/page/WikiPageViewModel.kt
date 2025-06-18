package io.eugenethedev.taigamobile.wiki.page

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.grappim.taigamobile.R
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eugenethedev.taigamobile.domain.entities.Attachment
import io.eugenethedev.taigamobile.domain.entities.WikiLink
import io.eugenethedev.taigamobile.domain.entities.WikiPage
import io.eugenethedev.taigamobile.domain.repositories.IUsersRepository
import io.eugenethedev.taigamobile.domain.repositories.IWikiRepository
import io.eugenethedev.taigamobile.ui.utils.MutableResultFlow
import io.eugenethedev.taigamobile.ui.utils.loadOrError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class WikiPageViewModel @Inject constructor(
    private val wikiRepository: IWikiRepository,
    private val userRepository: IUsersRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(
        WikiPageState(
            setDeleteAlertVisible = ::setDeleteAlertVisible,
            setDropdownMenuExpanded = ::setDropdownMenuExpanded,
            setEditPageVisible = ::setEditPageVisible,
            setDescription = ::setDescription
        )
    )
    val state = _state.asStateFlow()

    private val route = savedStateHandle.toRoute<WikiPageNavDestination>()
    val pageSlug: String
        get() = route.slug

    val page = MutableResultFlow<WikiPage>()
    val link = MutableResultFlow<WikiLink>()
    val attachments = MutableResultFlow<List<Attachment>>()
    val editWikiPageResult = MutableResultFlow<Unit>()
    val deleteWikiPageResult = MutableResultFlow<Unit>()

    init {
        loadData()
    }

    private fun setDescription(description: TextFieldValue) {
        _state.update {
            it.copy(description = description)
        }
    }

    private fun setEditPageVisible(visible: Boolean) {
        _state.update {
            it.copy(isEditPageVisible = visible)
        }
    }

    private fun setDeleteAlertVisible(visible: Boolean) {
        _state.update {
            it.copy(isDeleteAlertVisible = visible)
        }
    }

    private fun setDropdownMenuExpanded(expanded: Boolean) {
        _state.update {
            it.copy(isDropdownMenuExpanded = expanded)
        }
    }

    private fun loadData() = viewModelScope.launch {
        page.loadOrError {
            wikiRepository.getProjectWikiPageBySlug(pageSlug).also { page ->
                _state.update { state ->
                    state.copy(
                        page = page,
                        description = TextFieldValue(page.content)
                    )
                }

                val user = userRepository.getUser(page.lastModifier)
                _state.update { state ->
                    state.copy(user = user)
                }

                val jobsToLoad = arrayOf(
                    launch {
                        link.loadOrError(showLoading = false) {
                            wikiRepository.getWikiLinks().find { it.ref == pageSlug }
                        }
                    },
                    launch {
                        attachments.loadOrError(showLoading = false) {
                            wikiRepository.getPageAttachments(page.id)
                        }
                    }
                )

                joinAll(*jobsToLoad)
            }
        }
    }

    fun deleteWikiPage() = viewModelScope.launch {
        deleteWikiPageResult.loadOrError {
            val linkId = link.value.data?.id
            val pageId = page.value.data?.id

            pageId?.let { wikiRepository.deleteWikiPage(it) }
            linkId?.let { wikiRepository.deleteWikiLink(it) }
        }
    }

    fun editWikiPage(content: String) = viewModelScope.launch {
        editWikiPageResult.loadOrError {
            page.value.data?.let {
                wikiRepository.editWikiPage(
                    pageId = it.id,
                    content = content,
                    version = it.version
                )

                loadData().join()
            }
        }
    }

    fun deletePageAttachment(attachment: Attachment) = viewModelScope.launch {
        attachments.loadOrError(R.string.permission_error) {
            wikiRepository.deletePageAttachment(
                attachmentId = attachment.id
            )

            loadData().join()
            attachments.value.data
        }
    }

    fun addPageAttachment(fileName: String, inputStream: InputStream) = viewModelScope.launch {
        attachments.loadOrError(R.string.permission_error) {
            page.value.data?.id?.let { pageId ->
                wikiRepository.addPageAttachment(
                    pageId = pageId,
                    fileName = fileName,
                    inputStream = inputStream
                )
                loadData().join()
            }
            attachments.value.data
        }
    }
}