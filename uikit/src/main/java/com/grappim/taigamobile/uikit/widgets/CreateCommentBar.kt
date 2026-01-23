package com.grappim.taigamobile.uikit.widgets

import androidx.compose.foundation.background
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.editor.HintTextField
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun CreateCommentBar(onButtonClick: (String) -> Unit, modifier: Modifier = Modifier, canComment: Boolean = false) {
    if (canComment) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            tonalElevation = 8.dp
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            var commentTextValue by rememberSaveable { mutableStateOf("") }

            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp, horizontal = mainHorizontalScreenPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HintTextField(
                    modifier = Modifier
                        .weight(1f),
                    shape = MaterialTheme.shapes.large,
                    value = commentTextValue,
                    onValueChange = { commentTextValue = it },
                    hint = NativeText.Resource(RString.comment_hint),
                    maxLines = 3
                )

                TaigaWidthSpacer(6.dp)

                IconButton(
                    onClick = {
                        commentTextValue.trim().takeIf { it.isNotEmpty() }?.let {
                            keyboardController?.hide()
                            onButtonClick(it)
                            commentTextValue = ""
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
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
    TaigaMobileTheme {
        CreateCommentBar(
            onButtonClick = {},
            canComment = true
        )
    }
}
