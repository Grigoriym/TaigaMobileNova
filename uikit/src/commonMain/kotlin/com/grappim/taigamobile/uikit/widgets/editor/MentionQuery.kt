package com.grappim.taigamobile.uikit.widgets.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

// range spans from the `@` through the last query character (inclusive), for insertMention to replace.
data class MentionQuery(val query: String, val range: IntRange)

private val mentionQueryCharRegex = Regex("""[\w.-]""")
private val wordCharRegex = Regex("""\w""")
private val mentionRegex = Regex("""\B@[\w.-]+\b""")

// Mirrors the server's mention pattern (taiga-back's mentions.py:48) to decide whether posted
// text will cause the backend to add mentioned users as watchers.
fun containsMention(text: String): Boolean = mentionRegex.containsMatchIn(text)

// Mirrors the server's mention pattern (\B(@)([\w.-]+)\b, taiga-back's mentions.py:48): an `@`
// only starts a mention when it isn't itself preceded by a word character.
fun findActiveMentionQuery(value: TextFieldValue): MentionQuery? {
    if (!value.selection.collapsed) return null
    val cursor = value.selection.start
    val text = value.text

    var atIndex = cursor - 1
    while (atIndex >= 0 && mentionQueryCharRegex.matches(text[atIndex].toString())) {
        atIndex--
    }
    if (atIndex < 0 || text[atIndex] != '@') return null
    if (atIndex > 0 && wordCharRegex.matches(text[atIndex - 1].toString())) return null

    return MentionQuery(query = text.substring(atIndex + 1, cursor), range = atIndex..(cursor - 1))
}

fun insertMention(value: TextFieldValue, query: MentionQuery, username: String): TextFieldValue {
    val replacement = "@$username "
    val newText = value.text.replaceRange(query.range.first, query.range.last + 1, replacement)
    val newCursor = query.range.first + replacement.length
    return value.copy(text = newText, selection = TextRange(newCursor))
}
