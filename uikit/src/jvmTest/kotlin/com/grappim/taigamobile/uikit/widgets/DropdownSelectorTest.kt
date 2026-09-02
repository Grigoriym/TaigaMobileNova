package com.grappim.taigamobile.uikit.widgets

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md.
class DropdownSelectorTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clickingOpensMenuAndSelectingAnItemInvokesCallbackAndCloses() = runComposeUiTest {
        var selected: String? = null

        setContent {
            TaigaMobilePreviewTheme {
                DropdownSelector(
                    items = listOf("Alpha", "Beta", "Gamma"),
                    selectedItem = "Alpha",
                    onItemSelect = { selected = it },
                    isOffline = false,
                    canModify = true,
                    itemContent = { Text(it) },
                    selectedItemContent = { Text("Selected: $it") }
                )
            }
        }

        onNodeWithText("Beta").assertDoesNotExist()

        onNodeWithTag(DROPDOWN_SELECTOR_ROW_TEST_TAG).performClick()
        onNodeWithText("Beta").assertExists()

        onNodeWithText("Beta").performClick()

        assertEquals("Beta", selected)
        onNodeWithText("Gamma").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clickingWhenCannotModifyDoesNotOpenMenu() = runComposeUiTest {
        var selected: String? = null

        setContent {
            TaigaMobilePreviewTheme {
                DropdownSelector(
                    items = listOf("Alpha", "Beta"),
                    selectedItem = "Alpha",
                    onItemSelect = { selected = it },
                    isOffline = false,
                    canModify = false,
                    itemContent = { Text(it) },
                    selectedItemContent = { Text("Selected: $it") }
                )
            }
        }

        onNodeWithTag(DROPDOWN_SELECTOR_ROW_TEST_TAG).performClick()

        onNodeWithText("Beta").assertDoesNotExist()
        assertNull(selected)
    }
}
