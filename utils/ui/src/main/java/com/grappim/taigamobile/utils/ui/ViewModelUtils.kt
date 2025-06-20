package com.grappim.taigamobile.utils.ui

import androidx.annotation.StringRes
import com.grappim.taigamobile.strings.RString
import timber.log.Timber

inline fun <T> MutableResultFlow<T>.loadOrError(
    @StringRes messageId: Int = RString.common_error_message,
    preserveValue: Boolean = true,
    showLoading: Boolean = true,
    load: () -> T?
) {
    if (showLoading) {
        value = LoadingResult(value.data.takeIf { preserveValue })
    }

    value = try {
        SuccessResult(load())
    } catch (e: Exception) {
        Timber.e(e)
        ErrorResult(messageId)
    }
}
