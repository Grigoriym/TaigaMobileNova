package com.grappim.taigamobile.utils.ui

import androidx.compose.runtime.Composable
import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.auth_error_refresh_token_not_passed
import com.grappim.taigamobile.strings.generated.resources.connection_failed
import com.grappim.taigamobile.strings.generated.resources.error_blocked
import com.grappim.taigamobile.strings.generated.resources.error_host_not_found
import com.grappim.taigamobile.strings.generated.resources.error_http
import com.grappim.taigamobile.strings.generated.resources.error_internal_server
import com.grappim.taigamobile.strings.generated.resources.error_method_not_allowed
import com.grappim.taigamobile.strings.generated.resources.error_no_internet_connection
import com.grappim.taigamobile.strings.generated.resources.error_not_acceptable
import com.grappim.taigamobile.strings.generated.resources.error_not_found
import com.grappim.taigamobile.strings.generated.resources.error_permission_denied
import com.grappim.taigamobile.strings.generated.resources.error_something_has_gone_wrong
import com.grappim.taigamobile.strings.generated.resources.error_throttled
import com.grappim.taigamobile.strings.generated.resources.error_unsupported_media_type
import com.grappim.taigamobile.strings.generated.resources.error_validation
import com.grappim.taigamobile.strings.generated.resources.request_failed
import com.grappim.taigamobile.strings.generated.resources.timeout_exceeded
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

sealed class NativeText {
    data object Empty : NativeText()
    data class Simple(val text: String) : NativeText()
    data class Resource(val stringResource: StringResource) : NativeText()
    data class Plural(val pluralStringResource: PluralStringResource, val number: Int, val args: List<Any>) :
        NativeText()
    data class Arguments(val stringResource: StringResource, val args: List<Any>) : NativeText()
    data class Multi(val text: List<NativeText>) : NativeText()

    fun isEmpty(): Boolean = this is Empty
    fun isNotEmpty(): Boolean = this !is Empty
}

@Suppress("SpreadOperator")
@Composable
fun NativeText.asString(): String = when (this) {
    is NativeText.Arguments -> stringResource(stringResource, *args.toTypedArray())

    is NativeText.Multi -> {
        val builder = StringBuilder()
        for (t in text) {
            builder.append(t.asString())
        }
        builder.toString()
    }

    is NativeText.Plural -> pluralStringResource(
        pluralStringResource,
        number,
        *args.toTypedArray()
    )

    is NativeText.Resource -> stringResource(stringResource)

    is NativeText.Simple -> text

    is NativeText.Empty -> ""
}

/**
 * Non-composable version for use inside lambdas (onClick, snackbar callbacks, etc.)
 * where @Composable functions cannot be called.
 */
@Suppress("SpreadOperator")
fun NativeText.asStringBlocking(): String = when (this) {
    is NativeText.Arguments -> runBlocking { getString(stringResource, *args.toTypedArray()) }

    is NativeText.Multi -> text.joinToString("") { it.asStringBlocking() }

    is NativeText.Plural -> runBlocking {
        getPluralString(
            pluralStringResource,
            number,
            *args.toTypedArray()
        )
    }

    is NativeText.Resource -> runBlocking { getString(stringResource) }

    is NativeText.Simple -> text

    is NativeText.Empty -> ""
}

fun getErrorMessage(exception: Throwable): NativeText = if (exception is NetworkException) {
    exception.taigaError?.message?.let { NativeText.Simple(it) } ?: when (exception.errorCode) {
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

        NetworkException.ERROR_UNAUTHORIZED -> NativeText.Resource(
            RString.auth_error_refresh_token_not_passed
        )

        NetworkException.ERROR_PERMISSION_DENIED -> NativeText.Resource(
            RString.error_permission_denied
        )

        NetworkException.ERROR_NOT_FOUND -> NativeText.Resource(
            RString.error_not_found
        )

        NetworkException.ERROR_BLOCKED -> NativeText.Resource(
            RString.error_blocked
        )

        NetworkException.ERROR_VALIDATION -> NativeText.Resource(
            RString.error_validation
        )

        NetworkException.ERROR_METHOD_NOT_ALLOWED -> NativeText.Resource(
            RString.error_method_not_allowed
        )

        NetworkException.ERROR_NOT_ACCEPTABLE -> NativeText.Resource(
            RString.error_not_acceptable
        )

        NetworkException.ERROR_UNSUPPORTED_MEDIA_TYPE -> NativeText.Resource(
            RString.error_unsupported_media_type
        )

        NetworkException.ERROR_THROTTLED -> NativeText.Resource(
            RString.error_throttled
        )

        NetworkException.ERROR_INTERNAL_SERVER -> NativeText.Resource(
            RString.error_internal_server
        )

        NetworkException.ERROR_HTTP_EXCEPTION -> NativeText.Resource(
            RString.error_http
        )

        else -> NativeText.Resource(RString.error_something_has_gone_wrong)
    }
} else {
    NativeText.Simple(exception.message.toString())
}
