package com.grappim.taigamobile.utils.ui

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat

actual fun formatColor(color: Int): String {
    val hex = color.toUInt().toString(16).padStart(8, '0').uppercase()
    return "#$hex"
}

actual fun String.formatStringKmp(vararg args: Any?): String = when (args.size) {
    0 -> this
    1 -> NSString.stringWithFormat(this, args[0])
    2 -> NSString.stringWithFormat(this, args[0], args[1])
    3 -> NSString.stringWithFormat(this, args[0], args[1], args[2])
    4 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3])
    5 -> NSString.stringWithFormat(this, args[0], args[1], args[2], args[3], args[4])
    else -> throw IllegalArgumentException("formatStringKmp supports up to 5 arguments on iOS, got ${args.size}")
}
