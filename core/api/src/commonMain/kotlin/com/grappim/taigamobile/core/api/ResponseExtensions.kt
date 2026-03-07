package com.grappim.taigamobile.core.api

import io.ktor.client.statement.HttpResponse

fun HttpResponse.hasNextPage(): Boolean = headers["X-Pagination-Next"] != null
