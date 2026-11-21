package com.grappim.taigamobile.uikit.widgets.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ContinueMarkdownListTest {

    // `|` marks a collapsed cursor, `[` and `]` a selection.
    private fun v(marked: String): TextFieldValue {
        val cursor = marked.indexOf('|')
        if (cursor >= 0) return TextFieldValue(marked.removeRange(cursor, cursor + 1), TextRange(cursor))
        val start = marked.indexOf('[')
        val end = marked.indexOf(']') - 1
        return TextFieldValue(marked.replace("[", "").replace("]", ""), TextRange(start, end))
    }

    private fun TextFieldValue.marked(): String = text.replaceRange(selection.start, selection.start, "|")

    // Presses Enter the way a text field reports it: the selection replaced by a line break, the cursor after it.
    private fun enter(marked: String): String {
        val previous = v(marked)
        val start = previous.selection.min
        val next = TextFieldValue(
            text = previous.text.replaceRange(start, previous.selection.max, "\n"),
            selection = TextRange(start + 1)
        )
        return continueMarkdownList(previous, next).marked()
    }

    @Test
    fun `bullet item continues with the same marker`() {
        assertEquals("- a\n- |", enter("- a|"))
        assertEquals("* a\n* |", enter("* a|"))
        assertEquals("+ a\n+ |", enter("+ a|"))
    }

    @Test
    fun `numbered item continues with the next number`() {
        assertEquals("1. a\n2. |", enter("1. a|"))
        assertEquals("9. a\n10. |", enter("9. a|"))
    }

    @Test
    fun `task item continues unticked`() {
        assertEquals("- [x] a\n- [ ] |", enter("- [x] a|"))
        assertEquals("* [ ] a\n* [ ] |", enter("* [ ] a|"))
    }

    @Test
    fun `nested item keeps its indentation`() {
        assertEquals("- a\n    - b\n    - |", enter("- a\n    - b|"))
        assertEquals("\t1. b\n\t2. |", enter("\t1. b|"))
    }

    @Test
    fun `enter in the middle of an item moves the rest into the new item`() {
        assertEquals("- fo\n- |o", enter("- fo|o"))
    }

    @Test
    fun `enter right after the marker of a non-empty item leaves an empty item above`() {
        assertEquals("- \n- |foo", enter("- |foo"))
    }

    @Test
    fun `enter on an empty top-level item removes the marker and leaves a blank line`() {
        assertEquals("- a\n\n|", enter("- a\n- |"))
        assertEquals("1. a\n\n|\nnext", enter("1. a\n2. |\nnext"))
        assertEquals("- a\n\n|", enter("- a\n- [ ] |"))
    }

    @Test
    fun `enter on an empty nested item moves it out one level`() {
        assertEquals("- a\n- |", enter("- a\n    - |"))
        assertEquals("- a\n    - |", enter("- a\n        - |"))
        assertEquals("- a\n- [ ] |", enter("- a\n\t- [ ] |"))
    }

    @Test
    fun `plain lines and quotes are not continued`() {
        assertEquals("text\n|", enter("text|"))
        assertEquals("> quote\n|", enter("> quote|"))
        assertEquals("-not a list\n|", enter("-not a list|"))
    }

    @Test
    fun `list lines inside a fenced code block are not continued`() {
        assertEquals("```\n- a\n|", enter("```\n- a|"))
        assertEquals("```\nx\n```\n- a\n- |", enter("```\nx\n```\n- a|"))
    }

    @Test
    fun `enter replacing a selection continues the list`() {
        assertEquals("- a\n- |c", enter("- a[b]c"))
    }

    @Test
    fun `autocorrect committed together with the line break still continues`() {
        val previous = v("- teh|")
        val next = v("- the\n|")
        assertEquals("- the\n- |", continueMarkdownList(previous, next).marked())
    }

    @Test
    fun `changes other than a single line break pass through unchanged`() {
        val cases = listOf(
            v("- a|") to v("- ab|"),
            v("- a|") to v("- a\nb\n|"),
            v("- a\nb|") to v("- a\n|"),
            v("- a\n|") to v("- a\n|"),
            v("- a|\nb") to v("- a\n|"),
            v("- a|") to v("- a\n[x]")
        )
        cases.forEach { (previous, next) -> assertSame(next, continueMarkdownList(previous, next)) }
    }
}
