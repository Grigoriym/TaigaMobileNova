package com.grappim.taigamobile.wiki.page

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.R
import com.grappim.taigamobile.commontask.EditAction
import com.grappim.taigamobile.core.ui.NativeText
import com.grappim.taigamobile.domain.entities.Attachment
import com.grappim.taigamobile.domain.entities.User
import com.grappim.taigamobile.main.topbar.LocalTopBarConfig
import com.grappim.taigamobile.main.topbar.TopBarActionResource
import com.grappim.taigamobile.main.topbar.TopBarConfig
import com.grappim.taigamobile.ui.components.dialogs.ConfirmActionDialog
import com.grappim.taigamobile.ui.components.editors.TextFieldWithHint
import com.grappim.taigamobile.ui.components.lists.Attachments
import com.grappim.taigamobile.ui.components.lists.Description
import com.grappim.taigamobile.ui.components.lists.UserItem
import com.grappim.taigamobile.ui.components.loaders.CircularLoader
import com.grappim.taigamobile.ui.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.ui.utils.LoadingResult
import com.grappim.taigamobile.ui.utils.SubscribeOnError
import com.grappim.taigamobile.ui.utils.SuccessResult
import java.io.InputStream
import java.time.LocalDateTime

@Composable
fun WikiPageScreen(
    viewModel: WikiPageViewModel = hiltViewModel(),
    showMessage: (message: Int) -> Unit = {},
    goToProfile: (userId: Long) -> Unit,
    goBack: () -> Unit
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

    val isLoading = page is LoadingResult || link is LoadingResult ||
            editWikiPageResult is LoadingResult || deleteWikiPageResult is LoadingResult ||
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
                    NativeText.Resource(R.string.edit)
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
                        TopBarActionResource(
                            drawable = R.drawable.ic_save,
                            contentDescription = "Save",
                            onClick = {
                                viewModel.editWikiPage(state.description.text)
                                state.setEditPageVisible(false)
                            }
                        )
                    } else {
                        TopBarActionResource(
                            drawable = R.drawable.ic_options,
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
            title = stringResource(R.string.delete_wiki_title),
            text = stringResource(R.string.delete_wiki_text),
            onConfirm = {
                state.setDeleteAlertVisible(false)
                viewModel.deleteWikiPage()
            },
            onDismiss = { state.setDeleteAlertVisible(false) },
            iconId = R.drawable.ic_delete
        )
    }

    WikiPageDropDownMenu(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd),
        state = state
    )

    WikiPageScreenContent(
        state = state,
        lastModifierDate = page.data?.modifiedDate ?: LocalDateTime.now(),
        attachments = attachments.data.orEmpty(),
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
    attachments: List<Attachment> = emptyList(),
    isLoading: Boolean = false,
    onUserItemClick: (userId: Long) -> Unit = { _ -> },
    editAttachments: EditAction<Pair<String, InputStream>, Attachment> = EditAction(),
) = Box(
    modifier = Modifier
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
                hintId = R.string.description_hint,
                value = state.description,
                onValueChange = { state.setDescription(it) },
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
                    CircularLoader()
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
                        text = stringResource(R.string.last_modification),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    if (state.user != null) {
                        UserItem(
                            user = state.user,
                            dateTime = lastModifierDate,
                            onUserItemClick = {
                                onUserItemClick(state.user.id)
                            }
                        )
                    }
                }

                item {
                    Spacer(
                        Modifier.height(sectionsPadding)
                    )
                }

                Attachments(
                    attachments = attachments,
                    editAttachments = editAttachments
                )

                item {
                    Spacer(
                        Modifier.height(sectionsPadding)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun WikiPagePreview() {
    WikiPageScreenContent(
        lastModifierDate = LocalDateTime.now(),
        state = WikiPageState(
            user = User(
                _id = 0,
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