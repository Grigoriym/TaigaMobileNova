package com.grappim.taigamobile.uikit.widgets.text

import androidx.compose.material3.CheckboxDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlin.test.Test
import kotlin.test.assertEquals

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
        // composition; a single waitForIdle() is not reliably enough to observe it. The default
        // 1000ms wall-clock timeout is tight enough to flake under a full, multi-module jvmTest
        // run on a loaded machine (docs/revisit.md #27), so it's widened here.
        waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("Show more").fetchSemanticsNodes().isNotEmpty() }

        onNodeWithText("Show more").performClick()

        onNodeWithText("Show less").assertExists()
        onNodeWithText("Show more").assertDoesNotExist()
    }

    // Semantics can't catch this: a node's displayed bounds are clipped to its ancestors whether or not anything
    // clips drawing, so the check reads the pixels above the "Show more" button instead.
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun collapsedTextDoesNotDrawCheckboxesBelowTheCut() {
        for (interactive in listOf(true, false)) {
            runComposeUiTest {
                val text = (1..8).joinToString(separator = "\n\n") { "Paragraph number $it." } + "\n\n- [x] done"
                var checkboxColor = Color.Unspecified

                setContent {
                    TaigaMobilePreviewTheme {
                        checkboxColor = CheckboxDefaults.colors().checkedBoxColor
                        ExpandableMarkdownText(text = text, onTaskCheckboxToggle = if (interactive) ({}) else null)
                    }
                }

                waitUntil(timeoutMillis = 5_000) { onAllNodesWithText("Show more").fetchSemanticsNodes().isNotEmpty() }
                val buttonTop = onNodeWithText("Show more").fetchSemanticsNode().boundsInRoot.top.toInt()
                val pixels = onRoot().captureToImage().toPixelMap()
                val target = checkboxColor.toArgb()
                val checkboxPixels = (0 until buttonTop).sumOf { y ->
                    (0 until pixels.width).count { x -> pixels[x, y].toArgb() == target }
                }

                assertEquals(0, checkboxPixels, "interactive = $interactive")
            }
        }
    }
}
