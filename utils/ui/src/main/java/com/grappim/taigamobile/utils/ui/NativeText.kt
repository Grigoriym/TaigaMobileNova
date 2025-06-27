package com.grappim.taigamobile.utils.ui

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.strings.RString

sealed class NativeText {
    data object Empty : NativeText()
    data class Simple(val text: String) : NativeText()
    data class Resource(@StringRes val id: Int) : NativeText()
    data class Plural(@PluralsRes val id: Int, val number: Int, val args: List<Any>) : NativeText()
    data class Arguments(@StringRes val id: Int, val args: List<Any>) : NativeText()
    data class Multi(val text: List<NativeText>) : NativeText()
}

@Suppress("SpreadOperator")
fun NativeText.asString(context: Context): String = when (this) {
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

fun getErrorMessage(exception: Throwable): NativeText = if (exception is NetworkException) {
    when (exception.errorCode) {
        NetworkException.ERROR_ON_REFRESH -> NativeText.Resource(
            RString.auth_error_refresh_token_not_passed
        )

        NetworkException.ERROR_NO_INTERNET -> NativeText.Resource(
            RString.error_no_internet_connection
        )
        NetworkException.ERROR_HOST_NOT_FOUND -> NativeText.Resource(
            RString.error_host_not_found
        )
        NetworkException.ERROR_TIMEOUT -> NativeText.Resource(RString.timeout_exceeded)
        NetworkException.ERROR_NETWORK_IO -> NativeText.Resource(RString.connection_failed)
        NetworkException.ERROR_UNDEFINED -> NativeText.Resource(RString.request_failed)
        else -> NativeText.Resource(RString.error_something_has_gone_wrong)
    }
} else {
    NativeText.Simple(exception.message.toString())
}
