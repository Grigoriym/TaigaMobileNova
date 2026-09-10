package com.grappim.taigamobile.uikit.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.grappim.kit.uikit.NativeText
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.comment_hint
import com.grappim.taigamobile.uikit.generated.resources.ic_send
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.editor.HintTextField
import com.grappim.taigamobile.uikit.widgets.editor.MentionSuggestionsPopup
import com.grappim.taigamobile.uikit.widgets.editor.findActiveMentionQuery
import com.grappim.taigamobile.uikit.widgets.editor.insertMention
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.painterResource

const val CREATE_COMMENT_BAR_TEXT_FIELD_TEST_TAG = "create_comment_bar_text_field"
const val CREATE_COMMENT_BAR_SEND_BUTTON_TEST_TAG = "create_comment_bar_send_button"

@Composable
fun CreateCommentBar(
    isOffline: Boolean,
    onButtonClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    canComment: Boolean = false,
    members: ImmutableList<TeamMember> = persistentListOf()
) {
    if (canComment) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            tonalElevation = 8.dp
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            var commentTextValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                mutableStateOf(TextFieldValue(""))
            }
            var isMentionPopupDismissed by remember { mutableStateOf(false) }
            val activeMentionQuery = findActiveMentionQuery(commentTextValue)
            val mentionSuggestions = remember(activeMentionQuery, members) {
                val query = activeMentionQuery?.query
                if (query == null) {
                    persistentListOf()
                } else {
                    members.filter { it.username.startsWith(query, ignoreCase = true) }.toPersistentList()
                }
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = mainHorizontalScreenPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    HintTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(CREATE_COMMENT_BAR_TEXT_FIELD_TEST_TAG),
                        shape = MaterialTheme.shapes.large,
                        value = commentTextValue,
                        onValueChange = { newValue ->
                            if (newValue.text != commentTextValue.text) {
                                isMentionPopupDismissed = false
                            }
                            commentTextValue = newValue
                        },
                        hint = NativeText.Resource(RString.comment_hint),
                        maxLines = 3,
                        enabled = !isOffline
                    )

                    MentionSuggestionsPopup(
                        members = mentionSuggestions,
                        expanded = !isMentionPopupDismissed && mentionSuggestions.isNotEmpty(),
                        onSelect = { member ->
                            activeMentionQuery?.let { query ->
                                commentTextValue = insertMention(commentTextValue, query, member.username)
                            }
                        },
                        onDismissRequest = { isMentionPopupDismissed = true }
                    )
                }

                TaigaWidthSpacer(6.dp)

                IconButton(
                    onClick = {
                        commentTextValue.text.trim().takeIf { it.isNotEmpty() }?.let {
                            keyboardController?.hide()
                            onButtonClick(it)
                            commentTextValue = TextFieldValue("")
                        }
                    },
                    enabled = !isOffline,
                    modifier = Modifier
                        .testTag(CREATE_COMMENT_BAR_SEND_BUTTON_TEST_TAG)
                        .clip(CircleShape)
                        .background(
                            if (isOffline) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.38f)
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                ) {
                    Icon(
                        painter = painterResource(RDrawable.ic_send),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@[Composable PreviewTaigaDarkLight]
private fun CreateCommentBarPreview() {
    TaigaMobilePreviewTheme {
        CreateCommentBar(
            onButtonClick = {},
            canComment = true,
            isOffline = false
        )
    }
}
