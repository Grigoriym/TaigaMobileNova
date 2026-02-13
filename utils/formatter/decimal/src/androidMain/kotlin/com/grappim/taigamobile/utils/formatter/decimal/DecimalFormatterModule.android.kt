package com.grappim.taigamobile.utils.formatter.decimal

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * On taiga-front the 8 symbols after the decimal point are displayed.
 */
private const val PATTERN_SIMPLE_DECIMAL = "###.########"

private class AndroidDecimalFormatter: DecimalFormatter {
    private val df = DecimalFormat(
        PATTERN_SIMPLE_DECIMAL,
        DecimalFormatSymbols().apply { decimalSeparator = '.' }
    )
    override fun format(value: Double): String {
        return df.format(value)
    }

}

actual fun createDecimalFormatter(): DecimalFormatter = AndroidDecimalFormatter()
