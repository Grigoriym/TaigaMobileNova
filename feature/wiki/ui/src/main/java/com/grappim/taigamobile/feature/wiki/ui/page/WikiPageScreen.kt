package com.grappim.taigamobile.feature.wiki.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.grappim.taigamobile.feature.wiki.ui.widgets.WikiPageDropDownMenuWidget
import com.grappim.taigamobile.feature.workitem.ui.delegates.attachments.WorkItemAttachmentsState
import com.grappim.taigamobile.feature.workitem.ui.delegates.description.WorkItemDescriptionState
import com.grappim.taigamobile.feature.workitem.ui.widgets.AttachmentsSectionWidget
import com.grappim.taigamobile.feature.workitem.ui.widgets.WorkItemDescriptionWidget
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.PreviewDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.TaigaHeightSpacer
import com.grappim.taigamobile.uikit.widgets.TaigaLoadingDialog
import com.grappim.taigamobile.uikit.widgets.dialog.ConfirmActionDialog
import com.grappim.taigamobile.uikit.widgets.list.UserItem
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarActionIconButton
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.ObserveAsEvents
import kotlinx.collections.immutable.persistentListOf

@Composable
fun WikiPageScreen(
    goBack: () -> Unit,
    goToProfile: (userId: Long) -> Unit,
    showSnackbar: (NativeText) -> Unit,
    goToEditDescription: (String) -> Unit,
    viewModel: WikiPageViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current

    val state by viewModel.state.collectAsStateWithLifecycle()
    val attachmentsState by viewModel.attachmentsState.collectAsStateWithLifecycle()
    val descriptionState by viewModel.descriptionState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title =
                NativeText.Simple(state.link?.title ?: state.pageSlug),
                navigationIcon = NavigationIconConfig.Back(),
                actions = persistentListOf(
                    TopBarActionIconButton(
                        drawable = RDrawable.ic_options,
                        contentDescription = "More",
                        onClick = {
                            state.setDropdownMenuExpanded(true)
                        }
                    )
                )
            )
        )
    }

    ObserveAsEvents(viewModel.deleteWikiPageResult) {
        goBack()
    }

    ObserveAsEvents(viewModel.uiError) { error ->
        if (error !is NativeText.Empty) {
            showSnackbar(error)
        }
    }

    if (state.isDeleteAlertVisible) {
        ConfirmActionDialog(
            title = stringResource(RString.delete_wiki_title),
            description = stringResource(RString.delete_wiki_text),
            onConfirm = {
                state.setDeleteAlertVisible(false)
                state.onDeleteConfirm()
            },
            onDismiss = { state.setDeleteAlertVisible(false) }
        )
    }

    WikiPageDropDownMenuWidget(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.TopEnd),
        state = state
    )

    TaigaLoadingDialog(isVisible = state.isLoading)

    if (state.currentPage != null) {
        WikiPageScreenContent(
            state = state,
            attachmentsState = attachmentsState,
            descriptionState = descriptionState,
            onUserItemClick = goToProfile,
            goToEditDescription = goToEditDescription
        )
    }
}

@Composable
fun WikiPageScreenContent(
    state: WikiPageState,
    attachmentsState: WorkItemAttachmentsState,
    descriptionState: WorkItemDescriptionState,
    goToEditDescription: (String) -> Unit,
    modifier: Modifier = Modifier,
    onUserItemClick: (userId: Long) -> Unit = { _ -> }
) {
    requireNotNull(state.currentPage)

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        val sectionsPadding = 24.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            WorkItemDescriptionWidget(
                currentDescription = state.currentPage.content,
                onDescriptionClick = {
                    goToEditDescription(state.currentPage.content)
                },
                isLoading = descriptionState.isDescriptionLoading
            )

            TaigaHeightSpacer(sectionsPadding)

            Text(
                text = stringResource(RString.last_modification),
                style = MaterialTheme.typography.titleMedium
            )

            TaigaHeightSpacer(8.dp)

            if (state.user != null) {
                UserItem(
                    displayName = state.user.displayName,
                    avatarUrl = state.user.photo,
                    dateTime = state.currentPage.modifiedDate,
                    onUserItemClick = {
                        onUserItemClick(state.user.actualId)
                    }
                )
            }

            TaigaHeightSpacer(sectionsPadding)

            AttachmentsSectionWidget(
                attachments = attachmentsState.attachments,
                isAttachmentsLoading = attachmentsState.areAttachmentsLoading,
                onAttachmentAdd = { uri ->
                    state.onAttachmentAdd(uri)
                },
                areAttachmentsExpanded = attachmentsState.areAttachmentsExpanded,
                setAreAttachmentsExpanded = attachmentsState.setAreAttachmentsExpanded,
                onAttachmentRemove = {
                    state.onAttachmentRemove(it)
                }
            )

            TaigaHeightSpacer(sectionsPadding)
        }
    }
}

@[Composable PreviewDarkLight]
private fun WikiPagePreview() {
    TaigaMobileTheme {
        WikiPageScreenContent(
            state = WikiPageState(
                pageSlug = "adasds"
            ),
            attachmentsState = WorkItemAttachmentsState(),
            goToEditDescription = {},
            descriptionState = WorkItemDescriptionState()
        )
    }
}
