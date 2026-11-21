package com.grappim.taigamobile.uikit.widgets.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlin.test.Test
import kotlin.test.assertEquals

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md. Asserts English content descriptions.
@OptIn(ExperimentalTestApi::class)
class MarkdownToolbarTest {

    @Test
    fun tappingBoldOnSelectionWrapsIt() = runComposeUiTest {
        var value by mutableStateOf(TextFieldValue("say hello", TextRange(4, 9)))

        setContent {
            TaigaMobilePreviewTheme {
                MarkdownToolbar(
                    value = value,
                    onValueChange = { value = it },
                    isPreview = false,
                    onPreviewToggle = {}
                )
            }
        }

        onNodeWithContentDescription("Bold").performClick()

        assertEquals("say **hello**", value.text)
        assertEquals(TextRange(6, 11), value.selection)
    }

    @Test
    fun previewToggleInvokesCallbackAndSwapsItsLabel() = runComposeUiTest {
        var isPreview by mutableStateOf(false)

        setContent {
            TaigaMobilePreviewTheme {
                MarkdownToolbar(
                    value = TextFieldValue("text"),
                    onValueChange = {},
                    isPreview = isPreview,
                    onPreviewToggle = { isPreview = !isPreview }
                )
            }
        }

        onNodeWithContentDescription("Preview").performClick()
        assertEquals(true, isPreview)

        onNodeWithContentDescription("Edit").performClick()
        assertEquals(false, isPreview)
    }

    @Test
    fun formattingButtonsAreDisabledInPreview() = runComposeUiTest {
        var isPreview by mutableStateOf(true)

        setContent {
            TaigaMobilePreviewTheme {
                MarkdownToolbar(
                    value = TextFieldValue("text"),
                    onValueChange = {},
                    isPreview = isPreview,
                    onPreviewToggle = {}
                )
            }
        }

        onNodeWithContentDescription("Bold").assertIsNotEnabled()

        isPreview = false
        onNodeWithContentDescription("Bold").assertIsEnabled()
    }
}
