package com.grappim.taigamobile.utils.formatter.decimal

actual fun createDecimalFormatter(): DecimalFormatter = IosDecimalFormatter()

private class IosDecimalFormatter : DecimalFormatter {
    override fun format(value: Double): String = value.toInt().toString()
}
