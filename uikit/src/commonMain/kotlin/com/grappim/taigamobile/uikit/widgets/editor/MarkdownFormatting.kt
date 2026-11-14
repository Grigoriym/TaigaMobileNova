package com.grappim.taigamobile.uikit.widgets.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

// Taiga renders with Python-Markdown, which nests lists only at 4 spaces of indentation.
private const val INDENT = "    "
private const val CODE_FENCE = "```"
private const val LINK_TEXT_PLACEHOLDER = "text"
private const val LINK_URL_PLACEHOLDER = "url"

private val headingPrefix = Regex("""^#{1,6} """)
private val quotePrefix = Regex("""^> ?""")
private val taskPrefix = Regex("""^[-*+] \[[ xX]] """)
private val bulletPrefix = Regex("""^[-*+] (?!\[[ xX]] )""")
private val numberedPrefix = Regex("""^\d+\. """)
private val listPrefixes = listOf(bulletPrefix, numberedPrefix, taskPrefix)

fun toggleBold(value: TextFieldValue): TextFieldValue = toggleInlineWrap(value, "**")

fun toggleItalic(value: TextFieldValue): TextFieldValue = toggleInlineWrap(value, "_")

fun toggleStrikethrough(value: TextFieldValue): TextFieldValue = toggleInlineWrap(value, "~~")

fun toggleInlineCode(value: TextFieldValue): TextFieldValue = toggleInlineWrap(value, "`")

fun toggleHeading(value: TextFieldValue): TextFieldValue =
    toggleLinePrefix(value, own = headingPrefix, replaces = listOf(headingPrefix)) { "# " }

fun toggleQuote(value: TextFieldValue): TextFieldValue =
    toggleLinePrefix(value, own = quotePrefix, replaces = listOf(quotePrefix)) { "> " }

// The three list kinds replace each other, so switching a bullet list to a numbered one doesn't stack prefixes.
fun toggleBulletList(value: TextFieldValue): TextFieldValue =
    toggleLinePrefix(value, own = bulletPrefix, replaces = listPrefixes) { "- " }

fun toggleNumberedList(value: TextFieldValue): TextFieldValue =
    toggleLinePrefix(value, own = numberedPrefix, replaces = listPrefixes) { index -> "${index + 1}. " }

fun toggleTaskList(value: TextFieldValue): TextFieldValue =
    toggleLinePrefix(value, own = taskPrefix, replaces = listPrefixes) { "- [ ] " }

fun indentLines(value: TextFieldValue): TextFieldValue {
    val edits = touchedLines(value).map { LineEdit(at = it.start, removed = 0, inserted = INDENT) }
    return applyLineEdits(value, edits)
}

fun outdentLines(value: TextFieldValue): TextFieldValue {
    val edits = touchedLines(value).mapNotNull { line ->
        val removed = if (line.text.startsWith('\t')) 1 else line.text.takeWhile { it == ' ' }.length.coerceAtMost(4)
        if (removed == 0) null else LineEdit(at = line.start, removed = removed, inserted = "")
    }
    return applyLineEdits(value, edits)
}

// Wraps the selection as `[selection](url)` and selects `url`, the part the user has to fill in next.
fun insertLink(value: TextFieldValue): TextFieldValue {
    val start = value.selection.min
    val end = value.selection.max
    val label = value.text.substring(start, end).ifEmpty { LINK_TEXT_PLACEHOLDER }
    val link = "[$label]($LINK_URL_PLACEHOLDER)"
    val urlStart = start + label.length + 3
    return TextFieldValue(
        text = value.text.replaceRange(start, end, link),
        selection = TextRange(urlStart, urlStart + LINK_URL_PLACEHOLDER.length)
    )
}

// Fences must sit on their own lines; Taiga renders a fence directly after a paragraph line without a blank line.
fun insertCodeBlock(value: TextFieldValue): TextFieldValue {
    val start = value.selection.min
    val end = value.selection.max
    val text = value.text
    val before = text.substring(0, start)
    val after = text.substring(end)
    val leading = if (before.isEmpty() || before.endsWith('\n')) "" else "\n"
    val trailing = if (after.isEmpty() || after.startsWith('\n')) "" else "\n"
    val code = text.substring(start, end)
    val block = "$leading$CODE_FENCE\n$code\n$CODE_FENCE$trailing"
    val codeStart = start + leading.length + CODE_FENCE.length + 1
    return TextFieldValue(
        text = text.replaceRange(start, end, block),
        selection = TextRange(codeStart, codeStart + code.length)
    )
}

