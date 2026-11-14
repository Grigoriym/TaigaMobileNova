package com.grappim.taigamobile.uikit.widgets.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownFormattingTest {

    // `[` and `]` mark the selection, a lone `|` marks a collapsed cursor.
    private fun v(marked: String): TextFieldValue {
        val cursor = marked.indexOf('|')
        if (cursor >= 0) return TextFieldValue(marked.removeRange(cursor, cursor + 1), TextRange(cursor))
        val start = marked.indexOf('[')
        val end = marked.indexOf(']') - 1
        return TextFieldValue(marked.replace("[", "").replace("]", ""), TextRange(start, end))
    }

    private fun TextFieldValue.marked(): String = if (selection.collapsed) {
        text.replaceRange(selection.start, selection.start, "|")
    } else {
        text.replaceRange(selection.max, selection.max, "]").replaceRange(selection.min, selection.min, "[")
    }

    private fun assertTransform(expected: String, input: String, transform: (TextFieldValue) -> TextFieldValue) {
        assertEquals(expected, transform(v(input)).marked())
    }

    // Selections in link tests contain `[`/`]`, so these build values explicitly.
    private fun value(text: String, start: Int, end: Int = start) = TextFieldValue(text, TextRange(start, end))

    @Test
    fun `bold wraps the selection and keeps the word selected`() {
        assertTransform("a **[word]** b", "a [word] b", ::toggleBold)
    }

    @Test
    fun `bold on a bare cursor inserts an empty pair with the cursor inside`() {
        assertTransform("a **|** b", "a | b", ::toggleBold)
    }

    @Test
    fun `bold on a cursor inside an empty pair removes the pair`() {
        assertTransform("a | b", "a **|** b", ::toggleBold)
    }

    @Test
    fun `bold unwraps when the markers surround the selection`() {
        assertTransform("a [word] b", "a **[word]** b", ::toggleBold)
    }

    @Test
    fun `bold unwraps when the selection includes the markers`() {
        assertTransform("a [word] b", "a [**word**] b", ::toggleBold)
    }

    @Test
    fun `bold wraps a selection across multiple lines as one span`() {
        assertTransform("**[one\ntwo]**", "[one\ntwo]", ::toggleBold)
    }

    @Test
    fun `bold handles a reversed selection`() {
        val result = toggleBold(TextFieldValue("a word b", TextRange(6, 2)))

        assertEquals("a **[word]** b", result.marked())
    }

    @Test
    fun `italic wraps with underscores and toggles off`() {
        assertTransform("_[word]_", "[word]", ::toggleItalic)
        assertTransform("[word]", "_[word]_", ::toggleItalic)
        assertTransform("_|_", "|", ::toggleItalic)
    }

    @Test
    fun `strikethrough wraps with double tildes and toggles off`() {
        assertTransform("~~[word]~~", "[word]", ::toggleStrikethrough)
        assertTransform("[word]", "~~[word]~~", ::toggleStrikethrough)
        assertTransform("~~|~~", "|", ::toggleStrikethrough)
    }

    @Test
    fun `inline code wraps with backticks and toggles off`() {
        assertTransform("`[word]`", "[word]", ::toggleInlineCode)
        assertTransform("[word]", "`[word]`", ::toggleInlineCode)
        assertTransform("`|`", "|", ::toggleInlineCode)
    }

    @Test
    fun `heading prefixes the cursor line and moves the cursor with it`() {
        assertTransform("one\n# tw|o", "one\ntw|o", ::toggleHeading)
    }

    @Test
    fun `heading on an empty line leaves the cursor after the prefix`() {
        assertTransform("# |", "|", ::toggleHeading)
    }

    @Test
    fun `heading toggles off and also removes a deeper heading level`() {
        assertTransform("tit|le", "# tit|le", ::toggleHeading)
        assertTransform("tit|le", "### tit|le", ::toggleHeading)
    }

    @Test
    fun `heading prefixes every selected line and keeps the prefixes inside the selection`() {
        assertTransform("[# one\n# two]", "[one\ntwo]", ::toggleHeading)
    }

    @Test
    fun `quote prefixes every selected line and toggles off`() {
        assertTransform("[> one\n> two]", "[one\ntwo]", ::toggleQuote)
        assertTransform("[one\ntwo]", "[> one\n> two]", ::toggleQuote)
        assertTransform("> |", "|", ::toggleQuote)
    }

    @Test
    fun `bullet list prefixes a partially selected range of lines`() {
        assertTransform("- o[ne\n- tw]o\nthree", "o[ne\ntw]o\nthree", ::toggleBulletList)
    }

    @Test
    fun `bullet list adds to all lines when only some have it`() {
        assertTransform("[- one\n- two]", "[- one\ntwo]", ::toggleBulletList)
    }

    @Test
    fun `bullet list removes from all lines when all have it`() {
        assertTransform("[one\ntwo]", "[- one\n- two]", ::toggleBulletList)
        assertTransform("it|em", "- it|em", ::toggleBulletList)
    }

    @Test
    fun `bullet list skips blank lines inside the selection`() {
        assertTransform("[- one\n\n- two]", "[one\n\ntwo]", ::toggleBulletList)
    }

    @Test
    fun `bullet list does not touch the line after a selection ending in a newline`() {
        assertTransform("[- one\n]two", "[one\n]two", ::toggleBulletList)
    }

    @Test
    fun `bullet list keeps leading indentation`() {
        assertTransform("- a\n    - b|", "- a\n    b|", ::toggleBulletList)
    }

    @Test
    fun `bullet list does not treat a task item as a bullet`() {
        assertTransform("- it|em", "- [ ] it|em", ::toggleBulletList)
    }

    @Test
    fun `numbered list numbers the selected lines in order and toggles off`() {
        assertTransform("[1. one\n2. two\n3. three]", "[one\ntwo\nthree]", ::toggleNumberedList)
        assertTransform("[one\ntwo]", "[1. one\n2. two]", ::toggleNumberedList)
        assertTransform("1. |", "|", ::toggleNumberedList)
    }

    @Test
    fun `numbered list replaces a bullet prefix instead of stacking`() {
        assertTransform("[1. one\n2. two]", "[- one\n- two]", ::toggleNumberedList)
    }

    @Test
    fun `task list prefixes selected lines and toggles off a checked item too`() {
        assertTransform("[- [ ] one\n- [ ] two]", "[one\ntwo]", ::toggleTaskList)
        assertTransform("do|ne", "- [x] do|ne", ::toggleTaskList)
        assertTransform("- [ ] |", "|", ::toggleTaskList)
    }

    @Test
    fun `task list replaces a bullet prefix instead of stacking`() {
        assertTransform("- [ ] it|em", "- it|em", ::toggleTaskList)
    }

    @Test
    fun `indent adds four spaces to every selected line`() {
        assertTransform("[    - a\n    - b]", "[- a\n- b]", ::indentLines)
    }

    @Test
    fun `indent moves a bare cursor with its line`() {
        assertTransform("- a\n    - |b", "- a\n- |b", ::indentLines)
    }

    @Test
    fun `outdent removes up to four spaces from every selected line`() {
        assertTransform("[- a\n  - b]", "[    - a\n      - b]", ::outdentLines)
        assertTransform("- |b", "  - |b", ::outdentLines)
    }

    @Test
    fun `outdent removes a leading tab`() {
        assertTransform("- |b", "\t- |b", ::outdentLines)
    }

    @Test
    fun `outdent leaves an unindented line unchanged`() {
        assertTransform("- |b", "- |b", ::outdentLines)
    }

    @Test
    fun `link wraps the selection as link text and selects the url placeholder`() {
        val result = insertLink(value("see docs here", 4, 8))

        assertEquals(value("see [docs](url) here", 11, 14), result)
    }

    @Test
    fun `link on a bare cursor inserts placeholder text and selects the url placeholder`() {
        val result = insertLink(value("see ", 4))

        assertEquals(value("see [text](url)", 11, 14), result)
    }

    @Test
    fun `code block on a bare cursor on an empty line inserts fences with the cursor between`() {
        assertTransform("```\n|\n```", "|", ::insertCodeBlock)
    }

    @Test
    fun `code block puts the fences on their own lines when the cursor is mid-line`() {
        assertTransform("ab\n```\n|\n```\ncd", "ab|cd", ::insertCodeBlock)
    }

    @Test
    fun `code block wraps selected lines and keeps the code selected`() {
        assertTransform(
            "intro\n```\n[val a = 1\nval b = 2]\n```\nend",
            "intro\n[val a = 1\nval b = 2]\nend",
            ::insertCodeBlock
        )
    }
}
