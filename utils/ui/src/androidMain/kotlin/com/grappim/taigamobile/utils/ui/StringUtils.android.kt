package com.grappim.taigamobile.utils.ui

actual fun formatColor(color: Int): String {
    return "#%08X".format(color)
}

actual fun String.formatStringKmp(vararg args: Any?): String {
    return this.format(args)
}
