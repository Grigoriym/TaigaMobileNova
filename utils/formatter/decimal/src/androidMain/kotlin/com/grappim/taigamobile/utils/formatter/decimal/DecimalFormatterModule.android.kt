package com.grappim.taigamobile.utils.formatter.decimal

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

private class AndroidDecimalFormatter : DecimalFormatter {
    private val df = DecimalFormat(
        PATTERN_SIMPLE_DECIMAL,
        DecimalFormatSymbols().apply { decimalSeparator = '.' }
    )

    override fun format(value: Double): String = df.format(value)
}

actual fun createDecimalFormatter(): DecimalFormatter = AndroidDecimalFormatter()
