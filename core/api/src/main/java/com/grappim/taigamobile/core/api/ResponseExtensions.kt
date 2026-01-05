package com.grappim.taigamobile.core.api

import retrofit2.Response

fun <T> Response<T>.hasNextPage(): Boolean = headers()["X-Pagination-Next"] != null
