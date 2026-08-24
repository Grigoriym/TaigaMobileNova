package com.grappim.taigamobile.uikit.widgets.text

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlin.test.Test
import kotlin.test.assertEquals

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md.
class SectionTitleTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clickingAddButtonInvokesOnAddClick() = runComposeUiTest {
        var clickCount = 0

        setContent {
            TaigaMobilePreviewTheme {
                SectionTitle(
                    text = "Attachments",
                    onAddClick = { clickCount++ }
                )
            }
        }

        onNodeWithTag(SECTION_TITLE_ADD_BUTTON_TEST_TAG).performClick()

        assertEquals(1, clickCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun noOnAddClickHidesAddButton() = runComposeUiTest {
        setContent {
            TaigaMobilePreviewTheme {
                SectionTitle(text = "Attachments", onAddClick = null)
            }
        }

        onNodeWithTag(SECTION_TITLE_ADD_BUTTON_TEST_TAG).assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clickingSectionTitleExpandableInvokesOnExpandClick() = runComposeUiTest {
        var clickCount = 0

        setContent {
            TaigaMobilePreviewTheme {
                SectionTitleExpandable(
                    text = "3 Attachments",
                    isExpanded = false,
                    onExpandClick = { clickCount++ }
                )
            }
        }

        onNodeWithText("3 Attachments").performClick()

        assertEquals(1, clickCount)
    }
}
