package com.grappim.taigamobile.uikit.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.editor.TextFieldWithHint

@Composable
fun CreateCommentBar(onButtonClick: (String) -> Unit, modifier: Modifier = Modifier, canComment: Boolean = false) {
    if (canComment) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            tonalElevation = 8.dp
        ) {
            val keyboardController = LocalSoftwareKeyboardController.current
            var commentTextValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                mutableStateOf(
                    TextFieldValue()
                )
            }

            Row(
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = mainHorizontalScreenPadding)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = MaterialTheme.shapes.large
                        )
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        .padding(8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    TextFieldWithHint(
                        hintId = RString.comment_hint,
                        maxLines = 3,
                        value = commentTextValue,
                        onValueChange = { commentTextValue = it }
                    )
                }

                CompositionLocalProvider(
                    LocalMinimumInteractiveComponentSize provides Dp.Unspecified
                ) {
                    IconButton(
                        onClick = {
                            commentTextValue.text.trim().takeIf { it.isNotEmpty() }?.let {
                                onButtonClick(it)
                                commentTextValue = TextFieldValue()
                                keyboardController?.hide()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
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
}
