package com.grappim.taigamobile.uikit.widgets.text

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlin.test.Test

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md.
class ExpandableMarkdownTextTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun shortTextNeverShowsExpandButton() = runComposeUiTest {
        setContent {
            TaigaMobilePreviewTheme {
                ExpandableMarkdownText(text = "Short text that doesn't need expansion")
            }
        }

        onNodeWithText("Show more").assertDoesNotExist()
        onNodeWithText("Show less").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun longTextShowsExpandButtonAndTogglesOnClick() = runComposeUiTest {
        val longText = (1..30).joinToString(separator = "\n\n") { "Paragraph number $it." }

        setContent {
            TaigaMobilePreviewTheme {
                ExpandableMarkdownText(text = longText, maxLinesCollapsed = 6)
            }
        }

        // onSizeChanged's naturalHeight update lands on a later frame than the initial
        // composition; a single waitForIdle() is not reliably enough to observe it.
        waitUntil { onAllNodesWithText("Show more").fetchSemanticsNodes().isNotEmpty() }

        onNodeWithText("Show more").performClick()

        onNodeWithText("Show less").assertExists()
        onNodeWithText("Show more").assertDoesNotExist()
    }
}
