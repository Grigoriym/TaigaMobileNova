package com.grappim.taigamobile.feature.wiki.ui.page.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlin.test.Test
import kotlin.test.assertEquals

// Desktop/JVM only: `runComposeUiTest` needs `compose.desktop.uiTestJUnit4` +
// `compose.desktop.currentOs`, wired for jvmTest only in build.gradle.kts. Android and iOS
// actuals are not exercised.
@OptIn(ExperimentalTestApi::class)
class WikiCreatePageScreenContentTest {

    @Test
    fun toolbarBoldWrapsTheSelectionInTheContent() = runComposeUiTest {
        val current = setWikiCreatePageContent(initial = TextFieldValue("make bold", TextRange(5, 9)))

        onNodeWithContentDescription("Bold").performClick()

        assertEquals("make **bold**", current().text)
        onNodeWithTag(WIKI_CREATE_PAGE_CONTENT_TEST_TAG).assertExists()
    }

    @Test
    fun previewRendersMarkdownAndSwitchingBackKeepsTextAndSelection() = runComposeUiTest {
        val initial = TextFieldValue("plain **strong**", TextRange(6, 16))
        setWikiCreatePageContent(initial = initial)

        onNodeWithContentDescription("Preview").performClick()

        onNodeWithTag(WIKI_CREATE_PAGE_CONTENT_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(WIKI_CREATE_PAGE_PREVIEW_TEST_TAG).assertExists()
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithText("plain strong", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithContentDescription("Bold").assertIsNotEnabled()

        onNodeWithContentDescription("Edit").performClick()

        val field = onNodeWithText(initial.text)
        field.assertTextEquals(initial.text)
        assertEquals(initial.selection, field.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange])
    }

    private fun ComposeUiTest.setWikiCreatePageContent(initial: TextFieldValue): () -> TextFieldValue {
        var latest = initial
        setContent {
            TaigaMobilePreviewTheme {
                var content by remember { mutableStateOf(initial) }
                var isPreview by remember { mutableStateOf(false) }
                WikiCreatePageScreenContent(
                    state = WikiCreatePageState(
                        content = content,
                        setContent = {
                            content = it
                            latest = it
                        },
                        isPreview = isPreview,
                        onPreviewToggle = { isPreview = !isPreview }
                    )
                )
            }
        }
        return { latest }
    }
}
