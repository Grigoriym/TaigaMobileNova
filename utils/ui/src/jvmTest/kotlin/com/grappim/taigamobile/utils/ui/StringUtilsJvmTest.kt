package com.grappim.taigamobile.utils.ui

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `formatColor` and `formatStringKmp` are `expect` functions with three actuals. **This file covers
 * the JVM actual only** (`StringUtils.jvm.kt`), which delegates to `kotlin.text.String.format` /
 * `java.util.Formatter`. The Android actual is byte-for-byte the same code, so it is covered by
 * proxy; the iOS actual (`NSString.stringWithFormat`) is **not covered at all**.
 *
 * `String.format` without an explicit locale uses `Locale.getDefault(FORMAT)`, so only
 * locale-independent conversions are asserted here — `%08X` and `%s` produce the same output in
 * every locale, whereas `%d` would not (Arabic-Indic digits under `ar-EG`, for instance).
 */
class StringUtilsJvmTest {

    @Test
    fun `formatColor pads to eight upper-case hex digits with a leading hash`() {
        assertEquals("#FFFF0000", formatColor(0xFFFF0000.toInt()))
        assertEquals("#00000000", formatColor(0))
        assertEquals("#000000FF", formatColor(255))
        assertEquals("#FFFFFFFF", formatColor(-1))
    }

    @Test
    fun `formatStringKmp substitutes positional arguments`() {
        assertEquals("a then b", "%1\$s then %2\$s".formatStringKmp("a", "b"))
    }

    @Test
    fun `formatStringKmp reorders arguments by index`() {
        assertEquals("b then a", "%2\$s then %1\$s".formatStringKmp("a", "b"))
    }

    @Test
    fun `formatStringKmp renders a null argument as the literal null`() {
        assertEquals("value: null", "value: %1\$s".formatStringKmp(null))
    }

    @Test
    fun `formatStringKmp leaves a string without specifiers alone`() {
        assertEquals("nothing to substitute", "nothing to substitute".formatStringKmp())
    }
}
