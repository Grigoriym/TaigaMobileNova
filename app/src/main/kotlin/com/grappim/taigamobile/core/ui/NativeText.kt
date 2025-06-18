package com.grappim.taigamobile.core.ui

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

sealed class NativeText {
    data object Empty : NativeText()
    data class Simple(val text: String) : NativeText()
    data class Resource(@StringRes val id: Int) : NativeText()
    data class Plural(@PluralsRes val id: Int, val number: Int, val args: List<Any>) : NativeText()
    data class Arguments(@StringRes val id: Int, val args: List<Any>) : NativeText()
    data class Multi(val text: List<NativeText>) : NativeText()
}

@Suppress("SpreadOperator")
fun NativeText.asString(context: Context): String {
    return when (this) {
        is NativeText.Arguments -> context.getString(id, *args.toTypedArray())
        is NativeText.Multi -> {
            val builder = StringBuilder()
            for (t in text) {
                builder.append(t.asString(context))
            }
            builder.toString()
        }
        is NativeText.Plural -> context.resources.getQuantityString(
            id,
            number,
            *args.toTypedArray()
        )
        is NativeText.Resource -> context.getString(id)
        is NativeText.Simple -> text
        is NativeText.Empty -> ""
    }
}
