package com.grappim.taigamobile.feature.wiki.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.core.domain.AttachmentDTO
import com.grappim.taigamobile.core.domain.UserDTO
import com.grappim.taigamobile.feature.wiki.ui.widgets.WikiPageDropDownMenuWidget
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.Attachments
import com.grappim.taigamobile.uikit.EditAction
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.editor.TextFieldWithHint
import com.grappim.taigamobile.uikit.widgets.list.Description
import com.grappim.taigamobile.uikit.widgets.list.UserItem
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoaderWidget
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.LoadingResult
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SubscribeOnError
import com.grappim.taigamobile.utils.ui.SuccessResult
import java.io.InputStream
import java.time.LocalDateTime

@Composable
fun WikiPageScreen(
    goBack: () -> Unit,
    goToProfile: (userId: Long) -> Unit,
    viewModel: WikiPageViewModel = hiltViewModel(),
    showMessage: (message: Int) -> Unit = {}
) {
    val topBarController = LocalTopBarConfig.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    val page by viewModel.page.collectAsState()
    page.SubscribeOnError(showMessage)

    val link by viewModel.link.collectAsState()
    link.SubscribeOnError(showMessage)

    val editWikiPageResult by viewModel.editWikiPageResult.collectAsState()
    editWikiPageResult.SubscribeOnError(showMessage)

    val deleteWikiPageResult by viewModel.deleteWikiPageResult.collectAsState()
    deleteWikiPageResult.SubscribeOnError(showMessage)

    val attachments by viewModel.attachments.collectAsState()
    attachments.SubscribeOnError(showMessage)

    val isLoading = page is LoadingResult ||
        link is LoadingResult ||
        editWikiPageResult is LoadingResult ||
        deleteWikiPageResult is LoadingResult ||
        attachments is LoadingResult

    deleteWikiPageResult.takeIf { it is SuccessResult }?.let {
        LaunchedEffect(Unit) {
            goBack()
        }
    }

    LaunchedEffect(state.isEditPageVisible) {
        topBarController.update(
            TopBarConfig(
                title = if (state.isEditPageVisible) {
                    NativeText.Resource(RString.edit)
                } else {
                    NativeText.Simple(link.data?.title ?: viewModel.pageSlug)
                },
                showBackButton = true,
                overrideBackHandlerAction = {
                    if (state.isEditPageVisible) {
                        state.setEditPageVisible(false)
                    } else {
                        goBack()
                    }
                },
                actions = listOf(
                    if (state.isEditPageVisible) {
                        TopBarActionIconButton(
                            drawable = RDrawable.ic_save,
                            contentDescription = "Save",
                            onClick = {
                                viewModel.editWikiPage(state.description.text)
                                state.setEditPageVisible(false)
                            }
                        )
                    } else {
                        TopBarActionIconButton(
                            drawable = RDrawable.ic_options,
                            contentDescription = "More",
                            onClick = {
                                state.setDropdownMenuExpanded(true)
                            }
                        )
                    }
                )
            )
        )
    }

    if (state.isDeleteAlertVisible) {
        ConfirmActionDialog(
            title = stringResource(RString.delete_wiki_title),
            description = stringResource(RString.delete_wiki_text),
            onConfirm = {
                state.setDeleteAlertVisible(false)
                viewModel.deleteWikiPage()
            },
            onDismiss = { state.setDeleteAlertVisible(false) },
            iconId = RDrawable.ic_delete
        )
    }

    WikiPageDropDownMenuWidget(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd),
        state = state
    )

    WikiPageScreenContent(
        state = state,
        lastModifierDate = page.data?.modifiedDate ?: LocalDateTime.now(),
        attachmentDTOS = attachments.data.orEmpty(),
        isLoading = isLoading,
        onUserItemClick = goToProfile,
        editAttachments = EditAction(
            select = { (file, stream) -> viewModel.addPageAttachment(file, stream) },
            remove = viewModel::deletePageAttachment,
            isLoading = attachments is LoadingResult
        )
    )
}

@Composable
fun WikiPageScreenContent(
    state: WikiPageState,
    lastModifierDate: LocalDateTime,
    modifier: Modifier = Modifier,
    attachmentDTOS: List<AttachmentDTO> = emptyList(),
    isLoading: Boolean = false,
    onUserItemClick: (userId: Long) -> Unit = { _ -> },
    editAttachments: EditAction<Pair<String, InputStream>, AttachmentDTO> = EditAction()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        val sectionsPadding = 24.dp

        if (state.isEditPageVisible) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = mainHorizontalScreenPadding)
            ) {
                Spacer(Modifier.height(8.dp))

                TextFieldWithHint(
                    hintId = RString.description_hint,
                    value = state.description,
                    onValueChange = { state.setDescription(it) }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .imePadding()
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularLoaderWidget()
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = mainHorizontalScreenPadding)
                ) {
                    // description
                    Description(state.description.text)

                    item {
                        Spacer(
                            Modifier.height(sectionsPadding)
                        )
                    }

                    // last modification
                    item {
                        Text(
                            text = stringResource(RString.last_modification),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(Modifier.height(8.dp))

                        if (state.userDTO != null) {
                            UserItem(
                                userDTO = state.userDTO!!,
                                dateTime = lastModifierDate,
                                onUserItemClick = {
                                    onUserItemClick(state.userDTO!!.actualId)
                                }
                            )
                        }
                    }

                    item {
                        Spacer(
                            modifier = Modifier.height(sectionsPadding)
                        )
                    }

                    Attachments(
                        attachmentDTOS = attachmentDTOS,
                        editAttachments = editAttachments
                    )

                    item {
                        Spacer(
                            modifier = Modifier.height(sectionsPadding)
                        )
                    }
                }
            }
        }
    }
}

@[Composable PreviewDarkLight]
private fun WikiPagePreview() {
    TaigaMobileTheme {
        WikiPageScreenContent(
            lastModifierDate = LocalDateTime.now(),
            state = WikiPageState(
                userDTO = UserDTO(
                    id = 0,
                    fullName = "Some cool fullname",
                    photo = null,
                    bigPhoto = null,
                    username = "Some cool username"
                ),
                setDeleteAlertVisible = {},
                setDropdownMenuExpanded = {},
                setEditPageVisible = {},
                setDescription = {}
            )
        )
    }
}
