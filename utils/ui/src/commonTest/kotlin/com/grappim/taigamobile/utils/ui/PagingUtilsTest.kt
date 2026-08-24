package com.grappim.taigamobile.utils.ui

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import com.grappim.taigamobile.core.domain.NetworkException
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.strings.generated.resources.error_loading_data
import com.grappim.taigamobile.strings.generated.resources.error_not_found
import com.grappim.taigamobile.testing.utils.getRandomString
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Only the [CombinedLoadStates] extension is covered. The [androidx.paging.compose.LazyPagingItems]
 * extensions in the same file need a composition to produce an instance, so they belong to the
 * deferred Compose-UI-test work.
 */
class PagingUtilsTest {

    @Test
    fun `getErrorMessage maps a refresh error through getErrorMessage`() {
        val message = getRandomString()

        val result = loadStates(refresh = LoadState.Error(IllegalStateException(message))).getErrorMessage()

        assertEquals(NativeText.Simple(message), result)
    }

    @Test
    fun `getErrorMessage maps a NetworkException refresh error to its resource`() {
        val error = NetworkException(errorCode = NetworkException.ERROR_NOT_FOUND)

        val result = loadStates(refresh = LoadState.Error(error)).getErrorMessage()

        assertEquals(NativeText.Resource(RString.error_not_found), result)
    }

    @Test
    fun `getErrorMessage falls back when refresh is not an error`() {
        assertEquals(
            NativeText.Resource(RString.error_loading_data),
            loadStates(refresh = LoadState.Loading).getErrorMessage()
        )
        assertEquals(
            NativeText.Resource(RString.error_loading_data),
            loadStates(refresh = LoadState.NotLoading(endOfPaginationReached = false)).getErrorMessage()
        )
    }

    @Test
    fun `getErrorMessage ignores an error on append or prepend`() {
        val states = loadStates(
            refresh = LoadState.NotLoading(endOfPaginationReached = false),
            prepend = LoadState.Error(IllegalStateException(getRandomString())),
            append = LoadState.Error(IllegalStateException(getRandomString()))
        )

        assertEquals(NativeText.Resource(RString.error_loading_data), states.getErrorMessage())
    }

    @Test
    fun `getErrorMessage uses the caller supplied fallback`() {
        val fallback = NativeText.Simple(getRandomString())

        assertEquals(fallback, loadStates(refresh = LoadState.Loading).getErrorMessage(fallback))
    }

    private fun loadStates(
        refresh: LoadState,
        prepend: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
        append: LoadState = LoadState.NotLoading(endOfPaginationReached = false)
    ) = CombinedLoadStates(
        refresh = refresh,
        prepend = prepend,
        append = append,
        source = LoadStates(refresh = refresh, prepend = prepend, append = append)
    )
}
