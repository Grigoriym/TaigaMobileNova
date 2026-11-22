package com.grappim.taigamobile.uikit.widgets.text

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlin.test.Test

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md.
class MarkdownTextWidgetRenderTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun singleNewlineInsideParagraphRendersAsLineBreak() = runComposeUiTest {
        setContent {
            TaigaMobilePreviewTheme {
                MarkdownTextWidget(text = "line one\nline two")
            }
        }

        onNodeWithText("line one\nline two").assertExists()
    }
}
