package com.grappim.taigamobile.uikit.widgets.dialog

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.utils.ui.NativeText
import kotlin.test.Test
import kotlin.test.assertEquals

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md.
class ConfirmActionDialogTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun confirmingInvokesOnConfirmAndNotOnDismiss() = runComposeUiTest {
        var confirmCount = 0
        var dismissCount = 0

        setContent {
            TaigaMobilePreviewTheme {
                ConfirmActionDialog(
                    isVisible = true,
                    onConfirm = { confirmCount++ },
                    onDismiss = { dismissCount++ },
                    title = "Delete task",
                    description = "Are you sure?",
                    confirmButtonText = NativeText.Simple("Confirm"),
                    dismissButtonText = NativeText.Simple("Cancel")
                )
            }
        }

        onNodeWithText("Delete task").assertExists()
        onNodeWithText("Are you sure?").assertExists()

        onNodeWithText("Confirm").performClick()

        assertEquals(1, confirmCount)
        assertEquals(0, dismissCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dismissingInvokesOnDismissAndNotOnConfirm() = runComposeUiTest {
        var confirmCount = 0
        var dismissCount = 0

        setContent {
            TaigaMobilePreviewTheme {
                ConfirmActionDialog(
                    isVisible = true,
                    onConfirm = { confirmCount++ },
                    onDismiss = { dismissCount++ },
                    confirmButtonText = NativeText.Simple("Confirm"),
                    dismissButtonText = NativeText.Simple("Cancel")
                )
            }
        }

        onNodeWithText("Cancel").performClick()

        assertEquals(0, confirmCount)
        assertEquals(1, dismissCount)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun notVisibleRendersNothing() = runComposeUiTest {
        setContent {
            TaigaMobilePreviewTheme {
                ConfirmActionDialog(
                    isVisible = false,
                    onConfirm = {},
                    onDismiss = {},
                    title = "Delete task"
                )
            }
        }

        onNodeWithText("Delete task").assertDoesNotExist()
    }
}