private fun toggleInlineWrap(value: TextFieldValue, marker: String): TextFieldValue {
    val text = value.text
    val start = value.selection.min
    val end = value.selection.max
    val selected = text.substring(start, end)
    val m = marker.length
    return when {
        selected.length >= 2 * m && selected.startsWith(marker) && selected.endsWith(marker) -> TextFieldValue(
            text = text.replaceRange(start, end, selected.substring(m, selected.length - m)),
            selection = TextRange(start, end - 2 * m)
        )

        start >= m && text.startsWith(marker, start - m) && text.startsWith(marker, end) -> TextFieldValue(
            text = text.removeRange(end, end + m).removeRange(start - m, start),
            selection = TextRange(start - m, end - m)
        )

        else -> TextFieldValue(
            text = text.replaceRange(start, end, "$marker$selected$marker"),
            selection = TextRange(start + m, end + m)
        )
    }
}

// Removes `own` from every touched line when all of them already have it; otherwise gives every touched line
// `prefixFor(index)`, replacing whichever of `replaces` it already starts with. Leading indentation is kept.
private fun toggleLinePrefix(
    value: TextFieldValue,
    own: Regex,
    replaces: List<Regex>,
    prefixFor: (Int) -> String
): TextFieldValue {
    val lines = touchedLines(value)
    val removeAll = lines.all { own.containsMatchIn(it.content) }
    val edits = lines.mapIndexedNotNull { index, line ->
        val existing = if (removeAll) {
            own.find(line.content)
        } else {
            replaces.firstNotNullOfOrNull {
                it.find(line.content)
            }
        }
        val removed = existing?.value.orEmpty()
        val inserted = if (removeAll) "" else prefixFor(index)
        if (removed ==
            inserted
        ) {
            null
        } else {
            LineEdit(at = line.contentStart, removed = removed.length, inserted = inserted)
        }
    }
    return applyLineEdits(value, edits)
}

private data class Line(val start: Int, val text: String) {
    val indent: Int = text.takeWhile { it == ' ' || it == '\t' }.length
    val contentStart: Int = start + indent
    val content: String = text.substring(indent)
}

// Every line the selection touches, skipping blank ones unless nothing else is touched. A selection ending right
// after a newline doesn't touch the line that follows it.
private fun touchedLines(value: TextFieldValue): List<Line> {
    val text = value.text
    val start = value.selection.min
    var end = value.selection.max
    if (end > start && text[end - 1] == '\n') end--
    val firstStart = text.lastIndexOf('\n', start - 1) + 1
    val lastEnd = text.indexOf('\n', end).let { if (it == -1) text.length else it }

    var lineStart = firstStart
    val lines = text.substring(firstStart, lastEnd).split('\n').map { lineText ->
        Line(start = lineStart, text = lineText).also { lineStart += lineText.length + 1 }
    }
    return lines.filter { it.text.isNotBlank() }.ifEmpty { lines }
}

private data class LineEdit(val at: Int, val removed: Int, val inserted: String)

// `edits` must be in ascending, non-overlapping order. A non-empty selection that starts at an edit keeps the
// inserted text inside it; every other position inside an edit moves past the inserted text.
private fun applyLineEdits(value: TextFieldValue, edits: List<LineEdit>): TextFieldValue {
    if (edits.isEmpty()) return value
    val text = StringBuilder(value.text)
    edits.asReversed().forEach { text.setRange(it.at, it.at + it.removed, it.inserted) }

    fun map(position: Int, keepInserted: Boolean): Int {
        val shift = edits.filter { position > it.at + it.removed }.sumOf { it.inserted.length - it.removed }
        val containing = edits.firstOrNull { position in it.at..(it.at + it.removed) }
        return when {
            containing == null -> position + shift
            keepInserted -> containing.at + shift
            else -> containing.at + shift + containing.inserted.length
        }
    }

    val selection = value.selection
    val newStart = map(selection.min, keepInserted = !selection.collapsed)
    val newEnd = if (selection.collapsed) newStart else map(selection.max, keepInserted = false)
    return TextFieldValue(text = text.toString(), selection = TextRange(newStart, newEnd))
}
