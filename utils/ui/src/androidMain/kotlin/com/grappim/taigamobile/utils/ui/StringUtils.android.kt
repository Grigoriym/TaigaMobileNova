package com.grappim.taigamobile.utils.ui

actual fun formatColor(color: Int): String = "#%08X".format(color)

actual fun String.formatStringKmp(vararg args: Any?): String = this.format(*args)
