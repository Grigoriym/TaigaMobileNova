package com.grappim.taigamobile.uikit.widgets

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

// Desktop/JVM only: `runComposeUiTest` needs `compose.desktop.uiTestJUnit4` +
// `compose.desktop.currentOs`, wired for jvmTest only in uikit/build.gradle.kts. Android and iOS
// actuals are not exercised. See docs/testing/compose-ui-test-spike.md.
class CreateCommentBarTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun typingAndSendingClearsTheFieldAndInvokesCallback() = runComposeUiTest {
        var sentText: String? = null

        setContent {
            TaigaMobilePreviewTheme {
                CreateCommentBar(
                    isOffline = false,
                    onButtonClick = { sentText = it },
                    canComment = true
                )
            }
        }

        onNodeWithTag(CREATE_COMMENT_BAR_TEXT_FIELD_TEST_TAG).performTextInput("Looks good")
        onNodeWithText("Looks good").assertExists()

        onNodeWithTag(CREATE_COMMENT_BAR_SEND_BUTTON_TEST_TAG).performClick()

        assertEquals("Looks good", sentText)
        onNodeWithText("Looks good").assertDoesNotExist()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun clickingSendWithBlankTextDoesNotInvokeCallback() = runComposeUiTest {
        var callbackInvoked = false

        setContent {
            TaigaMobilePreviewTheme {
                CreateCommentBar(
                    isOffline = false,
                    onButtonClick = { callbackInvoked = true },
                    canComment = true
                )
            }
        }

        onNodeWithTag(CREATE_COMMENT_BAR_SEND_BUTTON_TEST_TAG).performClick()

        assertFalse(callbackInvoked)
    }
}
